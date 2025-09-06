import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  Dimensions,
  Modal,
  StatusBar,
  TextInput,
  Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { BlurView } from 'expo-blur';
import { Bell, BellOff, Clock } from 'lucide-react-native';
import { NFCInterface } from '../../components/NFCInterface';
import { NothingToggle } from '../../components/NothingToggle';
import { PrivacyCompliance } from '../../components/PrivacyCompliance';
import { AccessibilityWrapper } from '../../components/AccessibilityWrapper';
import { useNFCManager } from '../../hooks/useNFCManager';
import { usePermissions } from '../../hooks/usePermissions';
import { useNothingFonts } from '../../hooks/useFonts';

const { width } = Dimensions.get('window');

export default function NFCManager() {
  const fontsLoaded = useNothingFonts();

  const {
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
    toggleNFC,
    toggleAutoReminder,
    dismissNotification,
    openNFCSettings,
    formatTime,
  } = useNFCManager();

  const { nfcAvailable, nfcEnabled: permNfcEnabled, permissionGranted, requestPermissions, showNFCGuide } = usePermissions();
  const [showPrivacyModal, setShowPrivacyModal] = React.useState(!permissionGranted);
  
  // Custom interval states
  const reminderIntervals = [10, 30, 50];
  const [showCustomInterval, setShowCustomInterval] = useState(false);
  const [customInterval, setCustomInterval] = useState('');

  // Don't render until fonts are loaded
  if (!fontsLoaded) {
    return null;
  }

  const handlePrivacyAccept = async () => {
    setShowPrivacyModal(false);
    if (!permissionGranted) {
      await requestPermissions();
    }
  };

  const handleCustomInterval = () => {
    setShowCustomInterval(true);
  };

  const saveCustomInterval = () => {
    const interval = parseInt(customInterval);
    if (interval >= 5 && interval <= 300) {
      setReminderInterval(interval);
      setShowCustomInterval(false);
      setCustomInterval('');
      Alert.alert(
        'Custom Interval Set',
        `Privacy alert will trigger after ${interval} seconds`,
        [{ text: 'OK' }]
      );
    } else {
      Alert.alert(
        'Invalid Interval',
        'Please enter a value between 5 and 300 seconds',
        [{ text: 'OK' }]
      );
    }
  };
  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#000000" />
      
      {/* Header */}
      <SafeAreaView style={styles.header}>
        <Text style={styles.headerTitle}>NFC MANAGER</Text>
        <AccessibilityWrapper 
          label={`NFC Status: ${nfcStatus ? 'Active' : 'Inactive'}`}
          role="image"
        >
          <View style={[styles.statusIndicator, { backgroundColor: nfcStatus ? '#ef4444' : '#374151' }]} />
        </AccessibilityWrapper>
      </SafeAreaView>

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        {/* Main NFC Status Card */}
        <AccessibilityWrapper 
          label={`NFC Status Card: Currently ${nfcStatus ? 'Active' : 'Inactive'}`}
          role="text"
        >
          <BlurView intensity={20} style={[styles.mainCard, { borderColor: nfcStatus ? '#ef444450' : '#1f293750' }]}>
          <LinearGradient
            colors={['#11111150', '#1f293750']}
            style={styles.cardGradient}
          >
            <View style={styles.glyphContainer}>
              <NFCInterface isActive={nfcStatus} />
            </View>
            
            <View style={styles.statusContainer}>
              <Text style={styles.statusLabel}>NFC STATUS</Text>
              {!nfcSupported ? (
                <Text style={[styles.statusText, { color: '#6b7280' }]}>
                  NOT SUPPORTED
                </Text>
              ) : (
                <Text style={[styles.statusText, { color: nfcEnabled ? '#ef4444' : '#6b7280' }]}>
                  {nfcEnabled ? 'ENABLED' : 'DISABLED'}
                </Text>
              )}
              {nfcEnabled && (
                <Text style={styles.lastActivityText}>
                  Privacy monitoring • {formatTime((Date.now() - lastActivity) / 1000)} ago
                </Text>
              )}
              {error && (
                <Text style={styles.errorText}>
                  {error}
                </Text>
              )}
            </View>
          </LinearGradient>
          </BlurView>
        </AccessibilityWrapper>

        {/* Auto Reminder Card */}
        <BlurView intensity={20} style={styles.card}>
          <LinearGradient colors={['#11111150', '#1f293750']} style={styles.cardGradient}>
            <View style={styles.reminderHeader}>
              <View style={styles.reminderInfo}>
                <View style={styles.reminderTitleContainer}>
                  {autoReminderEnabled ? 
                    <Bell size={16} color="#ef4444" /> : 
                    <BellOff size={16} color="#6b7280" />
                  }
                  <Text style={styles.reminderTitle}>NFC Reminder</Text>
                </View>
                <Text style={styles.reminderSubtitle}>Protect privacy by alerting when NFC stays enabled.</Text>
              </View>
              <AccessibilityWrapper
                label="Auto Reminder Toggle"
                hint={`Currently ${autoReminderEnabled ? 'enabled' : 'disabled'}. Double tap to toggle.`}
                role="switch"
                state={{ checked: autoReminderEnabled }}
              >
                <NothingToggle isEnabled={autoReminderEnabled} onToggle={toggleAutoReminder} />
              </AccessibilityWrapper>
            </View>

            {autoReminderEnabled && (
              <View style={styles.intervalContainer}>
                <View style={styles.intervalHeader}>
                  <Clock size={12} color="#6b7280" />
                  <Text style={styles.intervalLabel}>Interval</Text>
                </View>
                <View style={styles.intervalButtons}>
                  {reminderIntervals.map((interval) => (
                    <AccessibilityWrapper
                      key={interval}
                      label={`${interval} seconds interval`}
                      hint={reminderInterval === interval ? 'Currently selected' : 'Tap to select'}
                      role="button"
                      state={{ selected: reminderInterval === interval }}
                    >
                      <TouchableOpacity
                        onPress={() => setReminderInterval(interval)}
                        style={[
                          styles.intervalButton,
                          {
                            backgroundColor: reminderInterval === interval ? '#ef4444' : '#1f2937',
                          },
                        ]}
                      >
                        <Text
                          style={[
                            styles.intervalButtonText,
                            {
                              color: reminderInterval === interval ? '#ffffff' : '#6b7280',
                            },
                          ]}
                        >
                          {interval}s
                        </Text>
                      </TouchableOpacity>
                    </AccessibilityWrapper>
                  ))}
                  
                  {/* Custom Interval Button */}
                  <AccessibilityWrapper
                    label="Custom interval"
                    hint={!reminderIntervals.includes(reminderInterval) ? 'Currently using custom interval' : 'Tap to set custom interval'}
                    role="button"
                    state={{ selected: !reminderIntervals.includes(reminderInterval) }}
                  >
                    <TouchableOpacity
                      onPress={handleCustomInterval}
                      style={[
                        styles.intervalButton,
                        {
                          backgroundColor: !reminderIntervals.includes(reminderInterval) ? '#ef4444' : '#1f2937',
                        },
                      ]}
                    >
                      <Text
                        style={[
                          styles.intervalButtonText,
                          {
                            color: !reminderIntervals.includes(reminderInterval) ? '#ffffff' : '#6b7280',
                            fontSize: 12,
                          },
                        ]}
                      >
                        {!reminderIntervals.includes(reminderInterval) ? `${reminderInterval}s` : 'Custom'}
                      </Text>
                    </TouchableOpacity>
                  </AccessibilityWrapper>
                </View>
              </View>
            )}
          </LinearGradient>
        </BlurView>

        {/* NFC Controls */}
        <View style={styles.controlsContainer}>
          {!nfcSupported ? (
            <View style={styles.warningContainer}>
              <Text style={styles.warningTitle}>NFC Not Supported</Text>
              <Text style={styles.warningText}>
                This device doesn't support NFC functionality. This app requires NFC for privacy monitoring.
              </Text>
            </View>
          ) : (
            <AccessibilityWrapper
              label={nfcEnabled ? "Open NFC Settings to Disable" : "Open NFC Settings to Enable"}
              hint="Tap to open system NFC settings"
              role="button"
            >
              <TouchableOpacity 
                onPress={toggleNFC} 
                style={[styles.nfcButton, { opacity: isAnimating ? 0.6 : 1 }]}
                disabled={isAnimating}
              >
                <Text style={styles.nfcButtonText}>
                  {isAnimating ? 'OPENING SETTINGS...' : 'OPEN NFC SETTINGS'}
                </Text>
              </TouchableOpacity>
            </AccessibilityWrapper>
          )}
        </View>
      </ScrollView>

      {/* Privacy Compliance Modal */}
      <PrivacyCompliance 
        visible={showPrivacyModal} 
        onAccept={handlePrivacyAccept} 
      />

      {/* Notification Modal */}
      <Modal visible={showNotification} transparent animationType="fade">
        <BlurView intensity={50} style={styles.modalContainer}>
          <View style={styles.notificationCard}>
            <LinearGradient colors={['#111111', '#1f2937']} style={styles.notificationGradient}>
              <View style={styles.notificationIconContainer}>
                <Bell size={32} color="#ef4444" />
              </View>
              
              <Text style={styles.notificationTitle}>NFC Reminder</Text>
              <Text style={styles.notificationMessage}>
                NFC enabled for {reminderInterval}s. Turn off to protect privacy and save power.
              </Text>
              
              <View style={styles.notificationButtons}>
                <AccessibilityWrapper
                  label="Turn Off NFC"
                  hint="Tap to turn off NFC and dismiss notification"
                  role="button"
                >
                  <TouchableOpacity onPress={toggleNFC} style={styles.primaryButton}>
                    <Text style={styles.primaryButtonText}>Turn Off NFC</Text>
                  </TouchableOpacity>
                </AccessibilityWrapper>
                <AccessibilityWrapper
                  label="Dismiss Notification"
                  hint="Tap to dismiss this notification"
                  role="button"
                >
                  <TouchableOpacity onPress={dismissNotification} style={styles.secondaryButton}>
                    <Text style={styles.secondaryButtonText}>Dismiss</Text>
                  </TouchableOpacity>
                </AccessibilityWrapper>
              </View>
            </LinearGradient>
          </View>
        </BlurView>
      </Modal>

      {/* Custom Interval Modal */}
      <Modal
        visible={showCustomInterval}
        transparent={true}
        animationType="fade"
        onRequestClose={() => setShowCustomInterval(false)}
      >
        <BlurView intensity={40} style={styles.modalOverlay}>
          <View style={styles.modalContainer}>
            <LinearGradient colors={['#111111ee', '#1f2937ee']} style={styles.modalContent}>
              <Text style={[styles.modalTitle, { fontFamily: 'NothingFont' }]}>
                Custom Interval
              </Text>
              <Text style={styles.modalMessage}>
                Enter a custom privacy alert interval (5-300 seconds):
              </Text>
              
              <TextInput
                style={[styles.customInput, { fontFamily: 'NothingFont' }]}
                value={customInterval}
                onChangeText={setCustomInterval}
                placeholder="Enter seconds..."
                placeholderTextColor="#6b7280"
                keyboardType="numeric"
                maxLength={3}
                autoFocus={true}
              />
              
              <View style={styles.modalButtons}>
                <TouchableOpacity 
                  onPress={() => setShowCustomInterval(false)} 
                  style={styles.secondaryButton}
                >
                  <Text style={[styles.secondaryButtonText, { fontFamily: 'NothingFont' }]}>
                    Cancel
                  </Text>
                </TouchableOpacity>
                <TouchableOpacity 
                  onPress={saveCustomInterval} 
                  style={styles.primaryButton}
                >
                  <Text style={[styles.primaryButtonText, { fontFamily: 'NothingFont' }]}>
                    Save
                  </Text>
                </TouchableOpacity>
              </View>
            </LinearGradient>
          </View>
        </BlurView>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000000',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 24,
    paddingVertical: 16,
  },
  headerTitle: {
    fontSize: 12,
    fontWeight: 'bold',
    fontFamily: 'NothingFont',
    color: '#ffffff',
    letterSpacing: 2,
    opacity: 0.5,
  },
  statusIndicator: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  content: {
    flex: 1,
    paddingHorizontal: 24,
  },
  mainCard: {
    borderRadius: 24,
    borderWidth: 2,
    marginBottom: 24,
    overflow: 'hidden',
  },
  card: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#1f2937',
    marginBottom: 24,
    overflow: 'hidden',
  },
  cardGradient: {
    padding: 24,
  },
  glyphContainer: {
    alignItems: 'center',
    marginBottom: 16,
  },
  statusContainer: {
    alignItems: 'center',
  },
  statusLabel: {
    fontSize: 12,
    fontWeight: '500',
    fontFamily: 'NothingFont',
    color: '#ffffff',
    opacity: 0.5,
    letterSpacing: 1,
    marginBottom: 8,
  },
  statusText: {
    fontSize: 48,
    fontWeight: 'bold',
    fontFamily: 'NothingFont',
    letterSpacing: -1,
  },
  lastActivityText: {
    fontSize: 12,
    color: '#ffffff',
    opacity: 0.4,
    marginTop: 8,
  },
  warningText: {
    fontSize: 14,
    color: '#ff8800',
    opacity: 0.8,
    marginTop: 8,
    textAlign: 'center',
  },
  infoText: {
    fontSize: 14,
    color: '#00ff88',
    opacity: 0.8,
    marginTop: 8,
    textAlign: 'center',
  },
  errorContainer: {
    backgroundColor: '#ff444420',
    padding: 12,
    borderRadius: 8,
    marginTop: 12,
    borderWidth: 1,
    borderColor: '#ff444450',
  },
  errorText: {
    fontSize: 12,
    color: '#ff4444',
    textAlign: 'center',
    fontFamily: 'NothingFont',
  },
  reminderHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  reminderInfo: {
    flex: 1,
  },
  reminderTitleContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  reminderTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    fontFamily: 'NothingFont',
    color: '#ffffff',
    marginLeft: 12,
  },
  reminderSubtitle: {
    fontSize: 12,
    fontFamily: 'NothingFont',
    color: '#ffffff',
    opacity: 0.5,
  },
  intervalContainer: {
    marginTop: 24,
  },
  intervalHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  intervalLabel: {
    fontSize: 12,
    fontFamily: 'NothingFont',
    color: '#ffffff',
    opacity: 0.6,
    marginLeft: 8,
  },
  intervalButtons: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 16,
  },
  intervalButton: {
    width: 64,
    height: 64,
    borderRadius: 32,
    justifyContent: 'center',
    alignItems: 'center',
  },
  intervalButtonText: {
    fontSize: 18,
    fontWeight: 'bold',
    fontFamily: 'NothingFont',
  },
  controlsContainer: {
    alignItems: 'center',
    paddingVertical: 16,
    marginBottom: 24,
  },
  nfcButton: {
    backgroundColor: '#ef444420',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#ef444450',
  },
  nfcButtonText: {
    fontSize: 12,
    fontFamily: 'NothingFont',
    color: '#ef4444',
    fontWeight: 'bold',
    letterSpacing: 1,
  },
  warningContainer: {
    backgroundColor: '#37415120',
    padding: 20,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#374151',
    alignItems: 'center',
  },
  warningTitle: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 8,
  },
  warningText: {
    fontSize: 12,
    color: '#ffffff',
    opacity: 0.7,
    textAlign: 'center',
    lineHeight: 18,
  },
  errorText: {
    fontSize: 11,
    color: '#ef4444',
    opacity: 0.8,
    marginTop: 8,
    textAlign: 'center',
  },
  modalContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  notificationCard: {
    width: '100%',
    maxWidth: 320,
    borderRadius: 24,
    borderWidth: 2,
    borderColor: '#ef444450',
    overflow: 'hidden',
  },
  notificationGradient: {
    padding: 32,
    alignItems: 'center',
  },
  notificationIconContainer: {
    width: 64,
    height: 64,
    backgroundColor: '#ef444420',
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 24,
  },
  notificationTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 12,
  },
  notificationMessage: {
    fontSize: 14,
    color: '#ffffff',
    opacity: 0.8,
    textAlign: 'center',
    marginBottom: 32,
    lineHeight: 20,
  },
  notificationButtons: {
    width: '100%',
    gap: 12,
  },
  primaryButton: {
    backgroundColor: '#ef4444',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 12,
    alignItems: 'center',
  },
  primaryButtonText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  secondaryButton: {
    backgroundColor: '#1f293780',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 12,
    alignItems: 'center',
  },
  secondaryButtonText: {
    fontSize: 14,
    color: '#ffffff',
  },
  customInput: {
    backgroundColor: '#1f2937',
    borderWidth: 1,
    borderColor: '#374151',
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 16,
    color: '#ffffff',
    fontSize: 16,
    textAlign: 'center',
    marginVertical: 20,
  },
  modalButtons: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 24,
  },
});