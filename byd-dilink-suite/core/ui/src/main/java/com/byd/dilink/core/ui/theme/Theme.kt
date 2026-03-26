package com.byd.dilink.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DiLinkDarkColorScheme = darkColorScheme(
    primary = DiLinkCyan,
    onPrimary = DiLinkBackground,
    primaryContainer = DiLinkCyanDark,
    onPrimaryContainer = DiLinkCyanLight,
    secondary = DiLinkCyanLight,
    onSecondary = DiLinkBackground,
    secondaryContainer = DiLinkSurfaceVariant,
    onSecondaryContainer = DiLinkTextPrimary,
    tertiary = StatusBlue,
    onTertiary = DiLinkBackground,
    tertiaryContainer = DiLinkSurfaceElevated,
    onTertiaryContainer = DiLinkTextPrimary,
    error = StatusRed,
    onError = DiLinkBackground,
    errorContainer = StatusRed.copy(alpha = 0.3f),
    onErrorContainer = StatusRed,
    background = DiLinkBackground,
    onBackground = DiLinkTextPrimary,
    surface = DiLinkSurface,
    onSurface = DiLinkTextPrimary,
    surfaceVariant = DiLinkSurfaceVariant,
    onSurfaceVariant = DiLinkTextSecondary,
    outline = DiLinkTextMuted,
    outlineVariant = DiLinkSurfaceElevated,
    inverseSurface = DiLinkTextPrimary,
    inverseOnSurface = DiLinkBackground,
    inversePrimary = DiLinkCyanDark,
    surfaceTint = DiLinkCyan
)

@Composable
fun DiLinkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DiLinkDarkColorScheme,
        typography = DiLinkTypography,
        content = content
    )
}
