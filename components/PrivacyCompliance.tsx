import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Linking } from 'react-native';
import { BlurView } from 'expo-blur';
import { LinearGradient } from 'expo-linear-gradient';
import { Shield, ExternalLink } from 'lucide-react-native';

interface PrivacyComplianceProps {
  onAccept: () => void;
  visible: boolean;
}

export const PrivacyCompliance: React.FC<PrivacyComplianceProps> = ({ onAccept, visible }) => {
  if (!visible) return null;

  const openPrivacyPolicy = () => {
    // In a real app, this would open your privacy policy URL
    Linking.openURL('https://your-app-domain.com/privacy');
  };

  return (
    <View style={styles.container}>
      <BlurView intensity={80} style={styles.backdrop}>
        <View style={styles.modal}>
          <LinearGradient colors={['#111111', '#1f2937']} style={styles.gradient}>
            <View style={styles.iconContainer}>
              <Shield size={32} color="#ef4444" />
            </View>
            
            <Text style={styles.title}>Privacy & Permissions</Text>
            <Text style={styles.description}>
              This app requires NFC permissions to demonstrate NFC management features. 
              No personal data is collected or transmitted.
            </Text>
            
            <View style={styles.features}>
              <Text style={styles.featureItem}>• Local data storage only</Text>
              <Text style={styles.featureItem}>• No analytics or tracking</Text>
              <Text style={styles.featureItem}>• No network communication</Text>
              <Text style={styles.featureItem}>• Open source design</Text>
            </View>
            
            <TouchableOpacity onPress={openPrivacyPolicy} style={styles.policyLink}>
              <Text style={styles.policyText}>Privacy Policy</Text>
              <ExternalLink size={12} color="#ef4444" />
            </TouchableOpacity>
            
            <TouchableOpacity onPress={onAccept} style={styles.acceptButton}>
              <Text style={styles.acceptText}>Accept & Continue</Text>
            </TouchableOpacity>
          </LinearGradient>
        </View>
      </BlurView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    zIndex: 1000,
  },
  backdrop: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  modal: {
    width: '100%',
    maxWidth: 340,
    borderRadius: 24,
    borderWidth: 2,
    borderColor: '#ef444450',
    overflow: 'hidden',
  },
  gradient: {
    padding: 32,
    alignItems: 'center',
  },
  iconContainer: {
    width: 64,
    height: 64,
    backgroundColor: '#ef444420',
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 24,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 16,
    textAlign: 'center',
  },
  description: {
    fontSize: 14,
    color: '#ffffff',
    opacity: 0.8,
    textAlign: 'center',
    lineHeight: 20,
    marginBottom: 24,
  },
  features: {
    alignSelf: 'stretch',
    marginBottom: 24,
  },
  featureItem: {
    fontSize: 12,
    color: '#ffffff',
    opacity: 0.7,
    marginBottom: 8,
  },
  policyLink: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 24,
    gap: 8,
  },
  policyText: {
    fontSize: 12,
    color: '#ef4444',
    textDecorationLine: 'underline',
  },
  acceptButton: {
    backgroundColor: '#ef4444',
    paddingVertical: 16,
    paddingHorizontal: 32,
    borderRadius: 12,
    alignSelf: 'stretch',
    alignItems: 'center',
  },
  acceptText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
  },
});