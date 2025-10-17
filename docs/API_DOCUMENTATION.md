# 📚 API Documentation - NFC Manager

**Last Updated:** October 16, 2025  
**Version:** 1.0.0  
**Status:** Production Ready ✅

This document provides comprehensive API documentation for the NFC Manager application, including all classes, methods, and interfaces.

---

## 📋 Table of Contents

1. [Core Classes](#core-classes)
2. [ViewModels](#viewmodels)
3. [Repository Layer](#repository-layer)
4. [Database Layer](#database-layer)
5. [Services](#services)
6. [Utilities](#utilities)
7. [Error Handling](#error-handling)
8. [UI Components](#ui-components)

---

## 🏗️ Core Classes

### NfcManagerApplication

Main application class with Hilt dependency injection.

```kotlin
@HiltAndroidApp
class NfcManagerApplication : Application() {
    
    /**
     * Called when the application is starting
     * Initializes logging and starts NFC monitoring if enabled
     */
    override fun onCreate()
    
    /**
     * Starts NFC monitoring service if enabled in settings
     */
    private suspend fun startNfcMonitoringIfEnabled()
}
```

### MainActivity

Main activity hosting the Compose UI.

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    /**
     * Sets up the main UI with Compose
     * Handles NFC adapter initialization
     */
    override fun onCreate(savedInstanceState: Bundle?)
    
    /**
     * Handles NFC tag discovery intents
     */
    override fun onNewIntent(intent: Intent?)
}
```

---

## 🧠 ViewModels

### MainViewModel

Manages home screen state and NFC operations.

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: NFCRepository,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    
    // State Properties
    val nfcStatus: StateFlow<NFCStatus>
    val isMonitoring: StateFlow<Boolean>
    val lastEvent: StateFlow<NFCEventEntity?>
    val privacyScore: StateFlow<Int>
    val securityLevel: StateFlow<SecurityLevel>
    
    // Actions
    fun toggleMonitoring()
    fun refreshNfcStatus()
    fun calculatePrivacyScore()
    
    // Private Methods
    private fun startMonitoringService()
    private fun stopMonitoringService()
    private fun updateNfcStatus()
}
```

### SettingsViewModel

Manages app settings and preferences.

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: NFCRepository,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    
    // State Properties
    val settings: StateFlow<NFCSettingsEntity>
    val isDarkTheme: StateFlow<Boolean>
    val selectedLanguage: StateFlow<String>
    val notificationSound: StateFlow<String?>
    
    // Actions
    fun updateAlertInterval(interval: Int)
    fun toggleTheme()
    fun changeLanguage(language: String)
    fun updateNotificationSound(uri: String?)
    fun resetToDefaults()
    
    // Private Methods
    private fun saveSettings(settings: NFCSettingsEntity)
    private fun validateSettings(settings: NFCSettingsEntity): Boolean
}
```

### ActivityViewModel

Manages activity log and filtering.

```kotlin
@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val repository: NFCRepository,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    
    // State Properties
    val events: StateFlow<List<NFCEventEntity>>
    val filteredEvents: StateFlow<List<NFCEventEntity>>
    val selectedEventType: StateFlow<String?>
    val selectedDateRange: StateFlow<DateRange>
    val isLoading: StateFlow<Boolean>
    
    // Actions
    fun filterByEventType(type: String?)
    fun filterByDateRange(range: DateRange)
    fun exportToCsv(): Flow<Result<String>>
    fun clearAllEvents()
    fun deleteEvent(eventId: Long)
    
    // Private Methods
    private fun applyFilters()
    private fun generateCsvContent(events: List<NFCEventEntity>): String
}
```

---

## 🗄️ Repository Layer

### NFCRepository

Main repository for NFC data operations.

```kotlin
@Singleton
class NFCRepository @Inject constructor(
    private val eventDao: NFCEventDao,
    private val settingsDao: NFCSettingsDao,
    private val errorHandler: ErrorHandler
) {
    
    // Event Operations
    suspend fun insertEvent(event: NFCEventEntity): Result<Long>
    suspend fun getAllEvents(): Flow<List<NFCEventEntity>>
    suspend fun getEventsByType(type: String): Flow<List<NFCEventEntity>>
    suspend fun getEventsByDateRange(start: Long, end: Long): Flow<List<NFCEventEntity>>
    suspend fun deleteEvent(eventId: Long): Result<Unit>
    suspend fun clearAllEvents(): Result<Unit>
    
    // Settings Operations
    suspend fun getSettings(): Flow<NFCSettingsEntity>
    suspend fun getSettingsSync(): NFCSettingsEntity
    suspend fun updateSettings(settings: NFCSettingsEntity): Result<Unit>
    suspend fun resetSettings(): Result<Unit>
    
    // Statistics
    suspend fun getEventCount(): Flow<Int>
    suspend fun getEventCountByType(): Flow<Map<String, Int>>
    suspend fun getLastEvent(): Flow<NFCEventEntity?>
    
    // Private Methods
    private suspend fun getDefaultSettings(): NFCSettingsEntity
    private fun handleDatabaseError(error: Throwable): AppError
}
```

---

## 🗃️ Database Layer

### AppDatabase

Room database configuration.

```kotlin
@Database(
    entities = [NFCEventEntity::class, NFCSettingsEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun eventDao(): NFCEventDao
    abstract fun settingsDao(): NFCSettingsDao
    
    companion object {
        const val DATABASE_NAME = "nfc_manager_database"
    }
}
```

### NFCEventDao

Data access object for NFC events.

```kotlin
@Dao
interface NFCEventDao {
    
    @Query("SELECT * FROM nfc_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<NFCEventEntity>>
    
    @Query("SELECT * FROM nfc_events WHERE event_type = :type ORDER BY timestamp DESC")
    fun getEventsByType(type: String): Flow<List<NFCEventEntity>>
    
    @Query("SELECT * FROM nfc_events WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getEventsByDateRange(start: Long, end: Long): Flow<List<NFCEventEntity>>
    
    @Insert
    suspend fun insertEvent(event: NFCEventEntity): Long
    
    @Delete
    suspend fun deleteEvent(event: NFCEventEntity)
    
    @Query("DELETE FROM nfc_events")
    suspend fun clearAllEvents()
    
    @Query("SELECT COUNT(*) FROM nfc_events")
    fun getEventCount(): Flow<Int>
    
    @Query("SELECT * FROM nfc_events ORDER BY timestamp DESC LIMIT 1")
    fun getLastEvent(): Flow<NFCEventEntity?>
}
```

### NFCSettingsDao

Data access object for app settings.

```kotlin
@Dao
interface NFCSettingsDao {
    
    @Query("SELECT * FROM nfc_settings LIMIT 1")
    fun getSettings(): Flow<NFCSettingsEntity>
    
    @Query("SELECT * FROM nfc_settings LIMIT 1")
    suspend fun getSettingsSync(): NFCSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: NFCSettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: NFCSettingsEntity)
    
    @Query("DELETE FROM nfc_settings")
    suspend fun clearSettings()
}
```

---

## ⚙️ Services

### NfcMonitoringService

Foreground service for NFC monitoring.

```kotlin
@AndroidEntryPoint
class NfcMonitoringService : Service() {
    
    @Inject lateinit var nfcRepository: NFCRepository
    @Inject lateinit var appNotificationManager: AppNotificationManager
    
    // Service Lifecycle
    override fun onCreate()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    override fun onDestroy()
    override fun onBind(intent: Intent?): IBinder? = null
    
    // NFC Monitoring
    private fun startNfcMonitoring()
    private fun stopNfcMonitoring()
    private fun startPeriodicMonitoring()
    private fun updateNfcStatusNotification()
    
    // WakeLock Management
    private fun initializeWakeLock()
    private fun acquireWakeLock()
    private fun releaseWakeLock()
    
    // Settings Management
    private suspend fun getCachedOrFetchSettings(): NFCSettingsEntity
    
    // Notification Management
    private fun createNotificationChannel(settings: NFCSettingsEntity?)
    private fun createNotification(contentText: String): Notification
    
    companion object {
        const val ACTION_START_MONITORING = "com.dxbmark.nfcmanager.services.ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "com.dxbmark.nfcmanager.services.ACTION_STOP_MONITORING"
    }
}
```

---

## 🔧 Utilities

### AppLogger

Centralized logging utility.

```kotlin
object AppLogger {
    
    // Log Levels
    enum class Level { VERBOSE, DEBUG, INFO, WARNING, ERROR }
    
    // General Logging
    fun v(tag: String?, message: String, throwable: Throwable? = null)
    fun d(tag: String?, message: String, throwable: Throwable? = null)
    fun i(tag: String?, message: String, throwable: Throwable? = null)
    fun w(tag: String?, message: String, throwable: Throwable? = null)
    fun e(tag: String?, message: String, throwable: Throwable? = null)
    
    // Specialized Logging
    fun nfc(message: String, throwable: Throwable? = null)
    fun database(message: String, throwable: Throwable? = null)
    fun network(message: String, throwable: Throwable? = null)
    fun service(message: String, throwable: Throwable? = null)
    fun ui(message: String, throwable: Throwable? = null)
    fun viewModel(message: String, throwable: Throwable? = null)
    
    // Private Methods
    private fun log(level: Level, tag: String?, message: String, throwable: Throwable?)
    private fun getTag(tag: String?): String
    private fun formatMessage(message: String, throwable: Throwable?): String
}
```

### NotificationManager

Manages app notifications.

```kotlin
@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val errorHandler: ErrorHandler
) {
    
    // Notification Management
    fun createNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int,
        soundUri: Uri? = null
    )
    
    fun showNotification(
        notificationId: Int,
        title: String,
        content: String,
        channelId: String,
        pendingIntent: PendingIntent? = null
    )
    
    fun cancelNotification(notificationId: Int)
    fun cancelAllNotifications()
    
    // Sound Management
    fun playNotificationSound(soundUri: Uri?)
    fun stopNotificationSound()
    
    // Private Methods
    private fun createNotificationBuilder(channelId: String): NotificationCompat.Builder
    private fun getDefaultSoundUri(): Uri
}
```

### PrivacyScoreCalculator

Calculates privacy scores based on NFC activity.

```kotlin
class PrivacyScoreCalculator {
    
    /**
     * Calculates privacy score based on NFC events and settings
     * @param events List of NFC events
     * @param settings Current app settings
     * @return Privacy score (0-100)
     */
    fun calculateScore(
        events: List<NFCEventEntity>,
        settings: NFCSettingsEntity
    ): Int
    
    /**
     * Determines security level based on privacy score
     * @param score Privacy score
     * @return Security level enum
     */
    fun getSecurityLevel(score: Int): SecurityLevel
    
    // Private Methods
    private fun calculateEventScore(events: List<NFCEventEntity>): Int
    private fun calculateSettingsScore(settings: NFCSettingsEntity): Int
    private fun calculateTimeScore(events: List<NFCEventEntity>): Int
}
```

---

## ❌ Error Handling

### AppError

Sealed class for application errors.

```kotlin
sealed class AppError(
    val message: String,
    val cause: Throwable? = null,
    val errorCode: String? = null
) {
    
    // Database Errors
    data class DatabaseError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause, "DB_ERROR")
    
    // NFC Errors
    data class NfcError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause, "NFC_ERROR")
    
    // Permission Errors
    data class PermissionError(
        override val message: String,
        val permission: String
    ) : AppError(message, null, "PERMISSION_ERROR")
    
    // Network Errors
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause, "NETWORK_ERROR")
    
    // Validation Errors
    data class ValidationError(
        override val message: String,
        val field: String? = null
    ) : AppError(message, null, "VALIDATION_ERROR")
    
    // Unknown Errors
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause, "UNKNOWN_ERROR")
}
```

### ErrorHandler

Centralized error handling.

```kotlin
@Singleton
class ErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Handles errors and returns user-friendly messages
     * @param error The error to handle
     * @return User-friendly error message
     */
    fun handleError(error: Throwable): String
    
    /**
     * Maps exceptions to AppError instances
     * @param throwable The exception to map
     * @return Corresponding AppError
     */
    fun mapToAppError(throwable: Throwable): AppError
    
    /**
     * Gets localized error message
     * @param error The AppError instance
     * @return Localized error message
     */
    fun getLocalizedMessage(error: AppError): String
    
    // Private Methods
    private fun getDatabaseErrorMessage(error: AppError.DatabaseError): String
    private fun getNfcErrorMessage(error: AppError.NfcError): String
    private fun getPermissionErrorMessage(error: AppError.PermissionError): String
    private fun getNetworkErrorMessage(error: AppError.NetworkError): String
    private fun getValidationErrorMessage(error: AppError.ValidationError): String
    private fun getUnknownErrorMessage(error: AppError.UnknownError): String
}
```

---

## 🎨 UI Components

### NFCManagerNavigation

Main navigation component.

```kotlin
@Composable
fun NFCManagerNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") { HomeScreen(navController) }
        composable("activity") { ActivityScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("notification_sound") { NotificationSoundSettingsScreen(navController) }
    }
}
```

### ErrorScreen

Error display component.

```kotlin
@Composable
fun ErrorScreen(
    error: String,
    details: String? = null,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Error UI implementation
}
```

---

## 📊 Data Models

### NFCEventEntity

Database entity for NFC events.

```kotlin
@Entity(tableName = "nfc_events")
data class NFCEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "event_type")
    val eventType: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "tag_id")
    val tagId: String? = null,
    
    @ColumnInfo(name = "technology")
    val technology: String? = null,
    
    @ColumnInfo(name = "location")
    val location: String? = null,
    
    @ColumnInfo(name = "additional_info")
    val additionalInfo: String? = null
)
```

### NFCSettingsEntity

Database entity for app settings.

```kotlin
@Entity(tableName = "nfc_settings")
data class NFCSettingsEntity(
    @PrimaryKey
    val id: Long = 1,
    
    @ColumnInfo(name = "is_monitoring_enabled")
    val isMonitoringEnabled: Boolean = false,
    
    @ColumnInfo(name = "alert_interval")
    val alertInterval: Int = 10,
    
    @ColumnInfo(name = "is_dark_theme")
    val isDarkTheme: Boolean = false,
    
    @ColumnInfo(name = "selected_language")
    val selectedLanguage: String = "en",
    
    @ColumnInfo(name = "notification_sound_uri")
    val notificationSoundUri: String? = null,
    
    @ColumnInfo(name = "vibration_enabled")
    val vibrationEnabled: Boolean = true,
    
    @ColumnInfo(name = "privacy_mode")
    val privacyMode: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

---

## 🔍 Enums & Constants

### SecurityLevel

Privacy security levels.

```kotlin
enum class SecurityLevel(val displayName: String, val color: Color) {
    HIGH("High", Color.Green),
    MEDIUM("Medium", Color.Yellow),
    LOW("Low", Color.Orange),
    CRITICAL("Critical", Color.Red)
}
```

### NFCStatus

NFC adapter status.

```kotlin
enum class NFCStatus {
    AVAILABLE,
    DISABLED,
    NOT_SUPPORTED,
    UNKNOWN
}
```

---

## 📱 Usage Examples

### Starting NFC Monitoring

```kotlin
// In ViewModel
fun startMonitoring() {
    viewModelScope.launch {
        try {
            val intent = Intent(context, NfcMonitoringService::class.java).apply {
                action = NfcMonitoringService.ACTION_START_MONITORING
            }
            context.startForegroundService(intent)
            _isMonitoring.value = true
        } catch (e: Exception) {
            val error = errorHandler.handleError(e)
            AppLogger.e(TAG, "Failed to start monitoring", e)
        }
    }
}
```

### Filtering Events

```kotlin
// In ActivityViewModel
fun filterByDateRange(range: DateRange) {
    viewModelScope.launch {
        try {
            val filteredEvents = when (range) {
                DateRange.TODAY -> repository.getEventsByDateRange(
                    start = getTodayStart(),
                    end = System.currentTimeMillis()
                )
                DateRange.LAST_7_DAYS -> repository.getEventsByDateRange(
                    start = get7DaysAgo(),
                    end = System.currentTimeMillis()
                )
                // ... other ranges
            }
            _filteredEvents.value = filteredEvents.first()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to filter events", e)
        }
    }
}
```

### Error Handling

```kotlin
// In Repository
suspend fun insertEvent(event: NFCEventEntity): Result<Long> {
    return try {
        val id = eventDao.insertEvent(event)
        AppLogger.d(TAG, "Event inserted with ID: $id")
        Result.success(id)
    } catch (e: Exception) {
        val error = errorHandler.mapToAppError(e)
        AppLogger.e(TAG, "Failed to insert event", e)
        Result.failure(error)
    }
}
```

---

## 🔗 Related Documentation

- [Architecture Overview](ARCHITECTURE.md)
- [Implementation Plan](IMPLEMENTATION_PLAN.md)
- [Security Review](SECURITY_REVIEW.md)
- [Progress Tracker](PROGRESS_TRACKER.md)

---

**Last Updated:** October 16, 2025  
**Version:** 1.0.0
