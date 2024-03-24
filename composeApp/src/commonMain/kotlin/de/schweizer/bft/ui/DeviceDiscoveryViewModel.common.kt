package de.schweizer.bft.ui

import co.touchlab.kermit.Logger
import de.schweizer.bft.BlueDevice
import de.schweizer.bft.BlueManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class DeviceDiscoveryViewModel {
    private val _uiState = MutableStateFlow(DeviceDiscoveryState())
    val uiState = _uiState.asStateFlow()

    open suspend fun discoverDevices() {
        _uiState.update { DeviceDiscoveryState(isLoading = true, error = null, discoveredDevices = LinkedHashSet()) }
        BlueManager.discover()
    }

    fun cancelDiscovery() {
        BlueManager.cancelDiscovery()
    }

    fun onDeviceDiscovered(device: BlueDevice) {
        val discoveredDevices = LinkedHashSet(uiState.value.discoveredDevices)
        discoveredDevices.add(device)

        _uiState.update { uiState.value.copy(discoveredDevices = discoveredDevices) }
    }

    fun onDiscoveryStopped() {
        _uiState.update { uiState.value.copy(isLoading = false) }
    }

    fun errorHandler(error: String) {
        Logger.i { "Error: $error" }
        _uiState.update { uiState.value.copy(isLoading = false, error = error, discoveredDevices = LinkedHashSet()) }
    }

    suspend fun connectToDevice(name: String) {
        BlueManager.connectToDevice(name)
    }
}

data class DeviceDiscoveryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val discoveredDevices: LinkedHashSet<BlueDevice> = LinkedHashSet(),
)
