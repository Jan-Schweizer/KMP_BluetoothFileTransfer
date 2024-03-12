package de.schweizer.bft.ui

import co.touchlab.kermit.Logger
import de.schweizer.bft.BlueManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class DeviceDiscoveryViewModel {
    private val _uiState = MutableStateFlow(DeviceDiscoveryState())
    val uiState = _uiState.asStateFlow()

    open suspend fun discoverDevices() {
        _uiState.update { DeviceDiscoveryState(isLoading = true, error = null, discoveredDevices = LinkedHashMap()) }
        BlueManager.discover(this@DeviceDiscoveryViewModel)
    }

    fun cancelDiscovery() {
        BlueManager.cancelDiscovery()
        _uiState.update { uiState.value.copy(isLoading = false) }
    }

    fun onDeviceDiscovered(discoveredDevice: String, deviceMacAddr: String) {
        val discoveredDevices = LinkedHashMap(uiState.value.discoveredDevices)
        discoveredDevices[deviceMacAddr] = discoveredDevice

        _uiState.update { uiState.value.copy(discoveredDevices = discoveredDevices) }
    }

    fun onDiscoveryStopped() {
        _uiState.update { uiState.value.copy(isLoading = false) }
    }

    fun errorHandler(error: String) {
        Logger.i { "Error: $error" }
        _uiState.update { uiState.value.copy(isLoading = false, error = error, discoveredDevices = LinkedHashMap()) }
    }

    suspend fun connectToDevice(name: String) {
        BlueManager.connectToDevice(name)
    }
}

data class DeviceDiscoveryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val discoveredDevices: LinkedHashMap<String, String> = LinkedHashMap(),
)
