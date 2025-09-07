# Troubleshooting Guide - NFC Manager

## Common Issues and Solutions

This guide helps you resolve common problems when using NFC Manager.

## Installation Issues

### App Won't Install
**Error**: "App not installed" or "Package conflicts"

**Solutions:**
1. Check storage space (need 100MB free)
2. Uninstall any previous versions
3. Clear Google Play Store cache
4. Restart device and try again

### Installation Stuck
**Solutions:**
1. Cancel and restart device
2. Clear Google Play Store data
3. Check internet connection
4. Try during off-peak hours

## NFC Detection Problems

### "NFC Not Supported" Error
**Solutions:**
1. Verify device has NFC hardware in specifications
2. Check Settings > Connected Devices for NFC option
3. Contact device manufacturer if unsure

### NFC Shows as Disabled
**Solutions:**
1. Enable NFC: Settings > Connected Devices > NFC
2. Disable interfering payment apps temporarily
3. Restart NFC service in system settings

## Permission Issues

### Notifications Not Working
**Solutions:**
1. Enable notifications: Settings > Apps > NFC Manager > Notifications
2. Set notification importance to "High"
3. Disable battery optimization for the app
4. Check Do Not Disturb settings

### Background Service Stops
**Solutions:**
1. Disable battery optimization
2. Enable background activity
3. Add to protected apps list (Xiaomi/MIUI)
4. Enable autostart permission

## Device-Specific Issues

### Samsung Devices
- Disable Adaptive Battery
- Check Samsung Pay conflicts
- Enable background activity

### Xiaomi/MIUI Devices
- Add to Protected Apps list
- Disable MIUI optimizations
- Enable autostart permission

### OnePlus Devices
- Disable battery optimization
- Enable background app refresh

## Getting Help

**Before contacting support, gather:**
- Device model and Android version
- App version
- Exact error message
- Steps to reproduce the issue

**Support channels:**
- GitHub Issues for bug reports
- Email for private issues
- Community forum for general questions

---

*For more detailed troubleshooting, see the full documentation.*