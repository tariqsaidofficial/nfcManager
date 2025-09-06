package com.nothingos.nfcmanager.ui.theme

import androidx.compose.ui.graphics.Color

// Nothing OS Brand Colors
object NothingColors {
    // Primary Red (Nothing's signature red)
    val NothingRed = Color(0xFFE53E3E)
    val NothingRedDark = Color(0xFFD32F2F)
    val NothingRedLight = Color(0xFFFF5252)

    // Grayscale System & Core Blacks/Whites
    val PureWhite = Color(0xFFFFFFFF)       // Primary Text
    val PureBlack = Color(0xFF000000)       // DeepBlack, Main Background
    val LightGray = Color(0xFFF5F5F5)
    val MediumGray = Color(0xFFE0E0E0)
    val DarkGray = Color(0xFF424242)
    val CharcoalGray = Color(0xFF2E2E2E)

    // Specific Surface/UI Element Colors
    val DarkSurface = Color(0xFF1A1A1A)       // Matches your code definition, described as Quick settings panels #0A0A0A (using 1A1A1A)
    val DarkElevated = Color(0xFF1C1C1C)     // For Cards and elevated surfaces
    val DarkSurfaceVariant = Color(0xFF2A2A2A) // Also used for DarkBorder and Inactive Toggles
    val LightSurface = Color(0xFFFAFAFA)
    val LightSurfaceVariant = Color(0xFFF0F0F0)

    // Specific Text Colors
    val LightOnSurface = Color(0xFF1A1A1A)    // For light theme text on surface
    val DarkOnSurface = Color(0xFFE5E5E5)     // For dark theme text on surface (general)
    val TextSecondary = Color(0xFFB3B3B3)     // Secondary text (light gray)
    val TextDisabled = Color(0xFF666666)      // Disabled text (dark gray)
    val LightOnSurfaceVariant = Color(0xFF757575)
    val DarkOnSurfaceVariant = Color(0xFFBDBDBD)

    // System Colors
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFE53E3E) // Using Nothing Red for errors

    // Transparent overlays
    val BlackOverlay = Color(0x80000000)
    val WhiteOverlay = Color(0x80FFFFFF)
}