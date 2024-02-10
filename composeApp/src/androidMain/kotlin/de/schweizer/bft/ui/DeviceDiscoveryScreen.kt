package de.schweizer.bft.ui

import androidx.compose.runtime.Composable

@Composable
fun DeviceDiscoveryScreen() {
    val viewModel = AndroidDeviceDiscoveryViewModel()
    DeviceDiscoveryScreenCommon(viewModel)
}
