package com.haoze.claudekeyboard.ui.compose

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.haoze.claudekeyboard.R
import com.haoze.claudekeyboard.macro.Macro
import com.haoze.claudekeyboard.ui.tvremote.CircularDpadView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppPage {
    HOME,
    AGENT,
    TV_REMOTE,
    SETTINGS,
    SETTINGS_CONNECTION,
    SETTINGS_TOUCHPAD,
    SETTINGS_INTERACTION,
    SETTINGS_DATA,
    SETTINGS_APPEARANCE,
    SETTINGS_ABOUT
}

enum class CoreCommand {
    YES,
    YES_TO_ALL,
    NO,
    CTRL_C,
    BACKSPACE,
    ENTER
}

enum class TvRemoteAction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    CONFIRM,
    BACK,
    ASSISTANT,
    HOME,
    MUTE,
    VOLUME_UP,
    VOLUME_DOWN,
    POWER,
    PLAY_PAUSE,
    NEXT,
    PREVIOUS,
    STOP
}

@Composable
fun SyncTouchApp(
    page: AppPage,
    isConnected: Boolean,
    connectedDeviceName: String?,
    versionName: String,
    macros: List<Macro>,
    onNavigate: (AppPage) -> Unit,
    onNavigateHome: () -> Unit,
    onOpenKeyboard: () -> Unit,
    onOpenTouchpad: () -> Unit,
    onShowDeviceList: () -> Unit,
    onCoreCommand: (CoreCommand) -> Unit,
    onMacroClick: (Macro) -> Unit,
    onMacroLongClick: (Macro) -> Unit,
    onAddMacro: () -> Unit,
    onResetMacros: () -> Unit,
    onBooleanSettingChanged: (String, Boolean) -> Unit,
    onThemeModeChanged: (Int) -> Unit,
    onTvRemoteAction: (TvRemoteAction) -> Unit
) {
    when (page) {
        AppPage.HOME -> HomeScreen(
            isConnected = isConnected,
            connectedDeviceName = connectedDeviceName,
            onShowDeviceList = onShowDeviceList,
            onOpenKeyboard = onOpenKeyboard,
            onOpenTouchpad = onOpenTouchpad,
            onNavigateAgent = { onNavigate(AppPage.AGENT) },
            onNavigateTvRemote = { onNavigate(AppPage.TV_REMOTE) },
            onNavigateSettings = { onNavigate(AppPage.SETTINGS) }
        )
        AppPage.AGENT -> AgentScreen(
            isConnected = isConnected,
            connectedDeviceName = connectedDeviceName,
            macros = macros,
            onBack = onNavigateHome,
            onSettings = { onNavigate(AppPage.SETTINGS) },
            onCoreCommand = onCoreCommand,
            onMacroClick = onMacroClick,
            onMacroLongClick = onMacroLongClick,
            onAddMacro = onAddMacro
        )
        AppPage.TV_REMOTE -> TvRemoteScreen(
            enabled = isConnected,
            onBack = onNavigateHome,
            onAction = onTvRemoteAction
        )
        AppPage.SETTINGS,
        AppPage.SETTINGS_CONNECTION,
        AppPage.SETTINGS_TOUCHPAD,
        AppPage.SETTINGS_INTERACTION,
        AppPage.SETTINGS_DATA,
        AppPage.SETTINGS_APPEARANCE,
        AppPage.SETTINGS_ABOUT -> SettingsScreenRoot(
            page = page,
            versionName = versionName,
            onBackHome = onNavigateHome,
            onNavigate = onNavigate,
            onResetMacros = onResetMacros,
            onBooleanSettingChanged = onBooleanSettingChanged,
            onThemeModeChanged = onThemeModeChanged
        )
    }
}

