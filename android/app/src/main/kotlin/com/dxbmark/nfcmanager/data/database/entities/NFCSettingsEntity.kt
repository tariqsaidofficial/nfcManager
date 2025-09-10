package com.dxbmark.nfcmanager.data.database.entities

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
    val reminderInterval: Int = 10, // seconds

    // Privacy & Security
    val blockUnknownTags: Boolean = false,
    val autoBlockSuspiciousTags: Boolean = false,
    val logSensitiveData: Boolean = false,

    // UI & Notifications
    val showNotifications: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true, // General toggle for if custom/default sounds play
    val notificationStyle: String = "standard",
    val customNotificationSoundUri: String? = null, // <<< NEW FIELD: URI for custom notification sound

    // Performance
    val batteryOptimized: Boolean = false,
    val monitoringInterval: Int = 2000, // milliseconds
    val backgroundServiceMonitoringEnabled: Boolean = false,

    // Theme (Nothing OS style)
    val isDarkMode: Boolean = true,
    val accentColor: String = "#ef4444",

    // Language Settings
    val selectedLanguageCode: String? = null, // null for system default
    
    // Onboarding
    val isOnboardingCompleted: Boolean = false,
    
    // Security Score
    val lastSecurityScore: Int = 100,
    val securityLevel: String = "EXCELLENT"
)
