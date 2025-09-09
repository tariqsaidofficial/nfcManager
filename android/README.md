ed security activity log with advanced filtering (by type & date) and CSV export capabilities. Tracks privacy monitoring history.

## 🔒 Privacy & Security

- ✅ **Local Storage Only**: No data transmission or cloud storage
- ✅ **No Analytics**: Zero tracking or user behavior monitoring
- ✅ **Minimal Permissions**: `NFC`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_AUDIO`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS` (Android 13+), and `VIBRATE` permissions.
- ✅ **Transparent**: Open source design with clear privacy policy
- ✅ **Privacy-First NFC Logging**: NFC tag scan events are logged without reading or storing sensitive tag data (like NDEF messages or payment card details).

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Latest version with Android SDK
- **Kotlin Support**: Included in Android Studio  
- **Java 11+**: **REQUIRED** for Android Gradle Plugin 7.2.2+
- **Android 11+**: Minimum API level 30 (Updated)

### ⚠️ Java 11+ Requirement
This project requires Java 11 or higher. Install it using:

```bash
# Option 1: Using Homebrew (macOS)
brew install openjdk@11
export JAVA_HOME=/opt/homebrew/opt/openjdk@11

# Option 2: Download from Oracle/OpenJDK
# https://adoptium.net/temurin/releases/

# Option 3: Use Android Studio's embedded JDK
# Android Studio → Preferences → Build Tools → Gradle
# Select "Use Gradle from: gradle-wrapper.properties file"
```

### Setup & Build
```bash
# Clone the repository
git clone [repository-url]
cd nfc-manager

# Navigate to Android project
cd android

# Verify Java version (must be 11+)
java -version

# Clean and build
./gradlew clean
./gradlew assembleDebug

# Or open in Android Studio
# File > Open > Select android/ folder
# Sync project with Gradle files
# Build > Make Project

# Run on device/emulator
# Run > Run 'app'
```

### Development Workflow
```bash
# For native Android development (Primary)
cd android
./gradlew clean assembleDebug

# Using Android Studio (Recommended)
# File > Open > android/ folder
# Build > Clean Project
# Build > Rebuild Project

# Testing
./gradlew test
./gradlew connectedAndroidTest
```

## ✅ Phase 1 Updates (Completed)

### ✅ **Android 11+ Support**
- **Minimum SDK**: Updated from API 21 to API 30 (Android 11+)
- **Target SDK**: API 34 (Android 14)
- **Enhanced Security**: Improved NFC privacy protection with modern Android APIs
- **Compatibility**: ~85% of active Android devices supported

### ✅ **App Identity & Branding**
- **App Name**: Changed from "NFC Glyph Manager" to "NFC Manager"
- **Package Name**: `com.dxbmark.nfcmanager`
- **Clean Structure**: Removed all React Native/Expo dependencies
- **Native Android**: 100% native Android with Kotlin & Jetpack Compose

### ✅ **Privacy-Focused Alert System**
- **Default Interval**: 10 seconds (enhanced privacy)
- **Available Options**: 10s, 30s, 50s (customizable in Settings)
- **Smart UI**: FilterChips for easy interval selection
- **Real-time Display**: Shows current interval in Settings

### ✅ **Build System Improvements**
- **Gradle Version**: 7.5.1 (stable)
- **Android Gradle Plugin**: 7.2.2 (Java 8+ compatible)
- **Dependencies**: Updated to latest stable versions
- **ProGuard**: Optimized rules for NFC Manager

### ✅ **UI Enhancements & Theming**
- **Theme Selection**: Implemented user-selectable Light and Dark themes with persistence.
- **Dynamic System UI**: Status bar and navigation bar icons adapt to the selected theme.
- **Visual Polish**: Improved text contrast for UI elements (e.g., badges) and resolved edge-to-edge display issues.
- **Nothing Font**: Integrated Nothing OS typography (NothingFont.ttf, nothing-font-5x7.otf).

### ✅ **Project Structure**
```
✅ strings.xml created with proper app name
✅ gradle.properties configured for optimal performance
✅ Gradle wrapper properly configured
✅ Repository management in settings.gradle
✅ Clean build.gradle without React Native dependencies
```

