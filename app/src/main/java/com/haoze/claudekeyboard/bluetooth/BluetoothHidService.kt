package com.haoze.claudekeyboard.bluetooth

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.haoze.claudekeyboard.MainActivity
import com.haoze.claudekeyboard.R
import java.util.concurrent.Executors

/**
 * Foreground service that manages Bluetooth HID device registration and connection.
 * Uses an explicit [HidState] state machine instead of independent boolean flags.
 */
class BluetoothHidService : Service() {

    companion object {
        private const val TAG = "BluetoothHidService"
        private const val NOTIFICATION_CHANNEL_ID = "bluetooth_hid_channel"
        private const val NOTIFICATION_ID = 1001
        private const val MAX_REGISTRATION_RETRIES = 3
        private const val PREFS_NAME = "bluetooth_prefs"
        private const val KEY_LAST_DEVICE_ADDRESS = "last_device_address"
        private const val KEY_LAST_DEVICE_NAME = "last_device_name"
    }

    // ---- Binder ----

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothHidService = this@BluetoothHidService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    // ---- Bluetooth components ----

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var hidDevice: BluetoothHidDevice? = null

    // ---- State machine ----

    private var state: HidState = HidState.Unregistered
        set(value) {
            Log.d(TAG, "State: ${field::class.simpleName} → ${value::class.simpleName}")
            field = value
        }

    private var userInitiatedDisconnect = false
    private var isShuttingDown = false
    private var registrationRetryCount = 0
    private var lastReconnectAttempt: Long = 0

    // ---- Sender instances (live only in Connected state) ----

    private var keyboardSender: KeyboardSender? = null
    private var mouseSender: MouseSender? = null
    private var tvRemoteSender: TvRemoteSender? = null

    // ---- Callbacks ----

    private var onConnectionStateChanged: ((Boolean, String?) -> Unit)? = null
    private var onRegistrationStateChanged: ((Boolean) -> Unit)? = null

