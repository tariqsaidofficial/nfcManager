import { useState, useEffect } from 'react';
import { Platform, PermissionsAndroid, Linking, Alert } from 'react-native';
import NfcManager from 'react-native-nfc-manager';
import * as Haptics from 'expo-haptics';

interface PermissionState {
  nfcAvailable: boolean;
  nfcEnabled: boolean;
  permissionGranted: boolean;
  notificationPermission: boolean;
  loading: boolean;
  error: string | null;
}

export const usePermissions = () => {
  const [permissionState, setPermissionState] = useState<PermissionState>({
    nfcAvailable: false,
    nfcEnabled: false,
    permissionGranted: false,
    notificationPermission: false,
    loading: true,
    error: null,
  });

  useEffect(() => {
    checkPermissions();
  }, []);

  const checkPermissions = async () => {
    try {
      setPermissionState(prev => ({ ...prev, loading: true, error: null }));

      // Only check on Android
      if (Platform.OS !== 'android') {
        setPermissionState({
          nfcAvailable: false,
          nfcEnabled: false,
          permissionGranted: false,
          notificationPermission: false,
          loading: false,
          error: 'This app only works on Android devices with NFC support',
        });
        return;
      }

      // Check NFC availability
      const isNFCAvailable = await NfcManager.isSupported();
      
      if (!isNFCAvailable) {
        setPermissionState({
          nfcAvailable: false,
          nfcEnabled: false,
          permissionGranted: false,
          notificationPermission: false,
          loading: false,
          error: 'NFC is not supported on this device',
        });
        return;
      }

      // Check if NFC is enabled
      const isNFCEnabled = await NfcManager.isEnabled();
      
      // Check notification permission (Android 13+)
      let hasNotificationPermission = true;
      if (Platform.Version >= 33) {
        const notificationStatus = await PermissionsAndroid.check(
          PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS
        );
        hasNotificationPermission = notificationStatus === PermissionsAndroid.RESULTS.GRANTED;
      }

      // All permissions are automatically granted for NFC on Android
      // The main requirement is that NFC hardware is available
      const hasPermission = isNFCAvailable;

      setPermissionState({
        nfcAvailable: isNFCAvailable,
        nfcEnabled: isNFCEnabled,
        permissionGranted: hasPermission,
        notificationPermission: hasNotificationPermission,
        loading: false,
        error: null,
      });

      // Provide haptic feedback
      if (Platform.OS !== 'web') {
        Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
      }
    } catch (error) {
      console.error('Permission check error:', error);
      
      let errorMessage = 'Failed to check permissions';
      
      if (error instanceof Error) {
        if (error.message.includes('null value')) {
          errorMessage = 'NFC permission check unavailable in Expo Go. This is expected.';
        } else if (error.message.includes('network')) {
          errorMessage = 'Network error while checking permissions. Check your connection.';
        } else {
          errorMessage = `Permission Error: ${error.message}`;
        }
      }
      
      setPermissionState(prev => ({
        ...prev,
        loading: false,
        error: errorMessage,
      }));
    }
  };

  const requestPermissions = async () => {
    try {
      setPermissionState(prev => ({ ...prev, loading: true }));
      
      if (Platform.OS !== 'android') {
        throw new Error('Permissions can only be requested on Android');
      }

      // Request notification permission if needed (Android 13+)
      if (Platform.Version >= 33 && !permissionState.notificationPermission) {
        const notificationResult = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
          {
            title: 'Notification Permission',
            message: 'This app needs notification permission to alert you about NFC privacy concerns.',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          }
        );
        
        if (notificationResult !== PermissionsAndroid.RESULTS.GRANTED) {
          Alert.alert(
            'Permission Required',
            'Notification permission is required for privacy alerts. Please enable it in settings.',
            [
              { text: 'Cancel', style: 'cancel' },
              { text: 'Open Settings', onPress: () => Linking.openSettings() }
            ]
          );
        }
      }

      // Check if NFC is enabled, if not, guide user to enable it
      if (!permissionState.nfcEnabled) {
        Alert.alert(
          'NFC Required',
          'This app requires NFC to be enabled for monitoring. Would you like to enable it now?',
          [
            { text: 'Cancel', style: 'cancel' },
            { text: 'Enable NFC', onPress: openSystemSettings }
          ]
        );
      }

      // Refresh permission state
      await checkPermissions();

      if (Platform.OS !== 'web') {
        Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      }
    } catch (error) {
      console.error('Permission request error:', error);
      setPermissionState(prev => ({
        ...prev,
        loading: false,
        error: `Permission request failed: ${error}`,
      }));
    }
  };

  const openSystemSettings = async () => {
    try {
      await NfcManager.goToNfcSetting();
      
      if (Platform.OS !== 'web') {
        Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
      }
    } catch (error) {
      console.error('Failed to open NFC settings:', error);
      
      let errorMessage = 'Cannot open NFC settings';
      
      if (error instanceof Error && error.message.includes('null value')) {
        errorMessage = 'NFC settings unavailable in Expo Go. Use a development build on a real device.';
      }
      
      // Show specific error first
      Alert.alert(
        'Settings Error',
        errorMessage,
        [
          { text: 'Cancel', style: 'cancel' },
          { text: 'Try General Settings', onPress: () => {
            Linking.openSettings().catch(settingsError => {
              console.error('Failed to open settings:', settingsError);
              Alert.alert(
                'Cannot Open Settings',
                'Please manually enable NFC in your device settings:\n\nSettings > Connected devices > NFC',
                [{ text: 'OK' }]
              );
            });
          }}
        ]
      );
    }
  };

  const showNFCGuide = () => {
    Alert.alert(
      'How to Enable NFC',
      'To enable NFC on your Android device:\n\n' +
      '1. Go to Settings\n' +
      '2. Look for "Connected devices" or "Connections"\n' +
      '3. Find "NFC" and turn it on\n' +
      '4. Return to this app\n\n' +
      'NFC is required for privacy monitoring.',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Open Settings', onPress: openSystemSettings }
      ]
    );
  };

  return {
    ...permissionState,
    checkPermissions,
    requestPermissions,
    openSystemSettings,
    showNFCGuide,
  };
};