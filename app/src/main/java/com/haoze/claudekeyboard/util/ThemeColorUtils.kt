package com.haoze.claudekeyboard.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb

data class DynamicViewColors(
    val surface: Int,
    val surfaceContainer: Int,
    val surfaceContainerLow: Int,
    val surfaceVariant: Int,
    val primary: Int,
    val primaryContainer: Int,
    val onPrimary: Int,
    val onPrimaryContainer: Int,
    val error: Int,
    val onSurface: Int,
    val onSurfaceVariant: Int,
    val outlineVariant: Int
)

fun Context.dynamicSurfaceColor(): Int {
    return dynamicViewColors().surface
}

fun Context.resolveAttrColor(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun Context.dynamicViewColors(): DynamicViewColors {
    val isDarkTheme = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val colorScheme = if (isDarkTheme) {
            dynamicDarkColorScheme(this)
        } else {
            dynamicLightColorScheme(this)
        }
        colorScheme.toDynamicViewColors()
    } else {
        DynamicViewColors(
            surface = resolveAttrColor(com.google.android.material.R.attr.colorSurface),
            surfaceContainer = resolveAttrColor(com.google.android.material.R.attr.colorSurfaceContainer),
            surfaceContainerLow = resolveAttrColor(com.google.android.material.R.attr.colorSurfaceContainerLow),
            surfaceVariant = resolveAttrColor(com.google.android.material.R.attr.colorSurfaceVariant),
            primary = resolveAttrColor(androidx.appcompat.R.attr.colorPrimary),
            primaryContainer = resolveAttrColor(com.google.android.material.R.attr.colorPrimaryContainer),
            onPrimary = resolveAttrColor(com.google.android.material.R.attr.colorOnPrimary),
            onPrimaryContainer = resolveAttrColor(com.google.android.material.R.attr.colorOnPrimaryContainer),
            error = resolveAttrColor(android.R.attr.colorError),
            onSurface = resolveAttrColor(com.google.android.material.R.attr.colorOnSurface),
            onSurfaceVariant = resolveAttrColor(com.google.android.material.R.attr.colorOnSurfaceVariant),
            outlineVariant = resolveAttrColor(com.google.android.material.R.attr.colorOutlineVariant)
        )
    }
}

private fun ColorScheme.toDynamicViewColors(): DynamicViewColors {
    return DynamicViewColors(
        surface = surface.toArgb(),
        surfaceContainer = surfaceContainer.toArgb(),
        surfaceContainerLow = surfaceContainerLow.toArgb(),
        surfaceVariant = surfaceVariant.toArgb(),
        primary = primary.toArgb(),
        primaryContainer = primaryContainer.toArgb(),
        onPrimary = onPrimary.toArgb(),
        onPrimaryContainer = onPrimaryContainer.toArgb(),
        error = error.toArgb(),
        onSurface = onSurface.toArgb(),
        onSurfaceVariant = onSurfaceVariant.toArgb(),
        outlineVariant = outlineVariant.toArgb()
    )
}
