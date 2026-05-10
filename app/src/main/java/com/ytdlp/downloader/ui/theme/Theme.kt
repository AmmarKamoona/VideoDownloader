package com.ytdlp.downloader.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LegendColorScheme = darkColorScheme(
    primary              = LgPrimary,
    onPrimary            = LgOnPrimary,
    primaryContainer     = LgPrimaryContainer,
    onPrimaryContainer   = LgOnPrimaryContainer,

    secondary            = LgSecondary,
    onSecondary          = LgOnSecondary,
    secondaryContainer   = LgSecondaryContainer,
    onSecondaryContainer = LgOnSecondaryContainer,

    tertiary             = LgTertiary,
    onTertiary           = LgOnSecondary,
    tertiaryContainer    = LgTertiaryContainer,
    onTertiaryContainer  = LgOnPrimaryContainer,

    background           = LgBackground,
    onBackground         = LgOnBackground,

    surface              = LgSurface,
    onSurface            = LgOnSurface,
    surfaceVariant       = LgSurfaceVariant,
    onSurfaceVariant     = LgOnSurfaceVariant,

    outline              = LgOutline,
    outlineVariant       = LgOutlineVariant,

    error                = LgError,
    onError              = LgOnError,
    errorContainer       = LgErrorContainer,
    onErrorContainer     = LgOnErrorContainer,

    inverseSurface       = LgInverseSurface,
    inverseOnSurface     = LgInverseOnSurface,
)

val LegendTypography = Typography(
    // headline-lg: 32sp / -0.02em / Bold
    displaySmall = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.64).sp
    ),
    // headline-md: 24sp / -0.01em / SemiBold
    headlineMedium = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = (-0.24).sp
    ),
    headlineSmall = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    // stats-number: 18sp / SemiBold
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    // body-lg: 16sp / Regular
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),
    // body-md: 14sp / Regular
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp
    ),
    // label-caps: 12sp / 0.05em / SemiBold (JetBrains Mono style)
    labelLarge = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.6.sp
    ),
    labelMedium = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.55.sp
    ),
    labelSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 10.sp,
        lineHeight    = 14.sp,
        letterSpacing = 0.5.sp
    ),
)

@Composable
fun YtDownloaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = LgBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = LegendColorScheme,
        typography  = LegendTypography,
        content     = content
    )
}
