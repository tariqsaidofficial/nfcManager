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

// Light Theme Color Scheme
private val NothingLightColorScheme = lightColorScheme(
    // Primary colors
    primary = NothingColors.NothingRed,
    onPrimary = NothingColors.PureWhite,
    primaryContainer = NothingColors.NothingRedLight,
    onPrimaryContainer = NothingColors.PureWhite,

    // Secondary colors
    secondary = NothingColors.DarkGray,
    onSecondary = NothingColors.PureWhite,
    secondaryContainer = NothingColors.MediumGray,
    onSecondaryContainer = NothingColors.DarkGray,

    // Tertiary colors
    tertiary = NothingColors.CharcoalGray,
    onTertiary = NothingColors.PureWhite,
    tertiaryContainer = NothingColors.LightGray,
    onTertiaryContainer = NothingColors.CharcoalGray,

    // Error colors
    error = NothingColors.Error,
    onError = NothingColors.PureWhite,
    errorContainer = NothingColors.NothingRedLight,
    onErrorContainer = NothingColors.PureWhite,

    // Background colors
    background = NothingColors.PureWhite,
    onBackground = NothingColors.LightOnSurface,

    // Surface colors
    surface = NothingColors.LightSurface,
    onSurface = NothingColors.LightOnSurface,
    surfaceVariant = NothingColors.LightSurfaceVariant,
    onSurfaceVariant = NothingColors.LightOnSurfaceVariant,
    surfaceTint = NothingColors.NothingRed,

    // Inverse colors
    inverseSurface = NothingColors.CharcoalGray,
    inverseOnSurface = NothingColors.PureWhite,
    inversePrimary = NothingColors.NothingRedLight,

    // Outline colors
    outline = NothingColors.MediumGray,
    outlineVariant = NothingColors.LightGray,

    // Surface container colors
    surfaceContainer = NothingColors.LightSurface,
    surfaceContainerHigh = NothingColors.LightSurfaceVariant,
    surfaceContainerHighest = NothingColors.MediumGray,
    surfaceContainerLow = NothingColors.PureWhite,
    surfaceContainerLowest = NothingColors.PureWhite,

    // Scrim
    scrim = NothingColors.BlackOverlay
)

// Dark Theme Color Scheme
private val NothingDarkColorScheme = darkColorScheme(
    // Primary colors
    primary = NothingColors.NothingRed,
    onPrimary = NothingColors.PureWhite,
    primaryContainer = NothingColors.NothingRedDark,
    onPrimaryContainer = NothingColors.PureWhite,

    // Secondary colors
    secondary = NothingColors.MediumGray,
    onSecondary = NothingColors.PureBlack,
    secondaryContainer = NothingColors.DarkGray,
    onSecondaryContainer = NothingColors.PureWhite,

    // Tertiary colors
    tertiary = NothingColors.LightGray,
    onTertiary = NothingColors.PureBlack,
    tertiaryContainer = NothingColors.CharcoalGray,
    onTertiaryContainer = NothingColors.LightGray,

    // Error colors
    error = NothingColors.NothingRedLight,
    onError = NothingColors.PureWhite,
    errorContainer = NothingColors.NothingRedDark,
    onErrorContainer = NothingColors.PureWhite,

    // Background colors
    background = NothingColors.PureBlack,
    onBackground = NothingColors.DarkOnSurface,

    // Surface colors
    surface = NothingColors.DarkSurface,
    onSurface = NothingColors.DarkOnSurface,
    surfaceVariant = NothingColors.DarkSurfaceVariant,
    onSurfaceVariant = NothingColors.DarkOnSurfaceVariant,
    surfaceTint = NothingColors.NothingRed,

    // Inverse colors
    inverseSurface = NothingColors.LightGray,
    inverseOnSurface = NothingColors.PureBlack,
    inversePrimary = NothingColors.NothingRedDark,

    // Outline colors
    outline = NothingColors.DarkGray,
    outlineVariant = NothingColors.CharcoalGray,

    // Surface container colors
    surfaceContainer = NothingColors.DarkSurface,
    surfaceContainerHigh = NothingColors.DarkSurfaceVariant,
    surfaceContainerHighest = NothingColors.DarkGray,
    surfaceContainerLow = NothingColors.PureBlack,
    surfaceContainerLowest = NothingColors.PureBlack,

    // Scrim
    scrim = NothingColors.BlackOverlay
)

// Theme Implementation
@Composable
fun NothingOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Nothing OS uses fixed brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NothingDarkColorScheme
        else -> NothingLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NothingTypography, // Corrected to use NothingTypography
        content = content
    )
}

// Additional utility colors for Nothing OS specific elements
object NothingUIColors {
    // Dot Matrix Pattern Colors
    val DotPatternLight = Color(0xFF9E9E9E)
    val DotPatternDark = Color(0xFF616161)

    // Widget specific colors
    val WidgetBackgroundLight = Color(0xFFF8F8F8)
    val WidgetBackgroundDark = Color(0xFF1E1E1E)

    // Quick Settings Toggle Colors
    val ToggleActiveLight = NothingColors.NothingRed
    val ToggleActiveDark = NothingColors.NothingRed
    val ToggleInactiveLight = Color(0xFFE0E0E0)
    val ToggleInactiveDark = Color(0xFF424242)

    // Status Bar Colors
    val StatusBarLight = Color(0xFFFAFAFA)
    val StatusBarDark = Color(0xFF1A1A1A)
}