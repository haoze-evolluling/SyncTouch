package com.haoze.claudekeyboard

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.haoze.claudekeyboard.bluetooth.BluetoothViewModel
import com.haoze.claudekeyboard.bluetooth.KeyboardSender
import com.haoze.claudekeyboard.macro.Macro
import com.haoze.claudekeyboard.macro.MacroRepository
import com.haoze.claudekeyboard.ui.compose.AppPage
import com.haoze.claudekeyboard.ui.compose.CoreCommand
import com.haoze.claudekeyboard.ui.compose.SyncTouchApp
import com.haoze.claudekeyboard.ui.compose.SyncTouchTheme
import com.haoze.claudekeyboard.ui.compose.TvRemoteAction
import com.haoze.claudekeyboard.ui.keyboard.KeyboardFragment
import com.haoze.claudekeyboard.ui.touchpad.TouchpadFragment
import com.haoze.claudekeyboard.util.dynamicSurfaceColor

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 0
    }

    private val bluetoothViewModel: BluetoothViewModel by viewModels()
    private lateinit var macroRepository: MacroRepository

    private lateinit var contentCompose: View
    private lateinit var contentKeyboard: View
    private lateinit var contentTouchpad: View
    private val allContentViews by lazy { listOf(contentCompose, contentKeyboard, contentTouchpad) }

    private var keyboardFragment: KeyboardFragment? = null
    private var touchpadFragment: TouchpadFragment? = null
    private var currentPage by mutableStateOf(AppPage.HOME)
    private var isConnectedState by mutableStateOf(false)
    private var connectedDeviceNameState by mutableStateOf<String?>(null)
    private var macrosState by mutableStateOf<List<Macro>>(emptyList())
    private var showDeviceListDialog by mutableStateOf(false)
    private var pairedDevicesState by mutableStateOf<List<BluetoothDevice>>(emptyList())
    private var deviceListPermissionDenied by mutableStateOf(false)
    private var connectingDeviceAddress by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val themeIndex = prefs.getInt("theme_mode_index", 0)
        AppCompatDelegate.setDefaultNightMode(
            when (themeIndex) {
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        if (!bluetoothViewModel.hasBluetoothSupport()) {
            Toast.makeText(this, R.string.toast_bluetooth_not_supported, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val missing = getMissingPermissions()
        if (missing.isNotEmpty()) {
            requestPermissions(missing, REQUEST_CODE_PERMISSIONS)
        } else {
            bluetoothViewModel.startAndBindService()
        }

        macroRepository = MacroRepository(this)
        initViews()
        applyDynamicViewBackgrounds()
        setupWindowInsets()
        setupComposeContent()
        loadMacros()
        observeViewModel()

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    contentCompose.visibility != View.VISIBLE -> navigateToHome()
                    currentPage.isSettingsDetail() -> navigateToPortrait(AppPage.SETTINGS)
                    currentPage != AppPage.HOME -> navigateToHome()
                    else -> finish()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isEmpty() || grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, R.string.toast_permission_denied, Toast.LENGTH_SHORT).show()
            } else {
                bluetoothViewModel.startAndBindService()
            }
        }
    }

    private fun getMissingPermissions(): Array<String> {
        val required = mutableListOf<String>()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    required.add(Manifest.permission.BLUETOOTH_CONNECT)
                }
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    required.add(Manifest.permission.BLUETOOTH_ADVERTISE)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    required.add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else -> {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    required.add(Manifest.permission.BLUETOOTH)
                }
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    required.add(Manifest.permission.BLUETOOTH_ADMIN)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            required.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return required.toTypedArray()
    }

    private fun initViews() {
        contentCompose = findViewById(R.id.content_compose)
        contentKeyboard = findViewById(R.id.content_keyboard)
        contentTouchpad = findViewById(R.id.content_touchpad)
    }

    private fun applyDynamicViewBackgrounds() {
        val surfaceColor = dynamicSurfaceColor()
        findViewById<View>(R.id.main).setBackgroundColor(surfaceColor)
        contentKeyboard.setBackgroundColor(surfaceColor)
        contentTouchpad.setBackgroundColor(surfaceColor)
    }

    private fun setupWindowInsets() {
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) insets.displayCutout else null

            val left = maxOf(systemBars.left, cutout?.safeInsetLeft ?: 0)
            val top = maxOf(systemBars.top, cutout?.safeInsetTop ?: 0)
            val right = maxOf(systemBars.right, cutout?.safeInsetRight ?: 0)
            v.setPadding(left, 0, right, 0)
            contentKeyboard.setPadding(0, top, 0, 0)
            contentTouchpad.setPadding(0, top, 0, 0)
            insets
        }
    }

    private fun setupComposeContent() {
        findViewById<ComposeView>(R.id.content_compose).setContent {
            SyncTouchTheme {
                SyncTouchApp(
                    page = currentPage,
                    isConnected = isConnectedState,
                    connectedDeviceName = connectedDeviceNameState,
                    versionName = getVersionName(),
                    macros = macrosState,
                    onNavigate = ::navigateToPortrait,
                    onNavigateHome = ::navigateToHome,
                    onOpenKeyboard = { navigateToPage(contentKeyboard, landscape = true) },
                    onOpenTouchpad = { navigateToPage(contentTouchpad, landscape = true) },
                    onShowDeviceList = ::showDeviceListDialog,
                    showDeviceList = showDeviceListDialog,
                    pairedDevices = pairedDevicesState,
                    deviceListPermissionDenied = deviceListPermissionDenied,
                    connectingDeviceAddress = connectingDeviceAddress,
                    connectedDeviceAddress = bluetoothViewModel.getConnectedDeviceAddress(),
                    lastConnectedDeviceAddress = bluetoothViewModel.getLastConnectedDeviceAddress(),
                    onDismissDeviceList = { dismissDeviceList(cancelConnection = true) },
                    onConnectDevice = ::connectToDevice,
                    onDisconnectDevice = ::disconnectDevice,
                    onConnectionTimeout = ::onDeviceConnectionTimeout,
                    onCoreCommand = ::sendCoreCommand,
                    onMacroClick = ::sendMacro,
                    onSaveMacro = { id, label, description, command, sendEnter ->
                        if (id == null) {
                            macroRepository.addCustomMacro(label, description, command, sendEnter)
                            Toast.makeText(this, R.string.toast_macro_added, Toast.LENGTH_SHORT).show()
                        } else {
                            macroRepository.updateCustomMacro(id, label, description, command, sendEnter)
                            Toast.makeText(this, R.string.toast_macro_updated, Toast.LENGTH_SHORT).show()
                        }
                        loadMacros()
                    },
                    onDeleteMacro = { id ->
                        macroRepository.deleteCustomMacro(id)
                        Toast.makeText(this, R.string.toast_macro_deleted, Toast.LENGTH_SHORT).show()
                        loadMacros()
                    },
                    onResetMacros = {
                        macroRepository.resetToDefaults()
                        loadMacros()
                    },
                    onBooleanSettingChanged = ::onBooleanSettingChanged,
                    onThemeModeChanged = ::applyThemeMode,
                    onTvRemoteAction = ::sendTvRemoteAction
                )
            }
        }
    }

    private fun observeViewModel() {
        bluetoothViewModel.connectionState.observe(this) { isConnected ->
            val deviceName = bluetoothViewModel.connectedDeviceName.value
            updateStatusUI(isConnected, deviceName)
            updateKeepScreenOn(isConnected)
            if (isConnected) {
                connectingDeviceAddress = null
                showDeviceListDialog = false
            }
        }

        bluetoothViewModel.connectedDeviceName.observe(this) {
            updateStatusUI(bluetoothViewModel.isConnected(), it)
        }

        bluetoothViewModel.registrationState.observe(this) { isRegistered ->
            if (!isRegistered) {
                Toast.makeText(this, R.string.toast_bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            }
        }

        bluetoothViewModel.keyboardSender.observe(this) {
            updateFragmentEnabledStates()
        }

        bluetoothViewModel.mouseSender.observe(this) {
            updateFragmentEnabledStates()
        }

        bluetoothViewModel.sendError.observe(this) { message ->
            Toast.makeText(this, getString(R.string.toast_send_error, message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOnly(target: View, animate: Boolean = true) {
        val oldView = allContentViews.firstOrNull { it.visibility == View.VISIBLE }
        if (oldView == target || !animate) {
            target.animate().cancel()
            target.alpha = 1f
            target.scaleX = 1f
            target.scaleY = 1f
            allContentViews.forEach { it.visibility = if (it == target) View.VISIBLE else View.GONE }
            return
        }

        oldView?.animate()
            ?.scaleX(0.8f)?.scaleY(0.8f)?.alpha(0f)
            ?.setDuration(150)
            ?.setInterpolator(AccelerateDecelerateInterpolator())
            ?.withEndAction {
                oldView.visibility = View.GONE
                startIncomingAnimation(target)
            }
            ?.start() ?: startIncomingAnimation(target)

        allContentViews.forEach { if (it != target && it != oldView) it.visibility = View.GONE }
    }

    private fun startIncomingAnimation(target: View) {
        target.apply {
            visibility = View.VISIBLE
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f
            animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    fun navigateToPage(targetContent: View, landscape: Boolean) {
        val currentOrientation = resources.configuration.orientation
        val targetOrientation = if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
        val sameOrientation = currentOrientation == targetOrientation

        showOnly(targetContent, animate = sameOrientation)

        requestedOrientation = if (landscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        if (targetContent == contentKeyboard) {
            keyboardFragment = supportFragmentManager.findFragmentById(R.id.keyboard_fragment_container) as? KeyboardFragment
            updateFragmentEnabledStates()
        } else if (targetContent == contentTouchpad) {
            touchpadFragment = supportFragmentManager.findFragmentById(R.id.touchpad_fragment_container) as? TouchpadFragment
            touchpadFragment?.reloadSettings()
            updateFragmentEnabledStates()
        }
    }

    private fun navigateToPortrait(page: AppPage) {
        currentPage = page
        val sameOrientation = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        showOnly(contentCompose, animate = sameOrientation)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        updateKeepScreenOn(bluetoothViewModel.isConnected())
    }

    fun navigateToHome() {
        navigateToPortrait(AppPage.HOME)
    }

    fun switchToTouchpadTab() {
        navigateToPage(contentTouchpad, landscape = true)
    }

    fun switchToKeyboardTab() {
        navigateToPage(contentKeyboard, landscape = true)
    }

    private fun updateStatusUI(isConnected: Boolean, deviceName: String?) {
        isConnectedState = isConnected
        connectedDeviceNameState = deviceName
        updateFragmentEnabledStates()
    }

    private fun updateFragmentEnabledStates() {
        val connected = bluetoothViewModel.isConnected()
        keyboardFragment?.setKeyboardEnabled(connected)
        touchpadFragment?.setTouchpadEnabled(connected)
    }

    private fun updateKeepScreenOn(isConnected: Boolean) {
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val keepScreenOn = prefs.getBoolean("keep_screen_on", true)
        if (isConnected && keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun onBooleanSettingChanged(key: String, value: Boolean) {
        if (key == "connection_notifications" && !value) {
            bluetoothViewModel.dismissConnectionNotification()
        }
        if (key == "keep_screen_on") {
            updateKeepScreenOn(bluetoothViewModel.isConnected())
        }
    }

    private fun applyThemeMode(index: Int) {
        AppCompatDelegate.setDefaultNightMode(
            when (index) {
                1 -> AppCompatDelegate.MODE_NIGHT_NO
                2 -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
        recreate()
    }

    private fun sendCoreCommand(command: CoreCommand) {
        val sender = bluetoothViewModel.getKeyboardSenderDirect() ?: return
        Thread {
            when (command) {
                CoreCommand.YES -> sender.sendText("y")
                CoreCommand.YES_TO_ALL -> sender.sendText("a")
                CoreCommand.NO -> sender.sendText("n")
                CoreCommand.CTRL_C -> sender.sendKeyPress(KeyboardSender.MODIFIER_CTRL_LEFT, KeyboardSender.KEY_C)
                CoreCommand.BACKSPACE -> sender.sendKeyPress(0x00, KeyboardSender.KEY_BACKSPACE)
                CoreCommand.ENTER -> sender.sendKeyPress(0x00, KeyboardSender.KEY_ENTER)
            }
        }.start()
    }

    private fun sendMacro(macro: Macro) {
        bluetoothViewModel.getKeyboardSenderDirect()?.let { sender ->
            Thread {
                if (macro.sendEnter) sender.sendMacro(macro.command) else sender.sendText(macro.command)
            }.start()
        }
    }

    private fun sendTvRemoteAction(action: TvRemoteAction) {
        bluetoothViewModel.getTvRemoteSenderDirect()?.let { sender ->
            when (action) {
                TvRemoteAction.UP -> sender.sendUp()
                TvRemoteAction.DOWN -> sender.sendDown()
                TvRemoteAction.LEFT -> sender.sendLeft()
                TvRemoteAction.RIGHT -> sender.sendRight()
                TvRemoteAction.CONFIRM -> sender.sendConfirm()
                TvRemoteAction.BACK -> sender.sendBack()
                TvRemoteAction.ASSISTANT -> sender.sendAssistant()
                TvRemoteAction.HOME -> sender.sendHome()
                TvRemoteAction.MUTE -> sender.sendMute()
                TvRemoteAction.VOLUME_UP -> sender.sendVolumeUp()
                TvRemoteAction.VOLUME_DOWN -> sender.sendVolumeDown()
                TvRemoteAction.POWER -> sender.sendPower()
                TvRemoteAction.PLAY_PAUSE -> sender.sendPlayPause()
                TvRemoteAction.NEXT -> sender.sendNext()
                TvRemoteAction.PREVIOUS -> sender.sendPrevious()
                TvRemoteAction.STOP -> sender.sendStop()
            }
        }
    }

    private fun loadMacros() {
        macrosState = macroRepository.getAllMacros()
    }

    private fun showDeviceListDialog() {
        deviceListPermissionDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        pairedDevicesState = if (deviceListPermissionDenied) {
            emptyList()
        } else {
            try {
                getSystemService(BluetoothManager::class.java)?.adapter?.bondedDevices?.toList().orEmpty()
            } catch (_: SecurityException) {
                deviceListPermissionDenied = true
                emptyList()
            }
        }
        showDeviceListDialog = true
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (connectingDeviceAddress != null || device.address == bluetoothViewModel.getConnectedDeviceAddress()) return
        connectingDeviceAddress = device.address
        Thread {
            if (!bluetoothViewModel.connectToDevice(device.address)) {
                runOnUiThread {
                    if (connectingDeviceAddress == device.address) {
                        connectingDeviceAddress = null
                        Toast.makeText(this, R.string.dialog_connect_timeout_message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()
    }

    private fun dismissDeviceList(cancelConnection: Boolean) {
        if (cancelConnection && connectingDeviceAddress != null) {
            bluetoothViewModel.disconnect()
        }
        connectingDeviceAddress = null
        showDeviceListDialog = false
    }

    private fun disconnectDevice() {
        bluetoothViewModel.disconnect()
        connectingDeviceAddress = null
        showDeviceListDialog = false
    }

    private fun onDeviceConnectionTimeout() {
        bluetoothViewModel.disconnect()
        connectingDeviceAddress = null
    }

    private fun getVersionName(): String {
        return try {
            val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun AppPage.isSettingsDetail(): Boolean {
        return this in setOf(
            AppPage.SETTINGS_CONNECTION,
            AppPage.SETTINGS_APPEARANCE,
            AppPage.SETTINGS_DATA,
            AppPage.SETTINGS_INPUT,
            AppPage.SETTINGS_FEEDBACK
        )
    }
}
