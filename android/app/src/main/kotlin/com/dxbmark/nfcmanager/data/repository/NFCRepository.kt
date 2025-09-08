package com.dxbmark.nfcmanager.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.dxbmark.nfcmanager.data.database.dao.NFCEventDao
import com.dxbmark.nfcmanager.data.database.dao.NFCSettingsDao
import com.dxbmark.nfcmanager.data.database.entities.NFCEventEntity
import com.dxbmark.nfcmanager.data.database.entities.NFCSettingsEntity
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NFCRepository @Inject constructor(
    private val nfcEventDao: NFCEventDao,
    private val nfcSettingsDao: NFCSettingsDao
) {

    // ==================== EVENT OPERATIONS ===================

    fun getAllEvents(): Flow<List<NFCEventEntity>> = nfcEventDao.getAllEvents()

    fun getTodayEvents(): Flow<List<NFCEventEntity>> = nfcEventDao.getTodayEvents()

    fun getTodayEventCount(): Flow<Int> = nfcEventDao.getTodayEventCount()

    fun getEventsByType(type: String): Flow<List<NFCEventEntity>> =
        nfcEventDao.getEventsByType(type)

    // New function to get distinct event types
    fun getDistinctEventTypes(): Flow<List<String>> = nfcEventDao.getDistinctEventTypes()

    fun getImportantEvents(): Flow<List<NFCEventEntity>> = nfcEventDao.getImportantEvents()

    fun searchEvents(query: String): Flow<List<NFCEventEntity>> =
        nfcEventDao.searchEvents(query)

    suspend fun insertEvent(event: NFCEventEntity): Long {
        return nfcEventDao.insertEvent(event)
    }

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

    suspend fun deleteEvent(event: NFCEventEntity) {
        nfcEventDao.deleteEvent(event)
    }

    suspend fun cleanupOldEvents(daysToKeep: Int = 30) {
        val cutoffDate = Date(System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L))
        nfcEventDao.deleteOldEvents(cutoffDate)
    }

    suspend fun clearAllEvents() {
        nfcEventDao.deleteAllEvents()
    }

    // ==================== SETTINGS OPERATIONS ====================

    fun getSettings(): Flow<NFCSettingsEntity> = nfcSettingsDao.getSettings().map { settings ->
        settings ?: getDefaultSettings().also { nfcSettingsDao.insertSettings(it) }
    }

    suspend fun getSettingsSync(): NFCSettingsEntity {
        return nfcSettingsDao.getSettingsSync() ?: getDefaultSettings().also {
            nfcSettingsDao.insertSettings(it)
        }
    }

    suspend fun initializeSettingsIfNeeded() {
        val existing = nfcSettingsDao.getSettingsSync()
        if (existing == null) {
            nfcSettingsDao.insertSettings(getDefaultSettings())
        }
    }

    suspend fun updateSettings(settings: NFCSettingsEntity) {
        nfcSettingsDao.updateSettings(settings)
    }

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

    suspend fun updateBackgroundServiceMonitoringEnabled(enabled: Boolean) {
        nfcSettingsDao.updateBackgroundServiceMonitoringEnabled(enabled)
    }

    suspend fun updateCustomNotificationSoundUri(soundUri: String?) {
        nfcSettingsDao.updateCustomNotificationSoundUri(soundUri)
    }

    suspend fun updateSelectedLanguageCode(languageCode: String?) { // <<< NEW FUNCTION
        nfcSettingsDao.updateSelectedLanguageCode(languageCode)     // <<< NEW FUNCTION
    }                                                              // <<< NEW FUNCTION

    fun isAutoReminderEnabled(): Flow<Boolean> = nfcSettingsDao.isAutoReminderEnabled()
    fun getReminderInterval(): Flow<Int> = nfcSettingsDao.getReminderInterval()
    fun areNotificationsEnabled(): Flow<Boolean> = nfcSettingsDao.areNotificationsEnabled()
    fun isDarkModeEnabled(): Flow<Boolean> = nfcSettingsDao.isDarkModeEnabled()
    fun isBatteryOptimized(): Flow<Boolean> = nfcSettingsDao.isBatteryOptimized()
    fun isBackgroundServiceMonitoringEnabled(): Flow<Boolean> = nfcSettingsDao.isBackgroundServiceMonitoringEnabled()
    fun getSelectedLanguageCode(): Flow<String?> = nfcSettingsDao.getSelectedLanguageCode() // <<< NEW FUNCTION


    suspend fun resetAllSettings() {
        nfcSettingsDao.resetSettings()
        nfcSettingsDao.insertSettings(getDefaultSettings())
    }

    // ==================== HELPER METHODS ===================

    private fun getDefaultSettings(): NFCSettingsEntity {
        return NFCSettingsEntity(
            id = 1,
            isNFCMonitoringEnabled = true,
            autoReminderEnabled = false,
            reminderInterval = 10,
            showNotifications = true,
            vibrationEnabled = true,
            isDarkMode = true,
            accentColor = "#ef4444",
            backgroundServiceMonitoringEnabled = false,
            customNotificationSoundUri = null,
            selectedLanguageCode = null // <<< Ensure new field is in default
        )
    }

    suspend fun getEventStatistics(): Map<String, Int> {
        val allEvents = getAllEvents().first()
        return allEvents.groupBy { it.eventType }.mapValues { it.value.size }
    }
}
