package com.smartalarm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    onPrimary = BlackBackground,
    secondary = RedSecondary,
    onSecondary = BlackBackground,
    background = BlackBackground,
    onBackground = RedPrimary,
    surface = BlackBackground,
    onSurface = RedPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = BlackBackground,
    secondary = RedSecondary,
    onSecondary = BlackBackground,
    background = BlackBackground,
    onBackground = RedPrimary,
    surface = BlackBackground,
    onSurface = RedPrimary
)

@Composable
fun SmartAlarmTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
