import React from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { BlurView } from 'expo-blur';
import {
  History,
  Bell,
  Shield,
  Power,
  Settings,
  Nfc,
} from 'lucide-react-native';
import { useNFCManager } from '../../hooks/useNFCManager';
import { useNothingFonts } from '../../hooks/useFonts';

const IconComponent = ({ name, ...props }) => {
  const icons = { Power, Bell, Shield, Settings, History, Nfc };
  const Icon = icons[name] || History;
  return <Icon {...props} />;
};

const ActivityItem = ({ item, formatRelativeTime }) => (
  <BlurView intensity={10} style={styles.activityItem}>
    <LinearGradient colors={['#11111120', '#1f293720']} style={styles.activityGradient}>
      <View style={styles.activityContent}>
        <View style={styles.activityLeft}>
          <View style={styles.activityIconContainer}>
            <IconComponent name={item.icon} size={16} color="#ffffff" />
          </View>
          <Text style={styles.activityMessage}>{item.message}</Text>
        </View>
        <Text style={styles.activityTime}>
          {formatRelativeTime(item.timestamp)}
        </Text>
      </View>
    </LinearGradient>
  </BlurView>
);

export default function ActivityScreen() {
  const fontsLoaded = useNothingFonts();
  const { activityLog, formatRelativeTime } = useNFCManager();

  if (!fontsLoaded) {
    return null;
  }

  const renderActivityItem = ({ item }) => (
    <ActivityItem item={item} formatRelativeTime={formatRelativeTime} />
  );

  return (
    <View style={styles.container}>
      <SafeAreaView style={styles.header}>
        <Text style={styles.headerTitle}>ACTIVITY LOG</Text>
        <Text style={styles.headerSubtitle}>
          {activityLog.length} recent events
        </Text>
      </SafeAreaView>

      <View style={styles.content}>
        {activityLog.length > 0 ? (
          <FlatList
            data={activityLog}
            renderItem={renderActivityItem}
            keyExtractor={(item) => item.id.toString()}
            showsVerticalScrollIndicator={false}
            contentContainerStyle={styles.listContainer}
            ItemSeparatorComponent={() => <View style={styles.separator} />}
          />
        ) : (
          <View style={styles.emptyContainer}>
            <BlurView intensity={20} style={styles.emptyCard}>
              <LinearGradient colors={['#11111150', '#1f293750']} style={styles.emptyGradient}>
                <History size={48} color="#6b7280" />
                <Text style={styles.emptyTitle}>No Activity Yet</Text>
                <Text style={styles.emptySubtitle}>
                  Activity logs will appear here when you interact with NFC features.
                </Text>
              </LinearGradient>
            </BlurView>
          </View>
        )}
      </View>
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
  headerSubtitle: {
    fontSize: 14,
    color: '#ffffff',
    opacity: 0.7,
    marginTop: 4,
  },
  content: {
    flex: 1,
    paddingHorizontal: 24,
  },
  listContainer: {
    paddingBottom: 24,
  },
  activityItem: {
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#1f293730',
    overflow: 'hidden',
  },
  activityGradient: {
    padding: 16,
  },
  activityContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  activityLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  activityIconContainer: {
    width: 32,
    height: 32,
    borderRadius: 8,
    backgroundColor: '#1f293750',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  activityMessage: {
    fontSize: 14,
    color: '#ffffff',
    opacity: 0.8,
    flex: 1,
  },
  activityTime: {
    fontSize: 12,
    color: '#ffffff',
    opacity: 0.5,
  },
  separator: {
    height: 12,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyCard: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#1f2937',
    overflow: 'hidden',
    width: '100%',
    maxWidth: 300,
  },
  emptyGradient: {
    padding: 32,
    alignItems: 'center',
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    fontFamily: 'NothingFont',
    color: '#ffffff',
    marginTop: 16,
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 14,
    color: '#ffffff',
    opacity: 0.6,
    textAlign: 'center',
    lineHeight: 20,
  },
});