import { useState, useEffect, useCallback } from 'react';
import { Platform, AppState, AppStateStatus } from 'react-native';
import NfcManager, { NfcTech, Ndef, NfcEvents } from 'react-native-nfc-manager';
import { reportCrash, reportError } from '../utils/crashReporting';

interface ActivityLogEntry {
  id: number;
  message: string;
  icon: string;
  timestamp: Date;
}

export const useNFCManager = () => {
  const [nfcStatus, setNfcStatus] = useState(false);
  const [nfcSupported, setNfcSupported] = useState(false);
  const [nfcEnabled, setNfcEnabled] = useState(false);
  const [autoReminderEnabled, setAutoReminderEnabled] = useState(false);
  const [reminderInterval, setReminderInterval] = useState(30);
  const [lastActivity, setLastActivity] = useState(Date.now());
  const [showNotification, setShowNotification] = useState(false);
  const [isAnimating, setIsAnimating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activityLog, setActivityLog] = useState<ActivityLogEntry[]>([
    { id: 1, message: 'App initialized.', icon: 'Shield', timestamp: new Date(Date.now() - 300000) },
    { id: 2, message: 'Checking NFC availability...', icon: 'Settings', timestamp: new Date(Date.now() - 180000) }
  ]);

  const addLogEntry = useCallback((message: string, icon: string) => {
    setActivityLog(prevLog => [
      { id: Date.now(), message, icon, timestamp: new Date() },
      ...prevLog
    ].slice(0, 15)); // Increased to 15 for more detailed logging
  }, []);

  // Initialize NFC Manager
  useEffect(() => {
    const initNFC = async () => {
      try {
        // Only initialize on Android
        if (Platform.OS !== 'android') {
          setError('NFC is only supported on Android devices');
          addLogEntry('NFC not supported on this platform.', 'Shield');
          setNfcSupported(false);
          return;
        }

        // Check if NFC is supported
        const supported = await NfcManager.isSupported();
        setNfcSupported(supported);
        
        if (!supported) {
          setError('NFC is not supported on this device');
          addLogEntry('NFC not supported on this device.', 'Shield');
          return;
        }

        // Initialize NFC Manager
        await NfcManager.start();
        addLogEntry('NFC Manager initialized successfully.', 'Settings');

        // Check if NFC is enabled
        const enabled = await NfcManager.isEnabled();
        setNfcEnabled(enabled);
        setNfcStatus(enabled);
        
        if (enabled) {
          addLogEntry('NFC is enabled on device.', 'Nfc');
          setLastActivity(Date.now());
        } else {
          addLogEntry('NFC is disabled on device.', 'Power');
        }

      } catch (error) {
        console.error('NFC initialization error:', error);
        
        const errorMessage = `NFC initialization failed: ${error}`;
        setError(errorMessage);
        addLogEntry(errorMessage, 'Shield');
        
        // Report crash for monitoring
        reportError(error instanceof Error ? error : new Error(String(error)), {
          component: 'useNFCManager',
          action: 'initNFC',
          additionalInfo: { nfcSupported, platform: Platform.OS }
        });
      }
    };

    initNFC();

    // Cleanup on unmount
    return () => {
      NfcManager.cancelTechnologyRequest().catch(() => {
        // Ignore errors on cleanup
      });
    };
  }, [addLogEntry]);

  // Monitor NFC state changes
  useEffect(() => {
    let stateCheckInterval: NodeJS.Timeout;

    if (nfcSupported) {
      // Check NFC state every 2 seconds
      stateCheckInterval = setInterval(async () => {
        try {
          const enabled = await NfcManager.isEnabled();
          
          if (enabled !== nfcEnabled) {
            setNfcEnabled(enabled);
            setNfcStatus(enabled);
            
            if (enabled) {
              addLogEntry('NFC has been enabled.', 'Power');
              setLastActivity(Date.now());
            } else {
              addLogEntry('NFC has been disabled.', 'Power');
              setShowNotification(false); // Hide notification if NFC is disabled
            }
          }
        } catch (error) {
          console.error('NFC state check error:', error);
          
          // Handle specific NFC state check errors
          if (error instanceof Error && error.message.includes('null value')) {
            // This is expected in Expo Go - don't spam error messages
            if (error !== null) {
              setError('NFC monitoring unavailable in Expo Go');
            }
          } else {
            setError('Failed to monitor NFC state');
          }
        }
      }, 2000);
    }

    return () => {
      if (stateCheckInterval) {
        clearInterval(stateCheckInterval);
      }
    };
  }, [nfcSupported, nfcEnabled, addLogEntry]);

  // Monitor app state to update NFC status when app comes to foreground
  useEffect(() => {
    const handleAppStateChange = async (nextAppState: AppStateStatus) => {
      if (nextAppState === 'active' && nfcSupported) {
        try {
          const enabled = await NfcManager.isEnabled();
          if (enabled !== nfcEnabled) {
            setNfcEnabled(enabled);
            setNfcStatus(enabled);
            addLogEntry(`App resumed - NFC is ${enabled ? 'enabled' : 'disabled'}.`, 'Settings');
            if (enabled) {
              setLastActivity(Date.now());
            }
          }
        } catch (error) {
          console.error('App state NFC check error:', error);
        }
      }
    };

    const subscription = AppState.addEventListener('change', handleAppStateChange);
    return () => subscription?.remove();
  }, [nfcSupported, nfcEnabled, addLogEntry]);

  // Check for reminder notifications - Privacy Protection Logic
  useEffect(() => {
    if (!autoReminderEnabled || !nfcStatus || !nfcEnabled) return;
    
    const checkTimer = setInterval(() => {
      const timeSinceLastActivity = (Date.now() - lastActivity) / 1000;
      
      if (timeSinceLastActivity >= reminderInterval) {
        setShowNotification(true);
        addLogEntry(`Privacy alert: NFC enabled for ${reminderInterval}s`, 'Bell');
      }
    }, 1000);
    
    return () => clearInterval(checkTimer);
  }, [autoReminderEnabled, nfcStatus, nfcEnabled, lastActivity, reminderInterval, addLogEntry]);

  // Open system NFC settings (Android only)
  const openNFCSettings = useCallback(async () => {
    try {
      // Check if NFC is supported before attempting to open settings
      if (!nfcSupported) {
        const errorMsg = 'Cannot open NFC settings: NFC not supported on this device';
        setError(errorMsg);
        addLogEntry(errorMsg, 'Shield');
        return;
      }
      
      await NfcManager.goToNfcSetting();
      addLogEntry('Opened system NFC settings.', 'Settings');
      
      // Clear any previous errors if successful
      setError(null);
      
    } catch (error) {
      console.error('Failed to open NFC settings:', error);
      
      let errorMessage = 'Failed to open NFC settings';
      
      if (error instanceof Error) {
        if (error.message.includes('null value')) {
          errorMessage = 'Cannot access NFC settings in Expo Go. Use a development build on a real device.';
        } else if (error.message.includes('permission')) {
          errorMessage = 'Permission denied to access NFC settings.';
        } else {
          errorMessage = `Settings Error: ${error.message}`;
        }
      }
      
      setError(errorMessage);
      addLogEntry(errorMessage, 'Shield');
    }
  }, [nfcSupported, addLogEntry]);

  // Toggle NFC (opens settings since apps can't directly toggle NFC)
  const toggleNFC = useCallback(async () => {
    setIsAnimating(true);
    
    try {
      if (nfcEnabled) {
        // NFC is enabled, suggest user to disable it for privacy
        addLogEntry('Redirecting to settings to disable NFC for privacy.', 'Shield');
        await openNFCSettings();
      } else {
        // NFC is disabled, user can enable it if needed
        addLogEntry('Redirecting to settings to enable NFC.', 'Settings');
        await openNFCSettings();
      }
    } catch (error) {
      console.error('Toggle NFC error:', error);
      addLogEntry('Failed to open NFC settings.', 'Shield');
    } finally {
      setTimeout(() => setIsAnimating(false), 1000);
    }
  }, [nfcEnabled, openNFCSettings, addLogEntry]);

  const toggleAutoReminder = useCallback(() => {
    setAutoReminderEnabled(prev => {
      const newStatus = !prev;
      addLogEntry(`Privacy monitoring ${newStatus ? 'enabled' : 'disabled'}.`, 'Bell');
      if (!newStatus) {
        setShowNotification(false);
      }
      return newStatus;
    });
  }, [addLogEntry]);

  const dismissNotification = useCallback(() => {
    setShowNotification(false);
    setLastActivity(Date.now());
    addLogEntry('Privacy alert dismissed.', 'Shield');
  }, [addLogEntry]);

  const formatTime = (seconds: number): string => {
    if (seconds < 60) return `${Math.floor(seconds)}s`;
    return `${Math.floor(seconds / 60)}m ${Math.floor(seconds % 60)}s`;
  };

  const formatRelativeTime = (date: Date): string => {
    const seconds = Math.round((new Date().getTime() - date.getTime()) / 1000);
    if (seconds < 60) return 'now';
    if (seconds < 3600) return `${Math.round(seconds / 60)}m ago`;
    return `${Math.round(seconds / 3600)}h ago`;
  };

  return {
    nfcStatus,
    nfcSupported,
    nfcEnabled,
    autoReminderEnabled,
    reminderInterval,
    setReminderInterval,
    lastActivity,
    showNotification,
    isAnimating,
    error,
    activityLog,
    toggleNFC,
    toggleAutoReminder,
    dismissNotification,
    openNFCSettings,
    formatTime,
    formatRelativeTime,
    addLogEntry,
  };
};