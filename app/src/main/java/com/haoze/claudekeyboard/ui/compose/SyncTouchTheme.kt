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
    primary = Color(0xFF315CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE3FF),
    onPrimaryContainer = Color(0xFF07164E),
    secondary = Color(0xFF53627D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1E3ED),
    onSecondaryContainer = Color(0xFF1A223A),
    tertiary = Color(0xFFA23C7B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8EC),
    onTertiaryContainer = Color(0xFF3D0028),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF191B24),
    surface = Color(0xFFF8F9FC),
    onSurface = Color(0xFF191B24),
    surfaceVariant = Color(0xFFE9EAF0),
    onSurfaceVariant = Color(0xFF6A6E7C),
    outline = Color(0xFFB5B9C8),
    outlineVariant = Color(0xFFD9DCE6),
    surfaceContainerLow = Color(0xFFF1F3F8),
    surfaceContainer = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF859EFF),
    onPrimary = Color(0xFF07164E),
    primaryContainer = Color(0xFF07164E),
    onPrimaryContainer = Color(0xFFDCE3FF),
    secondary = Color(0xFFC1C9DD),
    onSecondary = Color(0xFF2B334B),
    secondaryContainer = Color(0xFF414A62),
    onSecondaryContainer = Color(0xFFDCE3F9),
    tertiary = Color(0xFFFFB0D0),
    onTertiary = Color(0xFF611A48),
    tertiaryContainer = Color(0xFF7C315C),
    onTertiaryContainer = Color(0xFFFFD8EC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB),
    background = Color(0xFF121318),
    onBackground = Color(0xFFE3E2E7),
    surface = Color(0xFF121318),
    onSurface = Color(0xFFE3E2E7),
    surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44464F),
    surfaceContainerLow = Color(0xFF191A20),
    surfaceContainer = Color(0xFF1E1F25)
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
