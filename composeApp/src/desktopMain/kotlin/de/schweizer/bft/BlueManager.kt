package de.schweizer.bft

import de.schweizer.bft.ui.DeviceDiscoveryViewModel

class BlueManager {
    private external fun init()
    external fun discover(viewModel: DeviceDiscoveryViewModel)
    external fun connectToDevice(deviceAddr: String)
    external fun cancelDiscovery()

    init {
        init()
    }
}