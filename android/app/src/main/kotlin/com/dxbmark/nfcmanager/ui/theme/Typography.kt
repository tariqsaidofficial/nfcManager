package com.dxbmark.nfcmanager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dxbmark.nfcmanager.R // Import your R class

/**
 * Nothing OS Typography System
 * Clean, minimal, and highly readable
 */

// Font Families
val NothingDotMatrixFontFamily = FontFamily(
    Font(R.font.nothing_font_5x7, FontWeight.Normal),
    Font(R.font.nothing_font_5x7, FontWeight.Bold) // Assuming the same font file for Bold, adjust if you have a separate bold variant
)

val MonospaceFontFamily = FontFamily.Monospace

// Custom Typography following Nothing OS design
val NothingTypography = Typography(
    // Display Styles (Using Dot Matrix for consistency, adjust if needed)
    displayLarge = TextStyle(
        fontFamily = NothingDotMatrixFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = NothingDotMatrixFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = NothingDotMatrixFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),

    // Headline Styles (Using Dot Matrix)
    headlineLarge = TextStyle(
        fontFamily = NothingDotMatrixFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = NothingDotMatrixFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = NothingDotMatrixFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    // Title Styles (Using a default sans-serif, adjust if Dot Matrix is preferred here too)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default, // Or NothingDotMatrixFontFamily
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default, // Or NothingDotMatrixFontFamily
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default, // Or NothingDotMatrixFontFamily
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // Body Styles (Using a default sans-serif for readability)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // Label Styles
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default, // Or NothingDotMatrixFontFamily
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default, // Or NothingDotMatrixFontFamily
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default, // Or NothingDotMatrixFontFamily
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// Custom Text Styles for Nothing OS specific needs
object NothingTextStyles {

    // Header styles with increased letter spacing (Using Dot Matrix)
    val HeaderTitle = TextStyle(
        fontFamily = NothingDotMatrixFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 2.sp, // Wide letter spacing for headers
    )

    // NFC Status display (Using Dot Matrix)
    val NFCStatusLarge = TextStyle(
        fontFamily = NothingDotMatrixFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-1).sp,
    )

    // Monospace for technical data
    val MonospaceBody = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    )

    // Button text (Using Dot Matrix or Default, depending on desired style)
    val ButtonText = TextStyle(
        fontFamily = NothingDotMatrixFontFamily, // Or FontFamily.Default
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.sp,
    )

    // Small caps for labels (Using Dot Matrix or Default)
    val SmallCaps = TextStyle(
        fontFamily = NothingDotMatrixFontFamily, // Or FontFamily.Default
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
    )

    // Subtitle (Using Default for readability)
    val Subtitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    )

    // Caption text (Using Default for readability)
    val Caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    )

    // Overline (small headers, using Dot Matrix or Default)
    val Overline = TextStyle(
        fontFamily = NothingDotMatrixFontFamily, // Or FontFamily.Default
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp,
    )
}
