package com.ytdlp.downloader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Color schemes ─────────────────────────────────────────────────────────────

private val LegendDarkColors = darkColorScheme(
    primary            = Violet400,
    onPrimary          = Dark950,
    primaryContainer   = Violet700,
    onPrimaryContainer = Violet200,

    secondary          = Dark200,
    onSecondary        = Dark950,
    secondaryContainer = Dark700,
    onSecondaryContainer = Violet200,

    tertiary           = Success,
    onTertiary         = Dark950,

    background         = Dark900,
    onBackground       = Violet100,

    surface            = Dark800,
    onSurface          = Violet100,
    surfaceVariant     = Dark700,
    onSurfaceVariant   = Dark200,

    outline            = Dark600,
    outlineVariant     = Dark700,

    error              = ErrorRed,
    onError            = Dark950,
    errorContainer     = ErrorDim,
    onErrorContainer   = ErrorRed,
)

private val LegendLightColors = lightColorScheme(
    primary            = Violet600,
    onPrimary          = Light50,
    primaryContainer   = Violet200,
    onPrimaryContainer = Violet700,

    secondary          = Light600,
    onSecondary        = Light50,
    secondaryContainer = Light200,
    onSecondaryContainer = Violet700,

    tertiary           = Success,
    onTertiary         = Light50,

    background         = Light50,
    onBackground       = Dark900,

    surface            = Light100,
    onSurface          = Dark900,
    surfaceVariant     = Light200,
    onSurfaceVariant   = Light600,

    outline            = Light200,
    outlineVariant     = Light200,

    error              = ErrorRed,
    onError            = Light50,
    errorContainer     = androidx.compose.ui.graphics.Color(0xFFFFE4E4),
    onErrorContainer   = ErrorRed,
)

// ── Typography ────────────────────────────────────────────────────────────────

private val LegendTypography = Typography(
    // App title / hero
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    // Section headings
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    // Card titles
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 13.sp,
        lineHeight = 18.sp
    ),
    // Body
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    // Labels / caps
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
)

// ── Theme entry point ─────────────────────────────────────────────────────────

@Composable
fun YtDownloaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We own the palette — no dynamic color so the brand identity is consistent
    val colorScheme = if (darkTheme) LegendDarkColors else LegendLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = LegendTypography,
        content     = content
    )
}
