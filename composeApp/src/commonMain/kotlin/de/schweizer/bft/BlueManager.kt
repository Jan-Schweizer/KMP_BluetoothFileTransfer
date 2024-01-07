package de.schweizer.bft

import de.schweizer.bft.ui.DeviceDiscoveryViewModel

expect class BlueManager() {
    internal fun init()
    fun discover(viewModel: DeviceDiscoveryViewModel)
    fun connectToDevice(deviceAddr: String)
    fun cancelDiscovery()
}
