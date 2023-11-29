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
        _uiState.update { DeviceDiscoveryState(isLoading = true, error = null, discoveredDevices = emptyArray()) }
        scope.launch {
            blueManager.discover(this@DeviceDiscoveryViewModel)
        }
    }

    private fun discoveredDevicesHandler(discoveredDevices: Array<String>) {
        _uiState.update { DeviceDiscoveryState(isLoading = false, error = null, discoveredDevices = discoveredDevices) }
    }

    private fun errorHandler(error: String) {
        Logger.i { "Error: $error" }
        _uiState.update { DeviceDiscoveryState(isLoading = false, error = null, discoveredDevices = emptyArray()) }
    }
}

data class DeviceDiscoveryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val discoveredDevices: Array<String> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceDiscoveryState

        if (isLoading != other.isLoading) return false
        if (error != other.error) return false
        if (!discoveredDevices.contentEquals(other.discoveredDevices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + discoveredDevices.contentHashCode()
        return result
    }
}