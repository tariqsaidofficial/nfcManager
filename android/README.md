# NFC Manager

A smart NFC management application for privacy protection and security monitoring, inspired by Nothing OS design aesthetics, built with **official Android development tools** and modern architecture patterns.

## 🎨 Features

- ✅ **Privacy Protection**: Smart alerts when NFC stays enabled to prevent unauthorized access
- ✅ **Security Monitoring**: Real-time NFC status tracking with customizable intervals
- ✅ **Background NFC Monitoring**: Continuous NFC status tracking via a foreground service with notification, permission handling, and robust lifecycle management.
- ✅ **Nothing OS Design**: Authentic Nothing OS visual language with clean lines and thoughtful animations
- ✅ **Auto Reminders**: Privacy-focused notifications to protect against NFC vulnerabilities
- ✅ **Activity Logging**: Comprehensive security activity tracking with timestamps
- ✅ **Accessibility**: Full accessibility support with screen reader compatibility
- ✅ **Privacy Compliant**: Google Play compliant with transparent privacy practices and no data collection

## 🏗️ Architecture (Official Android Stack)

- ✅ **Android Studio**: Official IDE with full debugging and profiling tools
- ✅ **Kotlin**: Google's preferred language for Android development
- ✅ **Jetpack Compose**: Modern declarative UI toolkit
- ✅ **Room Database**: Official SQLite abstraction for local data storage
- ✅ **MVVM Architecture**: Industry-standard pattern with ViewModel and Repository
- ✅ **Navigation Component**: Official navigation framework
- ✅ **Material Design 3**: Latest design system with Nothing OS theming
- ✅ **Foreground Service**: For persistent background tasks like NFC monitoring.

## 🎯 Design System

### Colors
- **Primary**: `#ef4444` (Nothing Red)
- **Background**: `#000000` (Pure Black)
- **Surface**: `#1f2937` (Dark Gray)
- **Text**: `#ffffff` (White)

### Typography
- **Font Family**: System default with monospace fallbacks
- **Weights**: Regular (400), Medium (500), Bold (700)
- **Spacing**: Consistent 8px grid system

### Animations
- **Duration**: 300ms for interactions, 1500ms for complex animations
- **Easing**: Custom bezier curves for Nothing OS feel
- **Micro-interactions**: Haptic feedback and visual responses

## 📱 Screens

1. ✅ **NFC Manager** - Main interface with real-time NFC monitoring and privacy alerts
2. ✅ **Settings** - Security configuration, theme selection, and background service management
3. ✅ **Activity** - Detailed security activity log and privacy monitoring history

## 🔒 Privacy & Security

- ✅ **Local Storage Only**: No data transmission or cloud storage
- ✅ **No Analytics**: Zero tracking or user behavior monitoring
- ✅ **Minimal Permissions**: `NFC`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS` (Android 13+), and `VIBRATE` permissions.
- ✅ **Transparent**: Open source design with clear privacy policy

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
- **Package Name**: `com.nothingos.nfcmanager`
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
- **No Data Collection**: 100% local processing
- **Privacy First**: 10-second default alerts for maximum security
- **Nothing OS Design**: Authentic design language maintained

## 🎯 Google Play Compliance

- ✅ Privacy Policy included
- ✅ Minimal permission requests (`NFC`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS` for Android 13+, `VIBRATE`)
- ✅ No sensitive data collection
- ✅ Accessibility standards met
- ✅ Content rating appropriate
- ✅ Security best practices

## 🏗️ Project Structure (Updated)

```
nfcManager/
├── android/                                    # 🏗️ Official Android Project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── kotlin/com/nothingos/nfcmanager/
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
│   │   │   │   │   │   └── SettingsScreen.kt  # App Settings
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
- ✅ **Jetpack Compose BOM 2023.10.01**: Latest UI toolkit with Material 3
- ✅ **Room Database 2.6.0**: Official SQLite abstraction for local data
- ✅ **ViewModel & LiveData 2.7.0**: MVVM architecture components (ViewModel with StateFlow used)
- ✅ **Navigation Component 2.7.5**: Type-safe navigation between screens
- ✅ **WorkManager 2.8.1**: Background task management (Foreground Service used for NFC monitoring)
- ✅ **Kotlin Coroutines 1.7.3**: Asynchronous programming
- ✅ **Hilt**: Dependency Injection

### **Features Implemented**
- ✅ **3 Main Screens**: Home, Activity Log, Settings
- ✅ **Room Database**: Complete data persistence layer
- ✅ **MVVM Architecture**: ViewModels with StateFlow
- ✅ **Navigation Component**: Bottom navigation with type safety
- ✅ **Nothing OS Theme**: Complete theming system (Light/Dark modes, dynamic system UI)
- ✅ **Material Design 3**: Latest design components
- ✅ **Background NFC Monitoring & Service**: Continuous NFC status tracking via a foreground service with notification, permission handling, and robust lifecycle management.

### **Code Quality**
- ✅ **Kotlin**: 100% Kotlin codebase
- ✅ **Coroutines & Flow**: Asynchronous and reactive data streams
- ✅ **StateFlow**: UI state management
- ✅ **Hilt**: For dependency injection, improving testability and modularity.

## 🌍 Internationalization

- ✅ **RTL Support**: Full right-to-left language support
- ✅ **Localization**: Ready for multiple language support with Android string resources
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

### Phase 1: Foundation & Core Functionality (Completed) - September 2024
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
- 🟡 **Real NFC APIs & Tag Interaction**: Implement foreground NFC tag scanning, detailed parsing of tag data (NDEF messages, records), and logging of scanned tag events. This is the primary focus for the next development cycle.
- 🟡 **Activity Logging Enhancements**: Improve the activity log with more detailed event information, filtering capabilities (by event type, date), and potentially export options.
- 🔄 **Advanced Settings & Customization**: Introduce options for customizable notification sounds, more granular monitoring intervals, and other user preferences.
- 🔄 **Accessibility Review & Enhancements**: Conduct a full accessibility audit (TalkBack, switch access, font scaling) and implement necessary improvements.
- 🟡 **UI Testing**: Develop comprehensive UI tests for all screens and critical user flows using Jetpack Compose testing libraries.
- 🔄 **Localization**: Prepare and add support for additional languages (e.g., Arabic, Spanish).

### Phase 3: Performance, Release Preparation & Future Enhancements (Planned)
- 🔄 **Performance Optimization**: Profile the app for CPU, memory, and battery usage (especially the background service) and implement optimizations.
- 🔄 **Security Hardening**: Conduct a security review, check for vulnerabilities, and implement best practices for data protection.
- 🔄 **Google Play Store Listing**: Prepare all store listing assets (screenshots, feature graphic, promo video), write compelling descriptions, and finalize the privacy policy.
- 🔄 **Beta Testing Program**: Set up a beta testing program to gather feedback before full release.
- 🔄 **Future Feature: NFC Tag Writing**: Explore and implement functionality to write data to NFC tags.
- 🔄 **Future Feature: NFC Event Automation**: Allow users to define actions based on specific NFC events (e.g., toggle Wi-Fi, open an app).

---

Built with ❤️ using **Official Android Development Tools** and Nothing OS design principles
