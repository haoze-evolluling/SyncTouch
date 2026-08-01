package com.haoze.claudekeyboard.ui.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.core.view.WindowInsetsControllerCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF5F5DB5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E1FF),
    onPrimaryContainer = Color(0xFF1B1A5A),
    secondary = Color(0xFF5E5E75),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3E1F9),
    onSecondaryContainer = Color(0xFF1B1B2C),
    tertiary = Color(0xFF79536B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8EC),
    onTertiaryContainer = Color(0xFF301121),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE5E1EC),
    onSurfaceVariant = Color(0xFF62606A),
    outline = Color(0xFF7A7782),
    outlineVariant = Color(0xFFCBC7D2),
    surfaceContainerLow = Color(0xFFF6F2FA),
    surfaceContainer = Color(0xFFF1EDF7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC5C2FF),
    onPrimary = Color(0xFF2F2D78),
    primaryContainer = Color(0xFF45448F),
    onPrimaryContainer = Color(0xFFE4E1FF),
    secondary = Color(0xFFC7C4DD),
    onSecondary = Color(0xFF2F2E44),
    secondaryContainer = Color(0xFF46465C),
    onSecondaryContainer = Color(0xFFE3E1F9),
    tertiary = Color(0xFFEAB9D6),
    onTertiary = Color(0xFF472638),
    tertiaryContainer = Color(0xFF613D53),
    onTertiaryContainer = Color(0xFFFFD8EC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB),
    background = Color(0xFF131218),
    onBackground = Color(0xFFE5E1E9),
    surface = Color(0xFF131218),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF46444D),
    onSurfaceVariant = Color(0xFFC8C4CF),
    outline = Color(0xFF938F9A),
    outlineVariant = Color(0xFF46444D),
    surfaceContainerLow = Color(0xFF1B1A21),
    surfaceContainer = Color(0xFF1E1D25)
)

@Composable
fun SyncTouchTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val view = LocalView.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    val surfaceColor = colors.surface.toArgb()

    SideEffect {
        context.findActivity()?.window?.let { window ->
            window.statusBarColor = surfaceColor
            window.navigationBarColor = surfaceColor
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            WindowInsetsControllerCompat(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
        view.rootView.setBackgroundColor(surfaceColor)
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
