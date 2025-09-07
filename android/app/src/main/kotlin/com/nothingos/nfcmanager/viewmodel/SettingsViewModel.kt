package com.nothingos.nfcmanager.viewmodel

import android.Manifest // Required for Manifest.permission.POST_NOTIFICATIONS
import android.app.Application // Required for AndroidViewModel and context
import android.content.pm.PackageManager // Required for PackageManager.PERMISSION_GRANTED
import android.os.Build // Required for Build.VERSION.SDK_INT
import androidx.core.content.ContextCompat // Required for ContextCompat.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nothingos.nfcmanager.data.database.entities.NFCSettingsEntity
import com.nothingos.nfcmanager.data.repository.NFCRepository
import com.nothingos.nfcmanager.services.NfcMonitoringService
import dagger.hilt.android.lifecycle.HiltViewModel // Import HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings screen
 * Manages all app settings and preferences
 */
@HiltViewModel // Add HiltViewModel annotation
class SettingsViewModel @Inject constructor(
    private val app: Application, 
    private val repository: NFCRepository
) : AndroidViewModel(app) {

    // ==================== UI STATE ====================

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Channel to request notification permission from the UI
    private val _requestNotificationPermissionChannel = MutableSharedFlow<Unit>(replay = 0)
    val requestNotificationPermissionFlow = _requestNotificationPermissionChannel.asSharedFlow()

    // ==================== SETTINGS DATA ====================

    val settings = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NFCSettingsEntity() // Ensure NFCSettingsEntity has default constructor or provide defaults
        )

    // ==================== NFC SETTINGS ====================

    fun toggleAutoReminder() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.autoReminderEnabled
                repository.updateAutoReminderEnabled(newSetting)
                repository.logEvent(
                    "SETTINGS",
                    "Auto-reminder ${if (newSetting) "enabled" else "disabled"}",
                    "Bell"
                )
                updateSuccess("Auto-reminder ${if (newSetting) "enabled" else "disabled"}")
            } catch (e: Exception) {
                updateError("Failed to toggle auto-reminder: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    fun updateReminderInterval(interval: Int) {
        if (interval < 5 || interval > 300) {
            updateError("Interval must be between 5 and 300 seconds")
            return
        }
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.updateReminderInterval(interval)
                repository.logEvent(
                    "SETTINGS",
                    "Reminder interval set to ${interval}s",
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

    // ==================== BACKGROUND SERVICE SETTINGS ====================

    fun toggleBackgroundServiceMonitoring() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val currentSetting = settings.value.backgroundServiceMonitoringEnabled
                val newSetting = !currentSetting
                repository.updateBackgroundServiceMonitoringEnabled(newSetting)

                if (newSetting) {
                    NfcMonitoringService.startService(app.applicationContext)
                } else {
                    NfcMonitoringService.stopService(app.applicationContext)
                }

                repository.logEvent(
                    "SETTINGS",
                    "Background NFC Monitoring ${if (newSetting) "enabled" else "disabled"}",
                    if (newSetting) "Radar" else "RadarOff"
                )
                updateSuccess("Background NFC Monitoring ${if (newSetting) "enabled" else "disabled"}")
            } catch (e: Exception) {
                updateError("Failed to toggle background monitoring: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    // ==================== NOTIFICATION SETTINGS ====================

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
                    "SETTINGS",
                    "Notifications ${if (newSetting) "enabled" else "disabled"}",
                    "Bell"
                )
                updateSuccess("Notifications ${if (newSetting) "enabled" else "disabled"}")
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
                    "SETTINGS",
                    "Vibration ${if (newSetting) "enabled" else "disabled"}",
                    "Vibrate"
                )
                updateSuccess("Vibration ${if (newSetting) "enabled" else "disabled"}")
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
                    "SETTINGS",
                    "Sound ${if (newSetting) "enabled" else "disabled"}",
                    "Volume2"
                )
                updateSuccess("Sound ${if (newSetting) "enabled" else "disabled"}")
            } catch (e: Exception) {
                updateError("Failed to toggle sound: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    // ==================== THEME SETTINGS ====================

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.updateDarkMode(isDark)
                repository.logEvent(
                    "SETTINGS",
                    if (isDark) "Dark mode enabled" else "Light mode enabled",
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
                    "SETTINGS",
                    "Accent color updated",
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

    // ==================== PERFORMANCE SETTINGS ====================

    fun toggleBatteryOptimization() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.batteryOptimized
                repository.updateBatteryOptimized(newSetting)
                repository.logEvent(
                    "SETTINGS",
                    "Battery optimization ${if (newSetting) "enabled" else "disabled"}",
                    "Battery"
                )
                updateSuccess("Battery optimization ${if (newSetting) "enabled" else "disabled"}")
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
                    "SETTINGS",
                    "Monitoring interval set to ${interval}ms",
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

    // ==================== PRIVACY & SECURITY ====================

    fun togglePrivacyMode() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.isPrivacyModeEnabled
                repository.updateSettings(settings.value.copy(isPrivacyModeEnabled = newSetting))
                repository.logEvent(
                    "SECURITY",
                    "Privacy mode ${if (newSetting) "enabled" else "disabled"}",
                    "Shield",
                    isImportant = true
                )
                updateSuccess("Privacy mode ${if (newSetting) "enabled" else "disabled"}")
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
                    "SECURITY",
                    "Block unknown tags ${if (newSetting) "enabled" else "disabled"}",
                    "Shield",
                    isImportant = true
                )
                updateSuccess("Unknown tag blocking ${if (newSetting) "enabled" else "disabled"}")
            } catch (e: Exception) {
                updateError("Failed to toggle unknown tag blocking: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    // ==================== DATA MANAGEMENT ====================

    fun exportSettings() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.logEvent("SETTINGS", "Settings exported", "Settings")
                updateSuccess("Settings exported successfully")
            } catch (e: Exception) {
                updateError("Failed to export settings: ${e.message}")
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
                repository.logEvent(
                    "SETTINGS",
                    "All settings reset to defaults",
                    "Settings",
                    isImportant = true
                )
                updateSuccess("All settings reset to defaults")
            } catch (e: Exception) {
                updateError("Failed to reset settings: ${e.message}")
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
                    "SETTINGS",
                    "Old data cleanup completed (${daysToKeep} days)",
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

    // ==================== UI STATE HELPERS ====================

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

    // ==================== VALIDATION ====================

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

/**
 * UI State for Settings screen
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showResetConfirmation: Boolean = false,
    val showExportDialog: Boolean = false
)
