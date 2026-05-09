package com.ytdlp.downloader.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ── Color schemes ─────────────────────────────────────────────────────────────

private val DarkScheme = darkColorScheme(
    primary              = Neon,
    onPrimary            = SpotWhite,
    primaryContainer     = NeonDim,
    onPrimaryContainer   = NeonBright,

    secondary            = SpotSub,
    onSecondary          = SpotBlack,
    secondaryContainer   = SpotDark3,
    onSecondaryContainer = NeonBright,

    tertiary             = GreenSpot,
    onTertiary           = SpotBlack,
    tertiaryContainer    = GreenDim,
    onTertiaryContainer  = GreenSpot,

    background           = SpotDark1,
    onBackground         = SpotWhite,

    surface              = SpotDark2,
    onSurface            = SpotWhite,
    surfaceVariant       = SpotDark3,
    onSurfaceVariant     = SpotSub,

    outline              = SpotLine,
    outlineVariant       = SpotDark4,

    error                = RedErr,
    onError              = SpotWhite,
    errorContainer       = RedDim,
    onErrorContainer     = RedErr,
)

private val LightScheme = lightColorScheme(
    primary              = NeonDim,
    onPrimary            = LightCard,
    primaryContainer     = LightCard2,
    onPrimaryContainer   = NeonDim,

    secondary            = LightSub,
    onSecondary          = LightCard,
    secondaryContainer   = LightCard2,
    onSecondaryContainer = NeonDim,

    tertiary             = GreenSpot,
    onTertiary           = LightCard,
    tertiaryContainer    = androidx.compose.ui.graphics.Color(0xFFD4F5E2),
    onTertiaryContainer  = androidx.compose.ui.graphics.Color(0xFF0A3D1F),

    background           = LightBg,
    onBackground         = SpotDark1,

    surface              = LightCard,
    onSurface            = SpotDark1,
    surfaceVariant       = LightCard2,
    onSurfaceVariant     = LightSub,

    outline              = LightBorder,
    outlineVariant       = LightBorder,

    error                = RedErr,
    onError              = LightCard,
    errorContainer       = androidx.compose.ui.graphics.Color(0xFFFFE4E4),
    onErrorContainer     = RedErr,
)

// ── Typography ────────────────────────────────────────────────────────────────

val LegendTypography = Typography(
    displayLarge = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 52.sp,
        lineHeight    = 58.sp,
        letterSpacing = (-1.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 40.sp,
        lineHeight    = 46.sp,
        letterSpacing = (-1.0).sp
    ),
    displaySmall = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 30.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 24.sp,
        lineHeight    = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 17.sp,
        lineHeight    = 22.sp
    ),
    titleLarge = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 18.sp,
        lineHeight    = 24.sp,
        letterSpacing = (-0.1).sp
    ),
    titleMedium = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 15.sp,
        lineHeight    = 20.sp
    ),
    titleSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 13.sp,
        lineHeight    = 18.sp
    ),
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
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.8.sp
    ),
)

// ── Theme ─────────────────────────────────────────────────────────────────────

@Composable
fun YtDownloaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme

    // Make status bar transparent and match background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = LegendTypography,
        content     = content
    )
}
