import React from 'react';
import { TouchableOpacity, View, StyleSheet } from 'react-native';
import Animated, { useSharedValue, useAnimatedStyle, withTiming } from 'react-native-reanimated';

interface NothingToggleProps {
  isEnabled: boolean;
  onToggle: () => void;
}

export const NothingToggle: React.FC<NothingToggleProps> = ({ isEnabled, onToggle }) => {
  const translateX = useSharedValue(isEnabled ? 24 : 4);

  React.useEffect(() => {
    translateX.value = withTiming(isEnabled ? 24 : 4, { duration: 300 });
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
    width: 44,
    height: 24,
    borderRadius: 12,
    justifyContent: 'center',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 3,
  },
  knob: {
    position: 'absolute',
    width: 16,
    height: 16,
    backgroundColor: '#ffffff',
    borderRadius: 8,
  },
});