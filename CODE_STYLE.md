# Code Style Guide

This document outlines the coding standards and style guidelines for the NFC Manager project. Following these guidelines ensures consistency, readability, and maintainability across the codebase.

## 🎯 Overview

NFC Manager follows modern Android development best practices with a focus on:
- **Kotlin-first**: 100% Kotlin codebase
- **Clean Architecture**: MVVM with Repository pattern
- **Jetpack Compose**: Modern declarative UI
- **Nothing OS Design**: Consistent visual language

## 📝 General Principles

### Code Quality
- **Readability**: Code should be self-documenting
- **Simplicity**: Prefer simple solutions over complex ones
- **Consistency**: Follow established patterns throughout the project
- **Maintainability**: Write code that's easy to modify and extend

### Performance
- **Efficiency**: Optimize for performance without sacrificing readability
- **Memory**: Be mindful of memory usage and leaks
- **Battery**: Consider battery impact of background operations
- **Responsiveness**: Keep UI responsive with proper threading

## 🏗️ Project Structure

### Package Organization

```
com.nothingos.nfcmanager/
├── data/                           # Data layer
│   ├── database/                   # Room database
│   │   ├── entities/               # Database entities
│   │   ├── dao/                    # Data access objects
│   │   └── AppDatabase.kt          # Database configuration
│   └── repository/                 # Repository pattern
├── services/                       # Background services
├── ui/                            # UI layer
│   ├── components/                # Reusable UI components
│   ├── screens/                   # Screen composables
│   └── theme/                     # Theming and design system
├── viewmodel/                     # ViewModels (MVVM)
├── utils/                         # Utility classes
├── di/                           # Dependency injection
└── MainActivity.kt               # Main activity
```

### File Naming

- **Classes**: PascalCase (e.g., `NFCRepository`, `MainViewModel`)
- **Files**: Match class name (e.g., `NFCRepository.kt`)
- **Resources**: snake_case (e.g., `activity_main.xml`, `ic_nfc_enabled.xml`)
- **Constants**: SCREAMING_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)

## 🔤 Kotlin Style Guidelines

### Basic Formatting

```kotlin
// ✅ Good: Proper spacing and indentation
class NFCRepository @Inject constructor(
    private val nfcEventDao: NFCEventDao,
    private val nfcSettingsDao: NFCSettingsDao
) {
    
    fun getAllEvents(): Flow<List<NFCEventEntity>> = 
        nfcEventDao.getAllEvents()
    
    suspend fun logEvent(
        eventType: String,
        message: String,
        icon: String
    ): Long {
        val event = NFCEventEntity(
            timestamp = Date(),
            eventType = eventType,
            message = message,
            icon = icon
        )
        return nfcEventDao.insertEvent(event)
    }
}

// ❌ Bad: Poor spacing and formatting
class NFCRepository @Inject constructor(private val nfcEventDao:NFCEventDao,private val nfcSettingsDao:NFCSettingsDao){
fun getAllEvents():Flow<List<NFCEventEntity>>=nfcEventDao.getAllEvents()
suspend fun logEvent(eventType:String,message:String,icon:String):Long{
val event=NFCEventEntity(timestamp=Date(),eventType=eventType,message=message,icon=icon)
return nfcEventDao.insertEvent(event)}}
```

### Indentation and Spacing

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: 120 characters maximum
- **Blank Lines**: Use to separate logical sections
- **Trailing Spaces**: Remove all trailing whitespace

```kotlin
// ✅ Good: Proper indentation and spacing
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: NFCRepository
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    fun refreshNfcStatus() {
        viewModelScope.launch {
            // Implementation here
        }
    }
}
```

### Naming Conventions

```kotlin
// ✅ Good: Clear, descriptive names
class NFCMonitoringService : Service() {
    
    companion object {
        private const val TAG = "NFCMonitoringService"
        const val ACTION_START_MONITORING = "ACTION_START_MONITORING"
    }
    
    private var isMonitoring = false
    private lateinit var nfcAdapter: NfcAdapter
    
    fun startNfcMonitoring() {
        // Implementation
    }
    
    private fun updateNotificationContent(message: String) {
        // Implementation
    }
}

// ❌ Bad: Unclear, abbreviated names
class NFCMonSvc : Service() {
    
    companion object {
        private const val T = "NFCMonSvc"
        const val ASM = "ASM"
    }
    
    private var isMon = false
    private lateinit var nfcAdp: NfcAdapter
    
    fun strtMon() {
        // Implementation
    }
    
    private fun updNotif(msg: String) {
        // Implementation
    }
}
```

### Function Style

