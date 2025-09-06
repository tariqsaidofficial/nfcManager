import { Link, Stack } from 'expo-router';
import { StyleSheet, Text, View } from 'react-native';
import { useNothingFonts } from '../hooks/useFonts';

export default function NotFoundScreen() {
  const fontsLoaded = useNothingFonts();

  if (!fontsLoaded) {
    return null;
  }

  return (
    <>
      <Stack.Screen options={{ title: 'NFC Manager - Not Found' }} />
      <View style={styles.container}>
        <Text style={styles.text}>This screen doesn't exist.</Text>
        <Link href="/(tabs)" style={styles.link}>
          <Text style={styles.linkText}>Go to NFC Manager!</Text>
        </Link>
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000000',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  text: {
    fontSize: 20,
    fontWeight: '600',
    fontFamily: 'NothingFont',
    color: '#ffffff',
    marginBottom: 20,
  },
  link: {
    backgroundColor: '#ef4444',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 12,
  },
  linkText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#ffffff',
  },
});
