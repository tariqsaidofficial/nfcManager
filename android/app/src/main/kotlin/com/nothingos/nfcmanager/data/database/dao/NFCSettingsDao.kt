package com.nothingos.nfcmanager.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.nothingos.nfcmanager.data.database.entities.NFCSettingsEntity

/**
 * Data Access Object for NFC Settings
 * Manages user preferences and app configuration
 */
@Dao
interface NFCSettingsDao {
    
    // Get settings
    @Query("SELECT * FROM nfc_settings WHERE id = 1")
    fun getSettings(): Flow<NFCSettingsEntity?>
    
    @Query("SELECT * FROM nfc_settings WHERE id = 1")
    suspend fun getSettingsSync(): NFCSettingsEntity?
    
    // Specific setting queries
    @Query("SELECT isNFCMonitoringEnabled FROM nfc_settings WHERE id = 1")
    fun isMonitoringEnabled(): Flow<Boolean>
    
    @Query("SELECT autoReminderEnabled FROM nfc_settings WHERE id = 1")
    fun isAutoReminderEnabled(): Flow<Boolean>
    
    @Query("SELECT reminderInterval FROM nfc_settings WHERE id = 1")
    fun getReminderInterval(): Flow<Int>
    
    @Query("SELECT showNotifications FROM nfc_settings WHERE id = 1")
    fun areNotificationsEnabled(): Flow<Boolean>
    
    @Query("SELECT isDarkMode FROM nfc_settings WHERE id = 1")
    fun isDarkModeEnabled(): Flow<Boolean>
    
    @Query("SELECT batteryOptimized FROM nfc_settings WHERE id = 1")
    fun isBatteryOptimized(): Flow<Boolean>

    @Query("SELECT backgroundServiceMonitoringEnabled FROM nfc_settings WHERE id = 1") // Added
    fun isBackgroundServiceMonitoringEnabled(): Flow<Boolean> // Added
    
    // Insert/Update settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: NFCSettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: NFCSettingsEntity)
    
    // Update specific settings
    @Query("UPDATE nfc_settings SET isNFCMonitoringEnabled = :enabled WHERE id = 1")
    suspend fun updateMonitoringEnabled(enabled: Boolean)
    
    @Query("UPDATE nfc_settings SET autoReminderEnabled = :enabled WHERE id = 1")
    suspend fun updateAutoReminderEnabled(enabled: Boolean)
    
    @Query("UPDATE nfc_settings SET reminderInterval = :interval WHERE id = 1")
    suspend fun updateReminderInterval(interval: Int)
    
    @Query("UPDATE nfc_settings SET showNotifications = :enabled WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    
    @Query("UPDATE nfc_settings SET vibrationEnabled = :enabled WHERE id = 1")
    suspend fun updateVibrationEnabled(enabled: Boolean)
    
    @Query("UPDATE nfc_settings SET isDarkMode = :enabled WHERE id = 1")
    suspend fun updateDarkMode(enabled: Boolean)
    
    @Query("UPDATE nfc_settings SET batteryOptimized = :enabled WHERE id = 1")
    suspend fun updateBatteryOptimized(enabled: Boolean)

    @Query("UPDATE nfc_settings SET backgroundServiceMonitoringEnabled = :enabled WHERE id = 1") // Added
    suspend fun updateBackgroundServiceMonitoringEnabled(enabled: Boolean) // Added
    
    @Query("UPDATE nfc_settings SET monitoringInterval = :interval WHERE id = 1")
    suspend fun updateMonitoringInterval(interval: Int)
    
    @Query("UPDATE nfc_settings SET accentColor = :color WHERE id = 1")
    suspend fun updateAccentColor(color: String)
    
    // Reset settings
    @Query("DELETE FROM nfc_settings WHERE id = 1")
    suspend fun resetSettings()
}