```kotlin
// ✅ Good: Clear function structure
suspend fun logEvent(
    eventType: String,
    message: String,
    icon: String,
    tagId: String? = null,
    isImportant: Boolean = false
): Long {
    return try {
        val event = NFCEventEntity(
            timestamp = Date(),
            eventType = eventType,
            message = message,
            icon = icon,
            tagId = tagId,
            isImportant = isImportant
        )
        repository.insertEvent(event)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to log event", e)
        -1L
    }
}

// ✅ Good: Single expression function
fun formatTime(seconds: Long): String = when {
    seconds < 60 -> "${seconds}s"
    seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
    else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
}
```

### Class Structure Order

```kotlin
class ExampleClass {
    // 1. Companion object
    companion object {
        private const val TAG = "ExampleClass"
    }
    
    // 2. Properties (private first, then public)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()
    
    // 3. Init blocks
    init {
        // Initialization code
    }
    
    // 4. Public functions
    fun publicFunction() {
        // Implementation
    }
    
    // 5. Private functions
    private fun privateFunction() {
        // Implementation
    }
    
    // 6. Nested classes/interfaces
    data class State(
        val isLoading: Boolean = false
    )
}
```

## 🎨 Jetpack Compose Guidelines

### Composable Function Style

```kotlin
// ✅ Good: Well-structured composable
@Composable
fun NFCStatusCard(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEnabled) "ENABLED" else "DISABLED",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = "TOGGLE NFC")
            }
        }
    }
}

// ❌ Bad: Poor structure and styling
@Composable
fun NFCCard(enabled: Boolean, toggle: () -> Unit) {
    Card {
        Column {
            Text(if (enabled) "ON" else "OFF")
            Button(onClick = toggle) { Text("Toggle") }
        }
    }
}
```

### State Management

```kotlin
// ✅ Good: Proper state hoisting
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    SettingsContent(
        uiState = uiState,
        settings = settings,
        onToggleTheme = viewModel::setTheme,
        onUpdateInterval = viewModel::updateReminderInterval
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    settings: NFCSettingsEntity,
    onToggleTheme: (Boolean) -> Unit,
    onUpdateInterval: (Int) -> Unit
) {
    // UI implementation
}

// ❌ Bad: No state hoisting
@Composable
fun BadSettingsScreen() {
    var isDarkMode by remember { mutableStateOf(true) }
    var interval by remember { mutableStateOf(30) }
    
    // Direct UI manipulation without proper state management
}
```

### Modifier Usage

```kotlin
// ✅ Good: Proper modifier chaining
@Composable
fun ExampleComposable(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { /* action */ }
    ) {
        // Content
    }
}

// ❌ Bad: Hardcoded modifiers, no parameter
@Composable
fun BadComposable() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Content - not reusable
    }
}
```

## 🗄️ Database and Data Layer

### Room Entity Style

```kotlin
// ✅ Good: Well-documented entity
@Entity(tableName = "nfc_events")
data class NFCEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Date,
    
    @ColumnInfo(name = "event_type")
    val eventType: String,
    
    val message: String,
    val icon: String,
    
    @ColumnInfo(name = "tag_id")
    val tagId: String? = null,
    
    @ColumnInfo(name = "is_important")
    val isImportant: Boolean = false
) {
    companion object {
        // Event type constants
        const val TYPE_NFC_ENABLED = "NFC_ENABLED"
        const val TYPE_NFC_DISABLED = "NFC_DISABLED"
        const val TYPE_TAG_DETECTED = "TAG_DETECTED"
    }
}
```

### DAO Style

```kotlin
// ✅ Good: Comprehensive DAO with clear queries
@Dao
interface NFCEventDao {
    
    @Query("SELECT * FROM nfc_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<NFCEventEntity>>
    
    @Query("""
        SELECT * FROM nfc_events 
        WHERE DATE(timestamp) = DATE('now') 
        ORDER BY timestamp DESC 
        LIMIT 15
    """)
    fun getTodayEvents(): Flow<List<NFCEventEntity>>
    
    @Query("SELECT COUNT(*) FROM nfc_events WHERE DATE(timestamp) = DATE('now')")
    fun getTodayEventCount(): Flow<Int>
    
    @Insert
    suspend fun insertEvent(event: NFCEventEntity): Long
    
    @Delete
    suspend fun deleteEvent(event: NFCEventEntity)
    
    @Query("DELETE FROM nfc_events WHERE timestamp < :cutoffDate")
    suspend fun deleteOldEvents(cutoffDate: Date)
}
```

### Repository Pattern

