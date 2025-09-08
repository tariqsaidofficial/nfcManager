package com.dxbmark.nfcmanager.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dxbmark.nfcmanager.NfcManagerApplication
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.data.database.entities.NFCSettingsEntity
import com.dxbmark.nfcmanager.data.repository.NFCRepository
import com.dxbmark.nfcmanager.services.NfcMonitoringService // For Action constants
import com.dxbmark.nfcmanager.ui.screens.availableLanguages
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val app: Application, // 'app' is the Application context
    private val repository: NFCRepository
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _requestNotificationPermissionChannel = MutableSharedFlow<Unit>(replay = 0)
    val requestNotificationPermissionFlow = _requestNotificationPermissionChannel.asSharedFlow()

    private val _requestNfcPermissionChannel = MutableSharedFlow<Unit>(replay = 0)
    val requestNfcPermissionFlow = _requestNfcPermissionChannel.asSharedFlow()

    private val _requestStoragePermissionChannel = MutableSharedFlow<Unit>(replay = 0)
    val requestStoragePermissionFlow = _requestStoragePermissionChannel.asSharedFlow()

    private val _recreateActivityChannel = MutableSharedFlow<Unit>(replay = 0)
    val recreateActivityFlow = _recreateActivityChannel.asSharedFlow()

    val settings = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NFCSettingsEntity()
        )

    // ... (other functions remain the same) ...

    fun updateSelectedLanguage(languageCode: String?) {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val oldLanguageCode = settings.value.selectedLanguageCode
                // Use 'app' (Application context) directly passed to the ViewModel's constructor
                val context: Context = app.applicationContext 

                fun getDisplayName(code: String?): String {
                    return availableLanguages.firstOrNull { it.code == code }?.let {
                        context.getString(it.nameResId)
                    } ?: context.getString(R.string.language_system_default)
                }
                
                fun getLogDisplayName(code: String?): String {
                    return when (code) {
                        "en" -> app.getString(R.string.language_name_english)
                        "ar" -> app.getString(R.string.language_name_arabic)
                        "de" -> app.getString(R.string.language_name_german)
                        else -> app.getString(R.string.language_name_system_default)
                    }
                }

                val currentLanguageDisplayName = getDisplayName(oldLanguageCode)
                val newLanguageDisplayName = getDisplayName(languageCode)

                if (oldLanguageCode == languageCode) {
                    updateSuccess(app.getString(R.string.language_already_set, currentLanguageDisplayName))
                    return@launch
                }

                repository.updateSelectedLanguageCode(languageCode)
                // Pass the application context to the updated static method
                NfcManagerApplication.updateLanguageCode(app.applicationContext, languageCode) 

                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    app.getString(R.string.language_changed_log, getLogDisplayName(oldLanguageCode), getLogDisplayName(languageCode)),
                    "Language"
                )
                updateSuccess(app.getString(R.string.language_updated_success, newLanguageDisplayName))
                _recreateActivityChannel.emit(Unit) 
            } catch (e: Exception) {
                updateError(app.getString(R.string.language_update_failed, e.message ?: "Unknown error"))
            } finally {
                updateLoading(false)
            }
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.resetAllSettings()
                // Pass context when resetting language in Application class
                NfcManagerApplication.updateLanguageCode(app.applicationContext, null) 
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    app.getString(R.string.settings_reset_log),
                    "Settings",
                    isImportant = true
                )
                updateSuccess(app.getString(R.string.settings_reset_success))
                _recreateActivityChannel.emit(Unit) // Also recreate on reset
            } catch (e: Exception) {
                updateError(app.getString(R.string.settings_reset_failed, e.message ?: "Unknown error"))
            } finally {
                updateLoading(false)
            }
        }
    }

    // ... (rest of the ViewModel code) ...

    private fun checkNfcPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            app.applicationContext,
            Manifest.permission.NFC
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(
            app.applicationContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestStoragePermissionForSoundPicker(): Boolean {
        if (checkStoragePermission()) {
            return true
        }
        viewModelScope.launch {
            _requestStoragePermissionChannel.emit(Unit)
        }
        return false
    }

    fun updateCustomNotificationSound(soundUri: String?) {
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.updateCustomNotificationSoundUri(soundUri)
                val message = if (soundUri != null) app.getString(R.string.notification_sound_updated) else app.getString(R.string.notification_sound_reset)
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    message,
                    if (soundUri != null) "SoundOn" else "SoundOff"
                )
                updateSuccess(message)
            } catch (e: Exception) {
                updateError(app.getString(R.string.notification_sound_update_failed, e.message ?: "Unknown error"))
            } finally {
                updateLoading(false)
            }
        }
    }

    fun toggleBackgroundServiceMonitoring() {
        viewModelScope.launch {
            val currentSetting = settings.value.backgroundServiceMonitoringEnabled
            val newSetting = !currentSetting
            val context = app.applicationContext

            if (newSetting) {
                if (checkNfcPermissions()) {
                    updateLoading(true)
                    try {
                        repository.updateBackgroundServiceMonitoringEnabled(true)
                        val intent = Intent(context, NfcMonitoringService::class.java).apply {
                            action = NfcMonitoringService.ACTION_START_MONITORING
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                        repository.logEvent(
                            app.getString(R.string.event_type_settings),
                            app.getString(R.string.background_monitoring_enabled_log),
                            "Radar"
                        )
                        updateSuccess(app.getString(R.string.background_monitoring_enabled_success))
                    } catch (e: Exception) {
                        updateError(app.getString(R.string.background_monitoring_enable_failed, e.message ?: "Unknown error"))
                        repository.updateBackgroundServiceMonitoringEnabled(false)
                    } finally {
                        updateLoading(false)
                    }
                } else {
                    _requestNfcPermissionChannel.emit(Unit)
                    updateError(app.getString(R.string.nfc_permission_required))
                }
            } else {
                updateLoading(true)
                try {
                    repository.updateBackgroundServiceMonitoringEnabled(false)
                    val intent = Intent(context, NfcMonitoringService::class.java).apply {
                        action = NfcMonitoringService.ACTION_STOP_MONITORING
                    }
                    context.startService(intent)
                        repository.logEvent(
                            app.getString(R.string.event_type_settings),
                            app.getString(R.string.background_monitoring_disabled_log),
                            "RadarOff"
                        )
                    updateSuccess("Background NFC Monitoring disabled")
                } catch (e: Exception) {
                    updateError("Failed to disable background monitoring: ${e.message}")
                } finally {
                    updateLoading(false)
                }
            }
        }
    }

    fun toggleAutoReminder() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.autoReminderEnabled
                repository.updateAutoReminderEnabled(newSetting)
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    if (newSetting) app.getString(R.string.auto_reminder_enabled) else app.getString(R.string.auto_reminder_disabled),
                    "Bell"
                )
                val statusText = if (newSetting) app.getString(R.string.enabled_status) else app.getString(R.string.disabled_status)
                updateSuccess(app.getString(R.string.auto_reminder_toggled_success, statusText))
            } catch (e: Exception) {
                updateError("Failed to toggle auto-reminder: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun updateReminderInterval(interval: Int) {
        if (interval < 5 || interval > 300) {
            updateError(app.getString(R.string.interval_out_of_range))
            return
        }
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.updateReminderInterval(interval)
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    app.getString(R.string.reminder_interval_set, interval),
                    "Clock"
                )
                updateSuccess("Reminder interval updated to ${interval} seconds")
            } catch (e: Exception) {
                updateError("Failed to update interval: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.showNotifications
                repository.updateNotificationsEnabled(newSetting)

                if (newSetting) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permissionStatus = ContextCompat.checkSelfPermission(
                            app.applicationContext,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                            _requestNotificationPermissionChannel.emit(Unit)
                        }
                    }
                }

                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    if (newSetting) app.getString(R.string.notifications_enabled) else app.getString(R.string.notifications_disabled),
                    "Bell"
                )
                updateSuccess(if (newSetting) app.getString(R.string.notifications_enabled) else app.getString(R.string.notifications_disabled))
            } catch (e: Exception) {
                updateError("Failed to toggle notifications: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun toggleVibration() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.vibrationEnabled
                repository.updateVibrationEnabled(newSetting)
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    if (newSetting) app.getString(R.string.vibration_enabled) else app.getString(R.string.vibration_disabled),
                    "Vibrate"
                )
                updateSuccess(if (newSetting) app.getString(R.string.vibration_enabled) else app.getString(R.string.vibration_disabled))
            } catch (e: Exception) {
                updateError("Failed to toggle vibration: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.soundEnabled
                repository.updateSettings(settings.value.copy(soundEnabled = newSetting))
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    if (newSetting) app.getString(R.string.sound_enabled) else app.getString(R.string.sound_disabled),
                    "Volume2"
                )
                updateSuccess(if (newSetting) app.getString(R.string.sound_enabled) else app.getString(R.string.sound_disabled))
            } catch (e: Exception) {
                updateError("Failed to toggle sound: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.updateDarkMode(isDark)
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    if (isDark) app.getString(R.string.dark_mode_enabled) else app.getString(R.string.light_mode_enabled),
                    "Settings"
                )
                updateSuccess(if (isDark) "Dark mode enabled" else "Light mode enabled")
            } catch (e: Exception) {
                updateError("Failed to set theme: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun updateAccentColor(color: String) {
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.updateSettings(settings.value.copy(accentColor = color))
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    app.getString(R.string.accent_color_updated),
                    "Settings"
                )
                updateSuccess("Accent color updated")
            } catch (e: Exception) {
                updateError("Failed to update color: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun toggleBatteryOptimization() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.batteryOptimized
                repository.updateBatteryOptimized(newSetting)
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    if (newSetting) app.getString(R.string.battery_optimization_enabled) else app.getString(R.string.battery_optimization_disabled),
                    "Battery"
                )
                updateSuccess(if (newSetting) app.getString(R.string.battery_optimization_enabled) else app.getString(R.string.battery_optimization_disabled))
            } catch (e: Exception) {
                updateError("Failed to toggle battery optimization: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun updateMonitoringInterval(interval: Int) {
        if (interval < 1000 || interval > 10000) {
            updateError("Monitoring interval must be between 1000 and 10000 milliseconds")
            return
        }
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.updateSettings(settings.value.copy(monitoringInterval = interval))
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    app.getString(R.string.monitoring_interval_set, interval),
                    "Clock"
                )
                updateSuccess("Monitoring interval updated")
            } catch (e: Exception) {
                updateError("Failed to update monitoring interval: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun togglePrivacyMode() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.isPrivacyModeEnabled
                repository.updateSettings(settings.value.copy(isPrivacyModeEnabled = newSetting))
                repository.logEvent(
                    app.getString(R.string.event_type_security),
                    if (newSetting) app.getString(R.string.privacy_mode_enabled) else app.getString(R.string.privacy_mode_disabled),
                    "Shield",
                    isImportant = true
                )
                updateSuccess(if (newSetting) app.getString(R.string.privacy_mode_enabled) else app.getString(R.string.privacy_mode_disabled))
            } catch (e: Exception) {
                updateError("Failed to toggle privacy mode: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun toggleBlockUnknownTags() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.blockUnknownTags
                repository.updateSettings(settings.value.copy(blockUnknownTags = newSetting))
                repository.logEvent(
                    app.getString(R.string.event_type_security),
                    if (newSetting) app.getString(R.string.unknown_tag_blocking_enabled) else app.getString(R.string.unknown_tag_blocking_disabled),
                    "Shield",
                    isImportant = true
                )
                updateSuccess(if (newSetting) app.getString(R.string.unknown_tag_blocking_enabled) else app.getString(R.string.unknown_tag_blocking_disabled))
            } catch (e: Exception) {
                updateError("Failed to toggle unknown tag blocking: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun exportSettings() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.logEvent(app.getString(R.string.event_type_settings), app.getString(R.string.settings_exported), "Settings")
                updateSuccess(app.getString(R.string.settings_exported))
            } catch (e: Exception) {
                updateError("Failed to export settings: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }


    fun cleanupOldData(daysToKeep: Int = 30) {
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.cleanupOldEvents(daysToKeep)
                repository.logEvent(
                    app.getString(R.string.event_type_settings),
                    app.getString(R.string.old_data_cleanup_completed, daysToKeep),
                    "Settings"
                )
                updateSuccess("Old data cleaned up successfully")
            } catch (e: Exception) {
                updateError("Failed to cleanup data: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    private fun updateLoading(isLoading: Boolean) {
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

    fun validateReminderInterval(interval: String): Boolean {
        return try {
            val value = interval.toInt()
            value in 5..300
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validateMonitoringInterval(interval: String): Boolean {
        return try {
            val value = interval.toInt()
            value in 1000..10000
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validateColorHex(color: String): Boolean {
        return color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showResetConfirmation: Boolean = false,
    val showExportDialog: Boolean = false
)
