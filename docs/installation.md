# Installation Guide - NFC Manager

## System Requirements

### Minimum Requirements
- **Android Version**: Android 11 (API 30) or higher
- **RAM**: 2GB minimum
- **Storage**: 50MB free space
- **NFC Hardware**: Required for app functionality
- **Architecture**: ARM64, ARM32, x86_64, x86

### Recommended Requirements
- **Android Version**: Android 12+ for optimal experience
- **RAM**: 4GB or more
- **Storage**: 100MB free space for updates and logs
- **Device**: Nothing Phone, Samsung Galaxy, Google Pixel, OnePlus

## Installation Methods

### Method 1: Google Play Store (Recommended)

1. **Open Google Play Store** on your Android device
2. **Search** for "NFC Manager" by [Developer Name]
3. **Verify** the developer and package name: `com.nothingos.nfcmanager`
4. **Tap Install** and wait for download to complete
5. **Open** the app and follow setup instructions

### Method 2: APK Sideloading (Advanced Users)

⚠️ **Warning**: Only install APKs from trusted sources

1. **Enable Unknown Sources**:
   - Go to Settings > Security > Install unknown apps
   - Enable for your browser or file manager

2. **Download APK**:
   - Visit the official GitHub releases page
   - Download the latest stable APK
   - Verify checksum if provided

3. **Install APK**:
   - Open the downloaded APK file
   - Tap "Install" when prompted
   - Wait for installation to complete

### Method 3: Development Build (Developers)

Prerequisites:
- Android Studio Arctic Fox or newer
- Java 17 or higher
- Android SDK with API 30+

Steps:
```bash
# Clone repository
git clone https://github.com/[USERNAME]/nfc-manager.git
cd nfc-manager/android

# Build debug APK
./gradlew assembleDebug

# Install to connected device
./gradlew installDebug
```

## Post-Installation Setup

### 1. Initial Configuration

**First Launch**:
1. **Grant Permissions**: Allow NFC and notification permissions when prompted
2. **Enable NFC**: If disabled, the app will guide you to system settings
3. **Choose Theme**: Select light or dark theme preference
4. **Set Alert Intervals**: Configure privacy alert timing (10/30/50 seconds)

### 2. Permission Setup

**Required Permissions**:
- `NFC` - To access NFC hardware
- `FOREGROUND_SERVICE` - For background monitoring
- `POST_NOTIFICATIONS` - For privacy alerts
- `VIBRATE` - For haptic feedback

**Optional Permissions**:
- `WAKE_LOCK` - To keep monitoring active
- `FOREGROUND_SERVICE_DATA_SYNC` - For service optimization

### 3. System Settings

**NFC Settings**:
1. Go to Android Settings > Connected devices > NFC
2. Enable "NFC" if not already enabled
3. Configure "Tap to pay" if desired
4. Return to NFC Manager

**Battery Optimization**:
1. Go to Settings > Apps > NFC Manager
2. Select "Battery" > "Battery optimization"
3. Choose "Don't optimize" for reliable background monitoring

**Notification Settings**:
1. Go to Settings > Apps > NFC Manager > Notifications
2. Enable all notification categories
3. Set importance to "High" for privacy alerts

## Verification

### Verify Installation
1. **Check App Info**:
   - Settings > Apps > NFC Manager
   - Verify version and permissions

2. **Test NFC Detection**:
   - Open NFC Manager
   - Check if NFC status shows correctly
   - Test with an NFC tag if available

3. **Test Notifications**:
   - Enable privacy alerts in settings
   - Verify notifications appear when NFC is enabled

## Troubleshooting Installation

### Common Issues

**Issue**: "App not installed" error
**Solution**: 
- Ensure sufficient storage space
- Clear Google Play Store cache
- Restart device and try again

**Issue**: "Package conflicts" error
**Solution**:
- Uninstall any previous versions
- Clear app data if upgrading
- Use fresh installation

**Issue**: NFC not detected
**Solution**:
- Verify device has NFC hardware
- Check if NFC is enabled in system settings
- Restart device after enabling NFC

**Issue**: Permissions not granted
**Solution**:
- Go to Settings > Apps > NFC Manager > Permissions
- Manually enable all required permissions
- Restart the app

### Device-Specific Issues

**Samsung Devices**:
- May require disabling "Adaptive battery"
- Check Samsung Pay NFC conflicts
- Enable "Allow background activity"

**Xiaomi/MIUI Devices**:
- Disable MIUI optimizations for the app
- Add to "Protected apps" list
- Enable "Autostart" permission

**OnePlus Devices**:
- Disable "Battery optimization"
- Enable "Allow background app refresh"
- Check OxygenOS privacy settings

**Nothing Phones**:
- Should work optimally with Nothing OS
- Check Glyph interface integration
- Verify Nothing-specific settings

## Uninstallation

### Clean Uninstallation
1. **Backup Settings** (optional):
   - Export settings from app menu
   - Save to device storage

2. **Stop Background Services**:
   - Open NFC Manager
   - Go to Settings
   - Disable background monitoring

3. **Uninstall App**:
   - Settings > Apps > NFC Manager > Uninstall
   - Or long-press app icon > Uninstall

4. **Clean Residual Data** (if needed):
   - Use file manager to check `/Android/data/com.nothingos.nfcmanager/`
   - Delete folder if it exists

## Updates

### Automatic Updates (Google Play)
- Updates download automatically
- User notified when ready to install
- Settings and data preserved

### Manual Updates
1. Check for updates in Google Play Store
2. Download and install when available
3. Review changelog for new features
4. Reconfigure settings if needed

### Beta Updates
- Join beta program in Google Play Store
- Receive early access to new features
- Report bugs through feedback channels
- Switch back to stable when needed

## Supported Devices

### Fully Tested
- Nothing Phone (1) - ✅ Optimal
- Samsung Galaxy S21+ - ✅ Fully Compatible
- Google Pixel 6 - ✅ Fully Compatible
- OnePlus 9 Pro - ✅ Compatible

### Community Tested
- Xiaomi Mi 11 - ✅ Compatible
- Samsung Galaxy Note 20 - ✅ Compatible
- Google Pixel 5 - ✅ Compatible
- OnePlus 8T - ✅ Compatible

### Known Issues
- Some budget devices may have limited NFC functionality
- Older Android versions (< 11) not supported
- Devices without NFC hardware cannot use the app

## Getting Help

### Support Channels
- **GitHub Issues**: Bug reports and feature requests
- **Email Support**: [SUPPORT_EMAIL]
- **Community Forum**: [FORUM_LINK]
- **Documentation**: Check troubleshooting guide

### Before Contacting Support
1. Check troubleshooting guide
2. Verify device compatibility
3. Try reinstalling the app
4. Gather device and app version info

---

*For technical support, please include your device model, Android version, and app version in your inquiry.*
