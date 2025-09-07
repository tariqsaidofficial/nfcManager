# Setup Guide - NFC Manager

## Basic Requirements

### Required Environment
- Android Studio Arctic Fox or newer
- Java 17 or newer
- Android SDK 30+ (Android 11+)
- Android device with NFC support

### Environment Setup

#### 1. Install Java 17
```bash
# macOS using Homebrew
brew install openjdk@17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

# Windows - Download from Oracle or OpenJDK
# Linux - Use package manager
```

#### 2. Setup Android Studio
1. Open Android Studio
2. File → Settings → Build Tools → Gradle
3. Select "Use Android Studio JDK"
4. Ensure Android SDK 30+ is installed

#### 3. Clone Project
```bash
git clone [repository-url]
cd nfcManager
```

#### 4. Build Project
```bash
cd android
./gradlew clean
./gradlew assembleDebug
```

## Running and Testing

### On Emulator
1. Create AVD with Android 11+
2. Enable NFC in emulator settings
3. Run the application

### On Physical Device
1. Enable Developer Mode
2. Enable USB Debugging
3. Ensure device has NFC support
4. Install the application

## Common Troubleshooting

### Java Version Error
```
Error: Android Gradle plugin requires Java 11 to run
```
**Solution:** Update Java to version 17

### NFC Not Supported Error
```
NFC not supported on this device
```
**Solution:** Use device with NFC support or updated emulator

## Next Steps
- Review [Troubleshooting Guide](troubleshooting.md)
- Check [Release Notes](release-notes.md)