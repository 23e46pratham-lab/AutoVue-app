package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightCreamColorScheme = lightColorScheme(
    primary = CockpitSteel,
    secondary = CockpitAmber,
    tertiary = CockpitRed,
    background = CockpitBackground,
    surface = CockpitCard,
    surfaceVariant = CockpitCardElevated,
    outline = CockpitSurfaceBorder,
    onPrimary = Color(0xFFFFFDF9),
    onSecondary = Color(0xFF231E19),
    onTertiary = Color(0xFFFFFDF9),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outlineVariant = CardBorder
)

@Composable
fun AutoVueTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightCreamColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