### ✅ **Background NFC Monitoring & Service Integration (Completed)**
- **Foreground Service**: Implemented `NfcMonitoringService` as a foreground service for continuous NFC status monitoring.
- **Service Lifecycle & Control**: Service managed via explicit Intent actions (`ACTION_START_MONITORING`, `ACTION_STOP_MONITORING`).
- **ViewModel Integration**: `SettingsViewModel` updated to use Intents for starting/stopping the service, ensuring separation of concerns.
- **Permission Handling**: Integrated NFC permission check in `SettingsViewModel` with a request flow to `SettingsScreen` (using `rememberLauncherForActivityResult`). `POST_NOTIFICATIONS` permission handled for Android 13+.
- **Application Startup Logic**: `NfcManagerApplication` now correctly starts the service on app launch if it was previously enabled in settings, using the new Intent actions.
- **Notification Enhancements**: 
    - Service notification now includes a `PendingIntent` to open `MainActivity`.
    - Notification content is dynamically updated to reflect the current monitoring status (e.g., "Initializing...", "NFC monitoring is active.", "NFC is disabled.").
- **Robust Error Handling**: Added `try-catch` blocks in the service, ViewModel, and Application class for more resilient operation.
- **Manifest Configuration**: Necessary permissions (`NFC`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS`) and service declaration added to `AndroidManifest.xml`.

### 🚨 **Important Notes**
- **Java 11+ Required**: For Android Gradle Plugin compatibility
- **No Data Collection**: 100% local processing, no sensitive tag data is read or stored.
- **Privacy First**: 10-second default alerts for maximum security.
- **Nothing OS Design**: Authentic design language maintained

## 🎯 Google Play Compliance

- ✅ Privacy Policy included
- ✅ Minimal permission requests (`NFC`, `READ_EXTERNAL_STORAGE`, `READ_MEDIA_AUDIO`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS` for Android 13+, `VIBRATE`)
- ✅ No sensitive data collection (NFC tag content is not read/stored)
- ✅ Accessibility standards met
- ✅ Content rating appropriate
- ✅ Security best practices

## 🏗️ Project Structure (Updated)

```
nfcManager/
├── android/                                    # 🏗️ Official Android Project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── kotlin/com/dxbmark/nfcmanager/
│   │   │   │   ├── data/                       # 📊 Data Layer
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── entities/           # Room Entities
│   │   │   │   │   │   │   ├── NFCEventEntity.kt
│   │   │   │   │   │   │   └── NFCSettingsEntity.kt
│   │   │   │   │   │   ├── dao/                # Data Access Objects
│   │   │   │   │   │   │   ├── NFCEventDao.kt
│   │   │   │   │   │   │   └── NFCSettingsDao.kt
│   │   │   │   │   │   └── AppDatabase.kt      # Room Database
│   │   │   │   │   └── repository/
│   │   │   │   │       └── NFCRepository.kt    # Repository Pattern
│   │   │   │   ├── services/                   # ⚙️ Background Services
│   │   │   │   │   └── NfcMonitoringService.kt # NFC Status Monitoring
│   │   │   │   ├── ui/                         # 🎨 UI Layer
│   │   │   │   │   ├── components/
│   │   │   │   │   │   └── NFCManagerNavigation.kt
│   │   │   │   │   ├── screens/               # Jetpack Compose Screens
│   │   │   │   │   │   ├── HomeScreen.kt      # Main NFC Management
│   │   │   │   │   │   ├── ActivityScreen.kt  # Event History
│   │   │   │   │   │   ├── SettingsScreen.kt  # App Settings
│   │   │   │   │   │   └── NotificationSoundSettingsScreen.kt # Notification Sound Customization
│   │   │   │   │   └── theme/                 # Nothing OS Theming
│   │   │   │   │       ├── Color.kt           # Color Palette
│   │   │   │   │       ├── Typography.kt      # Text Styles
│   │   │   │   │       └── Theme.kt           # Material3 Theme
│   │   │   │   ├── viewmodel/                 # 🧠 MVVM ViewModels
│   │   │   │   │   ├── MainViewModel.kt       # Home Screen Logic
│   │   │   │   │   ├── ActivityViewModel.kt   # Activity Screen Logic
│   │   │   │   │   └── SettingsViewModel.kt   # Settings Logic
│   │   │   │   ├── utils/                     # 🔧 Utilities
│   │   │   │   ├── NfcManagerApplication.kt   # 📱 Application Class
│   │   │   │   └── MainActivity.kt            # 📱 Entry Point
│   │   │   ├── assets/fonts/                  # 🔤 Custom Fonts
│   │   │   │   ├── NothingFont.ttf
│   │   │   │   └── nothing-font-5x7.otf
│   │   │   └── res/
│   │   │       ├── drawable/                  # 🖼️ Images & Icons
│   │   │       │   ├── icon.png
│   │   │       │   ├── adaptive-icon.png
│   │   │       │   └── nfc-svgrepo-com.svg
│   │   │       └── xml/
│   │   │           └── network_security_config.xml
│   │   ├── build.gradle                       # ⚙️ App Dependencies
│   │   └── proguard-rules.pro
│   ├── build.gradle                           # 🔧 Project Configuration
│   └── settings.gradle                        # 📋 Module Settings
├── README.md                                  # 📖 Documentation
├── TODO.md                                    # ✅ Task Management
└── .gitignore                                 # 🚫 Git Exclusions
```

