package de.schweizer.bft.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import de.schweizer.bft.ui.BftAppState

@Composable
fun BftNavHost(appState: BftAppState) {
    NavHost(
        navController = appState.navController,
        startDestination = DEVICE_DISCOVERY_ROUTE,
    ) {
        deviceDiscoveryScreen(appState)
        requestDeniedPermissionsScreen()
    }
}