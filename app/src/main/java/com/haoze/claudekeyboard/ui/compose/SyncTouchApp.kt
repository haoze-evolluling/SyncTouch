package com.haoze.claudekeyboard.ui.compose

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haoze.claudekeyboard.R
import com.haoze.claudekeyboard.macro.Macro
import kotlinx.coroutines.delay

enum class AppPage {
    HOME,
    AGENT,
    TV_REMOTE,
    SETTINGS,
    SETTINGS_CONNECTION,
    SETTINGS_APPEARANCE,
    SETTINGS_DATA,
    SETTINGS_INPUT,
    SETTINGS_FEEDBACK,
    SETTINGS_ABOUT,
    SETTINGS_SPONSOR
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
    showDeviceList: Boolean,
    pairedDevices: List<BluetoothDevice>,
    deviceListPermissionDenied: Boolean,
    connectingDeviceAddress: String?,
    connectedDeviceAddress: String?,
    lastConnectedDeviceAddress: String?,
    onDismissDeviceList: () -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit,
    onDisconnectDevice: () -> Unit,
    onConnectionTimeout: () -> Unit,
    onCoreCommand: (CoreCommand) -> Unit,
    onMacroClick: (Macro) -> Unit,
    onSaveMacro: (String?, String, String, String, Boolean) -> Unit,
    onDeleteMacro: (String) -> Unit,
    onResetMacros: () -> Unit,
    onBooleanSettingChanged: (String, Boolean) -> Unit,
    onThemeModeChanged: (Int) -> Unit,
    onTvRemoteAction: (TvRemoteAction) -> Unit
) {
    var showMacroEditor by remember { mutableStateOf(false) }
    var editingMacro by remember { mutableStateOf<Macro?>(null) }
    var pendingDeleteMacroId by remember { mutableStateOf<String?>(null) }
    var showConnectionTimeout by remember { mutableStateOf(false) }

    when (page) {
        AppPage.HOME -> HomeScreen(
            isConnected = isConnected,
            connectedDeviceName = connectedDeviceName,
            onShowDeviceList = onShowDeviceList,
            onOpenKeyboard = onOpenKeyboard,
            onOpenTouchpad = onOpenTouchpad,
            onNavigateAgent = { onNavigate(AppPage.AGENT) },
            onNavigateTvRemote = { onNavigate(AppPage.TV_REMOTE) },
            onNavigateSettings = { onNavigate(AppPage.SETTINGS) },
            onNavigateAbout = { onNavigate(AppPage.SETTINGS_ABOUT) },
            onNavigateSponsor = { onNavigate(AppPage.SETTINGS_SPONSOR) }
        )
        AppPage.AGENT -> AgentScreen(
            isConnected = isConnected,
            connectedDeviceName = connectedDeviceName,
            macros = macros,
            onBack = onNavigateHome,
            onCoreCommand = onCoreCommand,
            onMacroClick = onMacroClick,
            onMacroLongClick = { macro ->
                editingMacro = macro
                showMacroEditor = true
            },
            onAddMacro = {
                editingMacro = null
                showMacroEditor = true
            }
        )
        AppPage.TV_REMOTE -> TvRemoteScreen(
            enabled = isConnected,
            onBack = onNavigateHome,
            onAction = onTvRemoteAction
        )
        AppPage.SETTINGS,
        AppPage.SETTINGS_CONNECTION,
        AppPage.SETTINGS_APPEARANCE,
        AppPage.SETTINGS_DATA,
        AppPage.SETTINGS_INPUT,
        AppPage.SETTINGS_FEEDBACK -> SettingsScreenRoot(
            page = page,
            onBackHome = onNavigateHome,
            onNavigate = onNavigate,
            onResetMacros = onResetMacros,
            onBooleanSettingChanged = onBooleanSettingChanged,
            onThemeModeChanged = onThemeModeChanged
        )
        AppPage.SETTINGS_ABOUT -> AboutSettingsScreen(
            onBack = onNavigateHome,
            versionName = versionName,
            isConnected = isConnected
        )
        AppPage.SETTINGS_SPONSOR -> SponsorSettingsScreen(onBack = onNavigateHome)
    }
    if (showMacroEditor) {
        MacroEditorAlertDialog(
            macro = editingMacro,
            onDismiss = { showMacroEditor = false },
            onSave = { label, description, command, sendEnter ->
                onSaveMacro(editingMacro?.id, label, description, command, sendEnter)
                showMacroEditor = false
            },
            onDelete = editingMacro?.id?.let { id -> {
                showMacroEditor = false
                pendingDeleteMacroId = id
            } }
        )
    }
    pendingDeleteMacroId?.let { id ->
        SyncTouchConfirmationDialog(
            stringResource(R.string.dialog_delete_macro),
            stringResource(R.string.dialog_delete_macro_message),
            stringResource(R.string.dialog_delete),
            true,
            { onDeleteMacro(id); pendingDeleteMacroId = null },
            { pendingDeleteMacroId = null }
        )
    }
    if (showDeviceList) {
        DeviceListAlertDialog(
            devices = pairedDevices,
            permissionDenied = deviceListPermissionDenied,
            connectingAddress = connectingDeviceAddress,
            connectedAddress = connectedDeviceAddress,
            lastConnectedAddress = lastConnectedDeviceAddress,
            onDismiss = onDismissDeviceList,
            onDeviceSelected = onConnectDevice,
            onDisconnect = onDisconnectDevice,
            onConnectionTimeout = {
                onConnectionTimeout()
                onDismissDeviceList()
                showConnectionTimeout = true
            }
        )
    }
    if (showConnectionTimeout) {
        ConnectionTimeoutAlertDialog(onDismiss = { showConnectionTimeout = false })
    }
}

