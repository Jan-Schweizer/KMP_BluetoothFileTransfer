package de.schweizer.bft.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun DeviceDiscoveryScreen(appState: BftAppState) {
    val viewModel = remember { AndroidDeviceDiscoveryViewModel(appState) }
    DeviceDiscoveryScreenCommon(viewModel)
}