## 🛠️ Official Android Tools Used

### **Development Environment**
- ✅ **Android Studio**: Primary IDE with Layout Inspector, Database Inspector
- ✅ **Gradle Build System**: Official build automation
- ✅ **Android SDK Tools**: ADB, device management

### **Core Libraries (Official)**
- ✅ **Jetpack Compose BOM 2024.02.02**: Latest UI toolkit with Material 3
- ✅ **Room Database 2.6.0**: Official SQLite abstraction for local data
- ✅ **ViewModel & LiveData 2.7.0**: MVVM architecture components (ViewModel with StateFlow used)
- ✅ **Navigation Component 2.7.5**: Type-safe navigation between screens
- ✅ **WorkManager 2.8.1**: Background task management (Foreground Service used for NFC monitoring)
- ✅ **Kotlin Coroutines 1.7.3**: Asynchronous programming
- ✅ **Hilt**: Dependency Injection

### **Features Implemented**
- ✅ **3 Main Screens**: Home, Activity Log, Settings (including a sub-screen for Notification Sound customization)
- ✅ **Room Database**: Complete data persistence layer
- ✅ **MVVM Architecture**: ViewModels with StateFlow
- ✅ **Navigation Component**: Bottom navigation with type safety
- ✅ **Nothing OS Theme**: Complete theming system (Light/Dark modes, dynamic system UI)
- ✅ **Material Design 3**: Latest design components
- ✅ **Background NFC Monitoring & Service**: Continuous NFC status tracking via a foreground service with notification, permission handling, and robust lifecycle management.
- ✅ **Privacy-Focused NFC Tag Detection**: Logs NFC tag scan events without reading or storing sensitive tag data.
- ✅ **Advanced Notification Sound Customization**: Users can pick custom sounds, preview them (play/stop), and reset to system default with enhanced UI feedback and error handling.

### **Code Quality**
- ✅ **Kotlin**: 100% Kotlin codebase
- ✅ **Coroutines & Flow**: Asynchronous and reactive data streams
- ✅ **StateFlow**: UI state management
- ✅ **Hilt**: For dependency injection, improving testability and modularity.

## 🌍 Internationalization

- ✅ **RTL Support**: Full right-to-left language support
- ✅ **Localization**: Implemented support for English, Arabic, Spanish, French, and German using Android string resources.
- ✅ **Accessibility**: Screen reader compatible with TalkBack integration

## 📱 Deployment

### Android Studio Deployment
1. **Build APK**: Build > Build Bundle(s) / APK(s) > Build APK(s)
2. **Generate AAB**: Build > Generate Signed Bundle / APK
3. **Google Play Console**: Upload AAB for distribution

### React Native Parallel Development
```bash
# Continue React Native development (If applicable)
# npm run android
# npx expo prebuild --platform android --clean
```

## 🔧 Troubleshooting

### Common Build Issues

#### **1. Java Version Error**
```
Error: Android Gradle plugin requires Java 11 to run
```
**Solution:**
```bash
# Check current Java version
java -version

# Install Java 11+ (macOS)
brew install openjdk@11
export JAVA_HOME=/opt/homebrew/opt/openjdk@11

# Or use Android Studio's JDK
# File → Settings → Build Tools → Gradle → Use Android Studio JDK
```

#### **2. React Native Dependencies Error**
```
Error: Could not find com.facebook.react:react-native-gradle-plugin
```
**Solution:** This has been fixed in Phase 1. Ensure you're using the updated `build.gradle` files.

#### **3. Repository Configuration Error**
```
Error: Build was configured to prefer settings repositories
```
**Solution:** Use the updated `settings.gradle` with `PREFER_SETTINGS` mode.

#### **4. Gradle Sync Issues**
```bash
# Clean and retry
cd android
./gradlew clean
./gradlew --refresh-dependencies

# Or in Android Studio
# File → Invalidate Caches and Restart
```

### Build Verification Commands
```bash
# Verify project structure
cd android
./gradlew tasks

# Test debug build
./gradlew assembleDebug

# Run tests
./gradlew test

# Check for lint issues
./gradlew lint
```

