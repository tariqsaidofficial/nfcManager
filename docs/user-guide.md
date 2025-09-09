# User Guide - NFC Manager

## Welcome to NFC Manager

NFC Manager is a privacy-focused Android application that helps you monitor NFC activity on your device and protects you from unauthorized access and unwanted transactions.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Main Features](#main-features)
3. [Screen Guide](#screen-guide)
4. [Settings Configuration](#settings-configuration)
5. [Privacy & Security](#privacy--security)
6. [Troubleshooting](#troubleshooting)
7. [Tips & Best Practices](#tips--best-practices)

## Getting Started

### First Launch

When you open NFC Manager for the first time:

1. **Welcome Screen**: Brief introduction to the app's purpose
2. **Permission Requests**: Grant NFC and notification permissions
3. **NFC Check**: The app will verify if your device supports NFC
4. **Initial Setup**: Choose your preferred theme and alert settings

### Understanding NFC

**What is NFC?**
Near Field Communication (NFC) is a short-range wireless technology that enables communication between devices when they're close together (usually within 4cm).

**Common NFC Uses:**
- Contactless payments (Google Pay, Samsung Pay)
- Public transport cards
- Access cards for buildings
- Smart home device control
- File sharing between devices

**Privacy Concerns:**
- Unauthorized payment attempts
- Data skimming from cards
- Location tracking
- Unwanted tag interactions

## Main Features

### 🛡️ Privacy Protection
- **Smart Alerts**: Customizable warnings when NFC stays enabled
- **Real-time Monitoring**: Continuous NFC status tracking
- **Background Service**: Optional background monitoring
- **Privacy Mode**: Enhanced protection settings

### 📊 Activity Logging
- **Event History**: Complete log of NFC activities
- **Search & Filter**: Find specific events quickly
- **Export Data**: Save logs for analysis
- **Statistics**: Usage patterns and insights

### ⚙️ Customization
- **Alert Intervals**: 10, 30, or 50-second warnings
- **Theme Options**: Light, dark, and system themes
- **Notification Settings**: Customize alerts and sounds
- **Nothing OS Design**: Authentic Nothing phone experience

## Screen Guide

### 🏠 Home Screen

The main dashboard showing:

**NFC Status Card**
- **ENABLED**: NFC is active (green indicator)
- **DISABLED**: NFC is inactive (gray indicator)  
- **NOT SUPPORTED**: Device lacks NFC hardware

**Today's Activity**
- Count of NFC events detected today
- Quick access to detailed activity log

**Quick Actions**
- Tap status card to open NFC settings (when disabled)
- Enable NFC button (when supported but disabled)

### 📋 Activity Screen

Complete event history with:

**Event List**
- Timestamp of each event
- Event type (NFC_ENABLED, TAG_DISCOVERED, etc.)
- Descriptive message
- Importance indicator

**Search & Filter**
- Search by event type or message
- Filter by date range
- Sort by timestamp or importance

**Event Actions**
- Tap event for details
- Long press to delete
- Swipe for quick actions

### ⚙️ Settings Screen

Comprehensive configuration options:

**Privacy Settings**
- Background monitoring toggle
- Auto-reminder intervals
- Privacy mode activation
- Unknown tag blocking

**Notification Settings**
- Enable/disable notifications
- Vibration control
- Sound preferences
- Notification style

**Appearance**
- Theme selection (Light/Dark)
- Accent color (Nothing Red default)
- Font preferences
- Display options

**Advanced Settings**
- Battery optimization
- Monitoring intervals
- Data retention
- Export/import settings

## Settings Configuration

### Privacy Settings

**Background Monitoring**
- Enables continuous NFC monitoring
- Requires foreground service permission
- May impact battery life
- Recommended for maximum security

**Auto-Reminder Intervals**
- **10 seconds**: Maximum security (default)
- **30 seconds**: Balanced approach
- **50 seconds**: Minimal interruption
- **Custom**: Set your own interval

**Privacy Mode**
- Enhanced security features
- Stricter monitoring
- Additional warnings
- Recommended for sensitive environments

### Notification Settings

**Notification Types**
- **Privacy Alerts**: When NFC enabled too long
- **Security Warnings**: Suspicious activity detected
- **Status Updates**: NFC state changes
- **System Events**: App status and errors

**Customization Options**
- **Sound**: Choose notification tone
- **Vibration**: Enable haptic feedback
- **LED**: Light notification (if supported)
- **Priority**: Set notification importance

### Advanced Configuration

**Battery Optimization**
- **Enabled**: Reduces background activity
- **Disabled**: Maximum monitoring capability
- **Adaptive**: Balances performance and battery

**Data Management**
- **Retention Period**: How long to keep logs
- **Auto-cleanup**: Automatic old data removal
- **Export Format**: JSON or CSV
- **Backup Settings**: Save configuration

## Privacy & Security

### Data Protection

**Local Storage Only**
- All data stored on your device
- No cloud synchronization
- No external data transmission
- Complete user control

**Encryption**
- Local database encryption
- Secure key management
- Protected sensitive data
- Android Keystore integration

### Permission Management

**Required Permissions**
- **NFC**: Access to NFC hardware
- **Notifications**: Privacy alerts
- **Foreground Service**: Background monitoring

**Optional Permissions**
- **Vibration**: Haptic feedback
- **Wake Lock**: Reliable monitoring

### Security Best Practices

**For Users**
1. Keep NFC disabled when not needed
2. Monitor NFC activity regularly
3. Be aware of NFC-enabled locations
4. Use privacy alerts effectively
5. Keep the app updated

**For Sensitive Environments**
1. Enable privacy mode
2. Use 10-second alert interval
3. Enable background monitoring
4. Block unknown tags
5. Review activity logs regularly

## Troubleshooting

### Common Issues

**NFC Not Detected**
- Verify device has NFC hardware
- Check if NFC is enabled in system settings
- Restart device if needed
- Contact support if persistent

**Notifications Not Working**
- Check notification permissions
- Verify notification channels enabled
- Disable battery optimization for app
- Check Do Not Disturb settings

**Background Monitoring Stops**
- Add app to battery optimization whitelist
- Enable "Allow background activity"
- Check device manufacturer settings
- Restart background service in app

**App Crashes or Freezes**
- Force close and restart app
- Clear app cache (not data)
- Restart device
- Reinstall app if persistent

### Device-Specific Issues

**Samsung Devices**
- Disable Adaptive Battery for the app
- Check Samsung Pay conflicts
- Enable "Allow background activity"

**Xiaomi/MIUI**
- Add to Protected Apps list
- Disable MIUI optimizations
- Enable Autostart permission

**OnePlus**
- Disable battery optimization
- Enable background app refresh
- Check OxygenOS privacy settings

## Tips & Best Practices

### Security Tips

1. **Regular Monitoring**: Check activity logs weekly
2. **Alert Configuration**: Use shorter intervals in public spaces
3. **NFC Hygiene**: Disable when not actively using
4. **Awareness**: Be conscious of NFC-enabled locations
5. **Updates**: Keep app updated for latest security features

### Performance Tips

1. **Battery Management**: Balance monitoring with battery life
2. **Storage**: Regularly clean old logs
3. **Notifications**: Customize to avoid alert fatigue
4. **Background Apps**: Limit other background services
5. **Device Maintenance**: Keep Android system updated

### Privacy Tips

1. **Data Retention**: Set appropriate log retention periods
2. **Sharing**: Never share NFC logs with untrusted parties
3. **Public WiFi**: Extra caution with NFC in public spaces
4. **Payment Cards**: Monitor for unauthorized transactions
5. **Access Cards**: Be aware of card cloning attempts

### Customization Tips

1. **Theme Selection**: Choose theme for better visibility
2. **Notification Timing**: Adjust for your usage patterns
3. **Sound Settings**: Use distinctive tones for alerts
4. **Widget Setup**: Add shortcuts to home screen
5. **Backup Settings**: Save configuration before major changes

## Getting Help

### Support Resources

- **User Guide**: This comprehensive guide
- **Troubleshooting**: Detailed problem-solving guide
- **FAQ**: Common questions and answers
- **Community**: User forums and discussions

### Feedback

We value your feedback! Please:
- Rate the app on Google Play Store
- Report bugs through proper channels
- Suggest features via GitHub issues
- Share positive experiences with others

---

## 📞 User Support

Need help or have questions?

- **Email**: support@dxbmark.com
- **Subject**: "User Guide - NFC Manager"
- **Response Time**: 24-48 hours
- **Languages**: English, Arabic

---

**Built with ❤️ by Tariq Said - Nothing OS Inspired Design**

*Technical Support & Contact: support@dxbmark.com*

---

*Licensed under the Apache License, Version 2.0*  
*Copyright 2025 Tariq Said. All rights reserved.*

*Thank you for using NFC Manager! Stay secure and protect your privacy.* 🛡️
