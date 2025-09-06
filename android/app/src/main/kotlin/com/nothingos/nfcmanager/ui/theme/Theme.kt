package com.nothingos.nfcmanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Light Theme Color Scheme (as provided by you)
private val NothingLightColorScheme = lightColorScheme(
    primary = NothingColors.NothingRed,
    onPrimary = NothingColors.PureWhite,
    primaryContainer = NothingColors.NothingRedLight,
    onPrimaryContainer = NothingColors.PureWhite,
    secondary = NothingColors.DarkGray,
    onSecondary = NothingColors.PureWhite,
    secondaryContainer = NothingColors.MediumGray,
    onSecondaryContainer = NothingColors.DarkGray,
    tertiary = NothingColors.CharcoalGray,
    onTertiary = NothingColors.PureWhite,
    tertiaryContainer = NothingColors.LightGray,
    onTertiaryContainer = NothingColors.CharcoalGray,
    error = NothingColors.Error,
    onError = NothingColors.PureWhite,
    errorContainer = NothingColors.NothingRedLight,
    onErrorContainer = NothingColors.PureWhite,
    background = NothingColors.PureWhite,
    onBackground = NothingColors.LightOnSurface,
    surface = NothingColors.LightSurface,
    onSurface = NothingColors.LightOnSurface,
    surfaceVariant = NothingColors.LightSurfaceVariant,
    onSurfaceVariant = NothingColors.LightOnSurfaceVariant,
    surfaceTint = NothingColors.NothingRed,
    inverseSurface = NothingColors.CharcoalGray,
    inverseOnSurface = NothingColors.PureWhite,
    inversePrimary = NothingColors.NothingRedLight,
    outline = NothingColors.MediumGray,
    outlineVariant = NothingColors.LightGray,
    surfaceContainer = NothingColors.LightSurface,
    surfaceContainerHigh = NothingColors.LightSurfaceVariant,
    surfaceContainerHighest = NothingColors.MediumGray,
    surfaceContainerLow = NothingColors.PureWhite,
    surfaceContainerLowest = NothingColors.PureWhite,
    scrim = NothingColors.BlackOverlay
)

// Dark Theme Color Scheme (updated based on your detailed instructions)
private val NothingDarkColorScheme = darkColorScheme(
    primary = NothingColors.NothingRed,
    onPrimary = NothingColors.PureWhite, // Primary Text
    primaryContainer = NothingColors.NothingRedDark,
    onPrimaryContainer = NothingColors.PureWhite,
    secondary = NothingColors.MediumGray, // Or a more specific secondary like TextSecondary if used as onSecondary for some components
    onSecondary = NothingColors.PureBlack,
    secondaryContainer = NothingColors.DarkGray,
    onSecondaryContainer = NothingColors.PureWhite,
    tertiary = NothingColors.LightGray,
    onTertiary = NothingColors.PureBlack,
    tertiaryContainer = NothingColors.CharcoalGray,
    onTertiaryContainer = NothingColors.LightGray,
    error = NothingColors.NothingRedLight, // Error color is light red in dark theme as per your spec
    onError = NothingColors.PureWhite,
    errorContainer = NothingColors.NothingRedDark,
    onErrorContainer = NothingColors.PureWhite,
    background = NothingColors.PureBlack,      // DeepBlack (#000000) - Main background
    onBackground = NothingColors.PureWhite,    // Primary Text on background
    surface = NothingColors.DarkSurface,        // DarkSurface (#0A0A0A or #1A1A1A as per your Color.kt) - used for Quick settings panels in description
    onSurface = NothingColors.PureWhite,       // Primary Text on surface
    surfaceVariant = NothingColors.DarkSurfaceVariant, // #2A2A2A - Borders and inactive toggles, also general surface variant
    onSurfaceVariant = NothingColors.TextSecondary,    // Secondary text on surface variants
    surfaceTint = NothingColors.NothingRed,
    inverseSurface = NothingColors.LightGray,
    inverseOnSurface = NothingColors.PureBlack,
    inversePrimary = NothingColors.NothingRedDark,
    outline = NothingColors.DarkGray,           // General outline
    outlineVariant = NothingColors.CharcoalGray, // DarkBorder (#2A2A2A) -> this is DarkSurfaceVariant
    surfaceContainer = NothingColors.DarkSurface, // Base for containers
    surfaceContainerHigh = NothingColors.DarkElevated, // DarkElevated (#1C1C1C) - Cards and elevated surfaces
    surfaceContainerHighest = NothingColors.DarkGray, // A bit lighter than DarkElevated
    surfaceContainerLow = NothingColors.DarkSurface, // Slightly above PureBlack
    surfaceContainerLowest = NothingColors.PureBlack, // Absolute lowest container, same as background
    scrim = NothingColors.BlackOverlay
)

// Theme Implementation
@Composable
fun NothingOSTheme(
    darkTheme: Boolean = true, // Dark Mode as Default
    dynamicColor: Boolean = false, // Nothing OS uses fixed brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // Dynamic color is false by default, but if ever enabled:
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NothingDarkColorScheme
        else -> NothingLightColorScheme // Retain light theme option if darkTheme is explicitly set to false
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NothingTypography, // Using the Typography from Typography.kt
        content = content
    )
}

// Additional utility colors for Nothing OS specific elements
object NothingUIColors {
    // Quick Settings Toggle Colors (matching screenshots)
    val ToggleActiveBg = NothingColors.NothingRed
    val ToggleActiveText = NothingColors.PureWhite // Was PrimaryText
    val ToggleInactiveBg = NothingColors.DarkSurfaceVariant // Color(0xFF2A2A2A)
    val ToggleInactiveText = NothingColors.TextSecondary // Was SecondaryText

    // Widget & Card Backgrounds
    val QuickSettingsBg = NothingColors.DarkSurface
    val LockScreenWidgetBg = Color(0x60000000) // Semi-transparent black
    val CardBg = NothingColors.DarkElevated

    // Dot Matrix Pattern Colors
    val DotPatternActive = NothingColors.PureWhite // Was PrimaryText
    val DotPatternInactive = Color(0xFF333333)

    // Status Bar & Navigation
    val StatusBarBg = NothingColors.PureBlack // Was DeepBlack
    val NavBarBg = NothingColors.PureBlack     // Was DeepBlack

    // Button States
    val ButtonPressed = Color(0xFF1A1A1A) // This is NothingColors.DarkSurface
    val ButtonDisabled = Color(0xFF333333)

    // Slider Colors
    val SliderActive = NothingColors.NothingRed
    val SliderInactive = Color(0xFF404040)
    val SliderThumb = NothingColors.PureWhite // Was PrimaryText

    // Dividers & Borders
    val DividerColor = NothingColors.DarkSurfaceVariant // Color(0xFF2A2A2A)
    val BorderColor = Color(0xFF404040) // This is DarkGray

    // Special Elements
    val ClockText = NothingColors.PureWhite // Was PrimaryText
    val DateText = NothingColors.TextSecondary
    val WeatherText = NothingColors.TextSecondary

    // Media Player Colors
    val MediaBg = NothingColors.DarkSurface // Color(0xFF1A1A1A)
    val MediaProgress = NothingColors.NothingRed
    val MediaControls = NothingColors.PureWhite // Was PrimaryText
}
