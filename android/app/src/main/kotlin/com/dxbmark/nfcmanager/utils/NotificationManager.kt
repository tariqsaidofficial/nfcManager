package com.dxbmark.nfcmanager.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.dxbmark.nfcmanager.MainActivity
import com.dxbmark.nfcmanager.R
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
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
    
    companion object {
        // TODO: Consider moving channel names and descriptions to string resources
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
     * @param isEnabled Whether NFC is currently enabled
     * @param enabledDuration Duration NFC has been enabled in milliseconds
     * @param securityLevel Current security level
     * @param showOngoing Whether to show as ongoing notification (can be disabled by user)
     */
    fun createNfcStatusNotification(
        isEnabled: Boolean,
        enabledDuration: Long,
        securityLevel: SecurityLevel,
        showOngoing: Boolean = true
    ): Notification {
        // TODO: Localize title strings
        val title = if (isEnabled) "NFC Enabled" else "NFC Disabled"
        val durationText = formatDuration(enabledDuration)
        // TODO: Localize "Security Level: " part of the string
        val securityText = "Security Level: ${context.getString(securityLevel.displayNameResId)}"
        
        val contentText = if (isEnabled) {
            // TODO: Localize "Enabled for: " part
            "Enabled for: $durationText\n$securityText"
        } else {
            // TODO: Localize "NFC has been disabled" part
            "NFC has been disabled\n$securityText"
        }
        
        // TODO: Localize all parts of bigText
        val bigText = if (isEnabled) {
            "NFC has been enabled for $durationText\n\n" +
            "Security Level: ${context.getString(securityLevel.displayNameResId)}\n" +
            "Status: ${getSecurityStatusMessage(securityLevel)}\n\n" +
            "Tap to open NFC Manager for more details."
        } else {
            "NFC has been disabled\n\n" +
            "Last Security Level: ${context.getString(securityLevel.displayNameResId)}\n\n" +
            "Tap to open NFC Manager to manage your settings."
        }
        
        // TODO: Localize action button titles ("Settings", "Security Score")
        return NotificationCompat.Builder(context, NFC_STATUS_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth) // Using a more relevant icon
            .setColor(securityLevel.color.toArgb())
            .setOngoing(showOngoing) // Allow user to control ongoing status
            .setAutoCancel(false)
            .setOnlyAlertOnce(true) // Prevent repeated alerts
            .setPriority(getNotificationPriority(securityLevel))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(createMainActivityPendingIntent())
            .addAction(
                android.R.drawable.ic_menu_manage, // Custom icon for settings
                "Settings",
                createSettingsPendingIntent()
            )
            .addAction(
                android.R.drawable.ic_lock_lock, // Custom icon for security score
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
        message: String, // This message likely comes from a string resource already or should do
        severity: SecurityLevel
    ): Notification {
        // TODO: Localize titles based on alertType
        val title = when (alertType) {
            SecurityAlertType.NFC_ENABLED_TOO_LONG -> "⚠️ NFC Enabled Too Long"
            SecurityAlertType.UNKNOWN_TAG_DETECTED -> "🔍 Unknown Tag Detected"
            SecurityAlertType.MULTIPLE_TAGS_RAPID -> "⚡ Multiple Tags Detected"
            SecurityAlertType.SUSPICIOUS_ACTIVITY -> "🚨 Suspicious Activity"
        }
        
        // TODO: Localize all parts of bigText
        val bigText = "$message\n\n" +
                "Security Level: ${context.getString(severity.displayNameResId)}\n" +
                "Recommendation: ${getRecommendationForAlert(alertType)}\n\n" +
                "Tap to review your security settings."
        
        // TODO: Localize action button title ("Review Settings")
        return NotificationCompat.Builder(context, SECURITY_ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setSmallIcon(android.R.drawable.stat_notify_error) // Using a more relevant icon
            .setColor(severity.color.toArgb())
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(getNotificationPriority(severity))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(createMainActivityPendingIntent())
            .addAction(
                android.R.drawable.ic_menu_manage,
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
        // Do not show security alerts if the security level is NOT_APPLICABLE
        if (severity == SecurityLevel.NOT_APPLICABLE) return

        val notification = createSecurityAlertNotification(alertType, message, severity)
        notificationManager.notify(SECURITY_ALERT_NOTIFICATION_ID, notification)
    }
    
    /**
     * Alias for showSecurityAlert for backward compatibility
     */
    fun sendSecurityAlert(
        alertType: SecurityAlertType,
        severity: SecurityLevel,
        message: String
    ) = showSecurityAlert(alertType, message, severity)
    
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
            // TODO: Channel names and descriptions should be string resources
            // NFC Status Channel
            val nfcStatusChannelName = context.getString(R.string.notification_channel_nfc_status_name)
            val nfcStatusChannelDesc = context.getString(R.string.notification_channel_nfc_status_description)
            val nfcStatusChannel = NotificationChannel(
                NFC_STATUS_CHANNEL_ID,
                nfcStatusChannelName,
                AndroidNotificationManager.IMPORTANCE_LOW
            ).apply {
                description = nfcStatusChannelDesc
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            // Security Alert Channel
            val securityAlertChannelName = "Security Alerts"
            val securityAlertChannelDesc = "Security alerts and warnings"
            val securityAlertChannel = NotificationChannel(
                SECURITY_ALERT_CHANNEL_ID,
                securityAlertChannelName,
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = securityAlertChannelDesc
                setShowBadge(true)
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                // Consider allowing sound customization or using default system sound
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
        
        // TODO: Localize time unit symbols (h, m, s) if necessary, though these are quite standard
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
            SecurityLevel.NOT_APPLICABLE -> NotificationCompat.PRIORITY_LOW // Added NOT_APPLICABLE case
        }
    }
    
    /**
     * Gets security status message
     */
    // TODO: These status messages should be string resources
    private fun getSecurityStatusMessage(securityLevel: SecurityLevel): String {
        return when (securityLevel) {
            SecurityLevel.EXCELLENT -> "Excellent security practices"
            SecurityLevel.GOOD -> "Good security level"
            SecurityLevel.MODERATE -> "Moderate security - review recommendations"
            SecurityLevel.POOR -> "Poor security - immediate action needed"
            SecurityLevel.CRITICAL -> "Critical security issues detected"
            SecurityLevel.NOT_APPLICABLE -> "Security level not applicable" // Added NOT_APPLICABLE case
        }
    }
    
    /**
     * Gets recommendation for alert type
     */
    // TODO: These recommendations should be string resources (or mapped to R.string.recommendation_... if they match)
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
            putExtra("navigate_to", "settings") // TODO: Use constants for navigation routes
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
            putExtra("navigate_to", "security_score") // TODO: Use constants for navigation routes
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