```kotlin
// ✅ Good: Clean repository implementation
@Singleton
class NFCRepository @Inject constructor(
    private val nfcEventDao: NFCEventDao,
    private val nfcSettingsDao: NFCSettingsDao
) {
    
    // Expose data as Flow for reactive UI
    fun getAllEvents(): Flow<List<NFCEventEntity>> = 
        nfcEventDao.getAllEvents()
    
    fun getTodayEventCount(): Flow<Int> = 
        nfcEventDao.getTodayEventCount()
    
    // Suspend functions for one-shot operations
    suspend fun logEvent(
        eventType: String,
        message: String,
        icon: String,
        isImportant: Boolean = false
    ): Long {
        val event = NFCEventEntity(
            timestamp = Date(),
            eventType = eventType,
            message = message,
            icon = icon,
            isImportant = isImportant
        )
        return nfcEventDao.insertEvent(event)
    }
    
    // Helper functions with clear names
    suspend fun cleanupOldEvents(daysToKeep: Int = 30) {
        val cutoffDate = Date(
            System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        )
        nfcEventDao.deleteOldEvents(cutoffDate)
    }
}
```

## 🏛️ Architecture Guidelines

### MVVM Pattern

```kotlin
// ✅ Good: Proper ViewModel implementation
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: NFCRepository
) : AndroidViewModel(application) {
    
    // Private mutable state
    private val _uiState = MutableStateFlow(MainUiState())
    // Public read-only state
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // StateIn for data from repository
    val todayEvents = repository.getTodayEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Public functions for UI actions
    fun refreshNfcStatus() {
        viewModelScope.launch {
            try {
                val isEnabled = withContext(Dispatchers.IO) {
                    NFCUtils.isNfcEnabled(getApplication())
                }
                _uiState.value = _uiState.value.copy(isNFCEnabled = isEnabled)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to check NFC status"
                )
            }
        }
    }
    
    // Private helper functions
    private fun updateError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
}

// UI State data class
data class MainUiState(
    val isLoading: Boolean = false,
    val isNFCEnabled: Boolean = false,
    val errorMessage: String? = null,
    val lastActivity: Date = Date()
)
```

### Dependency Injection

```kotlin
// ✅ Good: Hilt module structure
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nfc_manager_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideNfcEventDao(database: AppDatabase): NFCEventDao {
        return database.nfcEventDao()
    }
    
    @Provides
    fun provideNfcSettingsDao(database: AppDatabase): NFCSettingsDao {
        return database.nfcSettingsDao()
    }
}

// ✅ Good: Hilt annotations usage
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var repository: NFCRepository
    
    private val mainViewModel: MainViewModel by viewModels()
    
    // Implementation
}
```

## 🎨 UI and Design Guidelines

### Nothing OS Design System

```kotlin
// ✅ Good: Following Nothing OS design principles
object NothingColors {
    val NothingRed = Color(0xFFEF4444)
    val PureBlack = Color(0xFF000000)
    val PureWhite = Color(0xFFFFFFFF)
    val DarkSurface = Color(0xFF1F2937)
    val DarkElevated = Color(0xFF1C1C1C)
}

object NothingTextStyles {
    val HeaderTitle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
    
    val NFCStatusLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
    )
    
    val SmallCaps = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp
    )
}

// ✅ Good: Consistent spacing
object NothingDimens {
    val SpacingXXS = 2.dp
    val SpacingXS = 4.dp
    val SpacingS = 8.dp
    val SpacingM = 16.dp
    val SpacingL = 24.dp
    val SpacingXL = 32.dp
    val SpacingXXL = 48.dp
}
```

### Accessibility

```kotlin
// ✅ Good: Accessibility support
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .semantics {
                contentDescription = text
                role = Role.Button
                if (!enabled) {
                    disabled()
                }
            },
        enabled = enabled
    ) {
        Text(text = text)
    }
}

// ✅ Good: Content descriptions for icons
Icon(
    imageVector = Icons.Default.Nfc,
    contentDescription = "NFC Status",
    modifier = Modifier.semantics {
        contentDescription = if (isEnabled) {
            "NFC is currently enabled"
        } else {
            "NFC is currently disabled"
        }
    }
)
```

## 🔧 Utility Classes

### Extension Functions

```kotlin
// ✅ Good: Useful extension functions
fun Context.isNfcSupported(): Boolean {
    return NfcAdapter.getDefaultAdapter(this) != null
}

fun Context.isNfcEnabled(): Boolean {
    val adapter = NfcAdapter.getDefaultAdapter(this)
    return adapter?.isEnabled == true
}

fun Date.formatRelativeTime(): String {
    val seconds = (System.currentTimeMillis() - time) / 1000
    return when {
        seconds < 60 -> "now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86400}d ago"
    }
}

// ✅ Good: Type-safe constants
object Constants {
    const val DEFAULT_REMINDER_INTERVAL = 10
    const val MAX_REMINDER_INTERVAL = 300
    const val MIN_REMINDER_INTERVAL = 5
    
    const val DATABASE_NAME = "nfc_manager_database"
    const val PREFERENCES_NAME = "nfc_manager_prefs"
    
    val SUPPORTED_INTERVALS = listOf(10, 30, 50)
}
```

