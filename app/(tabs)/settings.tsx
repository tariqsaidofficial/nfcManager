import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  Alert,
  Linking,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { BlurView } from 'expo-blur';
import {
  Settings,
  Smartphone,
  Shield,
  Power,
  Info,
  ExternalLink,
  ChevronRight,
} from 'lucide-react-native';
import { useNFCManager } from '../../hooks/useNFCManager';

const SettingsItem = ({ icon: Icon, title, subtitle, onPress, showArrow = true }) => (
  <TouchableOpacity onPress={onPress} style={styles.settingsItem}>
    <View style={styles.settingsItemContent}>
      <View style={styles.settingsItemLeft}>
        <View style={styles.settingsIconContainer}>
          <Icon size={20} color="#ffffff" />
        </View>
        <View>
          <Text style={styles.settingsItemTitle}>{title}</Text>
          {subtitle && <Text style={styles.settingsItemSubtitle}>{subtitle}</Text>}
        </View>
      </View>
      {showArrow && <ChevronRight size={16} color="#6b7280" />}
    </View>
  </TouchableOpacity>
);

export default function SettingsScreen() {
  const { nfcStatus, addLogEntry } = useNFCManager();

  const openSystemNFCSettings = () => {
    Alert.alert(
      'Open NFC Settings',
      'This would open the system NFC settings in a real app.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Open Settings',
          onPress: () => addLogEntry('System NFC settings opened.', 'Settings'),
        },
      ]
    );
  };

  const openSecuritySettings = () => {
    Alert.alert(
      'Security Settings',
      'Configure advanced security options for NFC communications.',
      [{ text: 'OK' }]
    );
  };

  const showAppInfo = () => {
    Alert.alert(
      'App Information',
      'NFC Glyph Manager v1.0\n\nA Nothing OS inspired NFC management interface.',
      [{ text: 'OK' }]
    );
  };

  const openDocumentation = () => {
    Alert.alert(
      'Documentation',
      'This would open the app documentation in a real app.',
      [{ text: 'OK' }]
    );
  };

  return (
    <View style={styles.container}>
      <SafeAreaView style={styles.header}>
        <Text style={styles.headerTitle}>SETTINGS</Text>
      </SafeAreaView>

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        {/* NFC Settings Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>NFC CONFIGURATION</Text>
          
          <BlurView intensity={20} style={styles.card}>
            <LinearGradient colors={['#11111150', '#1f293750']} style={styles.cardGradient}>
              <SettingsItem
                icon={Smartphone}
                title="System NFC Settings"
                subtitle="Open Android NFC settings"
                onPress={openSystemNFCSettings}
              />
              
              <View style={styles.separator} />
              
              <SettingsItem
                icon={Shield}
                title="Security Options"
                subtitle="Advanced NFC security settings"
                onPress={openSecuritySettings}
              />
              
              <View style={styles.separator} />
              
              <View style={styles.statusRow}>
                <View style={styles.settingsItemContent}>
                  <View style={styles.settingsItemLeft}>
                    <View style={styles.settingsIconContainer}>
                      <Power size={20} color="#ffffff" />
                    </View>
                    <View>
                      <Text style={styles.settingsItemTitle}>Current Status</Text>
                      <Text style={styles.settingsItemSubtitle}>
                        NFC is currently {nfcStatus ? 'enabled' : 'disabled'}
                      </Text>
                    </View>
                  </View>
                  <View style={[
                    styles.statusIndicator,
                    { backgroundColor: nfcStatus ? '#10b981' : '#ef4444' }
                  ]} />
                </View>
              </View>
            </LinearGradient>
          </BlurView>
        </View>

        {/* App Information Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>INFORMATION</Text>
          
          <BlurView intensity={20} style={styles.card}>
            <LinearGradient colors={['#11111150', '#1f293750']} style={styles.cardGradient}>
              <SettingsItem
                icon={Info}
                title="App Information"
                subtitle="Version, build info, and credits"
                onPress={showAppInfo}
              />
              
              <View style={styles.separator} />
              
              <SettingsItem
                icon={ExternalLink}
                title="Documentation"
                subtitle="Learn more about NFC management"
                onPress={openDocumentation}
              />
            </LinearGradient>
          </BlurView>
        </View>

        {/* About Nothing OS */}
        <View style={styles.aboutContainer}>
          <Text style={styles.aboutTitle}>Nothing OS Inspired Design</Text>
          <Text style={styles.aboutText}>
            This interface draws inspiration from Nothing OS's unique design language,
            featuring clean lines, thoughtful animations, and minimal aesthetics.
          </Text>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000000',
  },
  header: {
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
  content: {
    flex: 1,
    paddingHorizontal: 24,
  },
  section: {
    marginBottom: 32,
  },
  sectionTitle: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#ffffff',
    opacity: 0.5,
    letterSpacing: 1,
    marginBottom: 16,
  },
  card: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#1f2937',
    overflow: 'hidden',
  },
  cardGradient: {
    padding: 0,
  },
  settingsItem: {
    paddingVertical: 16,
    paddingHorizontal: 20,
  },
  settingsItemContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  settingsItemLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  settingsIconContainer: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: '#1f293750',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 16,
  },
  settingsItemTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffffff',
  },
  settingsItemSubtitle: {
    fontSize: 12,
    color: '#ffffff',
    opacity: 0.6,
    marginTop: 2,
  },
  separator: {
    height: 1,
    backgroundColor: '#1f2937',
    marginLeft: 76,
  },
  statusRow: {
    paddingVertical: 16,
    paddingHorizontal: 20,
  },
  statusIndicator: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  aboutContainer: {
    padding: 24,
    marginBottom: 32,
  },
  aboutTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 12,
  },
  aboutText: {
    fontSize: 14,
    color: '#ffffff',
    opacity: 0.7,
    lineHeight: 20,
  },
});