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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

actual object BlueManager {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var requestEnableBluetoothDelegate: () -> Unit
    private val bluetoothBroadcastReceiver = BluetoothBroadcastReceiver()

    private val _deviceDiscoveredSharedFlow = MutableSharedFlow<BlueDevice>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    actual val deviceDiscoveredSharedFlow = _deviceDiscoveredSharedFlow.asSharedFlow()
    private val _discoveryStoppedSharedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    actual val discoveryStoppedSharedFlow = _discoveryStoppedSharedFlow.asSharedFlow()

    actual enum class BluetoothState {
        Enabled,
        Disabled,
    }

    private val _isBluetoothEnabled = MutableStateFlow(BluetoothState.Disabled)
    actual val isBluetoothEnabled = _isBluetoothEnabled.asStateFlow()

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

    actual fun requestEnableBluetooth() = requestEnableBluetoothDelegate()

    actual fun init() {}

    actual suspend fun discover() {
        val startingSuccessful = bluetoothAdapter.startDiscovery()
        Logger.i { "BlueManager::discover(): start discovery successful=$startingSuccessful" }
    }

    actual fun connectToDevice(deviceAddr: String) {
        // > After you have found a device to connect to, be certain that you stop discovery with cancelDiscovery() before attempting a connection.
//        cancelDiscovery()
        Logger.i { "Android BlueManager connectToDevice() called" }
    }

    actual fun cancelDiscovery() {
        bluetoothAdapter.cancelDiscovery()
        Logger.i { "BlueManager::cancelDiscovery(): canceling discovery" }
    }

    actual fun onDiscoveryStopped() {
        _discoveryStoppedSharedFlow.tryEmit(Unit)
        Logger.i { "BlueManager::onDiscoveryStopped()" }
    }

    actual fun onDeviceDiscovered(deviceName: String, deviceAddress: String) {
        _deviceDiscoveredSharedFlow.tryEmit(BlueDevice(deviceName, deviceAddress))
        Logger.i { "BlueManager::onDeviceDiscovered(): deviceName=$deviceName, deviceAddress=$deviceAddress" }
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
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    onDiscoveryStopped()
                    Logger.i { "onReceive(): Bluetooth discovery finished" }
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("Deprecation")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null && device.name != null) {
                        onDeviceDiscovered(device.name, device.address)
                        Logger.i { "onReceive(): Bluetooth device discovered with name=${device.name} and address=${device.address}" }
                    }
                }
            }
        }
    }

    fun registerBluetoothBroadcastReceiver(context: Context) {
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
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