### Error Handling

```kotlin
// ✅ Good: Comprehensive error handling
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// ✅ Good: Safe API calls
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    apiCall: suspend () -> T
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            Result.Error(throwable)
        }
    }
}

// Usage
suspend fun loadEvents(): Result<List<NFCEventEntity>> {
    return safeApiCall {
        repository.getAllEvents().first()
    }
}
```

## 📝 Documentation Standards

### KDoc Comments

```kotlin
/**
 * Repository for managing NFC events and settings.
 * 
 * This repository provides a clean API for accessing NFC-related data
 * and follows the repository pattern to abstract data sources.
 * 
 * @param nfcEventDao DAO for NFC events
 * @param nfcSettingsDao DAO for NFC settings
 */
@Singleton
class NFCRepository @Inject constructor(
    private val nfcEventDao: NFCEventDao,
    private val nfcSettingsDao: NFCSettingsDao
) {
    
    /**
     * Logs an NFC-related event to the database.
     * 
     * @param eventType Type of event (e.g., "NFC_ENABLED", "TAG_DETECTED")
     * @param message Human-readable message describing the event
     * @param icon Icon identifier for UI display
     * @param isImportant Whether this event should be highlighted
     * @return The ID of the inserted event, or -1 if insertion failed
     */
    suspend fun logEvent(
        eventType: String,
        message: String,
        icon: String,
        isImportant: Boolean = false
    ): Long {
        // Implementation
    }
}
```

### Inline Comments

```kotlin
// ✅ Good: Explain complex logic
fun calculatePrivacyRisk(nfcEnabledDuration: Long): PrivacyRisk {
    // NFC enabled for less than 30 seconds is considered low risk
    if (nfcEnabledDuration < 30_000) {
        return PrivacyRisk.LOW
    }
    
    // Between 30 seconds and 5 minutes is medium risk
    if (nfcEnabledDuration < 300_000) {
        return PrivacyRisk.MEDIUM
    }
    
    // More than 5 minutes is high risk due to potential unauthorized access
    return PrivacyRisk.HIGH
}

// ❌ Bad: Obvious comments
fun isNfcEnabled(): Boolean {
    // Check if NFC is enabled
    return nfcAdapter.isEnabled
}
```

## 🧪 Testing Guidelines

### Unit Tests

```kotlin
// ✅ Good: Comprehensive unit test
@Test
fun `logEvent should insert event and return valid ID`() = runTest {
    // Given
    val eventType = "TEST_EVENT"
    val message = "Test message"
    val icon = "TestIcon"
    val expectedId = 123L
    
    coEvery { nfcEventDao.insertEvent(any()) } returns expectedId
    
    // When
    val result = repository.logEvent(eventType, message, icon)
    
    // Then
    assertEquals(expectedId, result)
    coVerify {
        nfcEventDao.insertEvent(
            match { event ->
                event.eventType == eventType &&
                event.message == message &&
                event.icon == icon
            }
        )
    }
}

// ✅ Good: Test naming convention
@Test
fun `getTodayEventCount should return correct count`() = runTest {
    // Implementation
}

@Test
fun `cleanupOldEvents should delete events older than specified days`() = runTest {
    // Implementation
}
```

### Compose Tests

```kotlin
// ✅ Good: UI test with proper setup
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun `NFCStatusCard should display correct status text`() {
    // Given
    val isEnabled = true
    
    // When
    composeTestRule.setContent {
        NFCStatusCard(
            isEnabled = isEnabled,
            onToggle = { }
        )
    }
    
    // Then
    composeTestRule
        .onNodeWithText("ENABLED")
        .assertIsDisplayed()
    
    composeTestRule
        .onNodeWithContentDescription("NFC Status")
        .assertExists()
}
```

## 📦 Resource Guidelines

### String Resources

