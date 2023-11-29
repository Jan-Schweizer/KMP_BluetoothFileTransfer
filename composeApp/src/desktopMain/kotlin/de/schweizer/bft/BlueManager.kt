package de.schweizer.bft

import de.schweizer.bft.ui.DeviceDiscoveryViewModel

class BlueManager {
    private external fun initLogger()
    external fun discover(viewModel: DeviceDiscoveryViewModel)

    init {
        initLogger()
    }
}