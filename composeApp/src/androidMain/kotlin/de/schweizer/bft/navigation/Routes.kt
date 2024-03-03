package de.schweizer.bft.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import de.schweizer.bft.ui.BftAppState
import de.schweizer.bft.ui.DeviceDiscoveryScreen
import de.schweizer.bft.ui.RequestDeniedPermissionsScreen

const val DEVICE_DISCOVERY_ROUTE = "device_discovery_route"
const val REQUEST_DENIED_PERMISSIONS_ROUTE = "request_denied_permissions_route"

fun NavController.navigateToDeviceDiscovery() = navigate(DEVICE_DISCOVERY_ROUTE)
fun NavController.navigateToRequestDeniedPermissions() = navigate(REQUEST_DENIED_PERMISSIONS_ROUTE)

fun NavGraphBuilder.deviceDiscoveryScreen(appState: BftAppState) {
    composable(
        route = DEVICE_DISCOVERY_ROUTE,
    ) {
        DeviceDiscoveryScreen(appState)
    }
}

fun NavGraphBuilder.requestDeniedPermissionsScreen() {
    composable(
        route = REQUEST_DENIED_PERMISSIONS_ROUTE,
    ) {
        RequestDeniedPermissionsScreen()
    }
}
