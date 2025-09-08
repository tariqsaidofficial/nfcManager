# UI Testing Checklist for TARIQ (Manual Testing)

## 1. Main Screens and Navigation:
- [ ] **HomeScreen:**
    - [ ] Correctly display NFC status (enabled/disabled/not supported).
    - [ ] User guidance button/indicator for system NFC settings works.
    - [ ] Display background service information (if active).
- [ ] **ActivityScreen:**
    - [ ] Display event logs correctly.
    - [ ] Event type filters work.
    - [ ] Date range filters work (Today, Last 7 Days, etc.).
    - [ ] Export to CSV function works (verify the output file).
- [ ] **SettingsScreen:**
    - [ ] Change monitoring interval and save it correctly.
    - [ ] Background monitoring service toggle (on/off) works.
    - [ ] Navigate to the notification sound customization screen.
    - [ ] Change theme (light/dark), apply it correctly, and ensure it persists.
- [ ] **NotificationSoundSettingsScreen:**
    - [ ] Select a custom notification sound from the device.
    - [ ] Preview custom sound (play/stop).
    - [ ] "Use system default sound" button works.
    - [ ] Ensure sound playback stops when leaving the screen.
- [ ] **General Navigation:**
    - [ ] Bottom Navigation bar works smoothly for screen transitions.
    - [ ] Back button (system and in-app) works as expected.

## 2. Core NFC Functions:
- [ ] **NFC Tag Detection:**
    - [ ] Notification appears when an NFC tag is detected (if service is active).
    - [ ] Tag detection event is logged in the activity log (ensure no sensitive data is read).
- [ ] **Background Monitoring Service (NfcMonitoringService):**
    - [ ] Start and stop the service correctly from settings.
    - [ ] Service continues to run when the app is closed (if enabled).
    - [ ] Persistent service notification appears and its content updates correctly (Initializing, Active, NFC disabled).
    - [ ] Tapping the service notification opens the app.
    - [ ] Service restarts automatically on device reboot (if previously enabled).

## 3. Permissions:
- [ ] **NFC:** Permission requested correctly, and app functions based on permission status.
- [ ] **POST_NOTIFICATIONS (Android 13+):** Permission requested, and notifications display correctly.
- [ ] **READ_EXTERNAL_STORAGE / READ_MEDIA_AUDIO:** Permission requested when attempting to select a custom sound (if necessary based on API level).
- [ ] **FOREGROUND_SERVICE:** Ensure the service runs without permission-related issues.

## 4. General User Experience (UX) and User Interface (UI):
- [ ] **Responsiveness:** App responds quickly to interactions.
- [ ] **Visual Clarity:** Text, icons, and contrast are clear.
- [ ] **Consistency:** Design and elements are consistent across different screens (Nothing OS style).
- [ ] **Error Handling:** Clear and helpful error messages are displayed (e.g., when NFC is unavailable, or sound playback fails).
- [ ] **RTL Support (for Arabic later):** Ensure the UI displays correctly.
- [ ] **Test on different screen sizes and orientations (portrait/landscape) if possible.**

## 5. Edge Cases:
- [ ] **No internet connection (if any component requires it, currently doesn't seem to be the case).**
- [ ] **Low storage space (when trying to export CSV or save settings).**
- [ ] **System interruptions (e.g., incoming calls).**
