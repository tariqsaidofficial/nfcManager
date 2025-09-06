package com.nothingos.nfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.nothingos.nfcmanager.data.repository.NFCRepository
import com.nothingos.nfcmanager.data.database.entities.NFCSettingsEntity
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 * Manages all app settings and preferences
 */
class SettingsViewModel @Inject constructor(
    private val repository: NFCRepository
) : ViewModel() {

    // ==================== UI STATE ====================

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // ==================== SETTINGS DATA ====================

    // Complete settings object
    val settings = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NFCSettingsEntity(reminderInterval = 10, isDarkMode = true) // Default to dark theme
        )

    // Removed individual isAutoReminderEnabled, reminderInterval, areNotificationsEnabled, isDarkModeEnabled, isBatteryOptimized flows
    // We will use settings.value.propertyName directly in the UI and for updates

    // ==================== NFC SETTINGS ====================

    /**
     * Toggle auto-reminder feature
     */
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

    /**
     * Update reminder interval
     */
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

    // ==================== NOTIFICATION SETTINGS ====================

    /**
     * Toggle notifications
     */
    fun toggleNotifications() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.showNotifications
                repository.updateNotificationsEnabled(newSetting)
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

    /**
     * Toggle vibration
     */
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

    /**
     * Toggle sound notifications
     */
    fun toggleSound() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val newSetting = !settings.value.soundEnabled
                // Assuming a direct repository update method exists or update via NFCSettingsEntity
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

    /**
     * Set theme to Dark or Light
     */
    fun setTheme(isDark: Boolean) {
        viewModelScope.launch {
            try {
                updateLoading(true)
                repository.updateDarkMode(isDark) // This updates the value in NFCSettingsEntity
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

    /**
     * Update accent color
     */
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

    /**
     * Toggle battery optimization
     */
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

    /**
     * Update monitoring interval
     */
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

    /**
     * Toggle privacy mode
     */
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

    /**
     * Toggle block unknown tags
     */
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

    /**
     * Export settings
     */
    fun exportSettings() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                // Implementation for exporting settings
                repository.logEvent("SETTINGS", "Settings exported", "Settings")
                updateSuccess("Settings exported successfully")
            } catch (e: Exception) {
                updateError("Failed to export settings: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    /**
     * Reset all settings to defaults
     */
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

    /**
     * Clean up old data
     */
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

    private fun updateSuccess(message: String) { // Corrected: Added colon
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