```xml
<!-- ✅ Good: Organized string resources -->
<resources>
    <!-- App Identity -->
    <string name="app_name">NFC Manager</string>
    
    <!-- Main Interface -->
    <string name="home_title">NFC Manager</string>
    <string name="nfc_monitoring">NFC Monitoring</string>
    <string name="privacy_protection">Privacy Protection</string>
    
    <!-- Settings -->
    <string name="settings_title">Settings</string>
    <string name="settings_theme_title">Theme</string>
    <string name="settings_notifications_title">Notifications</string>
    
    <!-- Status Messages -->
    <string name="nfc_enabled">Enabled</string>
    <string name="nfc_disabled">Disabled</string>
    <string name="nfc_not_supported">Not Supported</string>
    
    <!-- Error Messages -->
    <string name="error_nfc_unavailable">NFC is not available on this device</string>
    <string name="error_permission_denied">Permission denied</string>
</resources>
```

### Drawable Resources

```kotlin
// ✅ Good: Vector drawables for scalability
// res/drawable/ic_nfc_enabled.xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M20,2L4,2c-1.1,0 -2,0.9 -2,2v16c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2L22,4c0,-1.1 -0.9,-2 -2,-2z"/>
</vector>
```

## 🔒 Security Guidelines

### Data Protection

```kotlin
// ✅ Good: Secure data handling
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun storeSecureSetting(key: String, value: String) {
        encryptedPrefs.edit()
            .putString(key, value)
            .apply()
    }
}

// ✅ Good: Input validation
fun validateReminderInterval(interval: Int): Boolean {
    return interval in MIN_REMINDER_INTERVAL..MAX_REMINDER_INTERVAL
}

// ✅ Good: Safe string operations
fun sanitizeUserInput(input: String): String {
    return input.trim()
        .take(MAX_INPUT_LENGTH)
        .filter { it.isLetterOrDigit() || it.isWhitespace() }
}
```

## 📊 Performance Guidelines

### Memory Management

```kotlin
// ✅ Good: Proper lifecycle management
class NfcMonitoringService : Service() {
    
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel() // Clean up coroutines
        
        // Clean up resources
        nfcAdapter = null
    }
}

// ✅ Good: Efficient data loading
fun loadEventsWithPaging(): Flow<PagingData<NFCEventEntity>> {
    return Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { EventPagingSource(nfcEventDao) }
    ).flow
}
```

### Battery Optimization

```kotlin
// ✅ Good: Battery-conscious background work
class NfcMonitoringService : Service() {
    
    private fun startMonitoring() {
        // Use efficient monitoring intervals
        val monitoringInterval = settings.monitoringInterval.coerceAtLeast(2000L)
        
        serviceScope.launch {
            while (isActive && isMonitoring) {
                checkNfcStatus()
                delay(monitoringInterval)
            }
        }
    }
    
    private fun checkNfcStatus() {
        // Minimal work to check status
        val isEnabled = nfcAdapter?.isEnabled == true
        if (lastKnownStatus != isEnabled) {
            lastKnownStatus = isEnabled
            updateNotification()
        }
    }
}
```

## 📋 Code Review Checklist

### Before Submitting

- [ ] Code follows Kotlin conventions
- [ ] Functions are properly documented
- [ ] Error handling is implemented
- [ ] Tests are written and passing
- [ ] No hardcoded strings (use resources)
- [ ] Accessibility considerations addressed
- [ ] Performance impact considered
- [ ] Security implications reviewed

### Architecture Review

- [ ] Follows MVVM pattern correctly
- [ ] Proper separation of concerns
- [ ] Repository pattern implemented correctly
- [ ] Dependency injection used appropriately
- [ ] State management follows best practices

### UI Review

- [ ] Follows Nothing OS design system
- [ ] Responsive on different screen sizes
- [ ] Accessibility features implemented
- [ ] Proper state hoisting
- [ ] Performance optimizations applied

## 🛠️ Tools and Automation

### Code Formatting

```kotlin
// Use ktlint for automatic formatting
./gradlew ktlintFormat

// Check formatting
./gradlew ktlintCheck
```

### Static Analysis

```kotlin
// Use detekt for code analysis
./gradlew detekt

// Custom detekt rules for project
detekt {
    config = files("$projectDir/config/detekt.yml")
    buildUponDefaultConfig = true
}
```

### Git Hooks

```bash
#!/bin/sh
# pre-commit hook
./gradlew ktlintCheck
./gradlew detekt
./gradlew testDebugUnitTest
```

---

## 📞 Questions and Feedback

For questions about code style or to suggest improvements:

- **Email**: support@dxbmark.com
- **Subject**: "Code Style - NFC Manager"
- **GitHub Issues**: For style guide discussions

---

**Built with ❤️ by Tariq Said - Nothing OS Inspired Design**

*Technical Support & Contact: support@dxbmark.com*

---

*Licensed under the Apache License, Version 2.0*  
*Copyright 2025 Tariq Said. All rights reserved.*