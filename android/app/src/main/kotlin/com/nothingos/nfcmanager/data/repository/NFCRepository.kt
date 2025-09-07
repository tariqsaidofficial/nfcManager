package com.nothingos.nfcmanager.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.nothingos.nfcmanager.data.database.dao.NFCEventDao
import com.nothingos.nfcmanager.data.database.dao.NFCSettingsDao
import com.nothingos.nfcmanager.data.database.entities.NFCEventEntity
import com.nothingos.nfcmanager.data.database.entities.NFCSettingsEntity
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class implementing official Android patterns
 * Serves as single source of truth for NFC data
 */
@Singleton
class NFCRepository @Inject constructor(
    private val nfcEventDao: NFCEventDao,
    private val nfcSettingsDao: NFCSettingsDao
) {
    
    // ==================== EVENT OPERATIONS ====================
    
    /**
     * Get all events as Flow for reactive UI updates
     */
    fun getAllEvents(): Flow<List<NFCEventEntity>> = nfcEventDao.getAllEvents()
    
    /**
     * Get today's events for main screen
     */
    fun getTodayEvents(): Flow<List<NFCEventEntity>> = nfcEventDao.getTodayEvents()
    
    /**
     * Get today's event count for statistics
     */
    fun getTodayEventCount(): Flow<Int> = nfcEventDao.getTodayEventCount()
    
    /**
     * Get events by type (filtering)
     */
    fun getEventsByType(type: String): Flow<List<NFCEventEntity>> = 
        nfcEventDao.getEventsByType(type)
    
    /**
     * Get important events for alerts
     */
    fun getImportantEvents(): Flow<List<NFCEventEntity>> = nfcEventDao.getImportantEvents()
    
    /**
     * Search events by message content
     */
    fun searchEvents(query: String): Flow<List<NFCEventEntity>> = 
        nfcEventDao.searchEvents(query)
    
    /**
     * Insert new NFC event
     */
    suspend fun insertEvent(event: NFCEventEntity): Long {
        return nfcEventDao.insertEvent(event)
    }
    
    /**
     * Convenience method to log an event with current timestamp
     */
    suspend fun logEvent(
        eventType: String,
        message: String,
        icon: String,
        tagId: String? = null,
        tagType: String? = null,
        data: String? = null,
        appPackage: String? = null,
        isImportant: Boolean = false
    ): Long {
        val event = NFCEventEntity(
            timestamp = Date(),
            eventType = eventType,
            message = message,
            icon = icon,
            tagId = tagId,
            tagType = tagType,
            data = data,
            appPackage = appPackage,
            isImportant = isImportant
        )
        return insertEvent(event)
    }
    
    /**
     * Delete specific event
     */
    suspend fun deleteEvent(event: NFCEventEntity) {
        nfcEventDao.deleteEvent(event)
    }
    
    /**
     * Clean up old events (privacy & storage management)
     */
    suspend fun cleanupOldEvents(daysToKeep: Int = 30) {
        val cutoffDate = Date(System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L))
        nfcEventDao.deleteOldEvents(cutoffDate)
    }
    
    /**
     * Clear all events (user requested)
     */
    suspend fun clearAllEvents() {
        nfcEventDao.deleteAllEvents()
    }
    
    // ==================== SETTINGS OPERATIONS ====================
    
    /**
     * Get settings as Flow for reactive updates
     */
    fun getSettings(): Flow<NFCSettingsEntity> = nfcSettingsDao.getSettings().map { settings ->
        settings ?: getDefaultSettings().also { nfcSettingsDao.insertSettings(it) } // Ensure defaults are inserted if null
    }
    
    /**
     * Get settings synchronously (for immediate access)
     */
    suspend fun getSettingsSync(): NFCSettingsEntity {
        return nfcSettingsDao.getSettingsSync() ?: getDefaultSettings().also { 
            nfcSettingsDao.insertSettings(it)
        }
    }
    
    /**
     * Initialize settings with defaults if not exists
     */
    suspend fun initializeSettingsIfNeeded() {
        val existing = nfcSettingsDao.getSettingsSync()
        if (existing == null) {
            nfcSettingsDao.insertSettings(getDefaultSettings())
        }
    }
    
    /**
     * Update complete settings
     */
    suspend fun updateSettings(settings: NFCSettingsEntity) {
        nfcSettingsDao.updateSettings(settings)
    }
    
    /**
     * Specific setting updates for better performance
     */
    suspend fun updateAutoReminderEnabled(enabled: Boolean) {
        nfcSettingsDao.updateAutoReminderEnabled(enabled)
    }
    
    suspend fun updateReminderInterval(interval: Int) {
        nfcSettingsDao.updateReminderInterval(interval)
    }
    
    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        nfcSettingsDao.updateNotificationsEnabled(enabled)
    }
    
    suspend fun updateVibrationEnabled(enabled: Boolean) {
        nfcSettingsDao.updateVibrationEnabled(enabled)
    }
    
    suspend fun updateDarkMode(enabled: Boolean) {
        nfcSettingsDao.updateDarkMode(enabled)
    }
    
    suspend fun updateBatteryOptimized(enabled: Boolean) {
        nfcSettingsDao.updateBatteryOptimized(enabled)
    }

    suspend fun updateBackgroundServiceMonitoringEnabled(enabled: Boolean) { // Added
        nfcSettingsDao.updateBackgroundServiceMonitoringEnabled(enabled)
    }
    
    /**
     * Get specific settings as Flows for reactive UI
     */
    fun isAutoReminderEnabled(): Flow<Boolean> = nfcSettingsDao.isAutoReminderEnabled()
    fun getReminderInterval(): Flow<Int> = nfcSettingsDao.getReminderInterval()
    fun areNotificationsEnabled(): Flow<Boolean> = nfcSettingsDao.areNotificationsEnabled()
    fun isDarkModeEnabled(): Flow<Boolean> = nfcSettingsDao.isDarkModeEnabled()
    fun isBatteryOptimized(): Flow<Boolean> = nfcSettingsDao.isBatteryOptimized()
    fun isBackgroundServiceMonitoringEnabled(): Flow<Boolean> = nfcSettingsDao.isBackgroundServiceMonitoringEnabled() // Added

    /**
     * Reset all settings to defaults
     */
    suspend fun resetAllSettings() {
        nfcSettingsDao.resetSettings()
        nfcSettingsDao.insertSettings(getDefaultSettings())
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Get default settings with Nothing OS styling
     */
    private fun getDefaultSettings(): NFCSettingsEntity {
        return NFCSettingsEntity(
            id = 1,
            isNFCMonitoringEnabled = true,
            autoReminderEnabled = false,
            reminderInterval = 10, // Defaulted to 10s as per previous README updates
            showNotifications = true,
            vibrationEnabled = true,
            isDarkMode = true,
            accentColor = "#ef4444", // Nothing Red
            backgroundServiceMonitoringEnabled = false // Ensure this is part of defaults
            // Ensure all other NFCSettingsEntity fields are present with their defaults too
        )
    }
    
    /**
     * Get event statistics for performance dashboard
     */
    suspend fun getEventStatistics(): Map<String, Int> {
        val allEvents = getAllEvents().first()
        return allEvents.groupBy { it.eventType }.mapValues { it.value.size }
    }
}
