package com.example.asilo360.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TechBlue,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = LuminousTurquoise,
    onSecondary = DeepNightBlue,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF0F766E),
    tertiary = WarningAmber,
    onTertiary = DeepNightBlue,
    tertiaryContainer = SoftYellow,
    onTertiaryContainer = DeepNightBlue,
    background = SoftWhite,
    onBackground = DeepNightBlue,
    surface = PureWhite,
    onSurface = DeepNightBlue,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateGrey,
    outline = SurfaceBorder,
    error = CoralRed,
    errorContainer = Color(0xFFFEE2E2)
)

private val DarkColorScheme = darkColorScheme(
    primary = LuminousTurquoise,
    onPrimary = DeepNightBlue,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = LuminousTurquoise,
    secondary = TechBlue,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF0F172A),
    onSecondaryContainer = PureWhite,
    tertiary = WarningAmber,
    onTertiary = DeepNightBlue,
    background = Dark360,
    onBackground = SoftWhite,
    surface = DeepNightBlue,
    onSurface = SoftWhite,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = SlateGrey,
    outline = Color(0xFF334155),
    error = CoralRed,
    errorContainer = Color(0xFF7F1D1D)
)

@Composable
fun Asilo360Theme(
    themeOption: ThemeOption = ThemeOption.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeOption) {
        ThemeOption.SYSTEM -> systemDark
        ThemeOption.LIGHT -> false
        ThemeOption.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
