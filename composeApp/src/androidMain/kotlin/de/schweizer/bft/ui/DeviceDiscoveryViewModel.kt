package de.schweizer.bft.ui

import androidx.compose.runtime.NoLiveLiterals
import de.schweizer.bft.PermissionManager
import de.schweizer.bft.navigation.navigateToRequestDeniedPermissions
import kotlin.coroutines.suspendCoroutine

@NoLiveLiterals
class AndroidDeviceDiscoveryViewModel(private val appState: BftAppState) : DeviceDiscoveryViewModel() {
    override suspend fun discoverDevices() {
        val areAllPermissionsGranted = suspendCoroutine {
            PermissionManager.requestPermissions(PermissionManager.deniedPermissions.value, it)
        }

        if (areAllPermissionsGranted) {
            super.discoverDevices()
        } else {
            appState.navController.navigateToRequestDeniedPermissions()
        }
    }
}