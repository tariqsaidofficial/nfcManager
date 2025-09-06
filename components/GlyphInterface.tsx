import React from 'react';
import { View } from 'react-native';
import Svg, { Path } from 'react-native-svg';
import Animated, { useSharedValue, useAnimatedProps, withTiming, withDelay } from 'react-native-reanimated';

const AnimatedPath = Animated.createAnimatedComponent(Path);

interface GlyphInterfaceProps {
  isActive: boolean;
}

export const GlyphInterface: React.FC<GlyphInterfaceProps> = ({ isActive }) => {
  const strokeDashoffset1 = useSharedValue(1000);
  const strokeDashoffset2 = useSharedValue(1000);
  const strokeDashoffset3 = useSharedValue(1000);
  const strokeDashoffset4 = useSharedValue(1000);

  React.useEffect(() => {
    if (isActive) {
      strokeDashoffset1.value = withTiming(0, { duration: 1500 });
      strokeDashoffset2.value = withDelay(200, withTiming(0, { duration: 1500 }));
      strokeDashoffset3.value = withDelay(400, withTiming(0, { duration: 1500 }));
      strokeDashoffset4.value = withDelay(600, withTiming(0, { duration: 1500 }));
    } else {
      strokeDashoffset1.value = withTiming(1000, { duration: 1000 });
      strokeDashoffset2.value = withTiming(1000, { duration: 1000 });
      strokeDashoffset3.value = withTiming(1000, { duration: 1000 });
      strokeDashoffset4.value = withTiming(1000, { duration: 1000 });
    }
  }, [isActive]);

  const animatedProps1 = useAnimatedProps(() => ({
    strokeDashoffset: strokeDashoffset1.value,
  }));

  const animatedProps2 = useAnimatedProps(() => ({
    strokeDashoffset: strokeDashoffset2.value,
  }));

  const animatedProps3 = useAnimatedProps(() => ({
    strokeDashoffset: strokeDashoffset3.value,
  }));

  const animatedProps4 = useAnimatedProps(() => ({
    strokeDashoffset: strokeDashoffset4.value,
  }));

  return (
    <View>
      <Svg width={96} height={96} viewBox="0 0 100 100">
        <AnimatedPath
          d="M 85,50 A 35,35 0 0 1 50,85 A 35,35 0 0 1 15,50 A 35,35 0 0 1 50,15"
          fill="none"
          stroke={isActive ? '#ef4444' : '#374151'}
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray="1000"
          animatedProps={animatedProps1}
        />
        <AnimatedPath
          d="M 65,22 L 75,32"
          fill="none"
          stroke={isActive ? '#ef4444' : '#374151'}
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray="1000"
          animatedProps={animatedProps2}
        />
        <AnimatedPath
          d="M 50,50 L 50,50.1"
          fill="none"
          stroke={isActive ? '#ef4444' : '#374151'}
          strokeWidth="8"
          strokeLinecap="round"
          strokeDasharray="1000"
          animatedProps={animatedProps3}
        />
        <AnimatedPath
          d="M 50,65 L 50,75"
          fill="none"
          stroke={isActive ? '#ef4444' : '#374151'}
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray="1000"
          animatedProps={animatedProps4}
        />
      </Svg>
    </View>
  );
};