@Composable
private fun HomeScreen(
    isConnected: Boolean,
    connectedDeviceName: String?,
    onShowDeviceList: () -> Unit,
    onOpenKeyboard: () -> Unit,
    onOpenTouchpad: () -> Unit,
    onNavigateAgent: () -> Unit,
    onNavigateTvRemote: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val status = connectionStatusText(isConnected, connectedDeviceName)
    SettingsScaffold(
        title = stringResource(R.string.app_name),
        onBack = null,
        actions = {
            IconButton(onClick = onNavigateSettings) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.home_settings_title))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.home_connection_status))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.home_connection_status),
                    subtitle = stringResource(R.string.home_hero_subtitle),
                    value = status,
                    leadingIcon = Icons.Default.Bluetooth,
                    onClick = onShowDeviceList
                )
            }

            SettingsGroupTitle(stringResource(R.string.home_functions))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.home_keyboard_title),
                    subtitle = stringResource(R.string.home_keyboard_subtitle),
                    leadingIcon = Icons.Default.Keyboard,
                    onClick = onOpenKeyboard
                )
                SettingsDivider()
                SettingsNavigationItem(
                    title = stringResource(R.string.home_touchpad_title),
                    subtitle = stringResource(R.string.home_touchpad_subtitle),
                    leadingIcon = Icons.Default.Mouse,
                    onClick = onOpenTouchpad
                )
                SettingsDivider()
                SettingsNavigationItem(
                    title = stringResource(R.string.home_tvremote_title),
                    subtitle = stringResource(R.string.home_tvremote_subtitle),
                    leadingIcon = Icons.Default.SettingsRemote,
                    onClick = onNavigateTvRemote
                )
                SettingsDivider()
                SettingsNavigationItem(
                    title = stringResource(R.string.home_agent_title),
                    subtitle = stringResource(R.string.home_agent_subtitle),
                    leadingIcon = Icons.Default.Terminal,
                    onClick = onNavigateAgent
                )
            }

            SettingsGroupTitle(stringResource(R.string.home_system))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.home_settings_title),
                    subtitle = stringResource(R.string.home_settings_subtitle),
                    leadingIcon = Icons.Default.Settings,
                    onClick = onNavigateSettings
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AgentScreen(
    isConnected: Boolean,
    connectedDeviceName: String?,
    macros: List<Macro>,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onCoreCommand: (CoreCommand) -> Unit,
    onMacroClick: (Macro) -> Unit,
    onMacroLongClick: (Macro) -> Unit,
    onAddMacro: () -> Unit
) {
    SettingsScaffold(
        title = stringResource(R.string.home_agent_title),
        onBack = onBack,
        actions = {
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.home_settings_title))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.home_connection_status))
            SettingsGroup {
                SettingsItem(
                    title = stringResource(R.string.home_connection_status),
                    subtitle = connectionStatusText(isConnected, connectedDeviceName),
                    leadingIcon = Icons.Default.Bluetooth
                )
            }

            SettingsGroupTitle(stringResource(R.string.home_agent_title))
            SettingsGroup {
                CoreCommandRow(stringResource(R.string.btn_yes), Icons.Default.Check) { onCoreCommand(CoreCommand.YES) }
                SettingsDivider()
                CoreCommandRow(stringResource(R.string.btn_yes_to_all), Icons.Default.Check) { onCoreCommand(CoreCommand.YES_TO_ALL) }
                SettingsDivider()
                CoreCommandRow(stringResource(R.string.btn_no), Icons.Default.PowerSettingsNew, MaterialTheme.colorScheme.error) { onCoreCommand(CoreCommand.NO) }
                SettingsDivider()
                CoreCommandRow(stringResource(R.string.btn_ctrl_c), Icons.Default.Keyboard) { onCoreCommand(CoreCommand.CTRL_C) }
                SettingsDivider()
                CoreCommandRow(stringResource(R.string.btn_backspace), Icons.Default.Keyboard, MaterialTheme.colorScheme.error) { onCoreCommand(CoreCommand.BACKSPACE) }
                SettingsDivider()
                CoreCommandRow(stringResource(R.string.btn_enter), Icons.Default.Keyboard) { onCoreCommand(CoreCommand.ENTER) }
            }

            SettingsGroupTitle(stringResource(R.string.macro_list_title))
            SettingsGroup {
                macros.forEachIndexed { index, macro ->
                    MacroSettingsItem(
                        macro = macro,
                        onClick = { onMacroClick(macro) },
                        onLongClick = { if (!macro.isPreset) onMacroLongClick(macro) }
                    )
                    SettingsDivider()
                }
                SettingsTextItem(
                    title = stringResource(R.string.btn_add_macro),
                    subtitle = stringResource(R.string.macro_long_press_hint),
                    textColor = MaterialTheme.colorScheme.primary,
                    onClick = onAddMacro
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CoreCommandRow(
    title: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    SettingsTextItem(
        title = title,
        textColor = color,
        onClick = onClick,
        trailing = {
            Icon(icon, contentDescription = null, tint = color)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MacroSettingsItem(
    macro: Macro,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    SettingsItem(
        title = macro.label,
        subtitle = macro.description.ifBlank { macro.command },
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    )
}

@Composable
private fun TvRemoteScreen(
    enabled: Boolean,
    onBack: () -> Unit,
    onAction: (TvRemoteAction) -> Unit
) {
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

    SettingsScaffold(
        title = stringResource(R.string.home_tvremote_title),
        onBack = onBack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.tvremote_led_label))
            SettingsGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (ledActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (enabled) stringResource(R.string.status_connected_label) else stringResource(R.string.status_not_connected),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { runAction(TvRemoteAction.POWER) }, enabled = enabled) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = stringResource(R.string.tvremote_power))
                    }
                }
            }

            SettingsGroupTitle(stringResource(R.string.home_tvremote_title))
            SettingsGroup {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        modifier = Modifier
                            .size(212.dp)
                            .alpha(if (enabled) 1f else 0.4f),
                        factory = { context ->
                            CircularDpadView(context).apply {
                                onDirectionListener = object : CircularDpadView.OnDirectionListener {
                                    override fun onDirection(direction: CircularDpadView.Direction) {
                                        runAction(
                                            when (direction) {
                                                CircularDpadView.Direction.UP -> TvRemoteAction.UP
                                                CircularDpadView.Direction.DOWN -> TvRemoteAction.DOWN
                                                CircularDpadView.Direction.LEFT -> TvRemoteAction.LEFT
                                                CircularDpadView.Direction.RIGHT -> TvRemoteAction.RIGHT
                                            }
                                        )
                                    }
                                }
                                onConfirmListener = { runAction(TvRemoteAction.CONFIRM) }
                            }
                        },
                        update = { it.dpadEnabled = enabled }
                    )
                }
                RemoteButtonRow(
                    left = stringResource(R.string.tvremote_back) to TvRemoteAction.BACK,
                    right = stringResource(R.string.tvremote_assistant) to TvRemoteAction.ASSISTANT,
                    enabled = enabled,
                    onClick = ::runAction
                )
                RemoteButtonRow(
                    left = stringResource(R.string.tvremote_home) to TvRemoteAction.HOME,
                    right = stringResource(R.string.tvremote_mute) to TvRemoteAction.MUTE,
                    enabled = enabled,
                    onClick = ::runAction
                )
                RemoteButtonRow(
                    left = stringResource(R.string.tvremote_volume_up) to TvRemoteAction.VOLUME_UP,
                    right = stringResource(R.string.tvremote_volume_down) to TvRemoteAction.VOLUME_DOWN,
                    enabled = enabled,
                    onClick = ::runAction
                )
                RemoteButtonRow(
                    left = stringResource(R.string.tvremote_previous) to TvRemoteAction.PREVIOUS,
                    right = stringResource(R.string.tvremote_play_pause) to TvRemoteAction.PLAY_PAUSE,
                    enabled = enabled,
                    onClick = ::runAction
                )
                RemoteButtonRow(
                    left = stringResource(R.string.tvremote_next) to TvRemoteAction.NEXT,
                    right = stringResource(R.string.tvremote_stop) to TvRemoteAction.STOP,
                    enabled = enabled,
                    onClick = ::runAction
                )
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RemoteButtonRow(
    left: Pair<String, TvRemoteAction>,
    right: Pair<String, TvRemoteAction>,
    enabled: Boolean,
    onClick: (TvRemoteAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RemoteButton(
            title = left.first,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            onClick = { onClick(left.second) }
        )
        RemoteButton(
            title = right.first,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            onClick = { onClick(right.second) }
        )
    }
}

