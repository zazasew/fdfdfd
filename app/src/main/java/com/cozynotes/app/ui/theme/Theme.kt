package com.cozynotes.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LavenderDeep,
    onPrimary = Color.White,
    primaryContainer = Lavender.copy(alpha = 0.35f),
    onPrimaryContainer = WarmTextPrimary,
    secondary = PastelPink,
    onSecondary = WarmTextPrimary,
    secondaryContainer = PastelPink.copy(alpha = 0.5f),
    background = CreamBackground,
    onBackground = WarmTextPrimary,
    surface = CreamSurface,
    onSurface = WarmTextPrimary,
    surfaceVariant = PastelPeach.copy(alpha = 0.35f),
    onSurfaceVariant = WarmTextSecondary,
    outline = WarmTextSecondary.copy(alpha = 0.4f)
)

private val DarkColors = darkColorScheme(
    primary = Lavender,
    onPrimary = DarkBackground,
    primaryContainer = LavenderDeep.copy(alpha = 0.5f),
    onPrimaryContainer = DarkTextPrimary,
    secondary = PastelPink.copy(alpha = 0.8f),
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkTextSecondary.copy(alpha = 0.4f)
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun PersonalNotesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = cozyTypography(fontScale),
        shapes = CozyShapes,
        content = content
    )
}
