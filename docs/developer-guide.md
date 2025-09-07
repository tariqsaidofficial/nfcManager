# Developer Guide - NFC Manager

## Overview

This guide provides comprehensive information for developers working on or contributing to NFC Manager. It covers architecture, development setup, coding standards, and best practices.

## Table of Contents

1. [Project Architecture](#project-architecture)
2. [Development Environment](#development-environment)
3. [Project Structure](#project-structure)
4. [Core Components](#core-components)
5. [Development Workflow](#development-workflow)
6. [Testing Strategy](#testing-strategy)
7. [Deployment](#deployment)
8. [Contributing Guidelines](#contributing-guidelines)

## Project Architecture

### Architecture Pattern

NFC Manager follows **Clean Architecture** principles with **MVVM** pattern:

```
┌─────────────────────────────────────────┐
│             Presentation Layer          │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ UI (Compose)│  │   ViewModels    │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│              Domain Layer               │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Use Cases   │  │   Repository    │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│               Data Layer                │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │ Room DB     │  │    Services     │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
```

### Technology Stack

**Core Technologies:**
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt/Dagger
- **Async Programming**: Coroutines + Flow

**Android Libraries:**
- **Jetpack Compose BOM**: 2024.02.02
- **Room**: 2.6.0
- **Hilt**: 2.50
- **Navigation Compose**: 2.7.5
- **Lifecycle**: 2.7.0
- **Work Manager**: 2.8.1

## Development Environment

### Prerequisites

**Required Software:**
- **Android Studio**: Flamingo or newer
- **Java**: JDK 17 or higher
- **Android SDK**: API 30+ (Android 11+)
- **Git**: For version control

**Hardware Requirements:**
- **RAM**: 8GB minimum, 16GB recommended
- **Storage**: 10GB free space
- **CPU**: Multi-core processor recommended

### Setup Instructions

1. **Clone Repository**:
   ```bash
   git clone https://github.com/[USERNAME]/nfc-manager.git
   cd nfc-manager
   ```

2. **Configure Environment**:
   ```bash
   # Set Java 17
   export JAVA_HOME=/path/to/java17
   
   # Verify setup
   java -version
   ./android/gradlew --version
   ```

3. **Open in Android Studio**:
   - File → Open → Select `android/` folder
   - Wait for Gradle sync
   - Configure SDK if prompted

4. **Build Project**:
   ```bash
   cd android
   ./gradlew clean
   ./gradlew assembleDebug
   ```

### IDE Configuration

**Android Studio Settings:**
- **Code Style**: Import project code style
- **Inspections**: Enable Kotlin and Android inspections
- **Live Templates**: Import custom templates
- **Plugins**: Install recommended plugins

**Recommended Plugins:**
- Kotlin Multiplatform Mobile
- Database Navigator
- GitToolBox
- Rainbow Brackets
- Material Theme UI

## Project Structure

### Directory Layout

```
nfcManager/
├── android/                     # Android project root
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── kotlin/com/nothingos/nfcmanager/
│   │   │   │   ├── data/        # Data layer
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── entities/
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   └── AppDatabase.kt
│   │   │   │   │   └── repository/
│   │   │   │   ├── di/          # Dependency injection
│   │   │   │   ├── services/    # Background services
│   │   │   │   ├── ui/          # Presentation layer
│   │   │   │   │   ├── components/
│   │   │   │   │   ├── screens/
│   │   │   │   │   └── theme/
│   │   │   │   ├── utils/       # Utilities
│   │   │   │   ├── viewmodel/   # ViewModels
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/             # Resources
│   │   │   └── AndroidManifest.xml
│   │   ├── build.gradle         # App-level build config
│   │   └── proguard-rules.pro
│   ├── build.gradle             # Project-level build config
│   └── settings.gradle          # Project settings
├── docs/                        # Documentation
├── .github/                     # GitHub templates
└── README.md
```

### Package Structure

```kotlin
com.nothingos.nfcmanager/
├── data/
│   ├── database/
│   │   ├── entities/           # Room entities
│   │   ├── dao/               # Data access objects
│   │   └── AppDatabase.kt     # Database configuration
│   └── repository/            # Repository implementations
├── di/                        # Hilt modules
├── services/                  # Background services
├── ui/
│   ├── components/           # Reusable UI components
│   ├── screens/             # Screen composables
│   └── theme/               # Theme and styling
├── utils/                    # Utility classes
├── viewmodel/               # MVVM ViewModels
└── MainActivity.kt          # Main activity
```

## Core Components

### Data Layer

**Room Database:**
```kotlin
@Database(
    entities = [NFCEventEntity::class, NFCSettingsEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nfcEventDao(): NFCEventDao
    abstract fun nfcSettingsDao(): NFCSettingsDao
}
```

**Repository Pattern:**
```kotlin
@Singleton
class NFCRepository @Inject constructor(
    private val eventDao: NFCEventDao,
    private val settingsDao: NFCSettingsDao
) {
    fun getAllEvents(): Flow<List<NFCEventEntity>> = eventDao.getAllEvents()
    
    suspend fun logEvent(event: NFCEventEntity): Long {
        return eventDao.insertEvent(event)
    }
}
```

### Presentation Layer

**ViewModel Implementation:**
```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: NFCRepository
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    val settings = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NFCSettingsEntity()
        )
}
```

**Compose UI:**
```kotlin
@Composable
fun HomeScreen(viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    NothingOSTheme(darkTheme = settings.isDarkMode) {
        // UI implementation
    }
}
```

### Service Layer

**Background Service:**
```kotlin
class NfcMonitoringService : Service() {
    companion object {
        const val ACTION_START_MONITORING = "START_MONITORING"
        const val ACTION_STOP_MONITORING = "STOP_MONITORING"
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startNfcMonitoring()
            ACTION_STOP_MONITORING -> stopNfcMonitoring()
        }
        return START_STICKY
    }
}
```

### Dependency Injection

**Hilt Configuration:**
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
}
```

## Development Workflow

### Git Workflow

**Branch Strategy:**
- `main`: Production-ready code
- `develop`: Integration branch
- `feature/*`: Feature development
- `bugfix/*`: Bug fixes
- `hotfix/*`: Emergency fixes

**Commit Convention:**
```
type(scope): description

feat(nfc): add background monitoring service
fix(ui): resolve theme switching crash
docs(readme): update installation instructions
```

### Code Review Process

1. **Create Feature Branch**: From `develop`
2. **Implement Changes**: Follow coding standards
3. **Write Tests**: Unit and integration tests
4. **Run Checks**: Lint, tests, and build
5. **Create PR**: With detailed description
6. **Code Review**: By team members
7. **Address Feedback**: Make necessary changes
8. **Merge**: After approval

### Quality Gates

**Pre-commit Checks:**
- Kotlin lint (ktlint)
- Unit tests pass
- Code coverage > 80%
- No security vulnerabilities

**Pre-merge Checks:**
- All tests pass
- Integration tests pass
- Build successful
- Code review approved

## Testing Strategy

### Test Pyramid

```
        ┌─────────────┐
        │   UI Tests  │ (Few)
        └─────────────┘
      ┌─────────────────┐
      │Integration Tests│ (Some)  
      └─────────────────┘
    ┌───────────────────────┐
    │     Unit Tests        │ (Many)
    └───────────────────────┘
```

### Unit Testing

**ViewModel Tests:**
```kotlin
@Test
fun `updateReminderInterval should update setting and log event`() = runTest {
    // Arrange
    val newInterval = 30
    
    // Act
    viewModel.updateReminderInterval(newInterval)
    
    // Assert
    assertEquals(newInterval, viewModel.reminderInterval.value)
    verify(repository).logEvent(any())
}
```

**Repository Tests:**
```kotlin
@Test
fun `logEvent should insert event and return valid id`() = runTest {
    // Arrange
    val event = NFCEventEntity(
        timestamp = Date(),
        eventType = "TEST",
        message = "Test event",
        icon = "Test"
    )
    
    // Act
    val result = repository.logEvent(event)
    
    // Assert
    assertTrue(result > 0)
}
```

### Integration Testing

**Database Tests:**
```kotlin
@Test
fun `database should store and retrieve events correctly`() = runTest {
    // Test database operations
    val event = createTestEvent()
    val id = dao.insertEvent(event)
    
    val retrieved = dao.getEventById(id)
    assertEquals(event.message, retrieved.message)
}
```

### UI Testing

**Compose Tests:**
```kotlin
@Test
fun `home screen should display NFC status correctly`() {
    composeTestRule.setContent {
        HomeScreen(viewModel = mockViewModel)
    }
    
    composeTestRule
        .onNodeWithText("NFC STATUS")
        .assertIsDisplayed()
}
```

### Test Configuration

**Test Dependencies:**
```gradle
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito.kotlin:mockito-kotlin:4.1.0'
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'
testImplementation 'androidx.room:room-testing:2.6.0'

androidTestImplementation 'androidx.test.ext:junit:1.1.5'
androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
```

## Deployment

### Build Configuration

**Debug Build:**
```gradle
buildTypes {
    debug {
        applicationIdSuffix ".debug"
        debuggable true
        minifyEnabled false
        testCoverageEnabled true
    }
}
```

**Release Build:**
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release
    }
}
```

### Signing Configuration

**Keystore Setup:**
```gradle
signingConfigs {
    release {
        storeFile file(RELEASE_STORE_FILE)
        storePassword RELEASE_STORE_PASSWORD
        keyAlias RELEASE_KEY_ALIAS
        keyPassword RELEASE_KEY_PASSWORD
    }
}
```

### CI/CD Pipeline

**GitHub Actions:**
```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
    - name: Run tests
      run: ./gradlew test
```

## Contributing Guidelines

### Code Standards

**Kotlin Style:**
- Follow official Kotlin coding conventions
- Use meaningful variable and function names
- Keep functions small and focused
- Document public APIs with KDoc

**Android Patterns:**
- Follow Android Architecture Components guidelines
- Use lifecycle-aware components
- Implement proper error handling
- Follow Material Design principles

### Documentation Requirements

**Code Documentation:**
- KDoc for all public APIs
- Inline comments for complex logic
- README updates for new features
- Architecture decision records (ADRs)

**PR Requirements:**
- Clear description of changes
- Screenshots for UI changes
- Test coverage for new code
- Documentation updates

### Review Checklist

**Code Quality:**
- [ ] Follows coding standards
- [ ] Has appropriate tests
- [ ] Handles errors properly
- [ ] Follows architecture patterns

**Functionality:**
- [ ] Works as expected
- [ ] Handles edge cases
- [ ] Maintains backward compatibility
- [ ] Follows security best practices

**Documentation:**
- [ ] Code is self-documenting
- [ ] Public APIs documented
- [ ] README updated if needed
- [ ] Breaking changes noted

---

*This guide is continuously updated as the project evolves. Please refer to the latest version for current best practices.*
