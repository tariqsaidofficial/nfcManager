# API Documentation - NFC Manager

## Overview

This document provides comprehensive API documentation for NFC Manager's internal APIs, including Repository interfaces, ViewModel methods, Database operations, and Service endpoints.

## Table of Contents

1. [Repository APIs](#repository-apis)
2. [ViewModel APIs](#viewmodel-apis)
3. [Database APIs](#database-apis)
4. [Service APIs](#service-apis)
5. [Utility APIs](#utility-apis)
6. [Data Models](#data-models)
7. [Error Handling](#error-handling)

## Repository APIs

### NFCRepository

The central repository for managing NFC data and settings.

#### Event Management

```kotlin
/**
 * Retrieves all NFC events as a reactive Flow
 * @return Flow<List<NFCEventEntity>> Stream of all events
 */
fun getAllEvents(): Flow<List<NFCEventEntity>>

/**
 * Retrieves today's NFC events
 * @return Flow<List<NFCEventEntity>> Stream of today's events only
 */
fun getTodayEvents(): Flow<List<NFCEventEntity>>

/**
 * Gets count of today's events
 * @return Flow<Int> Stream of event count
 */
fun getTodayEventCount(): Flow<Int>

/**
 * Filters events by type
 * @param type Event type (READ, WRITE, STATE_CHANGED, etc.)
 * @return Flow<List<NFCEventEntity>> Filtered events stream
 */
fun getEventsByType(type: String): Flow<List<NFCEventEntity>>

/**
 * Retrieves important events only
 * @return Flow<List<NFCEventEntity>> Stream of important events
 */
fun getImportantEvents(): Flow<List<NFCEventEntity>>

/**
 * Searches events by message content
 * @param query Search query string
 * @return Flow<List<NFCEventEntity>> Matching events stream
 */
fun searchEvents(query: String): Flow<List<NFCEventEntity>>

/**
 * Inserts a new NFC event
 * @param event The event entity to insert
 * @return Long The ID of the inserted event
 * @throws DatabaseException If insertion fails
 */
suspend fun insertEvent(event: NFCEventEntity): Long

/**
 * Logs a new event with current timestamp
 * @param eventType Type of the event
 * @param message Descriptive message
 * @param icon Icon name for UI display
 * @param tagId Optional NFC tag ID
 * @param tagType Optional tag type
 * @param data Optional additional data
 * @param appPackage Optional app package name
 * @param isImportant Whether the event is important (default: false)
 * @return Long The ID of the logged event
 */
suspend fun logEvent(
    eventType: String,
    message: String,
    icon: String,
    tagId: String? = null,
    tagType: String? = null,
    data: String? = null,
    appPackage: String? = null,
    isImportant: Boolean = false
): Long
```

#### Settings Management

```kotlin
/**
 * Retrieves app settings as reactive Flow
 * @return Flow<NFCSettingsEntity> Stream of current settings
 */
fun getSettings(): Flow<NFCSettingsEntity>

/**
 * Retrieves settings synchronously
 * @return NFCSettingsEntity Current settings
 */
suspend fun getSettingsSync(): NFCSettingsEntity

/**
 * Initializes default settings if none exist
 */
suspend fun initializeSettingsIfNeeded()

/**
 * Updates complete settings object
 * @param settings New settings to save
 */
suspend fun updateSettings(settings: NFCSettingsEntity)

/**
 * Updates auto-reminder enabled state
 * @param enabled Whether auto-reminder is enabled
 */
suspend fun updateAutoReminderEnabled(enabled: Boolean)

/**
 * Updates reminder interval
 * @param interval Interval in seconds (5-300)
 * @throws IllegalArgumentException If interval is out of range
 */
suspend fun updateReminderInterval(interval: Int)

/**
 * Updates background service monitoring state
 * @param enabled Whether background monitoring is enabled
 */
suspend fun updateBackgroundServiceMonitoringEnabled(enabled: Boolean)
```

#### Data Cleanup

```kotlin
/**
 * Removes events older than specified days
 * @param daysToKeep Number of days to retain (default: 30)
 */
suspend fun cleanupOldEvents(daysToKeep: Int = 30)

/**
 * Removes all stored events
 */
suspend fun clearAllEvents()

/**
 * Resets all settings to defaults
 */
suspend fun resetAllSettings()
```

## ViewModel APIs

### MainViewModel

Manages main screen state and NFC operations.

#### State Properties

```kotlin
/**
 * Current UI state
 */
val uiState: StateFlow<MainUiState>

/**
 * Current app settings
 */
val settings: StateFlow<NFCSettingsEntity>

/**
 * Today's events list
 */
val todayEvents: StateFlow<List<NFCEventEntity>>

/**
 * Count of today's events
 */
val todayEventCount: StateFlow<Int>

/**
 * Auto-reminder enabled state
 */
val autoReminderEnabled: StateFlow<Boolean>

/**
 * Current reminder interval
 */
val reminderInterval: StateFlow<Int>
```

#### Event Operations

```kotlin
/**
 * Logs a new NFC event
 * @param eventType Type of event
 * @param message Event description
 * @param icon Icon identifier
 * @param tagId Optional tag ID
 * @param tagType Optional tag type
 * @param data Optional additional data
 * @param appPackage Optional app package
 * @param isImportant Whether event is important
 */
fun logEvent(
    eventType: String,
    message: String,
    icon: String,
    tagId: String? = null,
    tagType: String? = null,
    data: String? = null,
    appPackage: String? = null,
    isImportant: Boolean = false
)

/**
 * Logs real NFC tag scan
 * @param tagIdHex Tag ID in hexadecimal format
 * @param action Action performed on tag
 */
fun logRealNfcTagScan(tagIdHex: String, action: String)

/**
 * Deletes specific event
 * @param event Event to delete
 */
fun deleteEvent(event: NFCEventEntity)

/**
 * Clears all events
 */
fun clearAllEvents()
```

#### NFC State Management

```kotlin
/**
 * Refreshes current NFC status
 */
fun refreshNfcStatus()

/**
 * Opens NFC settings in system
 * @param activity Current activity context
 */
fun requestOpenNfcSettings(activity: Activity)

/**
 * Shows or hides privacy notification
 * @param show Whether to show notification
 */
fun showPrivacyNotification(show: Boolean)

/**
 * Dismisses privacy notification
 */
fun dismissPrivacyNotification()
```

### SettingsViewModel

Manages settings screen and configuration.

#### State Properties

```kotlin
/**
 * Settings UI state
 */
val uiState: StateFlow<SettingsUiState>

/**
 * Current settings
 */
val settings: StateFlow<NFCSettingsEntity>

/**
 * Notification permission request flow
 */
val requestNotificationPermissionFlow: SharedFlow<Unit>

/**
 * NFC permission request flow
 */
val requestNfcPermissionFlow: SharedFlow<Unit>
```

#### Configuration Methods

```kotlin
/**
 * Toggles background service monitoring
 */
fun toggleBackgroundServiceMonitoring()

/**
 * Toggles auto-reminder feature
 */
fun toggleAutoReminder()

/**
 * Updates reminder interval
 * @param interval New interval in seconds (5-300)
 */
fun updateReminderInterval(interval: Int)

/**
 * Toggles notification settings
 */
fun toggleNotifications()

/**
 * Toggles vibration settings
 */
fun toggleVibration()

/**
 * Toggles sound settings
 */
fun toggleSound()

/**
 * Sets app theme
 * @param isDark Whether to use dark theme
 */
fun setTheme(isDark: Boolean)

/**
 * Updates accent color
 * @param color Hex color string
 */
fun updateAccentColor(color: String)

/**
 * Toggles privacy mode
 */
fun togglePrivacyMode()

/**
 * Toggles unknown tag blocking
 */
fun toggleBlockUnknownTags()
```

#### Validation Methods

```kotlin
/**
 * Validates reminder interval input
 * @param interval Interval as string
 * @return Boolean Whether input is valid
 */
fun validateReminderInterval(interval: String): Boolean

/**
 * Validates monitoring interval input
 * @param interval Interval as string
 * @return Boolean Whether input is valid
 */
fun validateMonitoringInterval(interval: String): Boolean

/**
 * Validates hex color input
 * @param color Color hex string
 * @return Boolean Whether color is valid
 */
fun validateColorHex(color: String): Boolean
```

## Database APIs

### NFCEventDao

Data access object for NFC events.

#### Query Methods

```kotlin
/**
 * Gets all events ordered by timestamp
 */
@Query("SELECT * FROM nfc_events ORDER BY timestamp DESC")
fun getAllEvents(): Flow<List<NFCEventEntity>>

/**
 * Gets today's events only
 */
@Query("""
    SELECT * FROM nfc_events 
    WHERE DATE(timestamp/1000, 'unixepoch') = DATE('now') 
    ORDER BY timestamp DESC
""")
fun getTodayEvents(): Flow<List<NFCEventEntity>>

/**
 * Counts today's events
 */
@Query("""
    SELECT COUNT(*) FROM nfc_events 
    WHERE DATE(timestamp/1000, 'unixepoch') = DATE('now')
""")
fun getTodayEventCount(): Flow<Int>

/**
 * Filters events by type
 * @param type Event type to filter by
 */
@Query("SELECT * FROM nfc_events WHERE eventType = :type ORDER BY timestamp DESC")
fun getEventsByType(type: String): Flow<List<NFCEventEntity>>

/**
 * Gets important events only
 */
@Query("SELECT * FROM nfc_events WHERE isImportant = 1 ORDER BY timestamp DESC")
fun getImportantEvents(): Flow<List<NFCEventEntity>>

/**
 * Searches events by content
 * @param query Search query
 */
@Query("""
    SELECT * FROM nfc_events 
    WHERE message LIKE '%' || :query || '%' 
    OR eventType LIKE '%' || :query || '%'
    ORDER BY timestamp DESC
""")
fun searchEvents(query: String): Flow<List<NFCEventEntity>>
```

#### Modification Methods

```kotlin
/**
 * Inserts new event
 * @param event Event to insert
 * @return Long Generated event ID
 */
@Insert
suspend fun insertEvent(event: NFCEventEntity): Long

/**
 * Deletes specific event
 * @param event Event to delete
 */
@Delete
suspend fun deleteEvent(event: NFCEventEntity)

/**
 * Deletes events older than cutoff date
 * @param cutoffDate Date before which to delete
 */
@Query("DELETE FROM nfc_events WHERE timestamp < :cutoffDate")
suspend fun deleteOldEvents(cutoffDate: Date)

/**
 * Deletes all events
 */
@Query("DELETE FROM nfc_events")
suspend fun deleteAllEvents()
```

### NFCSettingsDao

Data access object for app settings.

#### Settings Queries

```kotlin
/**
 * Gets current settings
 */
@Query("SELECT * FROM nfc_settings WHERE id = 1")
fun getSettings(): Flow<NFCSettingsEntity?>

/**
 * Gets settings synchronously
 */
@Query("SELECT * FROM nfc_settings WHERE id = 1")
suspend fun getSettingsSync(): NFCSettingsEntity?

/**
 * Inserts or replaces settings
 */
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertSettings(settings: NFCSettingsEntity)

/**
 * Updates existing settings
 */
@Update
suspend fun updateSettings(settings: NFCSettingsEntity)
```

#### Specific Updates

```kotlin
/**
 * Updates auto-reminder setting
 */
@Query("UPDATE nfc_settings SET autoReminderEnabled = :enabled WHERE id = 1")
suspend fun updateAutoReminderEnabled(enabled: Boolean)

/**
 * Updates reminder interval
 */
@Query("UPDATE nfc_settings SET reminderInterval = :interval WHERE id = 1")
suspend fun updateReminderInterval(interval: Int)

/**
 * Updates background monitoring setting
 */
@Query("UPDATE nfc_settings SET backgroundServiceMonitoringEnabled = :enabled WHERE id = 1")
suspend fun updateBackgroundServiceMonitoringEnabled(enabled: Boolean)
```

## Service APIs

### NfcMonitoringService

Background service for NFC monitoring.

#### Service Actions

```kotlin
companion object {
    /**
     * Action to start NFC monitoring
     */
    const val ACTION_START_MONITORING = "com.nothingos.nfcmanager.services.ACTION_START_MONITORING"
    
    /**
     * Action to stop NFC monitoring
     */
    const val ACTION_STOP_MONITORING = "com.nothingos.nfcmanager.services.ACTION_STOP_MONITORING"
}
```

#### Service Methods

```kotlin
/**
 * Starts service command handling
 * @param intent Service intent with action
 * @param flags Service flags
 * @param startId Start ID
 * @return Int Service restart behavior
 */
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int

/**
 * Handles service binding (not supported)
 * @param intent Binding intent
 * @return IBinder? Always null
 */
override fun onBind(intent: Intent?): IBinder?

/**
 * Cleans up when service is destroyed
 */
override fun onDestroy()
```

#### Private Methods

```kotlin
/**
 * Initializes NFC adapter
 */
private fun initializeNfcAdapter()

/**
 * Starts NFC monitoring operations
 */
private fun startNfcMonitoring()

/**
 * Stops NFC monitoring operations
 */
private fun stopNfcMonitoring()

/**
 * Creates notification channel for service
 */
private fun createNotificationChannel()

/**
 * Creates service notification
 * @param contentText Notification content
 * @return Notification Service notification
 */
private fun createNotification(contentText: String): Notification

/**
 * Updates service notification
 * @param contentText New notification content
 */
private fun updateNotification(contentText: String)
```

## Utility APIs

### NFCUtils

NFC-related utility functions.

```kotlin
/**
 * Checks if device has NFC adapter
 * @param context Application context
 * @return Boolean True if NFC adapter exists
 */
fun hasNfcAdapter(context: Context): Boolean

/**
 * Checks if NFC is currently enabled
 * @param context Application context
 * @return Boolean True if NFC is enabled
 */
fun isNfcEnabled(context: Context): Boolean

/**
 * Opens NFC settings in system
 * @param activity Current activity
 */
fun openNfcSettings(activity: Activity)

/**
 * Opens wireless settings as fallback
 * @param activity Current activity
 */
fun openWirelessSettings(activity: Activity)
```

## Data Models

### NFCEventEntity

Represents an NFC event in the database.

```kotlin
@Entity(tableName = "nfc_events")
data class NFCEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Date,              // Event timestamp
    val eventType: String,            // Event type
    val message: String,              // Event description
    val icon: String,                 // UI icon name
    val tagId: String? = null,        // NFC tag ID
    val tagType: String? = null,      // Tag type
    val data: String? = null,         // Additional data
    val appPackage: String? = null,   // App package name
    val isBlocked: Boolean = false,   // Whether blocked
    val isImportant: Boolean = false  // Whether important
)
```

### NFCSettingsEntity

Represents app settings in the database.

```kotlin
@Entity(tableName = "nfc_settings")
data class NFCSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    
    // Main NFC settings
    val isNFCMonitoringEnabled: Boolean = true,
    val isPrivacyModeEnabled: Boolean = false,
    val autoReminderEnabled: Boolean = false,
    val reminderInterval: Int = 10,
    
    // Privacy & Security
    val blockUnknownTags: Boolean = false,
    val autoBlockSuspiciousTags: Boolean = false,
    val logSensitiveData: Boolean = false,
    
    // UI & Notifications
    val showNotifications: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val notificationStyle: String = "standard",
    
    // Performance
    val batteryOptimized: Boolean = false,
    val monitoringInterval: Int = 2000,
    val backgroundServiceMonitoringEnabled: Boolean = false,
    
    // Theme
    val isDarkMode: Boolean = true,
    val accentColor: String = "#ef4444"
)
```

### UI State Models

```kotlin
/**
 * Main screen UI state
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

/**
 * Settings screen UI state
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showResetConfirmation: Boolean = false,
    val showExportDialog: Boolean = false
)
```

## Error Handling

### Exception Types

```kotlin
/**
 * NFC not supported on device
 */
class NFCNotSupportedException(message: String) : Exception(message)

/**
 * NFC is disabled
 */
class NFCNotEnabledException(message: String) : Exception(message)

/**
 * Required permissions not granted
 */
class PermissionDeniedException(message: String) : SecurityException(message)

/**
 * Database operation failed
 */
class DatabaseException(message: String, cause: Throwable?) : Exception(message, cause)

/**
 * Service operation failed
 */
class ServiceException(message: String) : Exception(message)
```

### Error Response Format

```kotlin
/**
 * Standard error response
 */
data class ErrorResponse(
    val code: Int,
    val message: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```

## Constants

### Event Types

```kotlin
object EventTypes {
    const val NFC_ENABLED = "NFC_ENABLED"
    const val NFC_DISABLED = "NFC_DISABLED"
    const val TAG_DISCOVERED = "TAG_DISCOVERED"
    const val TAG_READ = "TAG_READ"
    const val TAG_WRITE = "TAG_WRITE"
    const val PRIVACY_ALERT = "PRIVACY_ALERT"
    const val SECURITY_WARNING = "SECURITY_WARNING"
    const val SYSTEM_EVENT = "SYSTEM_EVENT"
    const val SETTINGS_CHANGED = "SETTINGS_CHANGED"
}
```

### Icon Types

```kotlin
object IconTypes {
    const val NFC = "Nfc"
    const val SHIELD = "Shield"
    const val SETTINGS = "Settings"
    const val BELL = "Bell"
    const val POWER = "Power"
    const val CLOCK = "Clock"
    const val VIBRATE = "Vibrate"
    const val VOLUME = "Volume2"
    const val BATTERY = "Battery"
    const val RADAR = "Radar"
}
```

## Usage Examples

### Logging an Event

```kotlin
viewModelScope.launch {
    try {
        repository.logEvent(
            eventType = EventTypes.TAG_DISCOVERED,
            message = "NFC tag detected",
            icon = IconTypes.NFC,
            tagId = "04:A1:B2:C3",
            tagType = "MIFARE_CLASSIC",
            isImportant = true
        )
    } catch (e: DatabaseException) {
        handleError("Failed to log event: ${e.message}")
    }
}
```

### Updating Settings

```kotlin
fun updateReminderInterval(interval: Int) {
    if (!validateReminderInterval(interval.toString())) {
        updateError("Invalid interval: must be between 5 and 300 seconds")
        return
    }
    
    viewModelScope.launch {
        try {
            repository.updateReminderInterval(interval)
            updateSuccess("Reminder interval updated to ${interval}s")
        } catch (e: Exception) {
            updateError("Failed to update interval: ${e.message}")
        }
    }
}
```

### Observing Data

```kotlin
@Composable
fun EventsList(viewModel: ActivityViewModel) {
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LazyColumn {
        if (isLoading) {
            item { LoadingIndicator() }
        } else {
            items(events) { event ->
                EventItem(
                    event = event,
                    onDelete = { viewModel.deleteEvent(it) }
                )
            }
        }
    }
}
```

---

*This API documentation is automatically generated and kept in sync with the codebase. For the most up-to-date information, refer to the source code and inline documentation.*