    // ---- Persistence ----

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    // ---- Bluetooth state receiver ----

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (btState) {
                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "Bluetooth turned on, re-initializing HID")
                        resetToUnregistered()
                        mainHandler.postDelayed({ initializeHidDevice() }, 1000)
                    }
                    BluetoothAdapter.STATE_OFF -> {
                        Log.d(TAG, "Bluetooth turned off")
                        resetToUnregistered()
                        onConnectionStateChanged?.invoke(false, null)
                        updateNotification(getString(R.string.notification_waiting))
                    }
                }
            }
        }
    }

    // ---- Lifecycle ----

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager)?.adapter
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported")
            stopSelf()
            return
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(getString(R.string.notification_waiting)))
        initializeHidDevice()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        isShuttingDown = true
        mainHandler.removeCallbacksAndMessages(null)
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Receiver already unregistered")
        }
        disconnect()
        unregisterHidDevice()
    }

    // ---- Callbacks ----

    fun setOnConnectionStateChangedListener(listener: (Boolean, String?) -> Unit) {
        onConnectionStateChanged = listener
    }

    fun setOnRegistrationStateChangedListener(listener: (Boolean) -> Unit) {
        onRegistrationStateChanged = listener
    }

    // ---- Public API ----

    fun getHidDevice(): BluetoothHidDevice? = hidDevice
    fun getConnectedDevice(): BluetoothDevice? = (state as? HidState.Connected)?.device
    fun getKeyboardSender(): KeyboardSender? = keyboardSender
    fun getMouseSender(): MouseSender? = mouseSender
    fun getTvRemoteSender(): TvRemoteSender? = tvRemoteSender
    fun isConnected(): Boolean = state is HidState.Connected
    fun isRegistered(): Boolean = state is HidState.Registered || state is HidState.Connected
    fun hasLastConnectedDevice(): Boolean = getLastDeviceAddress() != null

    fun getConnectedDeviceName(): String? = (state as? HidState.Connected)?.deviceName
    fun getConnectedDeviceAddress(): String? = (state as? HidState.Connected)?.device?.address
    fun getLastConnectedDeviceAddress(): String? = prefs.getString(KEY_LAST_DEVICE_ADDRESS, null)

    fun disconnect() {
        userInitiatedDisconnect = true
        val currentState = state
        if (currentState is HidState.Connected) {
            hidDevice?.disconnect(currentState.device)
        }
        setDiscoverable()
    }

    fun connectToDevice(address: String): Boolean {
        val hd = hidDevice ?: return false
        val adapter = bluetoothAdapter ?: return false
        if (state !is HidState.Registered && state !is HidState.Connected) {
            Log.w(TAG, "Cannot connect: HID not registered")
            return false
        }

        // Disconnect current device if connected
        if (state is HidState.Connected) {
            userInitiatedDisconnect = true
            (state as? HidState.Connected)?.let { hd.disconnect(it.device) }
        }

        val device = adapter.bondedDevices?.find { it.address == address } ?: run {
            Log.w(TAG, "Device ($address) not found in bonded devices")
            return false
        }

        // Update last device for reconnect
        prefs.edit()
            .putString(KEY_LAST_DEVICE_ADDRESS, address)
            .putString(KEY_LAST_DEVICE_NAME, device.name)
            .apply()
        userInitiatedDisconnect = false
        lastReconnectAttempt = 0
        setDiscoverable()

        Log.d(TAG, "Attempting HID connect to ${device.name} ($address)")
        val result = try {
            hd.connect(device)
        } catch (e: Exception) {
            Log.w(TAG, "HID connect failed: ${e.message}")
            false
        }
        Log.d(TAG, "hd.connect returned $result")
        return result
    }

    // ---- HID initialization ----

    private fun initializeHidDevice() {
        Log.d(TAG, "Initializing HID device")
        bluetoothAdapter?.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                    Log.d(TAG, "HID device proxy obtained")
                    registerHidDevice()
                    setDiscoverable()
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = null
                    state = HidState.Unregistered
                    Log.d(TAG, "HID device proxy disconnected")
                }
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    private fun registerHidDevice() {
        val hd = hidDevice ?: return

        if (state is HidState.Registered || state is HidState.Connected) {
            Log.d(TAG, "HID device already registered, skipping")
            return
        }
        if (state is HidState.Registering) {
            Log.d(TAG, "HID device registration already in progress, skipping")
            return
        }

        // Clean up any stale registration
        try {
            hd.unregisterApp()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister stale app: ${e.message}")
        }

        val appName = getString(R.string.app_name)
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "$appName Combo",
            "$appName Bluetooth HID Combo",
            appName,
            BluetoothHidDevice.SUBCLASS1_COMBO,
            DescriptorCollection.COMBINED
        )

        val executor = Executors.newSingleThreadExecutor()

        val callback = object : BluetoothHidDevice.Callback() {
            override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                super.onAppStatusChanged(pluggedDevice, registered)
                Log.d(TAG, "HID app registration status changed: $registered")

                if (registered) {
                    registrationRetryCount = 0
                    state = HidState.Registered(
                        lastDeviceAddress = prefs.getString(KEY_LAST_DEVICE_ADDRESS, null),
                        lastDeviceName = prefs.getString(KEY_LAST_DEVICE_NAME, null)
                    )
                    updateNotification(getString(R.string.notification_waiting))
                    onRegistrationStateChanged?.invoke(true)
                } else {
                    state = HidState.Unregistered
                    scheduleRegistrationRetry()
                }
            }

            override fun onConnectionStateChanged(device: BluetoothDevice?, connState: Int) {
                super.onConnectionStateChanged(device, connState)
                when (connState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        val name = device?.name
                        state = HidState.Connected(device!!, name)
                        userInitiatedDisconnect = false

                        // Persist last connected device
                        prefs.edit()
                            .putString(KEY_LAST_DEVICE_ADDRESS, device.address)
                            .putString(KEY_LAST_DEVICE_NAME, name)
                            .apply()

                        Log.d(TAG, "Connected to: $name")

                        // Create senders
                        keyboardSender = KeyboardSender(hidDevice!!, device)
                        mouseSender = MouseSender(hidDevice!!, device)
                        tvRemoteSender = TvRemoteSender(hidDevice!!, device, keyboardSender!!)

                        // Set connection policy for auto-reconnect (API 33+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                val method = hidDevice!!.javaClass.getMethod(
                                    "setConnectionPolicy",
                                    BluetoothDevice::class.java,
                                    Int::class.javaPrimitiveType
                                )
                                method.invoke(hidDevice, device, 1) // CONNECTION_POLICY_ALLOWED
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to set connection policy: ${e.message}")
                            }
                        }
                        updateNotification(getString(R.string.notification_connected, name ?: ""))
                        onConnectionStateChanged?.invoke(true, name)
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        val wasConnected = state is HidState.Connected
                        state = HidState.Registered(
                            lastDeviceAddress = prefs.getString(KEY_LAST_DEVICE_ADDRESS, null),
                            lastDeviceName = prefs.getString(KEY_LAST_DEVICE_NAME, null)
                        )
                        keyboardSender = null
                        mouseSender = null
                        tvRemoteSender = null
                        Log.d(TAG, "Disconnected")
                        updateNotification(getString(R.string.notification_waiting))
                        onConnectionStateChanged?.invoke(false, null)
                        setDiscoverable()

                        if (!userInitiatedDisconnect) {
                            scheduleReconnect()
                        }
                        userInitiatedDisconnect = false
                    }
                }
            }

            override fun onGetReport(device: BluetoothDevice, type: Byte, id: Byte, bufferSize: Int) {
                super.onGetReport(device, type, id, bufferSize)
                try {
                    val method = hidDevice!!.javaClass.getMethod(
                        "sendReply",
                        BluetoothDevice::class.java,
                        Byte::class.javaPrimitiveType,
                        Byte::class.javaPrimitiveType,
                        ByteArray::class.java
                    )
                    method.invoke(hidDevice, device, type, id, ByteArray(8))
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to send GET_REPORT reply: ${e.message}")
                }
            }
        }

        state = HidState.Registering
        val registerStarted = hd.registerApp(sdpSettings, null, null, executor, callback)
        Log.d(TAG, "HID app registration started: $registerStarted")
        if (!registerStarted) {
            state = HidState.Unregistered
            scheduleRegistrationRetry()
        }
    }

    // ---- Discoverability ----

    private fun setDiscoverable() {
        try {
            val method = bluetoothAdapter?.javaClass?.getMethod(
                "setScanMode",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            )
            val result = method?.invoke(bluetoothAdapter, 23, 300)
            Log.d(TAG, "setScanMode result: $result")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set discoverable mode: ${e.message}")
        }
    }

    // ---- Retry logic ----

    private fun scheduleRegistrationRetry() {
        val adapter = bluetoothAdapter ?: return
        if (isShuttingDown || !adapter.isEnabled || hidDevice == null) return
        if (state is HidState.Registered || state is HidState.Connected) return
        if (registrationRetryCount >= MAX_REGISTRATION_RETRIES) {
            Log.w(TAG, "HID app registration failed after retries")
            onRegistrationStateChanged?.invoke(false)
            return
        }

        registrationRetryCount++
        Log.d(TAG, "Retrying HID app registration ($registrationRetryCount/$MAX_REGISTRATION_RETRIES)")
        mainHandler.postDelayed({
            registerHidDevice()
        }, 1000L * registrationRetryCount)
    }

    private fun scheduleReconnect() {
        val address = getLastDeviceAddress() ?: return
        Log.d(TAG, "Scheduling reconnect to $address")

        mainHandler.postDelayed({
            val hd = hidDevice ?: return@postDelayed
            if (state is HidState.Connected || userInitiatedDisconnect) return@postDelayed
            tryConnectToLastDevice()
        }, 2500)
    }

    private fun tryConnectToLastDevice(): Boolean {
        val address = getLastDeviceAddress() ?: return false
        val hd = hidDevice ?: return false
        val adapter = bluetoothAdapter ?: return false
        if (state is HidState.Connected) return true
        if (state !is HidState.Registered) {
            Log.w(TAG, "Cannot connect: HID not registered yet")
            return false
        }

        val now = System.currentTimeMillis()
        if (now - lastReconnectAttempt < 2000) return false
        lastReconnectAttempt = now

        val device = adapter.bondedDevices?.find { it.address == address } ?: run {
            Log.w(TAG, "Last device ($address) not found in bonded devices")
            return false
        }
        Log.d(TAG, "Attempting HID connect to ${device.name} ($address)")
        val result = try {
            hd.connect(device)
        } catch (e: Exception) {
            Log.w(TAG, "HID connect failed: ${e.message}")
            false
        }
        Log.d(TAG, "hd.connect returned $result")
        if (!result) {
            Log.d(TAG, "Phone-initiated connect failed — the host must discover and connect to this device")
        }
        return result
    }

    // ---- Cleanup ----

    private fun unregisterHidDevice() {
        hidDevice?.let { device ->
            if (state is HidState.Registered || state is HidState.Connected) {
                device.unregisterApp()
                state = HidState.Unregistered
                Log.d(TAG, "HID device unregistered")
            }
        }
    }

    private fun resetToUnregistered() {
        state = HidState.Unregistered
        registrationRetryCount = 0
        keyboardSender = null
        mouseSender = null
        tvRemoteSender = null
        mainHandler.removeCallbacksAndMessages(null)
        hidDevice?.let {
            try {
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, it)
            } catch (_: Exception) {}
        }
        hidDevice = null
    }

    private fun getLastDeviceAddress(): String? = prefs.getString(KEY_LAST_DEVICE_ADDRESS, null)

    // ---- Notifications ----

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
