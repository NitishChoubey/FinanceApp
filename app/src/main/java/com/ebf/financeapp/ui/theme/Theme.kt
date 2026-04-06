package com.ebf.financeapp.ui.theme

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
    primary = Color(0xFF5DCAA5),
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF0F6E56),
    onPrimaryContainer = Color(0xFF9FE1CB),
    secondary = Color(0xFF85B7EB),
    background = SurfaceDark,
    surface = CardDark,
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8),

    outline = Color(0xFF2A2A4A),
    surfaceVariant      = SurfaceVariantDark,
    onSurfaceVariant    = Color(0xFFB0B8C1),
    inverseSurface      = Color(0xFFE8E8E8),
    inverseOnSurface    = Color(0xFF1A1A2E),
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenLight,
    onPrimaryContainer = PrimaryGreenDark,
    secondary = Color(0xFF378ADD),
    background = SurfaceLight,
    surface = CardLight,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF1F3F4),
    outline = Color(0xFFE0E0E0),
)



@Composable
fun FinanceAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FinanceTypography,
        content = content
    )
}