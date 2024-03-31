package de.schweizer.bft.ui

import cafe.adriel.voyager.core.model.StateScreenModel
import co.touchlab.kermit.Logger
import de.schweizer.bft.BlueDevice
import de.schweizer.bft.BlueManager
import de.schweizer.bft.PermissionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.suspendCoroutine

class DeviceDiscoveryScreenModel : StateScreenModel<DeviceDiscoveryScreenModel.DeviceDiscoveryState>(DeviceDiscoveryState.Init) {

    sealed class DeviceDiscoveryState {
        data object Init : DeviceDiscoveryState()
        data object Loading : DeviceDiscoveryState()
        data class Error(val error: String) : DeviceDiscoveryState()
    }

    private val _discoveredDevices: MutableStateFlow<LinkedHashSet<BlueDevice>> = MutableStateFlow(linkedSetOf())
    val discoveredDevices = _discoveredDevices.asStateFlow()

    private val _requestDeniedPermissionsSignal = MutableSharedFlow<Unit>()
    val requestDeniedPermissionsSignal = _requestDeniedPermissionsSignal.asSharedFlow()

    suspend fun discoverDevices() {
        val areAllPermissionsGranted = suspendCoroutine {
            PermissionManager.requestPermissions(PermissionManager.deniedPermissions.value, it)
        }

        if (areAllPermissionsGranted) {
            mutableState.update { DeviceDiscoveryState.Loading }
            _discoveredDevices.update { linkedSetOf() }
            BlueManager.discover()
        } else {
            _requestDeniedPermissionsSignal.emit(Unit)
        }
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
        mutableState.update { DeviceDiscoveryState.Init }
    }

    fun errorHandler(error: String) {
        Logger.i { "Error: $error" }
        mutableState.update { DeviceDiscoveryState.Error(error) }
    }

    suspend fun connectToDevice(name: String) {
        BlueManager.connectToDevice(name)
    }
}
