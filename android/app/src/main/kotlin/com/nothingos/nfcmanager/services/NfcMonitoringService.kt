package com.nothingos.nfcmanager.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context // Added for Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
// import com.nothingos.nfcmanager.R // Assuming R class is in this package and you have an icon

class NfcMonitoringService : Service() {
    
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "NFC_MONITORING_CHANNEL"
    private var nfcAdapter: NfcAdapter? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get NFC adapter", e)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with intent action: ${intent?.action}")
        
        if (intent?.action == ACTION_STOP_SERVICE) {
            Log.d(TAG, "Received stop action. Stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
            startNfcMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service in foreground", e)
            stopSelf()
            return START_NOT_STICKY
        }
        
        return START_STICKY
    }
    
    private fun startNfcMonitoring() {
        try {
            if (nfcAdapter != null && nfcAdapter!!.isEnabled) {
                Log.d(TAG, "NFC monitoring started")
                // Actual NFC monitoring logic should be implemented here.
                // For example, you might register an NFC reader callback or periodically check status.
            } else {
                Log.w(TAG, "NFC not available or disabled. Service might not be useful.")
                // Optionally, stop the service if NFC is essential and not available.
                // stopSelf();
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in NFC monitoring. Check NFC permissions.", e)
            // Stop service if permissions are missing, as it cannot function.
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error in NFC monitoring logic", e)
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
                    description = "Monitors NFC activity in background"
                    setShowBadge(false)
                }
                
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create notification channel", e)
            }
        }
    }
    
    private fun createNotification(): Notification {
        // TODO: Replace with your actual app icon. Using a system icon as a placeholder.
        // val icon = com.nothingos.nfcmanager.R.drawable.ic_nfc_active 
        val icon = android.R.drawable.stat_sys_data_bluetooth // Temporary placeholder

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NFC Monitor Active")
            .setContentText("Monitoring NFC status in background.")
            .setSmallIcon(icon)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        // The system calls stopForeground when the service is stopping if it was started with startForeground.
        // Explicitly calling stopForeground(true) or stopForeground(STOP_FOREGROUND_REMOVE)
        // is often redundant here if the service is simply being stopped.
        // However, if you need to remove the notification immediately without waiting for the service to fully stop,
        // or under specific conditions, it can be useful.
    }
    
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "NfcMonitoringService"
        const val ACTION_STOP_SERVICE = "com.nothingos.nfcmanager.services.ACTION_STOP_SERVICE"

        fun startService(context: Context) {
            val intent = Intent(context, NfcMonitoringService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                    Log.d(TAG, "Starting foreground service (Oreo+)")
                } else {
                    context.startService(intent)
                    Log.d(TAG, "Starting service (Pre-Oreo)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service", e)
                // Handle cases where service cannot be started (e.g., background restrictions)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NfcMonitoringService::class.java)
            // Optional: Add an action to differentiate stop from other commands if needed in onStartCommand.
            // intent.action = ACTION_STOP_SERVICE 
            try {
                context.stopService(intent)
                Log.d(TAG, "Stopping service")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop service", e)
            }
        }
    }
}
