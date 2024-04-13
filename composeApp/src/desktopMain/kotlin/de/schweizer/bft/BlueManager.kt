package de.schweizer.bft

import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

actual object BlueManager {
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

    actual external fun init()
    actual external suspend fun discover()
    actual external fun connectToDevice(deviceAddr: String)
    actual external fun cancelDiscovery()
    actual external fun requestEnableBluetooth()

    @JvmStatic
    actual fun onDiscoveryStopped() {
        _discoveryStoppedSharedFlow.tryEmit(Unit)
        Logger.i { "BlueManager::onDiscoveryStopped()" }
    }

    @JvmStatic
    actual fun onDeviceDiscovered(deviceName: String, deviceAddress: String) {
        _deviceDiscoveredSharedFlow.tryEmit(BlueDevice(deviceName, deviceAddress))
        Logger.i { "BlueManager::onDeviceDiscovered(): deviceName=$deviceName, deviceAddress=$deviceAddress" }
    }

    // TODO: Try to make private and see if still callable from native code
    @JvmStatic
    fun updateBluetoothEnabled(enabled: Boolean) = _isBluetoothEnabled.update {
        when (enabled) {
            true -> BluetoothState.Enabled
            false -> BluetoothState.Disabled
        }
    }

    init {
        init()
    }
}