@Composable
private fun SyncTouchConfirmationDialog(title: String, message: String, confirmLabel: String, destructive: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(message) }, confirmButton = {
        TextButton(onClick = onConfirm) { Text(confirmLabel, color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } })
}

@Composable
private fun DeviceListAlertDialog(
    devices: List<BluetoothDevice>,
    permissionDenied: Boolean,
    connectingAddress: String?,
    connectedAddress: String?,
    lastConnectedAddress: String?,
    onDismiss: () -> Unit,
    onDeviceSelected: (BluetoothDevice) -> Unit,
    onDisconnect: () -> Unit,
    onConnectionTimeout: () -> Unit
) {
    LaunchedEffect(connectingAddress) {
        if (connectingAddress != null) {
            delay(10_000L)
            onConnectionTimeout()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.device_list_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.device_list_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                when {
                    permissionDenied -> DeviceListMessage(stringResource(R.string.toast_permission_denied))
                    devices.isEmpty() -> DeviceListMessage(stringResource(R.string.device_no_paired))
                    else -> LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        itemsIndexed(devices, key = { _, device -> device.address }) { index, device ->
                            DeviceListItem(
                                device = device,
                                colorIndex = index,
                                connectingAddress = connectingAddress,
                                connectedAddress = connectedAddress,
                                lastConnectedAddress = lastConnectedAddress,
                                onClick = { onDeviceSelected(device) },
                                onDisconnect = onDisconnect
                            )
                            if (index < devices.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
private fun DeviceListMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DeviceListItem(
    device: BluetoothDevice,
    colorIndex: Int,
    connectingAddress: String?,
    connectedAddress: String?,
    lastConnectedAddress: String?,
    onClick: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isConnecting = device.address == connectingAddress
    val isConnected = device.address == connectedAddress
    val status = when {
        isConnecting -> stringResource(R.string.device_connecting)
        isConnected -> stringResource(R.string.status_connected_label)
        device.address == lastConnectedAddress -> stringResource(R.string.device_last_connected)
        else -> null
    }
    val canSelect = !isConnected && connectingAddress == null
    val interactionSource = remember { MutableInteractionSource() }
    val bluetoothColors = monetBluetoothColors(colorIndex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = canSelect,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(bluetoothColors.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = bluetoothColors.foreground
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.name ?: stringResource(R.string.status_unknown_device),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    status?.let {
                        Text(
                            text = it,
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = device.address,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isConnected) {
                TextButton(onClick = onDisconnect) {
                    Text(stringResource(R.string.btn_disconnect), color = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (isConnecting) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
        }
    }
}

private data class BluetoothIconColors(
    val background: Color,
    val foreground: Color
)

private fun monetBluetoothColors(index: Int): BluetoothIconColors {
    val palette = listOf(
        BluetoothIconColors(Color(0xFFD7E8ED), Color(0xFF245A6D)), // Water lily blue
        BluetoothIconColors(Color(0xFFE3DCEA), Color(0xFF57416D)), // Iris violet
        BluetoothIconColors(Color(0xFFDCE9DB), Color(0xFF365B42)), // Garden green
        BluetoothIconColors(Color(0xFFEEDBDD), Color(0xFF713E4B)), // Rose garden
        BluetoothIconColors(Color(0xFFF0E7C9), Color(0xFF6A5725)), // Sunlit haystack
        BluetoothIconColors(Color(0xFFDDE9E7), Color(0xFF2C5D58)), // Morning mist
        BluetoothIconColors(Color(0xFFEADBD3), Color(0xFF70483B)), // Warm reflection
        BluetoothIconColors(Color(0xFFDCE3F0), Color(0xFF354C78))  // Evening sky
    )
    return palette[index % palette.size]
}

@Composable
private fun ConnectionTimeoutAlertDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_connect_timeout_title)) },
        text = { Text(stringResource(R.string.dialog_connect_timeout_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_ok)) }
        }
    )
}

@Composable
private fun MacroEditorAlertDialog(macro: Macro?, onDismiss: () -> Unit, onSave: (String, String, String, Boolean) -> Unit, onDelete: (() -> Unit)?) {
    var label by remember(macro) { mutableStateOf(macro?.label.orEmpty()) }
    var description by remember(macro) { mutableStateOf(macro?.description.orEmpty()) }
    var command by remember(macro) { mutableStateOf(macro?.command.orEmpty()) }
    var sendEnter by remember(macro) { mutableStateOf(macro?.sendEnter ?: false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(if (macro == null) R.string.dialog_add_macro else R.string.dialog_edit_macro)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(label, { label = it }, label = { Text(stringResource(R.string.dialog_macro_label)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, { description = it }, label = { Text(stringResource(R.string.dialog_macro_description)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(command, { command = it }, label = { Text(stringResource(R.string.dialog_macro_command)) }, minLines = 3, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.switch_send_enter), Modifier.weight(1f)); Switch(sendEnter, { sendEnter = it }) }
        }
    }, confirmButton = {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            onDelete?.let { delete ->
                TextButton(onClick = delete) {
                    Text(stringResource(R.string.dialog_delete), color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
            TextButton(onClick = {
                val trimmedLabel = label.trim()
                val trimmedCommand = command.trim()
                if (trimmedLabel.isNotEmpty() && trimmedCommand.isNotEmpty()) {
                    onSave(trimmedLabel, description.trim(), trimmedCommand, sendEnter)
                } else {
                    onDismiss()
                }
            }) { Text(stringResource(R.string.dialog_save)) }
        }
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(
    isConnected: Boolean,
    connectedDeviceName: String?,
    onShowDeviceList: () -> Unit,
    onOpenKeyboard: () -> Unit,
    onOpenTouchpad: () -> Unit,
    onNavigateAgent: () -> Unit,
    onNavigateTvRemote: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateSponsor: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = 1) { 2 }

    LaunchedEffect(Unit) {
        val preferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        if (!preferences.getBoolean("home_feature_hub_peek_shown", false)) {
            preferences.edit().putBoolean("home_feature_hub_peek_shown", true).apply()
            delay(500)
            pagerState.animateScrollToPage(
                page = 1,
                pageOffsetFraction = -0.3f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
            delay(650)
            pagerState.animateScrollToPage(
                page = 1,
                pageOffsetFraction = 0.018f,
                animationSpec = tween(durationMillis = 285, easing = FastOutSlowInEasing)
            )
            pagerState.animateScrollToPage(
                page = 1,
                pageOffsetFraction = 0f,
                animationSpec = tween(durationMillis = 95, easing = FastOutSlowInEasing)
            )
        }
    }

    HomeScaffold { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> HomeFeatureHub(
                    onOpenKeyboard = onOpenKeyboard,
                    onOpenTouchpad = onOpenTouchpad,
                    onNavigateAgent = onNavigateAgent,
                    onNavigateTvRemote = onNavigateTvRemote,
                    onNavigateSettings = onNavigateSettings,
                    onNavigateAbout = onNavigateAbout,
                    onNavigateSponsor = onNavigateSponsor
                )
                else -> HomeConnectionPage(
                    isConnected = isConnected,
                    connectedDeviceName = connectedDeviceName,
                    onShowDeviceList = onShowDeviceList
                )
            }
        }
    }
}

@Composable
private fun HomeScaffold(content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        content = content
    )
}

@Composable
private fun HomeConnectionPage(
    isConnected: Boolean,
    connectedDeviceName: String?,
    onShowDeviceList: () -> Unit
) {
    val glowColor by animateColorAsState(
        targetValue = if (isConnected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        },
        animationSpec = tween(250),
        label = "BluetoothControlGlowColor"
    )
    val haloColor by animateColorAsState(
        targetValue = if (isConnected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(250),
        label = "BluetoothControlHaloColor"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(250),
        label = "BluetoothControlContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isConnected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(250),
        label = "BluetoothControlContentColor"
    )
    val haloSize by animateDpAsState(if (isConnected) 148.dp else 124.dp, tween(250), label = "BluetoothControlHaloSize")
    val glowSize by animateDpAsState(if (isConnected) 128.dp else 108.dp, tween(250), label = "BluetoothControlGlowSize")
    val buttonSize by animateDpAsState(if (isConnected) 92.dp else 84.dp, tween(250), label = "BluetoothControlButtonSize")
    val statusText = stringResource(
        if (isConnected) R.string.status_connected_label else R.string.status_not_connected
    )
    val hintText = if (isConnected) {
        connectedDeviceName ?: stringResource(R.string.home_connection_connected_hint)
    } else {
        stringResource(R.string.home_connection_disconnected_hint)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(156.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(haloSize)
                    .background(haloColor, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(glowSize)
                    .background(glowColor, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .background(containerColor, CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onShowDeviceList),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = stringResource(
                        if (isConnected) R.string.home_connection_action_connected
                        else R.string.home_connection_action_disconnected
                    ),
                    tint = contentColor,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
        Text(
            text = statusText,
            modifier = Modifier.padding(top = 20.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = hintText,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        HomeDeviceSelector(
            isConnected = isConnected,
            connectedDeviceName = connectedDeviceName,
            onClick = onShowDeviceList,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
private fun HomeDeviceSelector(
    isConnected: Boolean,
    connectedDeviceName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceName = if (isConnected && !connectedDeviceName.isNullOrBlank()) {
        connectedDeviceName
    } else {
        stringResource(R.string.home_select_device)
    }
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = deviceName,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.home_device_selector_label)) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.home_connection_action_disconnected)
                )
            },
            shape = SettingsCornerShape,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(SettingsCornerShape)
                .clickable(onClick = onClick)
        )
    }
}

private data class HomeFeatureItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun HomeFeatureHub(
    onOpenKeyboard: () -> Unit,
    onOpenTouchpad: () -> Unit,
    onNavigateAgent: () -> Unit,
    onNavigateTvRemote: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateSponsor: () -> Unit
) {
    val inputItems = listOf(
        HomeFeatureItem(stringResource(R.string.home_keyboard_title), Icons.Default.Keyboard, onOpenKeyboard),
        HomeFeatureItem(stringResource(R.string.home_touchpad_title), Icons.Default.Mouse, onOpenTouchpad),
        HomeFeatureItem(stringResource(R.string.home_tvremote_title), Icons.Default.SettingsRemote, onNavigateTvRemote)
    )
    val systemItems = listOf(
        HomeFeatureItem(stringResource(R.string.home_agent_title), Icons.Default.Terminal, onNavigateAgent),
        HomeFeatureItem(stringResource(R.string.home_settings_title), Icons.Default.Settings, onNavigateSettings)
    )
    val aboutItems = listOf(
        HomeFeatureItem(stringResource(R.string.home_about_title), Icons.Default.Info, onNavigateAbout),
        HomeFeatureItem(stringResource(R.string.home_sponsor_title), Icons.Default.Favorite, onNavigateSponsor)
    )
    val inputControlsTitle = stringResource(R.string.home_input_controls)
    val shortcutsAndSystemTitle = stringResource(R.string.home_shortcuts_and_system)
    val aboutTitle = stringResource(R.string.home_about_support)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        homeFeatureCategory(inputControlsTitle, inputItems)
        homeFeatureCategory(shortcutsAndSystemTitle, systemItems)
        homeFeatureCategory(aboutTitle, aboutItems)
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.homeFeatureCategory(
    title: String,
    featureItems: List<HomeFeatureItem>
) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    items(featureItems) { item ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .clickable(onClick = item.onClick),
            shape = SettingsCornerShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AgentScreen(
    isConnected: Boolean,
    connectedDeviceName: String?,
    macros: List<Macro>,
    onBack: () -> Unit,
    onCoreCommand: (CoreCommand) -> Unit,
    onMacroClick: (Macro) -> Unit,
    onMacroLongClick: (Macro) -> Unit,
    onAddMacro: () -> Unit
) {
    SettingsScaffold(
        title = stringResource(R.string.home_agent_title),
        onBack = onBack
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
    onBackHome: () -> Unit,
    onNavigate: (AppPage) -> Unit,
    onResetMacros: () -> Unit,
    onBooleanSettingChanged: (String, Boolean) -> Unit,
    onThemeModeChanged: (Int) -> Unit
) {
    when (page) {
        AppPage.SETTINGS -> SettingsHomeScreen(
            onBack = onBackHome,
            onNavigate = onNavigate
        )
        AppPage.SETTINGS_CONNECTION -> ConnectionSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) },
            onBooleanSettingChanged = onBooleanSettingChanged
        )
        AppPage.SETTINGS_APPEARANCE -> AppearanceSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) },
            onThemeModeChanged = onThemeModeChanged
        )
        AppPage.SETTINGS_DATA -> DataSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) },
            onResetMacros = onResetMacros
        )
        AppPage.SETTINGS_INPUT -> InputSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) }
        )
        AppPage.SETTINGS_FEEDBACK -> FeedbackSettingsScreen(
            onBack = { onNavigate(AppPage.SETTINGS) }
        )
        else -> Unit
    }
}

@Composable
private fun SettingsHomeScreen(
    onBack: () -> Unit,
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
            SettingsGroupTitle(stringResource(R.string.settings_group_appearance_data))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_theme_mode),
                    subtitle = stringResource(R.string.settings_summary_appearance),
                    value = themeSummary,
                    onClick = { onNavigate(AppPage.SETTINGS_APPEARANCE) }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_group_commands),
                    subtitle = stringResource(R.string.settings_summary_data),
                    onClick = { onNavigate(AppPage.SETTINGS_DATA) }
                )
            }

            SettingsGroupTitle(stringResource(R.string.settings_group_input_feedback))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.home_touchpad_title),
                    subtitle = stringResource(R.string.settings_summary_input),
                    value = prefs.getInt("touchpad_sensitivity", 5).toString(),
                    onClick = { onNavigate(AppPage.SETTINGS_INPUT) }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_haptic_feedback),
                    subtitle = stringResource(R.string.settings_summary_feedback),
                    value = boolSummary(prefs.getBoolean("haptic_feedback", true)),
                    onClick = { onNavigate(AppPage.SETTINGS_FEEDBACK) }
                )
            }

            SettingsGroupTitle(stringResource(R.string.settings_group_connection))
            SettingsGroup {
                SettingsNavigationItem(
                    title = stringResource(R.string.settings_connection_route_title),
                    subtitle = stringResource(R.string.settings_summary_connection),
                    value = boolSummary(prefs.getBoolean("auto_connect_on_launch", true)),
                    onClick = { onNavigate(AppPage.SETTINGS_CONNECTION) }
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
private fun InputSettingsScreen(onBack: () -> Unit) {
    val prefs = settingsPrefs()
    var sensitivity by rememberIntSetting(prefs, "touchpad_sensitivity", 5)
    var cursorSpeed by rememberIntSetting(prefs, "cursor_speed", 5)
    var naturalScroll by rememberBooleanSetting(prefs, "scroll_direction_natural", false)

    SettingsScaffold(title = stringResource(R.string.settings_section_input), onBack = onBack) { innerPadding ->
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
private fun FeedbackSettingsScreen(onBack: () -> Unit) {
    val prefs = settingsPrefs()
    var haptic by rememberBooleanSetting(prefs, "haptic_feedback", true)

    SettingsScaffold(title = stringResource(R.string.settings_section_feedback), onBack = onBack) { innerPadding ->
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
