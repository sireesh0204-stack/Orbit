package com.portfolio.jobpilot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonEmerald,
    tertiary = SatinGold,
    background = Surface000,
    surface = Surface100,
    surfaceVariant = Surface200,
    onPrimary = Surface000,
    onSecondary = Surface000,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun JobPilotTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = JobPilotTypography,
        content = content
    )
}

