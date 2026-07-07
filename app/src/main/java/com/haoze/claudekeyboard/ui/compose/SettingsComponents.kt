package com.haoze.claudekeyboard.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

val SettingsCornerShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                },
                actions = { actions() }
            )
        },
        content = content
    )
}

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = SettingsCornerShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsGroupTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun SettingsInfoText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 4.dp, bottom = 8.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun SettingsDivider() {
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun SettingsLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val contentAlpha = if (enabled) 1f else 0.38f
    val clickableModifier = if (onClick != null && enabled) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .then(clickableModifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor.copy(alpha = contentAlpha)
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    SettingsItem(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun SettingsCheckboxItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    SettingsItem(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Checkbox(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun SettingsNavigationItem(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    leadingIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    SettingsItem(
        title = title,
        subtitle = subtitle,
        leadingIcon = leadingIcon,
        onClick = onClick,
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!value.isNullOrBlank()) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun SettingsTextItem(
    title: String,
    subtitle: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    SettingsItem(
        title = title,
        subtitle = subtitle,
        titleColor = textColor,
        enabled = enabled,
        onClick = onClick,
        trailing = trailing
    )
}

@Composable
fun SettingsRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    SettingsItem(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = onClick,
        trailing = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
fun SettingsSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
