package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DragonBallDarkColorScheme = darkColorScheme(
    primary = SaiyanOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF421D00),
    onPrimaryContainer = SaiyanOrangeGlow,
    secondary = KiBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF003855),
    onSecondaryContainer = Color(0xFF80D8FF),
    tertiary = KiYellow,
    onTertiary = Color.Black,
    background = SaiyanNavyBackground,
    onBackground = DarkTextPrimary,
    surface = SaiyanCardSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = SaiyanSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkDivider
)

private val DragonBallLightColorScheme = lightColorScheme(
    primary = SaiyanOrangePrimary,
    onPrimary = Color.White,
    secondary = KiBlue,
    onSecondary = Color.White,
    background = Color(0xFF0F111A), // Keep dark-themed for anime wallpapers showcase
    onBackground = DarkTextPrimary,
    surface = SaiyanCardSurface,
    onSurface = DarkTextPrimary
)

@Composable
fun DragonWallpapersTheme(
    darkTheme: Boolean = true, // Default to stunning dark theme for wallpapers
    content: @Composable () -> Unit
) {
    val colorScheme = DragonBallDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
