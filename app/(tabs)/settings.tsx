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
  Clock,
  Battery,
  Volume2,
  Vibrate,
} from 'lucide-react-native';
import { useNFCManager } from '../../hooks/useNFCManager';
import { useNothingFonts } from '../../hooks/useFonts';
import { performanceMonitor } from '../../utils/performanceMonitor';

interface SettingsItemProps {
  icon: any;
  title: string;
  subtitle?: string;
  onPress: () => void;
  showArrow?: boolean;
}

const SettingsItem: React.FC<SettingsItemProps> = ({ icon: Icon, title, subtitle, onPress, showArrow = true }) => (
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
  const fontsLoaded = useNothingFonts();
  const { nfcStatus, addLogEntry, getPerformanceStats } = useNFCManager();

  if (!fontsLoaded) {
    return null;
  }

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
      'NFC Manager v1.0\n\nA Nothing OS inspired NFC management interface for privacy protection.',
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

  const openAdvancedSettings = () => {
    Alert.alert(
      'Advanced Settings',
      'Choose advanced configuration options:',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Alert Sensitivity', onPress: openSensitivitySettings },
        { text: 'Battery Optimization', onPress: openBatterySettings },
        { text: 'Notification Customization', onPress: openNotificationSettings },
      ]
    );
  };

  const openSensitivitySettings = () => {
    Alert.alert(
      'Alert Sensitivity',
      'Configure how sensitive the NFC monitoring should be:\n\n• High: More frequent checks\n• Medium: Balanced performance\n• Low: Battery optimized',
      [
        { text: 'High Sensitivity', onPress: () => {} },
        { text: 'Medium (Default)', onPress: () => {} },
        { text: 'Low Sensitivity', onPress: () => {} },
      ]
    );
  };

  const openBatterySettings = () => {
    Alert.alert(
      'Battery Optimization',
      'Smart battery features:\n\n• Adaptive monitoring based on usage\n• Sleep mode during inactive hours\n• Background optimization',
      [
        { text: 'Enable Smart Mode', onPress: () => addLogEntry('Smart battery mode enabled.', 'Battery') },
        { text: 'Optimize Now', onPress: () => addLogEntry('Battery optimization applied.', 'Battery') },
        { text: 'Cancel', style: 'cancel' },
      ]
    );
  };

  const openNotificationSettings = () => {
    Alert.alert(
      'Notification Customization',
      'Customize privacy alerts:\n\n• Sound: Default, Silent, Custom\n• Vibration: Off, Light, Strong\n• Alert Text: Standard, Detailed',
      [
        { text: 'Sound Settings', onPress: () => {} },
        { text: 'Vibration Settings', onPress: () => {} },
        { text: 'Text Settings', onPress: () => {} },
      ]
    );
  };

  const showPerformanceStats = () => {
    const stats = getPerformanceStats();
    const uptime = Math.round(stats.uptime / 1000 / 60); // minutes
    
    Alert.alert(
      'Performance Statistics',
      `🚀 App Performance:\n\n` +
      `⏱️ Uptime: ${uptime} minutes\n` +
      `📊 Memory Usage: ${stats.avgMemoryUsage}MB avg\n` +
      `⚡ CPU Usage: ${stats.avgCPUUsage}% avg\n` +
      `📡 NFC Checks: ${stats.totalNFCChecks}\n` +
      `🔧 Optimizations: ${stats.optimizationCount}`,
      [
        { text: 'Reset Stats', onPress: () => {
          performanceMonitor.reset();
          addLogEntry('Performance statistics reset.', 'Settings');
        }},
        { text: 'OK' }
      ]
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
              
              <SettingsItem
                icon={<Clock size={20} color="#fbbf24" />}
                title="Advanced Settings"
                subtitle="Sensitivity, battery, and notifications"
                onPress={openAdvancedSettings}
                accessibilityLabel="Advanced configuration settings"
                accessibilityHint="Configure advanced monitoring and notification options"
              />
              
              <View style={styles.separator} />
              
              <SettingsItem
                icon={<Battery size={20} color="#10b981" />}
                title="Battery Optimization"
                subtitle="Smart power management"
                onPress={openBatterySettings}
                accessibilityLabel="Battery optimization settings"
                accessibilityHint="Configure smart battery features"
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
              
              <View style={styles.separator} />
              
              <SettingsItem
                icon={<Settings size={20} color="#8b5cf6" />}
                title="Performance Stats"
                subtitle="View app performance metrics"
                onPress={showPerformanceStats}
                accessibilityLabel="Performance statistics"
                accessibilityHint="View detailed performance and usage statistics"
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
    fontFamily: 'NothingFont',
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
    fontFamily: 'NothingFont',
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
    fontFamily: 'NothingFont',
    color: '#ffffff',
  },
  settingsItemSubtitle: {
    fontSize: 12,
    fontFamily: 'NothingFont',
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
    fontFamily: 'NothingFont',
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