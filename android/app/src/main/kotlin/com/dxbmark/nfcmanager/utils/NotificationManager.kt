package com.dxbmark.nfcmanager.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.dxbmark.nfcmanager.MainActivity
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.utils.SecurityLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced notification manager for NFC status and security alerts
 */
@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val NFC_STATUS_CHANNEL_ID = "NFC_STATUS_CHANNEL"
        private const val SECURITY_ALERT_CHANNEL_ID = "SECURITY_ALERT_CHANNEL"
        private const val NFC_STATUS_NOTIFICATION_ID = 1001
        private const val SECURITY_ALERT_NOTIFICATION_ID = 1002
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Creates NFC status notification with duration and security level
     */
    fun createNfcStatusNotification(
        isEnabled: Boolean,
        enabledDuration: Long,
        securityLevel: SecurityLevel
    ): Notification {
        val title = if (isEnabled) "NFC Enabled" else "NFC Disabled"
        val durationText = formatDuration(enabledDuration)
        val securityText = "Security Level: ${securityLevel.displayName}"
        
        val contentText = if (isEnabled) {
            "Enabled for: $durationText\n$securityText"
        } else {
            "NFC has been disabled\n$securityText"
        }
        
        val bigText = if (isEnabled) {
            "NFC has been enabled for $durationText\n\n" +
            "Security Level: ${securityLevel.displayName}\n" +
            "Status: ${getSecurityStatusMessage(securityLevel)}\n\n" +
            "Tap to open NFC Manager for more details."
        } else {
            "NFC has been disabled\n\n" +
            "Last Security Level: ${securityLevel.displayName}\n\n" +
            "Tap to open NFC Manager to manage your settings."
        }
        
        return NotificationCompat.Builder(context, NFC_STATUS_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setColor(securityLevel.color.toArgb())
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(getNotificationPriority(securityLevel))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(createMainActivityPendingIntent())
            .addAction(
                android.R.drawable.ic_menu_preferences,
                "Settings",
                createSettingsPendingIntent()
            )
            .addAction(
                android.R.drawable.ic_menu_info_details,
                "Security Score",
                createSecurityScorePendingIntent()
            )
            .build()
    }
    
    /**
     * Creates security alert notification
     */
    fun createSecurityAlertNotification(
        alertType: SecurityAlertType,
        message: String,
        severity: SecurityLevel
    ): Notification {
        val title = when (alertType) {
            SecurityAlertType.NFC_ENABLED_TOO_LONG -> "⚠️ NFC Enabled Too Long"
            SecurityAlertType.UNKNOWN_TAG_DETECTED -> "🔍 Unknown Tag Detected"
            SecurityAlertType.MULTIPLE_TAGS_RAPID -> "⚡ Multiple Tags Detected"
            SecurityAlertType.SUSPICIOUS_ACTIVITY -> "🚨 Suspicious Activity"
        }
        
        val bigText = "$message\n\n" +
                "Security Level: ${severity.displayName}\n" +
                "Recommendation: ${getRecommendationForAlert(alertType)}\n\n" +
                "Tap to review your security settings."
        
        return NotificationCompat.Builder(context, SECURITY_ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setColor(severity.color.toArgb())
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(getNotificationPriority(severity))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(createMainActivityPendingIntent())
            .addAction(
                android.R.drawable.ic_menu_preferences,
                "Review Settings",
                createSettingsPendingIntent()
            )
            .build()
    }
    
    /**
     * Updates NFC status notification
     */
    fun updateNfcStatusNotification(
        isEnabled: Boolean,
        enabledDuration: Long,
        securityLevel: SecurityLevel
    ) {
        val notification = createNfcStatusNotification(isEnabled, enabledDuration, securityLevel)
        notificationManager.notify(NFC_STATUS_NOTIFICATION_ID, notification)
    }
    
    /**
     * Shows security alert notification
     */
    fun showSecurityAlert(
        alertType: SecurityAlertType,
        message: String,
        severity: SecurityLevel
    ) {
        val notification = createSecurityAlertNotification(alertType, message, severity)
        notificationManager.notify(SECURITY_ALERT_NOTIFICATION_ID, notification)
    }
    
    /**
     * Cancels NFC status notification
     */
    fun cancelNfcStatusNotification() {
        notificationManager.cancel(NFC_STATUS_NOTIFICATION_ID)
    }
    
    /**
     * Cancels security alert notification
     */
    fun cancelSecurityAlertNotification() {
        notificationManager.cancel(SECURITY_ALERT_NOTIFICATION_ID)
    }
    
    /**
     * Creates notification channels for Android O and above
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // NFC Status Channel
            val nfcStatusChannel = NotificationChannel(
                NFC_STATUS_CHANNEL_ID,
                "NFC Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current NFC status and security level"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            // Security Alert Channel
            val securityAlertChannel = NotificationChannel(
                SECURITY_ALERT_CHANNEL_ID,
                "Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Security alerts and warnings"
                setShowBadge(true)
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(nfcStatusChannel)
            notificationManager.createNotificationChannel(securityAlertChannel)
        }
    }
    
    /**
     * Formats duration in milliseconds to human readable format
     */
    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    
    /**
     * Gets notification priority based on security level
     */
    private fun getNotificationPriority(securityLevel: SecurityLevel): Int {
        return when (securityLevel) {
            SecurityLevel.EXCELLENT, SecurityLevel.GOOD -> NotificationCompat.PRIORITY_LOW
            SecurityLevel.MODERATE -> NotificationCompat.PRIORITY_DEFAULT
            SecurityLevel.POOR, SecurityLevel.CRITICAL -> NotificationCompat.PRIORITY_HIGH
        }
    }
    
    /**
     * Gets security status message
     */
    private fun getSecurityStatusMessage(securityLevel: SecurityLevel): String {
        return when (securityLevel) {
            SecurityLevel.EXCELLENT -> "Excellent security practices"
            SecurityLevel.GOOD -> "Good security level"
            SecurityLevel.MODERATE -> "Moderate security - review recommendations"
            SecurityLevel.POOR -> "Poor security - immediate action needed"
            SecurityLevel.CRITICAL -> "Critical security issues detected"
        }
    }
    
    /**
     * Gets recommendation for alert type
     */
    private fun getRecommendationForAlert(alertType: SecurityAlertType): String {
        return when (alertType) {
            SecurityAlertType.NFC_ENABLED_TOO_LONG -> "Consider disabling NFC when not in use"
            SecurityAlertType.UNKNOWN_TAG_DETECTED -> "Be cautious with unknown NFC tags"
            SecurityAlertType.MULTIPLE_TAGS_RAPID -> "Avoid crowded areas when using NFC"
            SecurityAlertType.SUSPICIOUS_ACTIVITY -> "Review your NFC usage patterns"
        }
    }
    
    /**
     * Creates pending intent for main activity
     */
    private fun createMainActivityPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        return PendingIntent.getActivity(context, 0, intent, flags)
    }
    
    /**
     * Creates pending intent for settings
     */
    private fun createSettingsPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "settings")
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        return PendingIntent.getActivity(context, 1, intent, flags)
    }
    
    /**
     * Creates pending intent for security score
     */
    private fun createSecurityScorePendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "security_score")
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        return PendingIntent.getActivity(context, 2, intent, flags)
    }
}

/**
 * Enum for different types of security alerts
 */
enum class SecurityAlertType {
    NFC_ENABLED_TOO_LONG,
    UNKNOWN_TAG_DETECTED,
    MULTIPLE_TAGS_RAPID,
    SUSPICIOUS_ACTIVITY
}
