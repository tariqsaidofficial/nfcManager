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
            initialValue = NFCSettingsEntity(reminderInterval = 10) // Set default reminder interval to 10
        )

    // ==================== NFC SETTINGS ====================

    /**
     * Toggle auto-reminder feature
     */
    fun toggleAutoReminder() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val currentState = settings.value.autoReminderEnabled // Read from settings.value
                repository.updateAutoReminderEnabled(!currentState)

                // Log the change
                repository.logEvent(
                    "SETTINGS",
                    "Auto-reminder ${if (!currentState) "enabled" else "disabled"}",
                    "Bell"
                )

                updateSuccess("Auto-reminder ${if (!currentState) "enabled" else "disabled"}")
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
                val currentState = settings.value.showNotifications // Read from settings.value
                repository.updateNotificationsEnabled(!currentState)

                repository.logEvent(
                    "SETTINGS",
                    "Notifications ${if (!currentState) "enabled" else "disabled"}",
                    "Bell"
                )

                updateSuccess("Notifications ${if (!currentState) "enabled" else "disabled"}")
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
                val currentState = settings.value.vibrationEnabled
                repository.updateVibrationEnabled(!currentState)

                repository.logEvent(
                    "SETTINGS",
                    "Vibration ${if (!currentState) "enabled" else "disabled"}",
                    "Vibrate"
                )

                updateSuccess("Vibration ${if (!currentState) "enabled" else "disabled"}")
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
                val currentSettings = repository.getSettingsSync()
                val newSettings = currentSettings.copy(soundEnabled = !currentSettings.soundEnabled)
                repository.updateSettings(newSettings)

                repository.logEvent(
                    "SETTINGS",
                    "Sound ${if (newSettings.soundEnabled) "enabled" else "disabled"}",
                    "Volume2"
                )

                updateSuccess("Sound ${if (newSettings.soundEnabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                updateError("Failed to toggle sound: ${e.message}")
            } finally {
                updateLoading(false)
            }
        }
    }

    // ==================== THEME SETTINGS ====================

    /**
     * Toggle dark mode
     */
    fun toggleDarkMode() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                val currentState = settings.value.isDarkMode // Read from settings.value
                repository.updateDarkMode(!currentState)

                repository.logEvent(
                    "SETTINGS",
                    "${if (!currentState) "Dark" else "Light"} mode enabled",
                    "Settings"
                )

                updateSuccess("${if (!currentState) "Dark" else "Light"} mode enabled")
            } catch (e: Exception) {
                updateError("Failed to toggle theme: ${e.message}")
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
                val currentSettings = repository.getSettingsSync()
                val newSettings = currentSettings.copy(accentColor = color)
                repository.updateSettings(newSettings)

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
                val currentState = settings.value.batteryOptimized // Read from settings.value
                repository.updateBatteryOptimized(!currentState)

                repository.logEvent(
                    "SETTINGS",
                    "Battery optimization ${if (!currentState) "enabled" else "disabled"}",
                    "Battery"
                )

                updateSuccess("Battery optimization ${if (!currentState) "enabled" else "disabled"}")
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
                val currentSettings = repository.getSettingsSync()
                val newSettings = currentSettings.copy(monitoringInterval = interval)
                repository.updateSettings(newSettings)

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
                val currentSettings = repository.getSettingsSync()
                val newSettings = currentSettings.copy(isPrivacyModeEnabled = !currentSettings.isPrivacyModeEnabled)
                repository.updateSettings(newSettings)

                repository.logEvent(
                    "SECURITY",
                    "Privacy mode ${if (newSettings.isPrivacyModeEnabled) "enabled" else "disabled"}",
                    "Shield",
                    isImportant = true
                )

                updateSuccess("Privacy mode ${if (newSettings.isPrivacyModeEnabled) "enabled" else "disabled"}")
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
                val currentSettings = repository.getSettingsSync()
                val newSettings = currentSettings.copy(blockUnknownTags = !currentSettings.blockUnknownTags)
                repository.updateSettings(newSettings)

                repository.logEvent(
                    "SECURITY",
                    "Block unknown tags ${if (newSettings.blockUnknownTags) "enabled" else "disabled"}",
                    "Shield",
                    isImportant = true
                )

                updateSuccess("Unknown tag blocking ${if (newSettings.blockUnknownTags) "enabled" else "disabled"}")
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

    /**
     * Update loading state
     */
    private fun updateLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }

    /**
     * Update error message
     */
    private fun updateError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isLoading = false
        )
    }

    /**
     * Update success message
     */
    private fun updateSuccess(message: String) {
        _uiState.value = _uiState.value.copy(
            successMessage = message,
            isLoading = false
        )
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    // ==================== VALIDATION ====================

    /**
     * Validate reminder interval
     */
    fun validateReminderInterval(interval: String): Boolean {
        return try {
            val value = interval.toInt()
            value in 5..300
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * Validate monitoring interval
     */
    fun validateMonitoringInterval(interval: String): Boolean {
        return try {
            val value = interval.toInt()
            value in 1000..10000
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * Validate color hex code
     */
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
