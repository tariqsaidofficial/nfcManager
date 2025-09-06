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
    val isNFCMonitoringEnabled: Boolean = true,
    val isPrivacyModeEnabled: Boolean = false,
    val autoReminderEnabled: Boolean = false,
    val reminderInterval: Int = 10, // seconds - default 10s for enhanced privacy
    
    // Privacy & Security
    val blockUnknownTags: Boolean = false,
    val autoBlockSuspiciousTags: Boolean = false,
    val logSensitiveData: Boolean = false,
    
    // UI & Notifications
    val showNotifications: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val notificationStyle: String = "standard", // "standard", "detailed", "minimal"
    
    // Performance
    val batteryOptimized: Boolean = false,
    val monitoringInterval: Int = 2000, // milliseconds
    val backgroundMonitoring: Boolean = true,
    
    // Theme (Nothing OS style)
    val isDarkMode: Boolean = true,
    val accentColor: String = "#ef4444" // Nothing Red
)
