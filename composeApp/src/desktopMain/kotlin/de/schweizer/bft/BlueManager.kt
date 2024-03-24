package de.schweizer.bft

import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

actual object BlueManager {
    actual val deviceDiscoveredSharedFlow = MutableSharedFlow<BlueDevice>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    actual val discoveryStoppedSharedFlow = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    actual external fun init()
    actual external suspend fun discover()
    actual external fun connectToDevice(deviceAddr: String)
    actual external fun cancelDiscovery()

    @JvmStatic
    actual fun onDiscoveryStopped() {
        discoveryStoppedSharedFlow.tryEmit(Unit)
        Logger.i { "BlueManager::onDiscoveryStopped()" }
    }

    @JvmStatic
    actual fun onDeviceDiscovered(deviceName: String, deviceAddress: String) {
        deviceDiscoveredSharedFlow.tryEmit(BlueDevice(deviceName, deviceAddress))
        Logger.i { "BlueManager::onDeviceDiscovered(): deviceName=$deviceName, deviceAddress=$deviceAddress" }
    }

    init {
        init()
    }
}
