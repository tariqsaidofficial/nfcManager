package com.dxbmark.nfcmanager.data.repository

import android.util.Log
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
        try {
            Log.e("NFCRepository", "=== initializeSettingsIfNeeded() STARTED ===")
            val existing = nfcSettingsDao.getSettingsSync()
            if (existing == null) {
                Log.e("NFCRepository", "No existing settings found, inserting default settings...")
                nfcSettingsDao.insertSettings(getDefaultSettings())
                Log.e("NFCRepository", "Default settings inserted successfully")
            } else {
                Log.e("NFCRepository", "Settings already exist, skipping initialization")
            }
            Log.e("NFCRepository", "=== initializeSettingsIfNeeded() COMPLETED ===")
        } catch (e: Exception) {
            Log.e("NFCRepository", "ERROR in initializeSettingsIfNeeded(): ${e.message}", e)
            // Check if it's a storage space issue
            if (e.message?.contains("No space left on device") == true || 
                e.message?.contains("ENOSPC") == true) {
                Log.e("NFCRepository", "STORAGE SPACE ERROR: Device is out of storage space!")
                // Don't try to insert again if it's a space issue
                return
            }
            // If there's another error, try to insert default settings
            try {
                Log.e("NFCRepository", "Retrying to insert default settings...")
                nfcSettingsDao.insertSettings(getDefaultSettings())
                Log.e("NFCRepository", "Default settings inserted on retry")
            } catch (insertError: Exception) {
                Log.e("NFCRepository", "CRITICAL ERROR: Failed to insert settings even on retry: ${insertError.message}", insertError)
                // If even that fails, log the error but don't crash
                insertError.printStackTrace()
            }
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

    // Onboarding
    suspend fun updateOnboardingCompleted(completed: Boolean) {
        try {
            nfcSettingsDao.updateOnboardingCompleted(completed)
        } catch (e: Exception) {
            e.printStackTrace()
            // If update fails, try to get current settings and update them
            try {
                val currentSettings = getSettingsSync()
                val updatedSettings = currentSettings.copy(isOnboardingCompleted = completed)
                nfcSettingsDao.updateSettings(updatedSettings)
            } catch (updateError: Exception) {
                updateError.printStackTrace()
                // If all else fails, just log the error and continue
                // The UI will still work because we set the state immediately
            }
        }
    }

    // Security Score
    suspend fun updateSecurityScore(score: Int) {
        nfcSettingsDao.updateSecurityScore(score)
    }

    suspend fun updateSecurityLevel(level: String) {
        nfcSettingsDao.updateSecurityLevel(level)
    }

    suspend fun updateSecurityScore(score: Int, level: String) {
        nfcSettingsDao.updateSecurityScoreAndLevel(score, level)
    }

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
            selectedLanguageCode = null, // <<< Ensure new field is in default
            isOnboardingCompleted = false,
            lastSecurityScore = 100,
            securityLevel = "EXCELLENT"
        )
    }

    suspend fun getEventStatistics(): Map<String, Int> {
        val allEvents = getAllEvents().first()
        return allEvents.groupBy { it.eventType }.mapValues { it.value.size }
    }

    suspend fun getEventsFromLastDays(days: Int): Flow<List<NFCEventEntity>> {
        val cutoffDate = Date(System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L))
        return nfcEventDao.getEventsFromDate(cutoffDate)
    }
}
