package com.nakanostay.ui.theme

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

// Colores basados en el logo
val PrimaryPink = Color(0xFFE8B4CB)
val PrimaryPurple = Color(0xFF9B6BA8)
val SecondaryPink = Color(0xFFF5D5E8)
val AccentPurple = Color(0xFF7B4397)
val LightPink = Color(0xFFFDF2F8)

// Colores de sistema
val BackgroundLight = Color(0xFFFFFBFE)
val BackgroundDark = Color(0xFF1C1B1F)
val SurfaceLight = Color(0xFFFFFBFE)
val SurfaceDark = Color(0xFF1C1B1F)
val OnSurfaceLight = Color(0xFF1C1B1F)
val OnSurfaceDark = Color(0xFFE6E1E5)

// Colores de estado
val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val ErrorRed = Color(0xFFF44336)
val InfoBlue = Color(0xFF2196F3)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPink,
    secondary = PrimaryPurple,
    tertiary = AccentPurple,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnSurfaceDark,
    onSecondary = OnSurfaceDark,
    onTertiary = OnSurfaceDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = PrimaryPink,
    tertiary = AccentPurple,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = BackgroundLight,
    onSecondary = OnSurfaceLight,
    onTertiary = BackgroundLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
)

@Composable
fun NakanostayTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}