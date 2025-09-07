# Code Style Guide - NFC Manager

## Overview

This guide defines the standards and rules for writing code in the NFC Manager project to ensure consistency, readability, and maintainability.

## Table of Contents

1. [Kotlin Style Guide](#kotlin-style-guide)
2. [Android Architecture](#android-architecture)
3. [Jetpack Compose Guidelines](#jetpack-compose-guidelines)
4. [Database Design](#database-design)
5. [Naming Conventions](#naming-conventions)
6. [Documentation Standards](#documentation-standards)

---

## Kotlin Style Guide

### 1. Code Formatting

#### Spaces and Brackets
```kotlin
// ✅ Correct
class NFCManager {
    fun processTag(tag: Tag) {
        if (tag.isValid) {
            // process tag
        }
    }
}

// ❌ Wrong
class NFCManager{
    fun processTag(tag:Tag){
        if(tag.isValid){
            // process tag
        }
    }
}
```

#### Line Length
- **Maximum**: 120 characters
- **Preferred**: 100 characters

```kotlin
// ✅ Correct - Split long lines
fun logEvent(
    eventType: String,
    message: String,
    icon: String,
    isImportant: Boolean = false
) {
    // implementation
}

// ❌ Wrong - Long line
fun logEvent(eventType: String, message: String, icon: String, isImportant: Boolean = false) { }
```

### 2. Variable and Function Naming

#### CamelCase for Variables and Functions
```kotlin
// ✅ Correct
val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
fun updateReminderInterval(interval: Int) { }

// ❌ Wrong
val nfc_adapter = NfcAdapter.getDefaultAdapter(context)
fun update_reminder_interval(interval: Int) { }
```

#### PascalCase for Classes
```kotlin
// ✅ Correct
class NFCEventEntity
class MainViewModel
interface NFCRepository

// ❌ Wrong
class nfcEventEntity
class mainViewModel
interface nfcRepository
```

#### UPPER_SNAKE_CASE for Constants
```kotlin
// ✅ Correct
companion object {
    const val ACTION_START_MONITORING = "com.nothingos.nfcmanager.START"
    const val DEFAULT_REMINDER_INTERVAL = 10
}

// ❌ Wrong
companion object {
    const val actionStartMonitoring = "com.nothingos.nfcmanager.START"
    const val defaultReminderInterval = 10
}
```

### 3. Comments and Documentation

#### KDoc for Public Functions
```kotlin
/**
 * Log a new NFC event to the database
 * 
 * @param eventType Event type (READ, WRITE, STATE_CHANGED)
 * @param message Descriptive message for the event
 * @param icon Icon name for UI display
 * @param isImportant Whether the event is important (default false)
 * @return ID of the logged event
 * @throws DatabaseException If insertion fails
 */
suspend fun logEvent(
    eventType: String,
    message: String,
    icon: String,
    isImportant: Boolean = false
): Long
```

#### Single Line Comments
```kotlin
// ✅ Correct - Useful comment
// Check NFC support before proceeding
if (NFCUtils.hasNfcAdapter(context)) {
    // initialize NFC monitoring
}

// ❌ Wrong - Obvious comment
// Set variable to true
val isEnabled = true
```

---

## Android Architecture

### 1. MVVM Pattern

#### ViewModel Structure
```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: NFCRepository
) : AndroidViewModel(application) {
    
    // UI State - Always private mutable, public read-only
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // Data Streams - Use stateIn for shared data
    val settings = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NFCSettingsEntity()
        )
    
    // Business Logic Functions
    fun refreshNfcStatus() {
        viewModelScope.launch {
            // implementation
        }
    }
}
```

#### Repository Pattern
```kotlin
@Singleton
class NFCRepository @Inject constructor(
    private val eventDao: NFCEventDao,
    private val settingsDao: NFCSettingsDao
) {
    // Always use suspend for expensive operations
    suspend fun logEvent(event: NFCEventEntity): Long {
        return eventDao.insertEvent(event)
    }
    
    // Use Flow for reactive data
    fun getAllEvents(): Flow<List<NFCEventEntity>> {
        return eventDao.getAllEvents()
    }
}
```

### 2. Dependency Injection (Hilt)

#### Module Structure
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nfc_manager_database"
        ).build()
    }
    
    @Provides
    fun provideNFCEventDao(database: AppDatabase): NFCEventDao {
        return database.nfcEventDao()
    }
}
```

---

## Jetpack Compose Guidelines

### 1. Composable Functions

#### Composable Naming
```kotlin
// ✅ Correct - PascalCase
@Composable
fun HomeScreen(viewModel: MainViewModel) { }

@Composable
fun NFCStatusCard(
    isEnabled: Boolean,
    onToggle: () -> Unit
) { }

// ❌ Wrong
@Composable
fun homeScreen() { }

@Composable
fun nfcStatusCard() { }
```

#### Composable Parameters
```kotlin
// ✅ Correct - Logical parameter order
@Composable
fun EventItem(
    event: NFCEventEntity,                    // Data first
    modifier: Modifier = Modifier,            // Modifier always has default value
    onDelete: (NFCEventEntity) -> Unit = {},  // Actions last with default values
    onEdit: (NFCEventEntity) -> Unit = {}
) {
    // implementation
}
```

#### State Management
```kotlin
// ✅ Correct - Use remember for local state
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    
    // UI implementation
}

// ❌ Wrong - Not using remember
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    var showDialog = mutableStateOf(false) // Will be recreated on every recomposition
}
```

### 2. Theme Integration

#### Using Material Theme
```kotlin
// ✅ Correct
@Composable
fun NFCStatusCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            text = "NFC Status",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ❌ Wrong - Direct colors
@Composable
fun NFCStatusCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Text(
            text = "NFC Status",
            color = Color.Black
        )
    }
}
```

---

## Database Design

### 1. Room Entities

#### Table Naming
```kotlin
// ✅ Correct - Descriptive names with snake_case
@Entity(tableName = "nfc_events")
data class NFCEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Date,
    val eventType: String
)

