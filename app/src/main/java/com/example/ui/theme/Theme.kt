package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PureWhiteColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = BlueTertiary,
    background = PureWhite,                 // Pure White background
    onBackground = TextPrimaryOnWhite,
    surface = PureWhite,                    // Pure White card surfaces
    onSurface = TextPrimaryOnWhite,
    surfaceVariant = WhiteSubtleSoft,
    onSurfaceVariant = TextSecondaryOnWhite,
    outline = BlueOutlineClean,
    outlineVariant = BlueOutlineSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Forced white background as explicitly requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PureWhiteColorScheme,
        typography = AppTypography,
        content = content
    )
}
