package com.nothingos.nfcmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Nothing OS Theme for NFC Manager
 * Follows official Material Design 3 patterns with Nothing OS aesthetics
 */

// Dark Color Scheme (Primary theme for Nothing OS)
private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = NothingRed,
    onPrimary = NothingWhite,
    primaryContainer = Color(0xFF2D1B1B),
    onPrimaryContainer = Color(0xFFFFDADA),
    
    // Secondary colors
    secondary = Gray400,
    onSecondary = NothingBlack,
    secondaryContainer = Gray800,
    onSecondaryContainer = Gray200,
    
    // Tertiary colors
    tertiary = InfoBlue,
    onTertiary = NothingWhite,
    tertiaryContainer = Color(0xFF1E3A8A),
    onTertiaryContainer = Color(0xFFDEEAFF),
    
    // Error colors
    error = ErrorRed,
    onError = NothingWhite,
    errorContainer = Color(0xFF2D1B1B),
    onErrorContainer = Color(0xFFFFDADA),
    
    // Background colors
    background = NothingBlack,
    onBackground = NothingWhite,
    
    // Surface colors
    surface = NothingSurface,
    onSurface = NothingWhite,
    surfaceVariant = Gray900,
    onSurfaceVariant = Gray300,
    
    // Outline colors
    outline = NothingBorder,
    outlineVariant = Gray800,
    
    // Container colors
    surfaceContainer = Gray950,
    surfaceContainerHigh = Gray900,
    surfaceContainerHighest = Gray800,
    surfaceContainerLow = Gray950,
    surfaceContainerLowest = NothingBlack,
    
    // Inverse colors
    inverseSurface = Gray100,
    inverseOnSurface = Gray900,
    inversePrimary = NothingRed,
    
    // Scrim
    scrim = BlackOverlay50
)

// Light Color Scheme (Optional, Nothing OS is primarily dark)
private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = NothingRed,
    onPrimary = NothingWhite,
    primaryContainer = Color(0xFFFFDADA),
    onPrimaryContainer = Color(0xFF2D1B1B),
    
    // Secondary colors
    secondary = Gray600,
    onSecondary = NothingWhite,
    secondaryContainer = Gray200,
    onSecondaryContainer = Gray800,
    
    // Tertiary colors
    tertiary = InfoBlue,
    onTertiary = NothingWhite,
    tertiaryContainer = Color(0xFFDEEAFF),
    onTertiaryContainer = Color(0xFF1E3A8A),
    
    // Error colors
    error = ErrorRed,
    onError = NothingWhite,
    errorContainer = Color(0xFFFFDADA),
    onErrorContainer = Color(0xFF2D1B1B),
    
    // Background colors
    background = NothingWhite,
    onBackground = NothingBlack,
    
    // Surface colors
    surface = Gray50,
    onSurface = NothingBlack,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    
    // Outline colors
    outline = Gray300,
    outlineVariant = Gray200,
    
    // Container colors
    surfaceContainer = Gray100,
    surfaceContainerHigh = Gray200,
    surfaceContainerHighest = Gray300,
    surfaceContainerLow = Gray50,
    surfaceContainerLowest = NothingWhite,
    
    // Inverse colors
    inverseSurface = Gray900,
    inverseOnSurface = Gray100,
    inversePrimary = NothingRed,
    
    // Scrim
    scrim = BlackOverlay50
)

// Custom Nothing OS color extensions
data class NothingColors(
    val nfcActive: Color,
    val nfcInactive: Color,
    val nfcWarning: Color,
    val nfcSuccess: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val overlay10: Color,
    val overlay20: Color,
    val overlay50: Color
)

// Dark theme Nothing colors
private val DarkNothingColors = NothingColors(
    nfcActive = NFCActive,
    nfcInactive = NFCInactive,
    nfcWarning = NFCWarning,
    nfcSuccess = NFCSuccess,
    gradientStart = GradientStart,
    gradientEnd = GradientEnd,
    overlay10 = BlackOverlay10,
    overlay20 = BlackOverlay20,
    overlay50 = BlackOverlay50
)

// Light theme Nothing colors
private val LightNothingColors = NothingColors(
    nfcActive = NFCActive,
    nfcInactive = NFCInactive,
    nfcWarning = NFCWarning,
    nfcSuccess = NFCSuccess,
    gradientStart = Gray100,
    gradientEnd = Gray300,
    overlay10 = WhiteOverlay10,
    overlay20 = WhiteOverlay20,
    overlay50 = BlackOverlay50
)

// CompositionLocal for Nothing colors
val LocalNothingColors = staticCompositionLocalOf { DarkNothingColors }

/**
 * Main Nothing OS Theme Composable
 */
@Composable
fun NFCManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Nothing OS doesn't use dynamic colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val nothingColors = when {
        darkTheme -> DarkNothingColors
        else -> LightNothingColors
    }

    CompositionLocalProvider(
        LocalNothingColors provides nothingColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NothingTypography,
            content = content
        )
    }
}

/**
 * Access Nothing OS specific colors
 */
object NothingTheme {
    val colors: NothingColors
        @Composable
        get() = LocalNothingColors.current
}

/**
 * Helper extensions for Material3 Theme
 */
val MaterialTheme.nothingColors: NothingColors
    @Composable
    get() = LocalNothingColors.current
