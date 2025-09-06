import { useState, useEffect } from 'react';
import { Platform } from 'react-native';
import * as Haptics from 'expo-haptics';

interface PermissionState {
  nfcAvailable: boolean;
  nfcEnabled: boolean;
  permissionGranted: boolean;
  loading: boolean;
  error: string | null;
}

export const usePermissions = () => {
  const [permissionState, setPermissionState] = useState<PermissionState>({
    nfcAvailable: false,
    nfcEnabled: false,
    permissionGranted: false,
    loading: true,
    error: null,
  });

  useEffect(() => {
    checkPermissions();
  }, []);

  const checkPermissions = async () => {
    try {
      setPermissionState(prev => ({ ...prev, loading: true, error: null }));

      // Simulate NFC availability check
      // In a real app, you would use react-native-nfc-manager or similar
      const isNFCAvailable = Platform.OS === 'android';
      
      // Simulate permission check
      const hasPermission = true; // Would check actual NFC permissions
      
      // Simulate NFC enabled check
      const isNFCEnabled = Math.random() > 0.3; // Random for demo

      setPermissionState({
        nfcAvailable: isNFCAvailable,
        nfcEnabled: isNFCEnabled,
        permissionGranted: hasPermission,
        loading: false,
        error: null,
      });

      // Provide haptic feedback
      if (Platform.OS !== 'web') {
        Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
      }
    } catch (error) {
      setPermissionState(prev => ({
        ...prev,
        loading: false,
        error: 'Failed to check NFC permissions',
      }));
    }
  };

  const requestPermissions = async () => {
    try {
      setPermissionState(prev => ({ ...prev, loading: true }));
      
      // Simulate permission request
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      setPermissionState(prev => ({
        ...prev,
        permissionGranted: true,
        loading: false,
      }));

      if (Platform.OS !== 'web') {
        Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      }
    } catch (error) {
      setPermissionState(prev => ({
        ...prev,
        loading: false,
        error: 'Permission request failed',
      }));
    }
  };

  const openSystemSettings = () => {
    // In a real app, this would open system NFC settings
    console.log('Opening system NFC settings...');
    
    if (Platform.OS !== 'web') {
      Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    }
  };

  return {
    ...permissionState,
    checkPermissions,
    requestPermissions,
    openSystemSettings,
  };
};