### Performance Tips
- **Use Gradle Daemon**: Enabled by default in `gradle.properties`
- **Parallel Builds**: Configured for faster compilation
- **Build Cache**: Enabled for incremental builds
- **R8 Full Mode**: Enabled for optimal APK size

## 📄 License

MIT License - See LICENSE file for details

## 🤝 Contributing

Contributions welcome! Please read our contributing guidelines and code of conduct.

### Development Guidelines
- Follow official Android development patterns
- Use Jetpack Compose for new UI components
- Implement MVVM architecture
- Write comprehensive unit tests
- Follow Nothing OS design principles

---

## 📝 Changelog

### Phase 1: Foundation & Core Functionality (Completed) - September 2025
- ✅ **Android 11+ Support**: Updated minimum SDK from API 21 to API 30 (Android 11+), Target SDK API 34.
- ✅ **App Identity & Branding**: Renamed to "NFC Manager", updated package name, 100% native Android with Kotlin & Jetpack Compose.
- ✅ **Privacy-Focused Alert System**: Default 10s interval, customizable options, FilterChips UI.
- ✅ **Build System Improvements**: Stable Gradle versions, optimized ProGuard rules.
- ✅ **UI Enhancements & Theming**: Light/Dark themes, dynamic system UI, visual polish, Nothing Font integration.
- ✅ **Project Structure**: Cleaned and organized structure, proper Gradle configurations.
- ✅ **Background NFC Monitoring & Service**: Integrated a foreground service for continuous NFC status monitoring with robust controls, comprehensive permission handling (NFC, Foreground Service, Post Notifications), dynamic notifications with PendingIntent, and resilient error handling.
- ✅ **NFC Status Handling & User Guidance**: Implemented checks for NFC adapter availability and enabled state, guiding users to system settings, and displaying status on the Home screen.
- ✅ **Initial Permission Model**: Comprehensive runtime permission requests and Manifest declarations for all core features and the background service.

### Phase 2: Advanced NFC Features & Polish (Current Focus)
- ✅ **Privacy-Focused NFC Tag Event Logging**: Implemented foreground NFC tag detection. The system logs the event of a tag scan (including timestamp, a generic tag identifier, and the type of NFC action detected, e.g., 'TAG_DISCOVERED') without attempting to parse or store detailed tag content (like NDEF messages or specific payment card data). This approach prioritizes user privacy by only recording that an NFC interaction occurred, not the sensitive details of the interaction, which is especially important for payment cards and other sensitive contactless uses.
- ✅ **Activity Logging Enhancements**: Significantly improved the activity log. Implemented robust filtering capabilities, allowing users to filter events by event type and predefined date ranges (Today, Last 7 Days, Last 30 Days, All Time). Added functionality to export the currently filtered activity log to a CSV file, providing users with a way to save and analyze their data externally.
- ✅ **Advanced Settings & Customization**:
    - Implemented options for more granular monitoring intervals.
    - **Enhanced Notification Sound Customization**: Users can now pick custom notification sounds from their device storage. The settings screen for notification sounds allows for playing/stopping the currently selected sound for preview. The "Use System Default Sound" button dynamically changes its appearance based on whether a custom sound is active. Sound playback automatically stops when navigating away from the screen, and error handling for sound playback has been improved.
- 🚧 **UI Testing**: Manual UI testing in progress by the developer. Automated Jetpack Compose tests to be developed later.
- ✅ **Localization**: Added support for German. App now supports English, Arabic, Spanish, French, and German. Chinese, Russian, Hindi and Filipino are planned for community contributions.

### Phase 3: Performance, Release Preparation & Future Enhancements (Planned)
- 🟡 **Performance Optimization**: Initial review and planning for profiling CPU, memory, and battery usage (especially the background service) and implementing optimizations.
- 🔄 **Security Hardening**: Conduct a security review, check for vulnerabilities, and implement best practices for data protection.
- 🔄 **Google Play Store Listing**: Prepare all store listing assets (screenshots, feature graphic, promo video), write compelling descriptions, and finalize the privacy policy.
- 🔄 **Beta Testing Program**: Set up a beta testing program to gather feedback before full release.
- 🔄 **Future Feature: NFC Tag Writing**: Explore and implement functionality to write data to NFC tags.
- 🔄 **Future Feature: NFC Event Automation**: Allow users to define actions based on specific NFC events (e.g., toggle Wi-Fi, open an app).

---

Built with ❤️ using **Official Android Development Tools** and Nothing OS design principles
