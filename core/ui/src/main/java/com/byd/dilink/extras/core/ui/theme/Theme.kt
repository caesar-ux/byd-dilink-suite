package com.byd.dilink.extras.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DiLinkDarkColorScheme = darkColorScheme(
    primary = DiLinkCyan,
    onPrimary = DiLinkBackground,
    primaryContainer = DiLinkCyanDark,
    onPrimaryContainer = DiLinkCyanLight,
    secondary = DiLinkCyanDark,
    onSecondary = DiLinkTextPrimary,
    secondaryContainer = DiLinkSurfaceVariant,
    onSecondaryContainer = DiLinkCyanLight,
    tertiary = DiLinkCyanLight,
    onTertiary = DiLinkBackground,
    background = DiLinkBackground,
    onBackground = DiLinkTextPrimary,
    surface = DiLinkSurface,
    onSurface = DiLinkTextPrimary,
    surfaceVariant = DiLinkSurfaceVariant,
    onSurfaceVariant = DiLinkTextSecondary,
    error = StatusRed,
    onError = DiLinkTextPrimary,
    outline = DiLinkTextMuted,
    outlineVariant = DiLinkSurfaceElevated
)

@Composable
fun DiLinkExtrasTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DiLinkDarkColorScheme,
        typography = DiLinkTypography,
        content = content
    )
}
