package com.dxbmark.nfcmanager.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.dxbmark.nfcmanager.MainActivity
import com.dxbmark.nfcmanager.data.database.entities.NFCSettingsEntity
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.utils.NotificationManager as AppNotificationManager
import com.dxbmark.nfcmanager.utils.PrivacyScoreCalculator
import com.dxbmark.nfcmanager.utils.SecurityLevel
import com.dxbmark.nfcmanager.utils.error.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Foreground service for continuous NFC monitoring.
 * 
 * This service runs in the background to monitor NFC status and activity.
 * It provides:
 * - Continuous NFC status monitoring
 * - Periodic privacy score calculations
 * - Persistent notification with current status
 * - Battery-optimized monitoring with WakeLock management
 * - Integration with app notification system
 * 
 * The service uses Hilt for dependency injection and follows Android's
 * foreground service guidelines. It maintains a persistent notification
 * and handles proper lifecycle management.
 * 
 * Key features:
 * - Efficient coroutine-based monitoring
 * - Configurable monitoring intervals
 * - Privacy score tracking
 * - Sound and vibration notifications
 * - Proper resource cleanup
 * 
 * @author NFC Manager Team
 * @since 1.0.0
 */
@AndroidEntryPoint // <<< HILT ANNOTATION
class NfcMonitoringService : Service() {

    @Inject // <<< INJECT REPOSITORY
    lateinit var nfcRepository: NFCRepository

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    // Coroutine management
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    // NFC monitoring
    private var nfcAdapter: NfcAdapter? = null
    private var isMonitoring = false
    private var nfcEnabledStartTime: Long = 0
    
    // Power management
    private var wakeLock: PowerManager.WakeLock? = null
    
    // Utilities
    private val privacyScoreCalculator by lazy { PrivacyScoreCalculator(applicationContext) }
    
    // Cached settings to reduce DB queries
    private var cachedSettings: NFCSettingsEntity? = null
    private var lastSettingsUpdate: Long = 0
    
    // Alert tracking
    private var lastAlertTime: Long = 0
    private var alertCount: Int = 0
    
    companion object {
        private const val TAG = "NfcMonitoringService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "NFC_MONITORING_CHANNEL_V2"
        private const val WAKELOCK_TAG = "NfcManager:MonitoringWakeLock"
        private const val SETTINGS_CACHE_DURATION = 30_000L // 30 seconds
        private const val MONITORING_CHECK_INTERVAL = 5_000L // 5 seconds
        
        // Alert thresholds - NFC is dangerous, alert early and often!
        private const val FIRST_ALERT_THRESHOLD = 2 * 60 * 1000L // 2 minutes - first warning
        private const val ALERT_INTERVAL = 2 * 60 * 1000L // Alert every 2 minutes after first warning

        const val ACTION_START_MONITORING = "com.dxbmark.nfcmanager.services.ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.dxbmark.nfcmanager.services.ACTION_STOP_MONITORING"
    }

