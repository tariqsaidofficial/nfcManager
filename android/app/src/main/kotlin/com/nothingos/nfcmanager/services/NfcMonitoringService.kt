package com.nothingos.nfcmanager.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.nothingos.nfcmanager.MainActivity // App's main entry point
import com.nothingos.nfcmanager.R // For drawable resources
import com.nothingos.nfcmanager.data.repository.NFCRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class NfcMonitoringService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    @Inject
    lateinit var repository: NFCRepository
    private var nfcAdapter: NfcAdapter? = null

    private var currentReminderIntervalSeconds = 10 // Default, will be updated from settings
    private var isNfcCurrentlyMonitoredAsEnabled = false
    private var nfcEnabledTimestamp: Long = 0L

    companion object {
        private const val TAG = "NfcMonitoringService"
        private const val PERSISTENT_NOTIFICATION_ID = 101
        private const val PRIVACY_ALERT_NOTIFICATION_ID = 202
        private const val PERSISTENT_CHANNEL_ID = "NfcMonitoringPersistentChannel"
        private const val ALERT_CHANNEL_ID = "NfcPrivacyAlertChannel"

        const val ACTION_START_MONITORING = "com.nothingos.nfcmanager.ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.nothingos.nfcmanager.ACTION_STOP_MONITORING"

        fun startService(context: Context) {
            val intent = Intent(context, NfcMonitoringService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NfcMonitoringService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
            context.startService(intent) 
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        createNotificationChannels()
        Log.d(TAG, "Service onCreate completed. Repository injected: ${::repository.isInitialized}")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand, action: ${intent?.action}, flags: $flags, startId: $startId. Repo initialized: ${::repository.isInitialized}")

        when (intent?.action) {
            ACTION_START_MONITORING -> {
                Log.i(TAG, "ACTION_START_MONITORING received.")
                startForeground(PERSISTENT_NOTIFICATION_ID, createPersistentNotification("Initializing NFC monitoring..."))
                serviceScope.launch {
                    Log.d(TAG, "Coroutine started for ACTION_START_MONITORING.")
                    if (!::repository.isInitialized) {
                        Log.e(TAG, "ACTION_START_MONITORING: Repository not initialized! Stopping service.")
                        stopSelf()
                        return@launch
                    }
                    try { // Added try-catch for settings fetching
                        val settings = repository.getSettings().first()
                        Log.d(TAG, "ACTION_START_MONITORING: Settings fetched: $settings")
                        if (settings == null) { 
                            Log.e(TAG, "ACTION_START_MONITORING: CRITICAL - Settings object is null! Stopping service.")
                            stopSelf()
                            return@launch
                        }
                        if (!settings.backgroundServiceMonitoringEnabled) {
                            Log.i(TAG, "ACTION_START_MONITORING: Background monitoring is disabled in settings. Stopping service.")
                            stopSelf()
                            return@launch
                        }
                        currentReminderIntervalSeconds = settings.reminderInterval
                        Log.i(TAG, "ACTION_START_MONITORING: Starting NFC monitoring loop. Reminder interval: $currentReminderIntervalSeconds s")
                        repository.logEvent("SERVICE", "Background NFC monitoring service started.", "Radar")
                        monitoringLoop()
                    } catch (e: Exception) {
                        Log.e(TAG, "ACTION_START_MONITORING: Exception fetching settings or starting loop: ${e.message}", e)
                        stopSelf()
                    }
                }
            }
            ACTION_STOP_MONITORING -> {
                Log.i(TAG, "ACTION_STOP_MONITORING received. Stopping service.")
                serviceScope.launch { 
                    if(::repository.isInitialized) repository.logEvent("SERVICE", "Background NFC monitoring service stopped by command.", "RadarOff")
                }
                stopSelf()
                return START_NOT_STICKY
            }
            else -> { 
                Log.i(TAG, "onStartCommand: intent or action is null/unknown (service restart?). Repo initialized: ${::repository.isInitialized}")
                serviceScope.launch {
                    Log.d(TAG, "Coroutine started for null/unknown action.")
                    if (!::repository.isInitialized) {
                        Log.e(TAG, "NULL/UNKNOWN_ACTION: Repository not initialized! Stopping service.")
                        stopSelf() 
                        return@launch
                    }
                     try { // Added try-catch for settings fetching
                        val settings = repository.getSettings().first()
                        Log.d(TAG, "NULL/UNKNOWN_ACTION: Settings fetched: $settings")
                        if (settings == null) { 
                            Log.e(TAG, "NULL/UNKNOWN_ACTION: CRITICAL - Settings object is null after fetch! Stopping service.")
                            stopSelf()
                            return@launch
                        }
                        if (settings.backgroundServiceMonitoringEnabled) {
                            Log.i(TAG, "NULL/UNKNOWN_ACTION: Service restarted, background monitoring IS enabled. Starting monitoring.")
                            currentReminderIntervalSeconds = settings.reminderInterval
                            startForeground(PERSISTENT_NOTIFICATION_ID, createPersistentNotification("NFC Monitoring Active (Restarted)..."))
                            monitoringLoop()
                        } else {
                            Log.i(TAG, "NULL/UNKNOWN_ACTION: Service restarted, but background monitoring IS disabled. Stopping service.")
                            stopSelf()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "NULL/UNKNOWN_ACTION: Exception fetching settings or starting loop: ${e.message}", e)
                        stopSelf()
                    }
                }
            }
        }
        return START_STICKY 
    }

    private suspend fun monitoringLoop() {
        Log.d(TAG, "monitoringLoop started. Repo initialized: ${::repository.isInitialized}")
        while (serviceScope.isActive) {
            try { // Comprehensive try-catch for the entire loop iteration
                if (!::repository.isInitialized) { 
                    Log.e(TAG, "monitoringLoop: Repository became uninitialized! Stopping service.")
                    stopSelf()
                    break
                }
                val nfcPhysicalAdapter = NfcAdapter.getDefaultAdapter(this)
                if (nfcPhysicalAdapter == null) {
                    Log.w(TAG, "monitoringLoop: NFC Adapter not available. Stopping monitoring.")
                    if (::repository.isInitialized) {
                         repository.logEvent("SERVICE_ERROR", "NFC Adapter not available in service.", "AlertTriangle", isImportant = true)
                    }
                    stopSelf()
                    break
                }
                nfcAdapter = nfcPhysicalAdapter

                val settings = repository.getSettings().first()
                if (settings == null) { 
                    Log.e(TAG, "monitoringLoop: CRITICAL - Settings object is null in loop! Stopping service.")
                    stopSelf()
                    break
                }
                currentReminderIntervalSeconds = settings.reminderInterval

                if (!settings.backgroundServiceMonitoringEnabled) {
                    Log.i(TAG, "monitoringLoop: Background monitoring disabled via settings. Stopping service.")
                     if (::repository.isInitialized) {
                        repository.logEvent("SERVICE", "Background NFC monitoring disabled via settings.", "RadarOff")
                    }
                    stopSelf()
                    break
                }

                val persistentNotificationText: String
                if (nfcAdapter!!.isEnabled) {
                    if (!isNfcCurrentlyMonitoredAsEnabled) {
                        isNfcCurrentlyMonitoredAsEnabled = true
                        nfcEnabledTimestamp = System.currentTimeMillis()
                        Log.i(TAG, "monitoringLoop: NFC has been enabled.")
                         if (::repository.isInitialized) {
                            repository.logEvent("NFC_STATE_SVC", "NFC enabled (detected by service).", "Zap")
                        }
                        persistentNotificationText = "NFC is ON. Monitoring for ${currentReminderIntervalSeconds}s reminder."
                    } else {
                        persistentNotificationText = "NFC is ON. Monitoring for ${currentReminderIntervalSeconds}s reminder."
                        val nfcActiveDurationMs = System.currentTimeMillis() - nfcEnabledTimestamp
                        if (nfcActiveDurationMs >= currentReminderIntervalSeconds * 1000) {
                            Log.i(TAG, "monitoringLoop: NFC active for ${nfcActiveDurationMs / 1000}s. Triggering privacy alert.")
                            if(settings.showNotifications && settings.autoReminderEnabled) {
                               sendPrivacyAlertNotification()
                            }
                            nfcEnabledTimestamp = System.currentTimeMillis() 
                        }
                    }
                } else {
                    if (isNfcCurrentlyMonitoredAsEnabled) {
                        isNfcCurrentlyMonitoredAsEnabled = false
                        nfcEnabledTimestamp = 0L
                        Log.i(TAG, "monitoringLoop: NFC has been disabled.")
                        if (::repository.isInitialized) {
                            repository.logEvent("NFC_STATE_SVC", "NFC disabled (detected by service).", "ZapOff")
                        }
                    }
                    persistentNotificationText = "NFC is OFF. Monitoring..."
                }
                startForeground(PERSISTENT_NOTIFICATION_ID, createPersistentNotification(persistentNotificationText))
                
                val delayMs = settings.monitoringInterval.toLong().coerceIn(1000L, 60000L) 
                Log.v(TAG, "monitoringLoop: Delaying for ${delayMs}ms.")
                delay(delayMs)

            } catch (t: Throwable) { // Catch all Throwables, not just Exceptions
                Log.e(TAG, "monitoringLoop: Unhandled Throwable in monitoring loop: ${t.message}", t)
                if (::repository.isInitialized) {
                    try {
                        repository.logEvent("SERVICE_CRASH", "Unhandled error in monitoring loop: ${t.message}", "AlertOctagon", isImportant = true)
                    } catch (logE: Exception) {
                        Log.e(TAG, "monitoringLoop: Failed to log crash event to repository: ${logE.message}", logE)
                    }
                }
                stopSelf() // Stop the service to prevent crash loops
                break      // Exit the loop
            }
        }
        Log.d(TAG, "monitoringLoop ended.")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val persistentChannel = NotificationChannel(
                PERSISTENT_CHANNEL_ID,
                "NFC Monitoring Service",
                NotificationManager.IMPORTANCE_LOW 
            ).apply {
                description = "Persistent notification for NFC Manager background monitoring."
                setSound(null, null) 
            }
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "NFC Privacy Alerts",
                NotificationManager.IMPORTANCE_DEFAULT 
            ).apply {
                description = "Alerts when NFC is left active for too long."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(persistentChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun createPersistentNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlag)

        return NotificationCompat.Builder(this, PERSISTENT_CHANNEL_ID)
            .setContentTitle("NFC Manager Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_nfc_vector) 
            .setContentIntent(pendingIntent)
            .setOngoing(true) 
            .setSound(null) 
            .setPriority(NotificationCompat.PRIORITY_LOW) 
            .build()
    }

    private fun sendPrivacyAlertNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, pendingIntentFlag)

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("NFC Privacy Alert")
            .setContentText("NFC has been active for over $currentReminderIntervalSeconds seconds. Consider turning it off.")
            .setSmallIcon(R.drawable.ic_nfc_vector) 
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) 
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(PRIVACY_ALERT_NOTIFICATION_ID, notification)
        serviceScope.launch {
             if(::repository.isInitialized) repository.logEvent("PRIVACY_ALERT_SVC", "NFC active alert triggered by background service.", "AlertTriangle", isImportant = true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null 
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy. Cancelling serviceJob.")
        serviceJob.cancel() 
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(PERSISTENT_NOTIFICATION_ID)
        Log.d(TAG, "Service onDestroy completed.")
    }
}
