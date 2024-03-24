package de.schweizer.bft

import kotlinx.coroutines.flow.MutableSharedFlow

expect object BlueManager {
    val deviceDiscoveredSharedFlow: MutableSharedFlow<BlueDevice>
    val discoveryStoppedSharedFlow: MutableSharedFlow<Unit>

    internal fun init()
    suspend fun discover()
    fun connectToDevice(deviceAddr: String)
    fun cancelDiscovery()
    fun onDiscoveryStopped()
    fun onDeviceDiscovered(deviceName: String, deviceAddress: String)
}
