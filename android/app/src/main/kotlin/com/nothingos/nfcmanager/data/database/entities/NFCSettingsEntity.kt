package com.nothingos.nfcmanager.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class for NFC Manager settings
 * Stores user preferences and configuration
 */
@Entity(tableName = "nfc_settings")
data class NFCSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    
    // Main NFC settings
    val isNFCMonitoringEnabled: Boolean = true, // General NFC monitoring (e.g., in-app UI)
    val isPrivacyModeEnabled: Boolean = false,
    val autoReminderEnabled: Boolean = false, // This might be a good candidate for the background service's core logic
    val reminderInterval: Int = 10, // seconds - used by background service for alert checks
    
    // Privacy & Security
    val blockUnknownTags: Boolean = false,
    val autoBlockSuspiciousTags: Boolean = false,
    val logSensitiveData: Boolean = false,
    
    // UI & Notifications
    val showNotifications: Boolean = true, // General notification toggle
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val notificationStyle: String = "standard", // "standard", "detailed", "minimal"
    
    // Performance
    val batteryOptimized: Boolean = false,
    val monitoringInterval: Int = 2000, // milliseconds - potentially for foreground UI updates
    // Renamed and defaulted to false for explicit opt-in to the new background service
    val backgroundServiceMonitoringEnabled: Boolean = false,
    
    // Theme (Nothing OS style)
    val isDarkMode: Boolean = true,
    val accentColor: String = "#ef4444" // Nothing Red
)
