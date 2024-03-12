package de.schweizer.bft.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import de.schweizer.bft.BlueManager
import de.schweizer.bft.navigation.navigateToRequestEnableBluetooth
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun DeviceDiscoveryScreen(appState: BftAppState) {
    LaunchedEffect(Unit) {
        BlueManager.isBluetoothEnabled.onEach {
            if (it == BlueManager.BluetoothState.Disabled) {
                appState.navController.navigateToRequestEnableBluetooth()
            }
        }.launchIn(this)
    }

    // TODO: Provide list of paired devices

    val viewModel = remember { AndroidDeviceDiscoveryViewModel(appState) }
    DeviceDiscoveryScreenCommon(viewModel)
}
