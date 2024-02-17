package de.schweizer.bft.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import de.schweizer.bft.ui.DeviceDiscoveryScreen

const val DEVICE_DISCOVERY_ROUTE = "device_discovery_route"

fun NavController.navigateToDeviceDiscovery() = navigate(DEVICE_DISCOVERY_ROUTE)

fun NavGraphBuilder.deviceDiscoveryScreen() {
    composable(
        route = DEVICE_DISCOVERY_ROUTE,
    ) {
        DeviceDiscoveryScreen()
    }
}