@Composable
private fun RemoteButton(
    title: String,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SettingsScreenRoot(
    page: AppPage,
    versionName: String,
    onBackHome: () -> Unit,
    onNavigate: (AppPage) -> Unit,
    onResetMacros: () -> Unit,
    onBooleanSettingChanged: (String, Boolean) -> Unit,
    onThemeModeChanged: (Int) -> Unit
) {
    when (page) {
        AppPage.SETTINGS -> SettingsHomeScreen(
            onBack = onBackHome,
            versionName = versionName,
            onNavigate = onNavigate
        )
        AppPage.SETTINGS_CONNECTION -> ConnectionSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) },
            onBooleanSettingChanged = onBooleanSettingChanged
        )
        AppPage.SETTINGS_TOUCHPAD -> TouchpadSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) }
        )
        AppPage.SETTINGS_INTERACTION -> InteractionSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) }
        )
        AppPage.SETTINGS_DATA -> DataSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) },
            onResetMacros = onResetMacros
        )
        AppPage.SETTINGS_APPEARANCE -> AppearanceSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) },
            onThemeModeChanged = onThemeModeChanged
        )
        AppPage.SETTINGS_ABOUT -> AboutSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) },
            versionName = versionName
        )
        else -> Unit
    }
}

@Composable
private fun SettingsHomeScreen(
    onBack: () -> Unit,
    versionName: String,
    onNavigate: (AppPage) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) }
    val themeMode = prefs.getInt("theme_mode_index", 0)
    val themeSummary = listOf(
        stringResource(R.string.settings_theme_system),
        stringResource(R.string.settings_theme_light),
        stringResource(R.string.settings_theme_dark)
    )[themeMode.coerceIn(0, 2)]

    SettingsScaffold(
        title = stringResource(R.string.home_settings_title),
        onBack = onBack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.settings_section_connection))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_connection),
                    subtitle = stringResource(R.string.settings_connection_notifications_subtitle),
                    value = boolSummary(prefs.getBoolean("auto_connect_on_launch", true)),
                    leadingIcon = Icons.Default.Bluetooth,
                    onClick = { onNavigate(AppPage.SETTINGS_CONNECTION) }
                )
            }

            SettingsGroupTitle(stringResource(R.string.settings_section_touchpad))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_touchpad),
                    subtitle = stringResource(R.string.settings_touchpad_sensitivity),
                    value = prefs.getInt("touchpad_sensitivity", 5).toString(),
                    leadingIcon = Icons.Default.Mouse,
                    onClick = { onNavigate(AppPage.SETTINGS_TOUCHPAD) }
                )
            }

            SettingsGroupTitle(stringResource(R.string.settings_section_interaction))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_interaction),
                    subtitle = stringResource(R.string.settings_haptic_feedback_subtitle),
                    value = boolSummary(prefs.getBoolean("haptic_feedback", true)),
                    onClick = { onNavigate(AppPage.SETTINGS_INTERACTION) }
                )
            }

            SettingsGroupTitle(stringResource(R.string.settings_section_appearance))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_theme_mode),
                    subtitle = stringResource(R.string.home_settings_subtitle),
                    value = themeSummary,
                    leadingIcon = Icons.Default.Settings,
                    onClick = { onNavigate(AppPage.SETTINGS_APPEARANCE) }
                )
            }

            SettingsGroupTitle(stringResource(R.string.settings_section_data))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_data),
                    subtitle = stringResource(R.string.settings_reset_macros),
                    onClick = { onNavigate(AppPage.SETTINGS_DATA) }
                )
            }

            SettingsGroupTitle(stringResource(R.string.settings_section_about))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_about),
                    subtitle = stringResource(R.string.settings_app_name_label),
                    value = versionName,
                    onClick = { onNavigate(AppPage.SETTINGS_ABOUT) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConnectionSettingsScreen(
    onBack: () -> Unit,
    onBooleanSettingChanged: (String, Boolean) -> Unit
) {
    val prefs = settingsPrefs()
    var autoConnect by rememberBooleanSetting(prefs, "auto_connect_on_launch", true)
    var autoReconnect by rememberBooleanSetting(prefs, "auto_reconnect_on_disconnect", true)
    var keepScreenOn by rememberBooleanSetting(prefs, "keep_screen_on", true)
    var notifications by rememberBooleanSetting(prefs, "connection_notifications", true)

    SettingsScaffold(title = stringResource(R.string.settings_section_connection), onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.settings_section_connection))
            SettingsGroup {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_auto_connect_launch),
                    checked = autoConnect,
                    onCheckedChange = { autoConnect = it; saveBoolean(prefs, "auto_connect_on_launch", it, onBooleanSettingChanged) }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_auto_reconnect),
                    checked = autoReconnect,
                    onCheckedChange = { autoReconnect = it; saveBoolean(prefs, "auto_reconnect_on_disconnect", it, onBooleanSettingChanged) }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_keep_screen_on),
                    subtitle = stringResource(R.string.settings_keep_screen_on_subtitle),
                    checked = keepScreenOn,
                    onCheckedChange = { keepScreenOn = it; saveBoolean(prefs, "keep_screen_on", it, onBooleanSettingChanged) }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_connection_notifications),
                    subtitle = stringResource(R.string.settings_connection_notifications_subtitle),
                    checked = notifications,
                    onCheckedChange = { notifications = it; saveBoolean(prefs, "connection_notifications", it, onBooleanSettingChanged) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TouchpadSettingsScreen(onBack: () -> Unit) {
    val prefs = settingsPrefs()
    var sensitivity by rememberIntSetting(prefs, "touchpad_sensitivity", 5)
    var cursorSpeed by rememberIntSetting(prefs, "cursor_speed", 5)
    var naturalScroll by rememberBooleanSetting(prefs, "scroll_direction_natural", false)

    SettingsScaffold(title = stringResource(R.string.settings_section_touchpad), onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.settings_section_touchpad))
            SettingsGroup {
                SettingsSliderItem(
                    title = stringResource(R.string.settings_touchpad_sensitivity),
                    value = sensitivity.toFloat(),
                    onValueChange = { value ->
                        sensitivity = value.toInt()
                        prefs.edit().putInt("touchpad_sensitivity", sensitivity).apply()
                    },
                    valueRange = 1f..10f,
                    steps = 8
                )
                SettingsDivider()
                SettingsSliderItem(
                    title = stringResource(R.string.settings_cursor_speed),
                    value = cursorSpeed.toFloat(),
                    onValueChange = { value ->
                        cursorSpeed = value.toInt()
                        prefs.edit().putInt("cursor_speed", cursorSpeed).apply()
                    },
                    valueRange = 1f..10f,
                    steps = 8
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_scroll_direction_natural),
                    checked = naturalScroll,
                    onCheckedChange = {
                        naturalScroll = it
                        prefs.edit().putBoolean("scroll_direction_natural", it).apply()
                    }
                )
            }
            SettingsInfoText(stringResource(R.string.home_touchpad_subtitle))
        }
    }
}

