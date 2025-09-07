package com.nothingos.nfcmanager.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nothingos.nfcmanager.MainActivity
// import com.nothingos.nfcmanager.R // Keep for your actual app icon

class NfcMonitoringService : Service() {

    private var nfcAdapter: NfcAdapter? = null
    private var isMonitoring = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
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
                    startNfcMonitoring()
                }
            }
        } catch (se: SecurityException) {
            Log.e(TAG, "SecurityException in onStartCommand. Missing permissions?", se)
            updateNotification("Permission error. Cannot monitor NFC.")
            stopSelf()
            return START_NOT_STICKY
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in onStartCommand", e)
            updateNotification("Error starting service.")
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
            updateNotification("NFC monitoring is active.")
            return
        }

        if (nfcAdapter == null) {
            Log.w(TAG, "NFC adapter not available. Cannot start monitoring.")
            updateNotification("NFC not supported on this device.")
            isMonitoring = false
            return
        }

        if (!nfcAdapter!!.isEnabled) {
            Log.w(TAG, "NFC is disabled. Cannot start monitoring.")
            updateNotification("NFC is disabled. Please enable it.")
            isMonitoring = false
            return
        }

        try {
            isMonitoring = true
            Log.d(TAG, "NFC monitoring started successfully.")
            updateNotification("NFC monitoring is active.")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting NFC monitoring operations", e)
            updateNotification("Error enabling NFC monitoring.")
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
            updateNotification("NFC monitoring stopped.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping NFC monitoring operations", e)
        }
    }

    private fun createNotificationChannel() {
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
                    enableVibration(false)
                    setSound(null, null)
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

        val icon = android.R.drawable.stat_sys_data_bluetooth // Placeholder

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

    private fun updateNotification(contentText: String) {
        try {
            val notification = createNotification(contentText)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification updated with text: $contentText")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service being destroyed.")
        stopNfcMonitoring()
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
        // Service-specific constants, private as they are only used within this class
        private const val TAG = "NfcMonitoringService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "NFC_MONITORING_CHANNEL"

        // ACTION constants are public and used by other components like ViewModel
        const val ACTION_START_MONITORING = "com.nothingos.nfcmanager.services.ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.nothingos.nfcmanager.services.ACTION_STOP_MONITORING"
    }
}
