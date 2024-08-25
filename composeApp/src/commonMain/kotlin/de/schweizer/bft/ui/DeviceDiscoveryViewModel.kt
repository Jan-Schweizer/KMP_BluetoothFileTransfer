package de.schweizer.bft.ui

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import de.schweizer.bft.BlueDevice
import de.schweizer.bft.BlueError
import de.schweizer.bft.BlueManager
import de.schweizer.bft.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.suspendCoroutine

class DeviceDiscoveryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DeviceDiscoveryState>(DeviceDiscoveryState.Init)
    val uiState: StateFlow<DeviceDiscoveryState> = _uiState.asStateFlow()

    sealed class DeviceDiscoveryState {
        data object Init : DeviceDiscoveryState()
        data object Loading : DeviceDiscoveryState()
        data class Error(val error: BlueError) : DeviceDiscoveryState()
    }

    private val _discoveredDevices: MutableStateFlow<LinkedHashSet<BlueDevice>> = MutableStateFlow(linkedSetOf())
    val discoveredDevices = _discoveredDevices.asStateFlow()

    suspend fun areAllPermissionsGranted(): Boolean = suspendCoroutine {
        PermissionManager.requestPermissions(PermissionManager.deniedPermissions.value, it)
    }

    suspend fun discoverDevices() {
        _uiState.update { DeviceDiscoveryState.Loading }
        _discoveredDevices.update { linkedSetOf() }
        BlueManager.discover()
    }

    fun cancelDiscovery() {
        BlueManager.cancelDiscovery()
    }

    fun onDeviceDiscovered(device: BlueDevice) {
        _discoveredDevices.update {
            LinkedHashSet(it.plus(device))
        }
    }

    fun onDiscoveryStopped() {
        _uiState.update { DeviceDiscoveryState.Init }
    }

    fun onError(error: BlueError) {
        Logger.i { "Error: $error" }
        _uiState.update { DeviceDiscoveryState.Error(error) }
    }

    suspend fun connectToDevice(name: String) {
        BlueManager.connectToDevice(name)
    }
}
