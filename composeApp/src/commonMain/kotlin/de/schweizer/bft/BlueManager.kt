package de.schweizer.bft

import de.schweizer.bft.ui.DeviceDiscoveryViewModel

expect object BlueManager {
    internal fun init()
    suspend fun discover(viewModel: DeviceDiscoveryViewModel)
    fun connectToDevice(deviceAddr: String)
    fun cancelDiscovery()
}
