package com.dxbmark.nfcmanager.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log // Import Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.data.database.entities.NFCEventEntity
import com.dxbmark.nfcmanager.data.database.entities.NFCSettingsEntity
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.utils.NFCUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application, 
    private val repository: NFCRepository
) : AndroidViewModel(application) {
    
    init {
        Log.e("MainViewModel", "=== MainViewModel.init() STARTED ===")
        Log.e("MainViewModel", "MainViewModel initialized successfully")
        Log.e("MainViewModel", "=== MainViewModel.init() COMPLETED ===")
    }
    
    private val _uiState = MutableStateFlow(MainUiState(isLoading = true)) // Start with loading true
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // Temporarily use simple StateFlows with default values
    val settings: StateFlow<NFCSettingsEntity> = 
        MutableStateFlow(NFCSettingsEntity(isOnboardingCompleted = true)) // Default with onboarding completed
        .asStateFlow()
    
    val todayEvents: StateFlow<List<NFCEventEntity>> = 
        MutableStateFlow(emptyList<NFCEventEntity>()) 
        .asStateFlow()
    
    val todayEventCount: StateFlow<Int> = 
        MutableStateFlow(0)
        .asStateFlow()
    
    val autoReminderEnabled: StateFlow<Boolean> = 
        MutableStateFlow(false)
        .asStateFlow()
    
    val reminderInterval: StateFlow<Int> = 
        MutableStateFlow(30)
        .asStateFlow()
    
    init {
        Log.d("MainViewModelInit", "MainViewModel anminimal init started")
        // All complex initialization logic is temporarily commented out
        /*
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                logEvent(app.getString(R.string.event_type_system), app.getString(R.string.mainviewmodel_initialized), "Shield", isImportant = false)
                val appContext = getApplication<Application>().applicationContext
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
            } catch (e: Exception) {
                Log.e("MainViewModelInit", "Error during MainViewModel initialization", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Initialization failed: ${e.message}", 
                    isLoading = false
                )
            }
        }
        */
        // Simulate loading completion after a short delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Small delay 
            _uiState.value = MainUiState(isLoading = false, isNFCSupported = true, isNFCEnabled = true)
            Log.d("MainViewModelInit", "MainViewModel minimal init finished, UI state updated.")
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
        // viewModelScope.launch { repository.logEvent(...) } // Temporarily disable repository interaction
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
        Log.d("MainViewModel", "deleteEvent called for ${event.id}")
    }
    
    fun clearAllEvents() {
         Log.d("MainViewModel", "clearAllEvents called")
    }
    
    fun toggleAutoReminder() {
        Log.d("MainViewModel", "toggleAutoReminder called")
    }
    
    fun updateReminderInterval(interval: Int) {
        Log.d("MainViewModel", "updateReminderInterval called with $interval")
    }
    
    fun toggleNotifications() {
        Log.d("MainViewModel", "toggleNotifications called")
    }
    
    fun toggleVibration() {
         Log.d("MainViewModel", "toggleVibration called")
    }
    
    fun refreshNfcStatus() {
        Log.d("MainViewModel", "refreshNfcStatus called")
        _uiState.value = _uiState.value.copy(isNFCEnabled = true, isNFCSupported = true) // Simplified
    }

    private fun updateNFCStatus(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isNFCEnabled = enabled)
    }
    
    private fun updateNFCSupport(supported: Boolean) {
        _uiState.value = _uiState.value.copy(isNFCSupported = supported)
    }

    fun requestOpenNfcSettings(activity: Activity) {
       Log.d("MainViewModel", "requestOpenNfcSettings called")
       // NFCUtils.openNfcSettings(activity) // Actual call commented out
    }
    
    fun showPrivacyNotification(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPrivacyNotification = show)
    }
    
    fun dismissPrivacyNotification() {
        _uiState.value = _uiState.value.copy(showPrivacyNotification = false)
    }
    
    fun updateLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }
    
    private fun updateError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, isLoading = false)
    }
    
    private fun updateSuccess(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message, isLoading = false)
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
    
    private fun updateLastActivity() {
        _uiState.value = _uiState.value.copy(lastActivity = Date())
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
        return when {
            seconds < 60 -> getApplication<Application>().getString(R.string.time_now)
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            else -> "${seconds / 86400}d ago"
        }
    }
    
    suspend fun getPerformanceStats(): Map<String, Any> {
        Log.d("MainViewModel", "getPerformanceStats called")
        return emptyMap()
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