@Composable
private fun InteractionSettingsScreen(onBack: () -> Unit) {
    val prefs = settingsPrefs()
    var haptic by rememberBooleanSetting(prefs, "haptic_feedback", true)

    SettingsScaffold(title = stringResource(R.string.settings_section_interaction), onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.settings_section_interaction))
            SettingsGroup {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_haptic_feedback),
                    subtitle = stringResource(R.string.settings_haptic_feedback_subtitle),
                    checked = haptic,
                    onCheckedChange = {
                        haptic = it
                        prefs.edit().putBoolean("haptic_feedback", it).apply()
                    }
                )
            }
        }
    }
}

@Composable
private fun DataSettingsScreen(
    onBack: () -> Unit,
    onResetMacros: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    SettingsScaffold(title = stringResource(R.string.settings_section_data), onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsInfoText(stringResource(R.string.dialog_reset_macros_confirm))
            SettingsGroupTitle(stringResource(R.string.settings_section_data))
            SettingsGroup {
                SettingsTextItem(
                    title = stringResource(R.string.settings_reset_macros),
                    subtitle = stringResource(R.string.dialog_reset_macros_confirm),
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = { showConfirm = true }
                )
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.settings_reset_macros)) },
            text = { Text(stringResource(R.string.dialog_reset_macros_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    onResetMacros()
                    showConfirm = false
                }) {
                    Text(stringResource(R.string.dialog_reset), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    onThemeModeChanged: (Int) -> Unit
) {
    val prefs = settingsPrefs()
    var selected by rememberIntSetting(prefs, "theme_mode_index", 0)
    val options = listOf(
        stringResource(R.string.settings_theme_system),
        stringResource(R.string.settings_theme_light),
        stringResource(R.string.settings_theme_dark)
    )

    SettingsScaffold(title = stringResource(R.string.settings_section_appearance), onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.settings_theme_mode))
            SettingsGroup {
                options.forEachIndexed { index, title ->
                    SettingsRadioItem(
                        title = title,
                        selected = selected == index,
                        onClick = {
                            selected = index
                            prefs.edit().putInt("theme_mode_index", index).apply()
                            onThemeModeChanged(index)
                        }
                    )
                    if (index < options.lastIndex) SettingsDivider()
                }
            }
        }
    }
}

