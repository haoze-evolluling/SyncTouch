package com.haoze.claudekeyboard.ui.compose

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.haoze.claudekeyboard.R
import com.haoze.claudekeyboard.macro.Macro

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
    HomeScaffold(onNavigateSettings = onNavigateSettings) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.home_connection_status))
            HomeConnectionStatusCard(
                isConnected = isConnected,
                connectedDeviceName = connectedDeviceName,
                onClick = onShowDeviceList
            )

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
private fun HomeScaffold(
    onNavigateSettings: () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.home_hero_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNavigateSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.home_settings_title),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        content = content
    )
}

@Composable
private fun HomeConnectionStatusCard(
    isConnected: Boolean,
    connectedDeviceName: String?,
    onClick: () -> Unit
) {
    val statusText = if (isConnected) {
        stringResource(R.string.status_connected_label)
    } else {
        stringResource(R.string.status_not_connected)
    }
    val detailText = if (isConnected && connectedDeviceName != null) {
        connectedDeviceName
    } else {
        stringResource(R.string.home_connection_disconnected_hint)
    }
    val actionText = if (isConnected) {
        stringResource(R.string.home_connection_action_connected)
    } else {
        stringResource(R.string.home_connection_action_disconnected)
    }
    val statusColor = if (isConnected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    SettingsGroup(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(statusColor, CircleShape)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isConnected) {
                        stringResource(R.string.home_connection_connected_hint)
                    } else {
                        detailText
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isConnected && connectedDeviceName != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = connectedDeviceName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
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
                CoreCommandGrid(onCoreCommand = onCoreCommand)
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

private data class CoreCommandSpec(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val command: CoreCommand
)

@Composable
private fun CoreCommandGrid(
    onCoreCommand: (CoreCommand) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    val commands = listOf(
        CoreCommandSpec(stringResource(R.string.btn_yes), Icons.Default.Check, primary, CoreCommand.YES),
        CoreCommandSpec(stringResource(R.string.btn_yes_to_all), Icons.Default.Check, primary, CoreCommand.YES_TO_ALL),
        CoreCommandSpec(stringResource(R.string.btn_no), Icons.Default.PowerSettingsNew, error, CoreCommand.NO),
        CoreCommandSpec(stringResource(R.string.btn_ctrl_c), Icons.Default.Keyboard, primary, CoreCommand.CTRL_C),
        CoreCommandSpec(stringResource(R.string.btn_backspace), Icons.Default.Keyboard, error, CoreCommand.BACKSPACE),
        CoreCommandSpec(stringResource(R.string.btn_enter), Icons.Default.Keyboard, primary, CoreCommand.ENTER)
    )

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        commands.chunked(2).forEachIndexed { rowIndex, rowCommands ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                CoreCommandTile(
                    spec = rowCommands[0],
                    modifier = Modifier.weight(1f),
                    onClick = { onCoreCommand(rowCommands[0].command) }
                )
                if (rowCommands.size == 1) {
                    Spacer(Modifier.weight(1f))
                } else {
                    CoreCommandVerticalDivider()
                    CoreCommandTile(
                        spec = rowCommands[1],
                        modifier = Modifier.weight(1f),
                        onClick = { onCoreCommand(rowCommands[1].command) }
                    )
                }
            }
            if (rowIndex < commands.lastIndex / 2) {
                SettingsDivider()
            }
        }
    }
}

@Composable
private fun CoreCommandTile(
    spec: CoreCommandSpec,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = spec.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = spec.color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = spec.icon,
            contentDescription = null,
            tint = spec.color,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun CoreCommandVerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outlineVariant)
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
            SettingsGroupTitle(stringResource(R.string.settings_group_connect_control))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_connection),
                    subtitle = stringResource(R.string.settings_summary_connection),
                    value = boolSummary(prefs.getBoolean("auto_connect_on_launch", true)),
                    onClick = { onNavigate(AppPage.SETTINGS_CONNECTION) }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_touchpad),
                    subtitle = stringResource(R.string.settings_summary_touchpad),
                    value = prefs.getInt("touchpad_sensitivity", 5).toString(),
                    onClick = { onNavigate(AppPage.SETTINGS_TOUCHPAD) }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_interaction),
                    subtitle = stringResource(R.string.settings_summary_interaction),
                    value = boolSummary(prefs.getBoolean("haptic_feedback", true)),
                    onClick = { onNavigate(AppPage.SETTINGS_INTERACTION) }
                )
            }

            SettingsGroupTitle(stringResource(R.string.settings_group_experience))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_appearance),
                    subtitle = stringResource(R.string.settings_summary_appearance),
                    value = themeSummary,
                    onClick = { onNavigate(AppPage.SETTINGS_APPEARANCE) }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_data),
                    subtitle = stringResource(R.string.settings_summary_data),
                    onClick = { onNavigate(AppPage.SETTINGS_DATA) }
                )
            }

            SettingsGroupTitle(stringResource(R.string.settings_group_information))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_section_about),
                    subtitle = stringResource(R.string.settings_summary_about),
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
            SettingsGroupTitle(stringResource(R.string.settings_group_connection_behavior))
            SettingsGroup {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_auto_connect_launch),
                    subtitle = stringResource(R.string.settings_auto_connect_launch_subtitle),
                    checked = autoConnect,
                    onCheckedChange = { autoConnect = it; saveBoolean(prefs, "auto_connect_on_launch", it, onBooleanSettingChanged) }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_auto_reconnect),
                    subtitle = stringResource(R.string.settings_auto_reconnect_subtitle),
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
            SettingsGroupTitle(stringResource(R.string.settings_group_pointer_scroll))
            SettingsGroup {
                SettingsSliderItem(
                    title = stringResource(R.string.settings_touchpad_sensitivity),
                    subtitle = stringResource(R.string.settings_touchpad_sensitivity_subtitle),
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
                    subtitle = stringResource(R.string.settings_cursor_speed_subtitle),
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
                    subtitle = stringResource(R.string.settings_scroll_direction_natural_subtitle),
                    checked = naturalScroll,
                    onCheckedChange = {
                        naturalScroll = it
                        prefs.edit().putBoolean("scroll_direction_natural", it).apply()
                    }
                )
            }
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
            SettingsGroupTitle(stringResource(R.string.settings_group_feedback))
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
            SettingsGroupTitle(stringResource(R.string.settings_group_commands))
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
            SettingsGroupTitle(stringResource(R.string.settings_group_theme))
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
    val context = LocalContext.current
    val githubUrl = stringResource(R.string.settings_github_url)

    SettingsScaffold(title = stringResource(R.string.settings_section_about), onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroupTitle(stringResource(R.string.settings_group_about))
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
                    title = stringResource(R.string.settings_github_repo),
                    subtitle = githubUrl,
                    textColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl)))
                    }
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
    return if (value) stringResource(R.string.settings_state_on) else stringResource(R.string.settings_state_off)
}
