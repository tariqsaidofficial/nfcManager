import React from 'react';
import { View, ViewProps } from 'react-native';

interface AccessibilityWrapperProps extends ViewProps {
  children: React.ReactNode;
  label?: string;
  hint?: string;
  role?: 'button' | 'switch' | 'text' | 'image';
  state?: {
    disabled?: boolean;
    selected?: boolean;
    checked?: boolean;
  };
}

export const AccessibilityWrapper: React.FC<AccessibilityWrapperProps> = ({
  children,
  label,
  hint,
  role,
  state,
  ...props
}) => {
  return (
    <View
      accessible={true}
      accessibilityLabel={label}
      accessibilityHint={hint}
      accessibilityRole={role}
      accessibilityState={state}
      {...props}
    >
      {children}
    </View>
  );
};