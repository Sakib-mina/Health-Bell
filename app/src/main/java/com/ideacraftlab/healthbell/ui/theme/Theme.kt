package com.ideacraftlab.healthbell.ui.theme

import android.app.Activity
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

private val PrimaryOrange = Color(0xFFFF5F15)
private val BackgroundLight = Color(0xFFF5F2E9)
private val BackgroundDark = Color(0xFF121212)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    secondary = Color(0xFF2D4F44), // Our dark green
    onSecondary = Color.White,
    background = BackgroundDark,
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    secondary = Color(0xFF2D4F44),
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = Color(0xFF2D2D2D),
    surface = Color.White,
    onSurface = Color(0xFF2D2D2D)
)

@Composable
fun HealthBellTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Force light brand colors regardless of system theme
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    // Always use LightColorScheme to keep design consistent
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
