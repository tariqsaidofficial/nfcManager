package com.dxbmark.nfcmanager.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes // Needed for custom sound on channel
import android.net.Uri // Needed for custom sound URI
import android.nfc.NfcAdapter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dxbmark.nfcmanager.MainActivity
// import com.dxbmark.nfcmanager.R // Keep for your actual app icon
import com.dxbmark.nfcmanager.data.database.entities.NFCSettingsEntity // Import settings
import com.dxbmark.nfcmanager.data.repository.NFCRepository // Import repository
import com.dxbmark.nfcmanager.utils.NotificationManager as AppNotificationManager
import com.dxbmark.nfcmanager.utils.PrivacyScoreCalculator
import com.dxbmark.nfcmanager.utils.SecurityLevel
import dagger.hilt.android.AndroidEntryPoint // Hilt import
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint // <<< HILT ANNOTATION
class NfcMonitoringService : Service() {

    @Inject // <<< INJECT REPOSITORY
    lateinit var nfcRepository: NFCRepository

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob) // Use Main for UI-related or use IO for repo access

    private var nfcAdapter: NfcAdapter? = null
    private var isMonitoring = false
    private var nfcEnabledStartTime: Long = 0
    private val privacyScoreCalculator = PrivacyScoreCalculator()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        // Fetch settings and then create/update notification channel
        serviceScope.launch(Dispatchers.IO) { // Launch on IO dispatcher for repository access
            try {
                val settings = nfcRepository.getSettingsSync() // Fetch settings synchronously within coroutine
                createNotificationChannel(settings) // Pass settings to channel creation
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch settings or create notification channel on service create", e)
                // Fallback to creating channel with default sound settings if fetch fails
                createNotificationChannel(null)
            }
        }
        initializeNfcAdapter()
    }

    private fun initializeNfcAdapter() {
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            if (nfcAdapter == null) {
                Log.w(TAG, "Device doesn't support NFC")
            } else {
                Log.d(TAG, "NFC adapter initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize NFC adapter", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand received action: ${intent?.action}")

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
                    Log.d(TAG, "Service explicitly stopped via action.")
                    return START_NOT_STICKY
                }
                else -> {
                    Log.d(TAG, "Service started with no specific action, attempting to start monitoring.")
                    startNfcMonitoring() // Default action if service is restarted
                }
            }
        } catch (se: SecurityException) {
            Log.e(TAG, "SecurityException in onStartCommand. Missing permissions?", se)
            updateNfcStatusNotification()
            stopSelf() // Stop if critical permission is missing
            return START_NOT_STICKY
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in onStartCommand", e)
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
            Log.d(TAG, "Service started in foreground.")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service in foreground", e)
        }
    }

    private fun startNfcMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "NFC monitoring is already active.")
            updateNfcStatusNotification()
            return
        }

        if (nfcAdapter == null) {
            Log.w(TAG, "NFC adapter not available. Cannot start monitoring.")
            updateNfcStatusNotification()
            isMonitoring = false
            return
        }

        if (!nfcAdapter!!.isEnabled) {
            Log.w(TAG, "NFC is disabled. Cannot start monitoring.")
            updateNfcStatusNotification()
            isMonitoring = false
            return
        }

        try {
            isMonitoring = true
            nfcEnabledStartTime = System.currentTimeMillis()
            Log.d(TAG, "NFC monitoring started successfully.")
            updateNfcStatusNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting NFC monitoring operations", e)
            updateNfcStatusNotification()
            isMonitoring = false
        }
    }

    private fun stopNfcMonitoring() {
        if (!isMonitoring) {
            Log.d(TAG, "NFC monitoring is not active or already stopped.")
            return
        }

        try {
            isMonitoring = false
            Log.d(TAG, "NFC monitoring stopped.")
            updateNfcStatusNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping NFC monitoring operations", e)
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
                                Log.d(TAG, "Notification channel using custom sound: $customSoundUriString")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to parse custom sound URI: $customSoundUriString. Using default sound.", e)
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
                            Log.d(TAG, "Notification channel using system default sound.")
                        }
                        enableVibration(settings?.vibrationEnabled ?: false) 
                    } else {
                        setSound(null, null)
                        enableVibration(false) 
                        Log.d(TAG, "Notification channel sound disabled.")
                    }
                }
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created/updated.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create notification channel", e)
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
            Log.d(TAG, "NFC status notification updated - Enabled: $isEnabled, Duration: ${enabledDuration}ms")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update NFC status notification", e)
        }
    }
    
    private fun calculateCurrentSecurityLevel(): SecurityLevel {
        return try {
            // For now, return a default level. In a real implementation, 
            // this would need to be calculated asynchronously
            SecurityLevel.GOOD // Default fallback
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate security level", e)
            SecurityLevel.MODERATE // Safe fallback
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service being destroyed.")
        stopNfcMonitoring()
        serviceJob.cancel() 
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            Log.d(TAG, "Foreground state removed.")
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring foreground state is removed on destroy", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "NfcMonitoringService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "NFC_MONITORING_CHANNEL_V2" 

        const val ACTION_START_MONITORING = "com.dxbmark.nfcmanager.services.ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.dxbmark.nfcmanager.services.ACTION_STOP_MONITORING"
    }
}
