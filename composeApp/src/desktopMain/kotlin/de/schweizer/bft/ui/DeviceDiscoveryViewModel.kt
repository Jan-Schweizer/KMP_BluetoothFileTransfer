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

class DeviceDiscoveryViewModel {
    // TODO: How to cancel on destroy?
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val blueManager = BlueManager()

    private val _uiState = MutableStateFlow(DeviceDiscoveryState())
    val uiState = _uiState.asStateFlow()

    fun discoverDevices() {
        _uiState.update { DeviceDiscoveryState(isLoading = true, error = null, discoveredDevices = emptyList()) }
        scope.launch {
            blueManager.discover(this@DeviceDiscoveryViewModel)
        }
    }

    fun cancelDiscovery() {
        _uiState.update { uiState.value.copy(isLoading = false, error = null) }
        scope.launch {
            blueManager.cancelDiscovery()
        }
    }

    private fun onDeviceDiscovered(discoveredDevice: String) {
        val discoveredDevices = uiState.value.discoveredDevices
        if (discoveredDevice in discoveredDevices) return

        _uiState.update { uiState.value.copy(discoveredDevices = discoveredDevices + discoveredDevice) }
    }

    private fun onDiscoveryStopped() {
        _uiState.update { uiState.value.copy(isLoading = false) }
    }

    private fun errorHandler(error: String) {
        Logger.i { "Error: $error" }
        _uiState.update { uiState.value.copy(isLoading = false, error = error, discoveredDevices = emptyList()) }
    }

    fun connectToDevice(name: String) {
        scope.launch {
            blueManager.connectToDevice(name)
        }
    }
}

data class DeviceDiscoveryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val discoveredDevices: List<String> = emptyList(),
)
