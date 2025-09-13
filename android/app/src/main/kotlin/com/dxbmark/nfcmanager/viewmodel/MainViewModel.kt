package com.dxbmark.nfcmanager.viewmodel

import android.app.Activity
import android.app.Application
import android.nfc.NfcAdapter // Added import
import android.util.Log // Import Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.data.database.entities.NFCEventEntity
import com.dxbmark.nfcmanager.data.database.entities.NFCSettingsEntity
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.utils.NFCUtils // Assuming NFCUtils is reliable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val app: Application, // Changed to private val to use it in methods
    private val repository: NFCRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(MainUiState(isLoading = true))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // TODO: These should be properly initialized and collected from the repository
    val settings: StateFlow<NFCSettingsEntity> = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NFCSettingsEntity(isOnboardingCompleted = true)
        )

    val todayEvents: StateFlow<List<NFCEventEntity>> = repository.getTodayEvents() // Corrected function name
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayEventCount: StateFlow<Int> = todayEvents.map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Removed other temporary StateFlows as they should come from settings

    init {
        Log.d("MainViewModelInit", "MainViewModel anminimal init started")
        refreshNfcStatus() // Call refreshNfcStatus to perform the check
        // Additional initializations (like fetching settings, events) should be done here
        viewModelScope.launch {
            // Example: Log initial event
            logEvent(app.getString(R.string.event_type_system), app.getString(R.string.mainviewmodel_initialized), "Shield", isImportant = false)
            _uiState.update { it.copy(isLoading = false) } // Set loading to false after initial checks
        }
    }

    fun refreshNfcStatus() {
        Log.d("MainViewModel", "refreshNfcStatus called")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val nfcAdapter = NfcAdapter.getDefaultAdapter(app.applicationContext)
            val hasNfc = nfcAdapter != null
            val isNfcEnabled = hasNfc && nfcAdapter!!.isEnabled

            Log.d("MainViewModel", "NFC Supported: $hasNfc, NFC Enabled: $isNfcEnabled")
            _uiState.update {
                it.copy(
                    isNFCSupported = hasNfc,
                    isNFCEnabled = isNfcEnabled,
                    isLoading = false
                )
            }
        }
    }

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
        Log.d("MainViewModel", "logEvent called: $message")
        viewModelScope.launch {
            repository.logEvent(eventType, message, icon, tagId, tagType, data, appPackage, isImportant)
        }
    }

    fun logRealNfcTagScan(tagIdHex: String, action: String) {
        logEvent(
            eventType = app.getString(R.string.event_type_nfc_scan),
            message = app.getString(R.string.tag_scanned_format, tagIdHex, action),
            icon = "NFCTag", // Consider using a drawable resource or a constant
            tagId = tagIdHex,
            tagType = action,
            isImportant = true
        )
    }

    fun deleteEvent(event: NFCEventEntity) {
        Log.d("MainViewModel", "deleteEvent called for ${event.id}")
        viewModelScope.launch {
            repository.deleteEvent(event)
            // Optionally show a success message or handle error
        }
    }

    fun clearAllEvents() {
        Log.d("MainViewModel", "clearAllEvents called")
        viewModelScope.launch {
            repository.clearAllEvents()
            // Optionally show a success message or handle error
        }
    }

    // Removed toggles for settings as they should be handled in SettingsViewModel
    // If MainViewModel needs to react to settings changes, it should collect `settings` StateFlow

    private fun updateNFCStatus(enabled: Boolean) { // Kept for potential internal use but refreshNfcStatus is primary
        _uiState.update { it.copy(isNFCEnabled = enabled) }
    }

    private fun updateNFCSupport(supported: Boolean) { // Kept for potential internal use
        _uiState.update { it.copy(isNFCSupported = supported) }
    }

    fun requestOpenNfcSettings(activity: Activity) {
        Log.d("MainViewModel", "requestOpenNfcSettings called")
        NFCUtils.openNfcSettings(activity)
    }

    fun showPrivacyNotification(show: Boolean) {
        _uiState.update { it.copy(showPrivacyNotification = show) }
    }

    fun dismissPrivacyNotification() {
        _uiState.update { it.copy(showPrivacyNotification = false) }
    }

    fun updateLoading(isLoading: Boolean) { // This can be used by specific operations
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    private fun updateError(message: String) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }

    private fun updateSuccess(message: String) {
        _uiState.update { it.copy(successMessage = message, isLoading = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun updateLastActivity() {
        _uiState.update { it.copy(lastActivity = Date()) }
    }

    fun formatTime(seconds: Long): String {
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }

    fun formatRelativeTime(date: Date): String {
        val seconds = (System.currentTimeMillis() - date.time) / 1000
        val context = getApplication<Application>()
        return when {
            seconds < 5 -> context.getString(R.string.time_now) // Consider more granular for "now"
            seconds < 60 -> "${seconds}s ago" // Replace with plural string resources if needed
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            else -> "${seconds / 86400}d ago"
        }
    }

    suspend fun getPerformanceStats(): Map<String, Any> {
        Log.d("MainViewModel", "getPerformanceStats called")
        // This should ideally call a method in the repository
        return withContext(Dispatchers.IO) {
            // Simulate some stat fetching
            mapOf("cpuUsage" to "15%", "memoryUsage" to "250MB")
        }
    }
}

data class MainUiState(
    val isLoading: Boolean = false,
    val isNFCSupported: Boolean = false,
    val isNFCEnabled: Boolean = false,
    val showPrivacyNotification: Boolean = false,
    val lastActivity: Date = Date(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)
