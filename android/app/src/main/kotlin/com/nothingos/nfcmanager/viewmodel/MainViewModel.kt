package com.nothingos.nfcmanager.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nothingos.nfcmanager.data.database.entities.NFCEventEntity
import com.nothingos.nfcmanager.data.database.entities.NFCSettingsEntity
import com.nothingos.nfcmanager.data.repository.NFCRepository
import com.nothingos.nfcmanager.utils.NFCUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers // Added import
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Added import
import java.util.Date
import javax.inject.Inject

/**
 * Main ViewModel following official MVVM patterns
 * Manages UI state and business logic for the main NFC screen
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application, 
    private val repository: NFCRepository
) : AndroidViewModel(application) {
    
    // ==================== UI STATE ====================
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // ==================== DATA STREAMS ====================
    
    val settings = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NFCSettingsEntity()
        )
    
    val todayEvents = repository.getTodayEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val todayEventCount = repository.getTodayEventCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    val autoReminderEnabled = repository.isAutoReminderEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    val reminderInterval = repository.getReminderInterval()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 30
        )
    
    // ==================== INITIALIZATION ====================
    
    init {
        viewModelScope.launch {
            logEvent("SYSTEM", "MainViewModel initialized", "Shield", isImportant = false)
            val appContext = getApplication<Application>().applicationContext
            // Perform NFC checks on IO dispatcher
            val hasAdapter = withContext(Dispatchers.IO) {
                NFCUtils.hasNfcAdapter(appContext)
            }
            updateNFCSupport(hasAdapter)
            if (hasAdapter) {
                val isEnabled = withContext(Dispatchers.IO) {
                    NFCUtils.isNfcEnabled(appContext)
                }
                updateNFCStatus(isEnabled)
            }
        }
    }
    
    // ==================== NFC EVENT LOGGING ====================
    
    fun logEvent(
        eventType: String,
        message: String,
        icon: String,
        tagId: String? = null,
        tagType: String? = null,
        data: String? = null,
        appPackage: String? = null,
        isImportant: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                repository.logEvent(
                    eventType = eventType,
                    message = message,
                    icon = icon,
                    tagId = tagId,
                    tagType = tagType,
                    data = data,
                    appPackage = appPackage,
                    isImportant = isImportant
                )
                updateLastActivity()
            } catch (e: Exception) {
                updateError("Failed to log event: ${e.message}")
            }
        }
    }

    fun logRealNfcTagScan(tagIdHex: String, action: String) {
        logEvent(
            eventType = "NFC_SCAN",
            message = "Tag Scanned: $tagIdHex (Action: $action)",
            icon = "NFCTag",
            tagId = tagIdHex,
            tagType = action,
            isImportant = true
        )
    }
    
    fun deleteEvent(event: NFCEventEntity) {
        viewModelScope.launch {
            try {
                repository.deleteEvent(event)
                logEvent("SYSTEM", "Event deleted", "Settings")
            } catch (e: Exception) {
                updateError("Failed to delete event: ${e.message}")
            }
        }
    }
    
    fun clearAllEvents() {
        viewModelScope.launch {
            try {
                repository.clearAllEvents()
                logEvent("SYSTEM", "All events cleared", "Settings", isImportant = true)
                updateSuccess("All events cleared successfully")
            } catch (e: Exception) {
                updateError("Failed to clear events: ${e.message}")
            }
        }
    }
    
    // ==================== SETTINGS MANAGEMENT ====================
    
    fun toggleAutoReminder() {
        viewModelScope.launch {
            try {
                val currentState = autoReminderEnabled.value
                repository.updateAutoReminderEnabled(!currentState)
                logEvent(
                    "SETTINGS", 
                    "Auto-reminder ${if (!currentState) "enabled" else "disabled"}", 
                    "Bell"
                )
            } catch (e: Exception) {
                updateError("Failed to toggle auto-reminder: ${e.message}")
            }
        }
    }
    
    fun updateReminderInterval(interval: Int) {
        viewModelScope.launch {
            try {
                repository.updateReminderInterval(interval)
                logEvent("SETTINGS", "Reminder interval set to ${interval}s", "Clock")
            } catch (e: Exception) {
                updateError("Failed to update interval: ${e.message}")
            }
        }
    }
    
    fun toggleNotifications() {
        viewModelScope.launch {
            try {
                val current = settings.value.showNotifications
                repository.updateNotificationsEnabled(!current)
                logEvent("SETTINGS", "Notifications ${if (!current) "enabled" else "disabled"}", "Bell")
            } catch (e: Exception) {
                updateError("Failed to toggle notifications: ${e.message}")
            }
        }
    }
    
    fun toggleVibration() {
        viewModelScope.launch {
            try {
                val current = settings.value.vibrationEnabled
                repository.updateVibrationEnabled(!current)
                logEvent("SETTINGS", "Vibration ${if (!current) "enabled" else "disabled"}", "Vibrate")
            } catch (e: Exception) {
                updateError("Failed to toggle vibration: ${e.message}")
            }
        }
    }
    
    // ==================== NFC STATE MANAGEMENT ====================
    
    fun refreshNfcStatus() {
        viewModelScope.launch {
            val appContext = getApplication<Application>().applicationContext
            // Perform NFC checks on IO dispatcher
            val hasAdapter = withContext(Dispatchers.IO) {
                NFCUtils.hasNfcAdapter(appContext)
            }
            updateNFCSupport(hasAdapter)
            if (hasAdapter) {
                val isEnabled = withContext(Dispatchers.IO) {
                    NFCUtils.isNfcEnabled(appContext)
                }
                if (_uiState.value.isNFCEnabled != isEnabled) {
                    updateNFCStatus(isEnabled)
                } else {
                     _uiState.value = _uiState.value.copy(isNFCEnabled = isEnabled)
                }
            } else {
                 _uiState.value = _uiState.value.copy(isNFCEnabled = false)
            }
        }
    }

    private fun updateNFCStatus(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isNFCEnabled = enabled)
        logEvent(
            "NFC_STATE", 
            "NFC hardware now ${if (enabled) "enabled" else "disabled"}", 
            "Power",
            isImportant = true
        )
        updateLastActivity()
    }
    
    private fun updateNFCSupport(supported: Boolean) {
        _uiState.value = _uiState.value.copy(isNFCSupported = supported)
        if (!supported) {
            logEvent("SYSTEM", "NFC not supported on this device", "Shield", isImportant = true)
        }
    }

    fun requestOpenNfcSettings(activity: Activity) {
        NFCUtils.openNfcSettings(activity) // This one is fine as it launches an Intent
    }
    
    fun showPrivacyNotification(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPrivacyNotification = show)
        if (show) {
            logEvent("PRIVACY", "Privacy alert shown", "Bell", isImportant = true)
        }
    }
    
    fun dismissPrivacyNotification() {
        _uiState.value = _uiState.value.copy(showPrivacyNotification = false)
        updateLastActivity()
        logEvent("PRIVACY", "Privacy alert dismissed", "Shield")
    }
    
    // ==================== UI STATE HELPERS ====================
    
    fun updateLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }
    
    private fun updateError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isLoading = false
        )
    }
    
    private fun updateSuccess(message: String) {
        _uiState.value = _uiState.value.copy(
            successMessage = message,
            isLoading = false
        )
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    private fun updateLastActivity() {
        _uiState.value = _uiState.value.copy(lastActivity = Date())
    }
    
    // ==================== UTILITY METHODS ====================
    
    fun formatTime(seconds: Long): String {
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }
    
    fun formatRelativeTime(date: Date): String {
        val seconds = (System.currentTimeMillis() - date.time) / 1000
        return when {
            seconds < 60 -> "now"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            else -> "${seconds / 86400}d ago"
        }
    }
    
    suspend fun getPerformanceStats(): Map<String, Any> {
        return try {
            val eventStats = repository.getEventStatistics()
            val totalEvents = todayEventCount.value
            
            mapOf(
                "totalEvents" to totalEvents,
                "eventsByType" to eventStats,
                "uptime" to formatTime((System.currentTimeMillis() - _uiState.value.lastActivity.time) / 1000),
                "lastActivity" to _uiState.value.lastActivity
            )
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

/**
 * UI State data class
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val isNFCSupported: Boolean = false, 
    val isNFCEnabled: Boolean = false,   
    val showPrivacyNotification: Boolean = false,
    val lastActivity: Date = Date(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)
