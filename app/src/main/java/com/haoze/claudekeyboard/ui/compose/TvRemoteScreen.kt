package com.haoze.claudekeyboard.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.haoze.claudekeyboard.R
import com.haoze.claudekeyboard.ui.tvremote.CircularDpadView
import com.haoze.claudekeyboard.util.performKeyClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TvRemoteScreen(
    enabled: Boolean,
    onBack: () -> Unit,
    onAction: (TvRemoteAction) -> Unit
) {
    val colors = rememberTvRemoteColors()
    val scope = rememberCoroutineScope()
    var ledActive by remember { mutableStateOf(false) }

    fun runAction(action: TvRemoteAction) {
        if (!enabled) return
        ledActive = true
        onAction(action)
        scope.launch {
            delay(150)
            ledActive = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
    ) {
        TvRemoteHeader(
            colors = colors,
            onBack = onBack
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TvRemoteTopRow(
                    colors = colors,
                    ledActive = ledActive,
                    enabled = enabled,
                    onPower = { runAction(TvRemoteAction.POWER) }
                )
                TvRemoteDpad(
                    colors = colors,
                    enabled = enabled,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .size(212.dp),
                    onAction = ::runAction
                )
                RemoteTwoButtonRow(
                    modifier = Modifier.padding(top = 20.dp),
                    colors = colors,
                    enabled = enabled,
                    left = RemoteButtonSpec(
                        label = stringResource(R.string.tvremote_back),
                        iconRes = R.drawable.ic_arrow_back,
                        iconBackground = colors.primaryContainer,
                        onClick = { runAction(TvRemoteAction.BACK) }
                    ),
                    right = RemoteButtonSpec(
                        label = stringResource(R.string.tvremote_assistant),
                        iconRes = R.drawable.ic_assistant,
                        iconBackground = colors.tertiaryContainer,
                        onClick = { runAction(TvRemoteAction.ASSISTANT) }
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier.width(130.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RemoteCapsuleButton(
                            spec = RemoteButtonSpec(
                                label = stringResource(R.string.tvremote_home),
                                iconRes = R.drawable.ic_home,
                                iconBackground = colors.primaryContainer,
                                onClick = { runAction(TvRemoteAction.HOME) }
                            ),
                            colors = colors,
                            enabled = enabled
                        )
                        Spacer(Modifier.height(18.dp))
                        RemoteCapsuleButton(
                            spec = RemoteButtonSpec(
                                label = stringResource(R.string.tvremote_mute),
                                iconRes = R.drawable.ic_volume_off,
                                iconBackground = colors.secondaryContainer,
                                onClick = { runAction(TvRemoteAction.MUTE) }
                            ),
                            colors = colors,
                            enabled = enabled
                        )
                    }
                    Spacer(Modifier.width(28.dp))
                    VolumeStack(colors = colors, enabled = enabled, onAction = ::runAction)
                }
                MediaControlRow(
                    colors = colors,
                    enabled = enabled,
                    modifier = Modifier.padding(top = 20.dp),
                    onAction = ::runAction
                )
            }
        }
    }
}

@Composable
private fun TvRemoteHeader(
    colors: TvRemoteColors,
    onBack: () -> Unit
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.surfaceContainerLow)
                .clickable {
                    view.performKeyClick()
                    onBack()
                }
                .semantics { contentDescription = view.context.getString(R.string.nav_back) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_arrow_back_24),
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = stringResource(R.string.home_tvremote_title),
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TvRemoteTopRow(
    colors: TvRemoteColors,
    ledActive: Boolean,
    enabled: Boolean,
    onPower: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (ledActive) colors.primary else colors.outlineVariant)
            )
            Text(
                text = stringResource(R.string.tvremote_led_label),
                modifier = Modifier.padding(start = 10.dp),
                color = colors.primary,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.weight(1f))
        IconOnlyCircleButton(
            iconRes = R.drawable.baseline_power_settings_new_24,
            contentDescription = stringResource(R.string.tvremote_power),
            colors = colors,
            enabled = enabled,
            onClick = onPower
        )
    }
}

@Composable
private fun TvRemoteDpad(
    colors: TvRemoteColors,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onAction: (TvRemoteAction) -> Unit
) {
    val density = LocalDensity.current
    val outerRadius = with(density) { 106.dp.toPx() }
    val innerRadius = with(density) { 51.dp.toPx() }
    AndroidView(
        modifier = modifier.alpha(if (enabled) 1f else 0.4f),
        factory = { context -> CircularDpadView(context) },
        update = { dpad ->
            dpad.configureStyle(
                outerRadius = outerRadius,
                innerRadius = innerRadius,
                repeatDelay = 200L,
                ringColor = colors.surfaceContainer.toArgb(),
                ringBorderColor = colors.dpadBorder.toArgb(),
                centerColor = colors.surfaceContainer.toArgb(),
                centerBorderColor = colors.dpadBorder.toArgb(),
                dividerColor = colors.dpadBorder.toArgb(),
                iconColor = colors.primary.toArgb(),
                textColor = colors.onSurface.toArgb()
            )
            dpad.dpadEnabled = enabled
            dpad.onDirectionListener = object : CircularDpadView.OnDirectionListener {
                override fun onDirection(direction: CircularDpadView.Direction) {
                    onAction(
                        when (direction) {
                            CircularDpadView.Direction.UP -> TvRemoteAction.UP
                            CircularDpadView.Direction.DOWN -> TvRemoteAction.DOWN
                            CircularDpadView.Direction.LEFT -> TvRemoteAction.LEFT
                            CircularDpadView.Direction.RIGHT -> TvRemoteAction.RIGHT
                        }
                    )
                }
            }
            dpad.onConfirmListener = { onAction(TvRemoteAction.CONFIRM) }
        }
    )
}

