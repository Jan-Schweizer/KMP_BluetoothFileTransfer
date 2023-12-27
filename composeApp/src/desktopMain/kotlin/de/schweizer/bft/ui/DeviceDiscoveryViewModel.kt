package de.schweizer.bft.ui

import co.touchlab.kermit.Logger
import de.schweizer.bft.BlueManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class DeviceDiscoveryViewModel : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + SupervisorJob()

    private val blueManager = BlueManager()

    private val _uiState = MutableStateFlow(DeviceDiscoveryState())
    val uiState = _uiState.asStateFlow()

    fun discoverDevices() {
        _uiState.update { DeviceDiscoveryState(isLoading = true, error = null, discoveredDevices = LinkedHashMap()) }
        launch {
            blueManager.discover(this@DeviceDiscoveryViewModel)
        }
    }

    fun cancelDiscovery() {
        _uiState.update { uiState.value.copy(isLoading = false, error = null) }
        launch {
            blueManager.cancelDiscovery()
        }
    }

    private fun onDeviceDiscovered(discoveredDevice: String, deviceMacAddr: String) {
        val discoveredDevices = LinkedHashMap(uiState.value.discoveredDevices)
        discoveredDevices[deviceMacAddr] = discoveredDevice

        _uiState.update { uiState.value.copy(discoveredDevices = discoveredDevices) }
    }

    private fun onDiscoveryStopped() {
        _uiState.update { uiState.value.copy(isLoading = false) }
    }

    private fun errorHandler(error: String) {
        Logger.i { "Error: $error" }
        _uiState.update { uiState.value.copy(isLoading = false, error = error, discoveredDevices = LinkedHashMap()) }
    }

    fun connectToDevice(name: String) {
        launch {
            blueManager.connectToDevice(name)
        }
    }
}

data class DeviceDiscoveryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val discoveredDevices: LinkedHashMap<String, String> = LinkedHashMap(),
)
