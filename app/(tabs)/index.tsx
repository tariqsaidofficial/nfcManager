import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  Dimensions,
  Modal,
  StatusBar,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { BlurView } from 'expo-blur';
import { Bell, BellOff, Clock } from 'lucide-react-native';
import { GlyphInterface } from '../../components/GlyphInterface';
import { NothingToggle } from '../../components/NothingToggle';
import { PrivacyCompliance } from '../../components/PrivacyCompliance';
import { AccessibilityWrapper } from '../../components/AccessibilityWrapper';
import { useNFCManager } from '../../hooks/useNFCManager';
import { usePermissions } from '../../hooks/usePermissions';

const { width } = Dimensions.get('window');

export default function NFCGlyph() {
  const {
    nfcStatus,
    autoReminderEnabled,
    reminderInterval,
    setReminderInterval,
    lastActivity,
    showNotification,
    toggleNFC,
    toggleAutoReminder,
    dismissNotification,
    formatTime,
  } = useNFCManager();

  const { nfcAvailable, permissionGranted, requestPermissions } = usePermissions();
  const [showPrivacyModal, setShowPrivacyModal] = React.useState(!permissionGranted);

  const handlePrivacyAccept = async () => {
    setShowPrivacyModal(false);
    if (!permissionGranted) {
      await requestPermissions();
    }
  };
  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#000000" />
      
      {/* Header */}
      <SafeAreaView style={styles.header}>
        <Text style={styles.headerTitle}>NFC GLYPH</Text>
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
              <GlyphInterface isActive={nfcStatus} />
            </View>
            
            <View style={styles.statusContainer}>
              <Text style={styles.statusLabel}>NFC STATUS</Text>
              <Text style={[styles.statusText, { color: nfcStatus ? '#ef4444' : '#6b7280' }]}>
                {nfcStatus ? 'ACTIVE' : 'INACTIVE'}
              </Text>
              {nfcStatus && (
                <Text style={styles.lastActivityText}>
                  Last activity • {formatTime((Date.now() - lastActivity) / 1000)} ago
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
                  <Text style={styles.reminderTitle}>Glyph Reminder</Text>
                </View>
                <Text style={styles.reminderSubtitle}>Notify when NFC is idle.</Text>
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
                  {[30, 45, 60].map((interval) => (
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
                          {interval}
                        </Text>
                      </TouchableOpacity>
                    </AccessibilityWrapper>
                  ))}
                </View>
              </View>
            )}
          </LinearGradient>
        </BlurView>

        {/* Demo Controls */}
        <View style={styles.demoContainer}>
          <AccessibilityWrapper
            label="Demo Toggle NFC"
            hint="Tap to toggle NFC status for demonstration"
            role="button"
          >
            <TouchableOpacity onPress={toggleNFC} style={styles.demoButton}>
              <Text style={styles.demoButtonText}>DEMO: TOGGLE NFC</Text>
            </TouchableOpacity>
          </AccessibilityWrapper>
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
              
              <Text style={styles.notificationTitle}>Glyph Reminder</Text>
              <Text style={styles.notificationMessage}>
                NFC idle for {reminderInterval}s. Consider turning off to save power.
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
    padding: 32,
  },
  glyphContainer: {
    alignItems: 'center',
    marginBottom: 24,
  },
  statusContainer: {
    alignItems: 'center',
  },
  statusLabel: {
    fontSize: 12,
    fontWeight: '500',
    color: '#ffffff',
    opacity: 0.5,
    letterSpacing: 1,
    marginBottom: 8,
  },
  statusText: {
    fontSize: 48,
    fontWeight: 'bold',
    letterSpacing: -2,
  },
  lastActivityText: {
    fontSize: 12,
    color: '#ffffff',
    opacity: 0.4,
    marginTop: 8,
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
    color: '#ffffff',
    marginLeft: 12,
  },
  reminderSubtitle: {
    fontSize: 12,
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
  },
  demoContainer: {
    alignItems: 'center',
    paddingVertical: 16,
    marginBottom: 24,
  },
  demoButton: {
    paddingVertical: 12,
    paddingHorizontal: 24,
  },
  demoButtonText: {
    fontSize: 12,
    color: '#ffffff',
    opacity: 0.4,
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
});