    override fun onCreate() {
        super.onCreate()
        AppLogger.service("Service created")
        
        initializeWakeLock()
        initializeNfcAdapter()
        
        // Fetch settings and create notification channel
        serviceScope.launch(Dispatchers.IO) {
            try {
                val settings = getCachedOrFetchSettings()
                withContext(Dispatchers.Main) {
                    createNotificationChannel(settings)
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to fetch settings or create notification channel", e)
                withContext(Dispatchers.Main) {
                    createNotificationChannel(null)
                }
            }
        }
    }
    
    /**
     * Initialize WakeLock for keeping CPU awake during monitoring
     * Uses PARTIAL_WAKE_LOCK to minimize battery drain
     */
    private fun initializeWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKELOCK_TAG
            ).apply {
                setReferenceCounted(false)
            }
            AppLogger.service("WakeLock initialized")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to initialize WakeLock", e)
        }
    }
    
    /**
     * Get cached settings or fetch from database
     * Reduces database queries and improves performance
     */
    private suspend fun getCachedOrFetchSettings(): NFCSettingsEntity {
        val now = System.currentTimeMillis()
        return if (cachedSettings != null && (now - lastSettingsUpdate) < SETTINGS_CACHE_DURATION) {
            cachedSettings!!
        } else {
            withContext(Dispatchers.IO) {
                val settings = nfcRepository.getSettingsSync()
                cachedSettings = settings
                lastSettingsUpdate = now
                settings
            }
        }
    }

    /**
     * Initialize NFC adapter
     */
    private fun initializeNfcAdapter() {
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            if (nfcAdapter == null) {
                AppLogger.w(TAG, "Device doesn't support NFC")
            } else {
                AppLogger.service("NFC adapter initialized successfully")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to initialize NFC adapter", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLogger.service("onStartCommand received action: ${intent?.action}")

        try {
            if (intent?.action != ACTION_STOP_MONITORING) {
                 startAsForegroundService("Initializing NFC monitoring...")
            }

            when (intent?.action) {
                ACTION_START_MONITORING -> {
                    startNfcMonitoring()
                }
                ACTION_STOP_MONITORING -> {
                    stopNfcMonitoring()
                    stopSelf()
                    AppLogger.service("Service explicitly stopped via action")
                    return START_NOT_STICKY
                }
                else -> {
                    AppLogger.service("Service started with no specific action, attempting to start monitoring")
                    startNfcMonitoring()
                }
            }
        } catch (se: SecurityException) {
            AppLogger.e(TAG, "SecurityException in onStartCommand. Missing permissions?", se)
            updateNfcStatusNotification()
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            AppLogger.e(TAG, "Unexpected error in onStartCommand", e)
            updateNfcStatusNotification()
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun startAsForegroundService(initialContentText: String) {
        try {
            val notification = createNotification(initialContentText)
            startForeground(NOTIFICATION_ID, notification)
            AppLogger.service("Service started in foreground")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error starting service in foreground", e)
        }
    }

    /**
     * Start NFC monitoring with WakeLock management
     */
    private fun startNfcMonitoring() {
        if (isMonitoring) {
            AppLogger.service("NFC monitoring is already active")
            updateNfcStatusNotification()
            return
        }

        if (nfcAdapter == null) {
            AppLogger.w(TAG, "NFC adapter not available. Cannot start monitoring")
            updateNfcStatusNotification()
            isMonitoring = false
            return
        }

        if (!nfcAdapter!!.isEnabled) {
            AppLogger.w(TAG, "NFC is disabled. Cannot start monitoring")
            updateNfcStatusNotification()
            isMonitoring = false
            return
        }

        try {
            // Acquire WakeLock to keep CPU awake
            acquireWakeLock()
            
            isMonitoring = true
            nfcEnabledStartTime = System.currentTimeMillis()
            AppLogger.service("NFC monitoring started successfully")
            updateNfcStatusNotification()
            
            // Start periodic monitoring
            startPeriodicMonitoring()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error starting NFC monitoring operations", e)
            updateNfcStatusNotification()
            isMonitoring = false
            releaseWakeLock()
        }
    }
    
    /**
     * Start periodic monitoring loop
     */
    private fun startPeriodicMonitoring() {
        serviceScope.launch {
            while (isActive && isMonitoring) {
                try {
                    // Check NFC status
                    val isNfcEnabled = nfcAdapter?.isEnabled == true
                    if (!isNfcEnabled && isMonitoring) {
                        AppLogger.w(TAG, "NFC disabled during monitoring")
                        stopNfcMonitoring()
                        break
                    }
                    
                    // Check for long usage and send alert if needed
                    checkAndSendAlerts()
                    
                    // Update notification periodically
                    updateNfcStatusNotification()
                    
                    // Wait before next check
                    delay(MONITORING_CHECK_INTERVAL)
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Error in monitoring loop", e)
                    break
                }
            }
        }
    }
    
    /**
     * Check NFC usage duration and send alerts if needed
     * NFC is dangerous - alert early and often to protect users!
     * Works in conjunction with Auto Reminder setting
     */
    private suspend fun checkAndSendAlerts() {
        try {
            val settings = nfcRepository.getSettingsSync()
            
            // Only send alerts if Auto Reminder is enabled
            if (!settings.autoReminderEnabled) {
                return
            }
            
            // Also respect general notification settings
            if (!settings.showNotifications) {
                return
            }
            
            val currentTime = System.currentTimeMillis()
            val usageDuration = if (nfcEnabledStartTime > 0) {
                currentTime - nfcEnabledStartTime
            } else 0
            
            // NFC is dangerous! Start alerting after just 2 minutes
            if (usageDuration < FIRST_ALERT_THRESHOLD) {
                return // Not yet time for first alert
            }
            
            // Check if enough time has passed since last alert
            val timeSinceLastAlert = currentTime - lastAlertTime
            if (lastAlertTime > 0 && timeSinceLastAlert < ALERT_INTERVAL) {
                return // Still in cooldown period (2 minutes between alerts)
            }
            
            // Calculate severity based on duration
            val minutes = usageDuration / 60000
            val severity = when {
                minutes >= 10 -> SecurityLevel.CRITICAL  // 10+ minutes = CRITICAL
                minutes >= 5 -> SecurityLevel.POOR       // 5-9 minutes = POOR
                else -> SecurityLevel.MODERATE           // 2-4 minutes = MODERATE
            }
            
            // Send the alert
            withContext(Dispatchers.Main) {
                appNotificationManager.sendSecurityAlert(
                    alertType = com.dxbmark.nfcmanager.utils.SecurityAlertType.NFC_ENABLED_TOO_LONG,
                    severity = severity,
                    message = "NFC has been enabled for $minutes minutes. Consider disabling it to protect your bank cards and personal data."
                )
            }
            
            lastAlertTime = currentTime
            alertCount++
            AppLogger.service("Security alert sent - NFC enabled for $minutes minutes (Alert #$alertCount, Severity: $severity)")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error checking and sending alerts", e)
        }
    }
    
    /**
     * Acquire WakeLock to keep CPU awake
     */
    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(10*60*1000L /*10 minutes*/)
                AppLogger.service("WakeLock acquired")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to acquire WakeLock", e)
        }
    }
    
    /**
     * Release WakeLock to save battery
     */
    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                AppLogger.service("WakeLock released")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to release WakeLock", e)
        }
    }

    /**
     * Stop NFC monitoring and release resources
     */
    private fun stopNfcMonitoring() {
        if (!isMonitoring) {
            AppLogger.service("NFC monitoring is not active or already stopped")
            return
        }

        try {
            isMonitoring = false
            releaseWakeLock()
            AppLogger.service("NFC monitoring stopped")
            updateNfcStatusNotification()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error stopping NFC monitoring operations", e)
        }
    }

    // <<< MODIFIED createNotificationChannel to accept settings
    private fun createNotificationChannel(settings: NFCSettingsEntity?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "NFC Background Monitoring",
                    NotificationManager.IMPORTANCE_LOW 
                ).apply {
                    description = "Monitors NFC activity in the background."
                    setShowBadge(false) 
                    enableLights(false)  
                    
                    val soundEnabled = settings?.soundEnabled ?: true 
                    val customSoundUriString = settings?.customNotificationSoundUri

                    if (soundEnabled) {
                        if (!customSoundUriString.isNullOrEmpty()) {
                            try {
                                val customSoundUri = Uri.parse(customSoundUriString)
                                val audioAttributes = AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                    .build()
                                setSound(customSoundUri, audioAttributes)
                                AppLogger.service("Notification channel using custom sound: $customSoundUriString")
                            } catch (e: Exception) {
                                AppLogger.e(TAG, "Failed to parse custom sound URI: $customSoundUriString. Using default sound.", e)
                                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                    .build())
                            }
                        } else {
                             setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build())
                            AppLogger.service("Notification channel using system default sound")
                        }
                        enableVibration(settings?.vibrationEnabled ?: false) 
                    } else {
                        setSound(null, null)
                        enableVibration(false) 
                        AppLogger.service("Notification channel sound disabled")
                    }
                }
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
                AppLogger.service("Notification channel created/updated")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to create notification channel", e)
            }
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags)

        val icon = android.R.drawable.ic_dialog_info; // Explicitly corrected Icon with semicolon

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NFC Manager")
            .setContentText(contentText)
            .setSmallIcon(icon) 
            .setContentIntent(pendingIntent)
            .setOngoing(true) 
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW) 
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNfcStatusNotification() {
        try {
            val isEnabled = nfcAdapter?.isEnabled == true
            val enabledDuration = if (isEnabled && nfcEnabledStartTime > 0) {
                System.currentTimeMillis() - nfcEnabledStartTime
            } else 0
            
            val securityLevel = calculateCurrentSecurityLevel()
            
            val notification = appNotificationManager.createNfcStatusNotification(
                isEnabled = isEnabled,
                enabledDuration = enabledDuration,
                securityLevel = securityLevel
            )
            
            startForeground(NOTIFICATION_ID, notification)
            AppLogger.service("NFC status notification updated - Enabled: $isEnabled, Duration: ${enabledDuration}ms")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update NFC status notification", e)
        }
    }
    
    private fun calculateCurrentSecurityLevel(): SecurityLevel {
        return try {
            // For now, return a default level. In a real implementation, 
            // this would need to be calculated asynchronously
            SecurityLevel.GOOD // Default fallback
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to calculate security level", e)
            SecurityLevel.MODERATE // Safe fallback
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.service("Service being destroyed")
        stopNfcMonitoring()
        releaseWakeLock()
        serviceJob.cancel() 
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            AppLogger.service("Foreground state removed")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error ensuring foreground state is removed on destroy", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
