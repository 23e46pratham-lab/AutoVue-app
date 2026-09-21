package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CockpitSteel,
    secondary = CockpitAmber,
    tertiary = CockpitRed,
    background = CockpitBackground,
    surface = CockpitCard,
    surfaceVariant = CockpitCardElevated,
    outline = CockpitSurfaceBorder,
    onPrimary = TextPrimary,
    onSecondary = CockpitBackground,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun AutoVueTheme(
    darkTheme: Boolean = true, // Force dark theme for automotive feel
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Strictly dark theme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
