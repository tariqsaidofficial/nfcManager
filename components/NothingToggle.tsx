import React from 'react';
import { TouchableOpacity, View, StyleSheet } from 'react-native';
import Animated, { useSharedValue, useAnimatedStyle, withTiming } from 'react-native-reanimated';

interface NothingToggleProps {
  isEnabled: boolean;
  onToggle: () => void;
}

export const NothingToggle: React.FC<NothingToggleProps> = ({ isEnabled, onToggle }) => {
  const translateX = useSharedValue(isEnabled ? 52 : 4);

  React.useEffect(() => {
    translateX.value = withTiming(isEnabled ? 52 : 4, { duration: 300 });
  }, [isEnabled, translateX]);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ translateX: translateX.value }],
  }));

  return (
    <TouchableOpacity
      onPress={onToggle}
      style={[
        styles.container,
        {
          backgroundColor: isEnabled ? '#ef4444' : '#1f2937',
          shadowColor: isEnabled ? '#ef4444' : 'transparent',
        },
      ]}
    >
      <Animated.View style={[styles.knob, animatedStyle]} />
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  container: {
    width: 96,
    height: 48,
    borderRadius: 24,
    justifyContent: 'center',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.3,
    shadowRadius: 10,
    elevation: 5,
  },
  knob: {
    position: 'absolute',
    width: 40,
    height: 40,
    backgroundColor: '#ffffff',
    borderRadius: 20,
  },
});