@Composable
private fun AboutSettingsScreen(
    onBack: () -> Unit,
    versionName: String
) {
    SettingsScaffold(title = stringResource(R.string.settings_section_about), onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.settings_section_about))
            SettingsGroup {
                SettingsItem(
                    title = stringResource(R.string.settings_app_name_label),
                    trailing = { Text(stringResource(R.string.app_name), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
                SettingsDivider()
                SettingsItem(
                    title = stringResource(R.string.settings_version),
                    trailing = { Text(versionName, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
                SettingsDivider()
                SettingsTextItem(
                    title = stringResource(R.string.settings_open_source),
                    subtitle = stringResource(R.string.app_name)
                )
            }
        }
    }
}

@Composable
private fun settingsPrefs() =
    LocalContext.current.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

@Composable
private fun rememberBooleanSetting(
    prefs: android.content.SharedPreferences,
    key: String,
    defaultValue: Boolean
) = remember(key) { mutableStateOf(prefs.getBoolean(key, defaultValue)) }

@Composable
private fun rememberIntSetting(
    prefs: android.content.SharedPreferences,
    key: String,
    defaultValue: Int
) = remember(key) { mutableIntStateOf(prefs.getInt(key, defaultValue)) }

private fun saveBoolean(
    prefs: android.content.SharedPreferences,
    key: String,
    value: Boolean,
    onBooleanSettingChanged: (String, Boolean) -> Unit
) {
    prefs.edit().putBoolean(key, value).apply()
    onBooleanSettingChanged(key, value)
}

@Composable
private fun connectionStatusText(isConnected: Boolean, connectedDeviceName: String?): String {
    return if (isConnected && connectedDeviceName != null) {
        stringResource(
            R.string.device_name_status,
            connectedDeviceName,
            stringResource(R.string.status_connected_label)
        )
    } else {
        stringResource(R.string.status_not_connected)
    }
}

@Composable
private fun boolSummary(value: Boolean): String {
    return if (value) stringResource(R.string.status_connected_label) else stringResource(R.string.status_not_connected)
}
