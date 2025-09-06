import { useFonts } from 'expo-font';

export const useNothingFonts = () => {
  const [fontsLoaded] = useFonts({
    'NothingFont': require('../components/nothing-font-5x7.otf/nothing-font-5x7.otf'),
  });

  return fontsLoaded;
};
