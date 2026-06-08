package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NaturalGreenPrimaryDark,
    secondary = NaturalGreenSecondaryDark,
    tertiary = NaturalGreenTertiaryDark,
    background = NaturalDarkBackground,
    surface = NaturalDarkSurface,
    onPrimary = NaturalDarkBackground,
    onSecondary = NaturalDarkBackground,
    onTertiary = NaturalDarkBackground,
    onBackground = Color(0xFFE2EBE2),
    onSurface = Color(0xFFE2EBE2)
)

private val LightColorScheme = lightColorScheme(
    primary = NaturalGreenPrimary,
    secondary = NaturalGreenSecondary,
    tertiary = NaturalGreenTertiary,
    background = NaturalLightBackground,
    surface = NaturalLightSurface,
    onPrimary = Color.White,
    onSecondary = NaturalGreenPrimary,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A), // Slate 900
    onSurface = Color(0xFF1E293B)     // Slate 800
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color matches Android 12+ wallpaper but we prefer forcing our beautiful custom theme
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
