@file:Suppress("MissingPermission")
package de.schweizer.bft

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import co.touchlab.kermit.Logger
import de.schweizer.bft.ui.DeviceDiscoveryViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

actual object BlueManager {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var requestEnableBluetoothDelegate: () -> Unit
    private val bluetoothBroadcastReceiver = BluetoothBroadcastReceiver()

    private val deviceDiscovered = MutableSharedFlow<BluetoothDevice>(
        replay = 5,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private var deviceDiscoveredJob: Job? = null

    private val _isBluetoothEnabled = MutableStateFlow(BluetoothState.Disabled)
    val isBluetoothEnabled = _isBluetoothEnabled.asStateFlow()

    enum class BluetoothState {
        Enabled,
        Disabled,
    }

    private fun updateBluetoothEnabled() = _isBluetoothEnabled.update {
        when (bluetoothAdapter.isEnabled) {
            true -> BluetoothState.Enabled
            false -> BluetoothState.Disabled
        }
    }

    fun setup(context: Context, delegate: () -> Unit) {
        requestEnableBluetoothDelegate = delegate
        val bluetoothManager = getSystemService(context, BluetoothManager::class.java)!! // If a device doesn't support bluetooth, just crash ...
        bluetoothAdapter = bluetoothManager.adapter

        updateBluetoothEnabled()
    }

    fun requestEnableBluetooth() = requestEnableBluetoothDelegate()

    actual fun init() {}

    actual suspend fun discover(viewModel: DeviceDiscoveryViewModel) = coroutineScope {
        // Permissions are handled by viewModel.discoverDevices() right before this call.
        deviceDiscoveredJob = deviceDiscovered.onEach { viewModel.onDeviceDiscovered(it.name ?: "<Unknown>", it.address) }.launchIn(this)

        val startingSuccessful = bluetoothAdapter.startDiscovery()
        Logger.i { "BlueManager::discover(): start discovery successful=$startingSuccessful" }
    }

    actual fun connectToDevice(deviceAddr: String) {
        // > After you have found a device to connect to, be certain that you stop discovery with cancelDiscovery() before attempting a connection.
        cancelDiscovery()
        Logger.i { "Android BlueManager connectToDevice() called" }
    }

    actual fun cancelDiscovery() {
        deviceDiscoveredJob?.cancel()
        deviceDiscoveredJob = null

        bluetoothAdapter.cancelDiscovery()
        Logger.i { "BlueManager::cancelDiscovery(): canceling discovery" }
    }

    init {
        init()
    }

    private class BluetoothBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    updateBluetoothEnabled()
                    Logger.i { "onReceive(): Bluetooth state changed to enabled=${bluetoothAdapter.isEnabled}" }
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("Deprecation")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null) {
                        deviceDiscovered.tryEmit(device)
                        Logger.i { "onReceive(): Bluetooth device discovered with name=${device.name} and address=${device.address}" }
                    }
                }
            }
        }
    }

    fun registerBluetoothBroadcastReceiver(context: Context) {
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(bluetoothBroadcastReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(bluetoothBroadcastReceiver, intentFilter)
        }
    }

    fun unregisterBluetoothBroadcastReceiver(context: Context) {
        try {
            context.unregisterReceiver(bluetoothBroadcastReceiver)
        } catch (_: IllegalStateException) {
            // There is no way to find out if a receiver is registered or not, [IllegalStateException] is thrown if not registered
            Logger.w { "unregisterBluetoothBroadcastReceiver(): Trying to unregister not registered 'BluetoothBroadcastReceiver'" }
        }
    }
}