@Entity(tableName = "nfc_settings")
data class NFCSettingsEntity(
    @PrimaryKey
    val id: Int = 1
)
```

#### DAO Queries
```kotlin
// ✅ Correct - Optimized and understandable queries
@Dao
interface NFCEventDao {
    
    @Query("""
        SELECT * FROM nfc_events 
        WHERE DATE(timestamp/1000, 'unixepoch') = DATE('now') 
        ORDER BY timestamp DESC
    """)
    fun getTodayEvents(): Flow<List<NFCEventEntity>>
    
    @Query("""
        SELECT * FROM nfc_events 
        WHERE message LIKE '%' || :query || '%' 
        OR eventType LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun searchEvents(query: String, limit: Int = 100): Flow<List<NFCEventEntity>>
}
```

---

## Naming Conventions

### 1. Files and Packages

#### Package Structure
```
com.nothingos.nfcmanager/
├── data/
│   ├── database/
│   │   ├── entities/        # Data models
│   │   └── dao/            # Data access interfaces
│   └── repository/         # Data repositories
├── di/                     # Dependency injection
├── services/               # Services
├── ui/
│   ├── components/         # Reusable components
│   ├── screens/           # Main screens
│   └── theme/             # Theme and colors
├── utils/                  # Helper utilities
└── viewmodel/             # View models
```

#### File Naming
```kotlin
// ✅ Correct
NFCEventEntity.kt           // For objects
NFCRepository.kt           // For repositories
MainViewModel.kt           // For view models
HomeScreen.kt              // For screens
NFCUtils.kt                // For utilities

// ❌ Wrong
Event.kt                   // Not descriptive
Repository.kt              // Too general
VM.kt                      // Too abbreviated
```

### 2. Resources

#### Resource Naming
```xml
<!-- ✅ Correct - Descriptive names -->
<string name="app_name">NFC Manager</string>
<string name="home_title">Home Screen</string>
<string name="settings_reminder_interval">Reminder Interval</string>
<string name="error_nfc_not_supported">NFC not supported on this device</string>

<!-- ❌ Wrong - Unclear names -->
<string name="title">Title</string>
<string name="msg">Message</string>
<string name="err1">Error</string>
```

---

## Documentation Standards

### 1. README Structure

```markdown
# Project Name

## Overview
Brief project description

## Features
- List of main features

## Installation
Installation and setup steps

## Usage
Usage examples

## Contributing
Contribution guidelines

## License
License information
```

### 2. Code Documentation

#### Class Documentation
```kotlin
/**
 * Main repository for managing NFC data and settings
 * 
 * Provides unified interface for accessing:
 * - Logged NFC events
 * - App settings
 * - Usage statistics
 * 
 * @property eventDao Data access for NFC events
 * @property settingsDao Data access for settings
 */
@Singleton
class NFCRepository @Inject constructor(
    private val eventDao: NFCEventDao,
    private val settingsDao: NFCSettingsDao
) {
    // implementation
}
```

---

## Error Handling

### 1. Exception Handling

```kotlin
// ✅ Correct - Comprehensive error handling
suspend fun logEvent(event: NFCEventEntity): Long {
    return try {
        eventDao.insertEvent(event)
    } catch (e: SQLiteException) {
        Log.e(TAG, "Database error while logging event", e)
        throw DatabaseException("Failed to log event: ${e.message}", e)
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error while logging event", e)
        throw e
    }
}

// ❌ Wrong - Ignoring errors
suspend fun logEvent(event: NFCEventEntity): Long {
    return eventDao.insertEvent(event) // May fail without handling
}
```

### 2. Logging Standards

```kotlin
// ✅ Correct - Organized logging
class MainViewModel @Inject constructor(
    private val repository: NFCRepository
) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "MainViewModel"
    }
    
    fun refreshNfcStatus() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting NFC status refresh")
                // implementation
                Log.d(TAG, "NFC status refresh completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh NFC status", e)
                updateError("Failed to update NFC status: ${e.message}")
            }
        }
    }
}
```

---

## Testing Standards

### 1. Test Naming

```kotlin
// ✅ Correct - Descriptive test names
class NFCRepositoryTest {
    
    @Test
    fun `logEvent should insert event and return valid id`() {
        // test implementation
    }
    
    @Test
    fun `getTodayEvents should return only today's events`() {
        // test implementation
    }
    
    @Test
    fun `updateSettings should throw exception when database fails`() {
        // test implementation
    }
}

// ❌ Wrong - Unclear names
class NFCRepositoryTest {
    
    @Test
    fun test1() { }
    
    @Test
    fun testEvent() { }
    
    @Test
    fun testError() { }
}
```

### 2. Test Structure

```kotlin
// ✅ Correct - AAA (Arrange, Act, Assert) structure
@Test
fun `updateReminderInterval should update setting and log event`() = runTest {
    // Arrange
    val newInterval = 30
    val initialSettings = NFCSettingsEntity(reminderInterval = 10)
    
    // Act
    repository.updateReminderInterval(newInterval)
    
    // Assert
    val updatedSettings = repository.getSettingsSync()
    assertEquals(newInterval, updatedSettings.reminderInterval)
    
    // Verify event was logged
    val events = repository.getTodayEvents().first()
    assertTrue(events.any { it.eventType == "SETTINGS" })
}
```

---

## Performance Guidelines

### 1. Database Optimization

```kotlin
// ✅ Correct - Optimized queries
@Query("""
    SELECT * FROM nfc_events 
    WHERE eventType = :type 
    AND timestamp > :startTime 
    ORDER BY timestamp DESC 
    LIMIT :limit
""")
fun getRecentEventsByType(
    type: String, 
    startTime: Long, 
    limit: Int = 50
): Flow<List<NFCEventEntity>>

// ❌ Wrong - Unoptimized query
@Query("SELECT * FROM nfc_events ORDER BY timestamp DESC")
fun getAllEventsUnoptimized(): Flow<List<NFCEventEntity>>
```

### 2. Memory Management

```kotlin
// ✅ Correct - Resource cleanup
class NfcMonitoringService : Service() {
    
    private var nfcAdapter: NfcAdapter? = null
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        nfcAdapter = null
        stopNfcMonitoring()
    }
}
```

---

## Security Best Practices

### 1. Data Protection

```kotlin
// ✅ Correct - Don't log sensitive data
fun logNfcTag(tagId: String, data: String) {
    if (settings.logSensitiveData) {
        Log.d(TAG, "NFC tag detected: $tagId")
    } else {
        Log.d(TAG, "NFC tag detected: [REDACTED]")
    }
}

// ❌ Wrong - Logging sensitive data
fun logNfcTag(tagId: String, data: String) {
    Log.d(TAG, "NFC tag: $tagId, Data: $data") // May contain sensitive data
}
```

---

This guide ensures high-quality and consistent code writing in the NFC Manager project, making maintenance and future development easier.