@Composable
private fun RemoteTwoButtonRow(
    modifier: Modifier = Modifier,
    colors: TvRemoteColors,
    enabled: Boolean,
    left: RemoteButtonSpec,
    right: RemoteButtonSpec
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        RemoteCapsuleButton(spec = left, colors = colors, enabled = enabled)
        Spacer(Modifier.width(28.dp))
        RemoteCapsuleButton(spec = right, colors = colors, enabled = enabled)
    }
}

@Composable
private fun RemoteCapsuleButton(
    spec: RemoteButtonSpec,
    colors: TvRemoteColors,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Row(
        modifier = modifier
            .width(130.dp)
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) {
                view.performKeyClick()
                spec.onClick()
            }
            .semantics { contentDescription = spec.label }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RemoteIconBadge(
            iconRes = spec.iconRes,
            background = spec.iconBackground,
            colors = colors
        )
        Text(
            text = spec.label,
            modifier = Modifier.padding(start = 10.dp),
            color = colors.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun IconOnlyCircleButton(
    iconRes: Int,
    contentDescription: String,
    colors: TvRemoteColors,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Box(
        modifier = Modifier
            .size(60.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(CircleShape)
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant, CircleShape)
            .clickable(enabled = enabled) {
                view.performKeyClick()
                onClick()
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun VolumeStack(
    colors: TvRemoteColors,
    enabled: Boolean,
    onAction: (TvRemoteAction) -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        VolumeRow(
            label = stringResource(R.string.tvremote_volume_up),
            iconRes = R.drawable.ic_volume_up,
            colors = colors,
            enabled = enabled,
            onClick = { onAction(TvRemoteAction.VOLUME_UP) }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(colors.outlineVariant)
        )
        VolumeRow(
            label = stringResource(R.string.tvremote_volume_down),
            iconRes = R.drawable.ic_volume_down,
            colors = colors,
            enabled = enabled,
            onClick = { onAction(TvRemoteAction.VOLUME_DOWN) }
        )
    }
}

@Composable
private fun VolumeRow(
    label: String,
    iconRes: Int,
    colors: TvRemoteColors,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(enabled = enabled) {
                view.performKeyClick()
                onClick()
            }
            .semantics { contentDescription = label }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RemoteIconBadge(
            iconRes = iconRes,
            background = colors.secondaryContainer,
            colors = colors
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 10.dp),
            color = colors.onSurface,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MediaControlRow(
    colors: TvRemoteColors,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onAction: (TvRemoteAction) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        MediaIconButton(
            iconRes = R.drawable.ic_skip_previous,
            contentDescription = stringResource(R.string.tvremote_previous),
            colors = colors,
            enabled = enabled,
            onClick = { onAction(TvRemoteAction.PREVIOUS) }
        )
        Spacer(Modifier.width(18.dp))
        MediaIconButton(
            iconRes = R.drawable.ic_play_arrow,
            contentDescription = stringResource(R.string.tvremote_play_pause),
            colors = colors,
            enabled = enabled,
            onClick = { onAction(TvRemoteAction.PLAY_PAUSE) }
        )
        Spacer(Modifier.width(18.dp))
        MediaIconButton(
            iconRes = R.drawable.ic_skip_next,
            contentDescription = stringResource(R.string.tvremote_next),
            colors = colors,
            enabled = enabled,
            onClick = { onAction(TvRemoteAction.NEXT) }
        )
        Spacer(Modifier.width(18.dp))
        MediaIconButton(
            iconRes = R.drawable.ic_stop,
            contentDescription = stringResource(R.string.tvremote_stop),
            colors = colors,
            enabled = enabled,
            onClick = { onAction(TvRemoteAction.STOP) }
        )
    }
}

@Composable
private fun MediaIconButton(
    iconRes: Int,
    contentDescription: String,
    colors: TvRemoteColors,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Box(
        modifier = Modifier
            .width(56.dp)
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) {
                view.performKeyClick()
                onClick()
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        RemoteIconBadge(
            iconRes = iconRes,
            background = colors.secondaryContainer,
            colors = colors
        )
    }
}

@Composable
private fun RemoteIconBadge(
    iconRes: Int,
    background: Color,
    colors: TvRemoteColors
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = colors.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

private data class RemoteButtonSpec(
    val label: String,
    val iconRes: Int,
    val iconBackground: Color,
    val onClick: () -> Unit
)

private data class TvRemoteColors(
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerLow: Color,
    val outlineVariant: Color,
    val primary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primaryContainer: Color,
    val secondaryContainer: Color,
    val tertiaryContainer: Color,
    val dpadBorder: Color
)

@Composable
private fun rememberTvRemoteColors(): TvRemoteColors {
    return TvRemoteColors(
        surface = MaterialTheme.colorScheme.surface,
        surfaceContainer = MaterialTheme.colorScheme.surfaceContainer,
        surfaceContainerLow = MaterialTheme.colorScheme.surfaceContainerLow,
        outlineVariant = MaterialTheme.colorScheme.outlineVariant,
        primary = MaterialTheme.colorScheme.primary,
        onSurface = MaterialTheme.colorScheme.onSurface,
        onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
        primaryContainer = MaterialTheme.colorScheme.primaryContainer,
        secondaryContainer = MaterialTheme.colorScheme.secondaryContainer,
        tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer,
        dpadBorder = MaterialTheme.colorScheme.outline
    )
}
