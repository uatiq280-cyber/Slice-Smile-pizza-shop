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

private val DarkColorScheme = darkColorScheme(
    primary = PolishPrimaryRed,
    onPrimary = PolishBgLight,
    primaryContainer = PolishMaroonDark,
    onPrimaryContainer = PolishPrimaryContainer,
    secondary = CheeseGold,
    onSecondary = SurfaceCreamDark,
    secondaryContainer = CheeseGoldDark,
    onSecondaryContainer = PolishPrimaryContainer,
    tertiary = BasilGreenLight,
    background = SurfaceCreamDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = BorderDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = PolishBorderStrong,
    outlineVariant = PolishBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PolishPrimaryRed,
    onPrimary = Color.White,
    primaryContainer = PolishPrimaryContainer,
    onPrimaryContainer = PolishMaroonDark,
    secondary = PolishMaroonDark,
    onSecondary = Color.White,
    secondaryContainer = PolishPrimaryContainerSubtle,
    onSecondaryContainer = PolishMaroonDark,
    tertiary = BasilGreen,
    background = PolishBgLight,
    onBackground = PolishTextDark,
    surface = Color.White,
    onSurface = PolishTextDark,
    surfaceVariant = PolishPrimaryContainerSubtle,
    onSurfaceVariant = PolishTextMuted,
    outline = PolishBorderStrong,
    outlineVariant = PolishBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
