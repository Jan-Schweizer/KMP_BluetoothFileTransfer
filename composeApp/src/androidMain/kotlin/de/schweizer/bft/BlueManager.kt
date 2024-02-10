package de.schweizer.bft

import de.schweizer.bft.ui.DeviceDiscoveryViewModel
import co.touchlab.kermit.Logger

actual class BlueManager {

    actual fun init() {}

    actual fun discover(viewModel: DeviceDiscoveryViewModel) {
        Logger.i { "Android BlueManager discover() called" }
    }

    actual fun connectToDevice(deviceAddr: String) {
        Logger.i { "Android BlueManager connectToDevice() called" }
    }

    actual fun cancelDiscovery() {
        Logger.i { "Android BlueManager cancelDiscovery() called" }
    }

    init {
        init()
    }
}