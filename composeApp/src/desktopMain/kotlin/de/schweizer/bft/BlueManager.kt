package de.schweizer.bft

import de.schweizer.bft.ui.DeviceDiscoveryViewModel

actual object BlueManager {
    actual external fun init()
    actual external suspend fun discover(viewModel: DeviceDiscoveryViewModel)
    actual external fun connectToDevice(deviceAddr: String)
    actual external fun cancelDiscovery()

    init {
        init()
    }
}
