package de.schweizer.bft

import de.schweizer.bft.ui.DeviceDiscoveryViewModel

actual class BlueManager {
    actual external fun init()
    actual external fun discover(viewModel: DeviceDiscoveryViewModel)
    actual external fun connectToDevice(deviceAddr: String)
    actual external fun cancelDiscovery()

    init {
        init()
    }
}
