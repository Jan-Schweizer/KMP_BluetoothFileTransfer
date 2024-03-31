package de.schweizer.bft

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

expect object BlueManager {
    val deviceDiscoveredSharedFlow: SharedFlow<BlueDevice>
    val discoveryStoppedSharedFlow: SharedFlow<Unit>

    enum class BluetoothState {
        Enabled,
        Disabled,
    }

    val isBluetoothEnabled: StateFlow<BluetoothState>

    fun requestEnableBluetooth()

    internal fun init()
    suspend fun discover()
    fun connectToDevice(deviceAddr: String)
    fun cancelDiscovery()
    fun onDiscoveryStopped()
    fun onDeviceDiscovered(deviceName: String, deviceAddress: String)
}
