package com.dxbmark.nfcmanager.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.data.database.entities.NFCEventEntity
import com.dxbmark.nfcmanager.data.database.entities.NFCSettingsEntity
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.utils.NFCUtils
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
            val app = getApplication<Application>()
            logEvent(app.getString(R.string.event_type_system), app.getString(R.string.mainviewmodel_initialized), "Shield", isImportant = false)
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
        val app = getApplication<Application>()
        logEvent(
            eventType = app.getString(R.string.event_type_nfc_scan),
            message = app.getString(R.string.tag_scanned_format, tagIdHex, action),
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
                val app = getApplication<Application>()
                logEvent(app.getString(R.string.event_type_system), app.getString(R.string.event_deleted), "Settings")
            } catch (e: Exception) {
                val app = getApplication<Application>()
                updateError(app.getString(R.string.failed_to_delete_event, e.message ?: "Unknown error"))
            }
        }
    }
    
    fun clearAllEvents() {
        viewModelScope.launch {
            try {
                repository.clearAllEvents()
                val app = getApplication<Application>()
                logEvent(app.getString(R.string.event_type_system), app.getString(R.string.all_events_cleared), "Settings", isImportant = true)
                updateSuccess(app.getString(R.string.all_events_cleared_success))
            } catch (e: Exception) {
                val app = getApplication<Application>()
                updateError(app.getString(R.string.failed_to_clear_events, e.message ?: "Unknown error"))
            }
        }
    }
    
    // ==================== SETTINGS MANAGEMENT ====================
    
    fun toggleAutoReminder() {
        viewModelScope.launch {
            try {
                val currentState = autoReminderEnabled.value
                repository.updateAutoReminderEnabled(!currentState)
                val app = getApplication<Application>()
                logEvent(
                    app.getString(R.string.event_type_settings), 
                    if (!currentState) app.getString(R.string.auto_reminder_enabled) else app.getString(R.string.auto_reminder_disabled), 
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
                val app = getApplication<Application>()
                logEvent(app.getString(R.string.event_type_settings), app.getString(R.string.reminder_interval_set, interval), "Clock")
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
                val app = getApplication<Application>()
                logEvent(app.getString(R.string.event_type_settings), if (!current) app.getString(R.string.notifications_enabled) else app.getString(R.string.notifications_disabled), "Bell")
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
                val app = getApplication<Application>()
                logEvent(app.getString(R.string.event_type_settings), if (!current) app.getString(R.string.vibration_enabled) else app.getString(R.string.vibration_disabled), "Vibrate")
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
        val app = getApplication<Application>()
        logEvent(
            app.getString(R.string.event_type_system), 
            if (enabled) app.getString(R.string.nfc_hardware_enabled) else app.getString(R.string.nfc_hardware_disabled), 
            "Power",
            isImportant = true
        )
        updateLastActivity()
    }
    
    private fun updateNFCSupport(supported: Boolean) {
        _uiState.value = _uiState.value.copy(isNFCSupported = supported)
        if (!supported) {
            val app = getApplication<Application>()
            logEvent(app.getString(R.string.event_type_system), app.getString(R.string.nfc_not_supported_device), "Shield", isImportant = true)
        }
    }

    fun requestOpenNfcSettings(activity: Activity) {
        NFCUtils.openNfcSettings(activity) // This one is fine as it launches an Intent
    }
    
    fun showPrivacyNotification(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPrivacyNotification = show)
        if (show) {
            val app = getApplication<Application>()
            logEvent(app.getString(R.string.event_type_privacy), app.getString(R.string.privacy_alert_shown), "Bell", isImportant = true)
        }
    }
    
    fun dismissPrivacyNotification() {
        _uiState.value = _uiState.value.copy(showPrivacyNotification = false)
        updateLastActivity()
        val app = getApplication<Application>()
        logEvent(app.getString(R.string.event_type_privacy), app.getString(R.string.privacy_alert_dismissed), "Shield")
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
            seconds < 60 -> getApplication<Application>().getString(R.string.time_now)
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
