package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ImmTvColorScheme = darkColorScheme(
    primary = ImmNetflixRed,
    onPrimary = ImmTextPrimary,
    primaryContainer = ImmRedDark,
    onPrimaryContainer = ImmTextPrimary,
    secondary = ImmRedGlow,
    onSecondary = ImmTextPrimary,
    background = ImmDarkBackground,
    onBackground = ImmTextPrimary,
    surface = ImmSurface,
    onSurface = ImmTextPrimary,
    surfaceVariant = ImmSurfaceVariant,
    onSurfaceVariant = ImmTextSecondary,
    outline = ImmBorder,
    outlineVariant = ImmBorderSubtle
)

@Composable
fun ImmTvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ImmTvColorScheme,
        typography = Typography,
        content = content
    )
}

// For compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ImmTvTheme(content = content)
}
