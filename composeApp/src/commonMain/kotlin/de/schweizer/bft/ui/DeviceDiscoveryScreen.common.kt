package de.schweizer.bft.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.schweizer.bft.BlueManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun DeviceDiscoveryScreenCommon(viewModel: DeviceDiscoveryViewModel) {
    LaunchedEffect(Unit) {
        BlueManager.deviceDiscoveredSharedFlow.onEach { viewModel.onDeviceDiscovered(it) }.launchIn(this)
        BlueManager.discoveryStoppedSharedFlow.onEach { viewModel.onDiscoveryStopped() }.launchIn(this)
    }

    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(
            onClick = {
                if (state.isLoading) {
                    viewModel.cancelDiscovery()
                } else {
                    scope.launch { viewModel.discoverDevices() }
                }
            },
        ) {
            val text = if (state.isLoading) "Cancel Discovery" else "Discover"
            Text(text)
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))

        if (state.isLoading) {
            Text("Discovering Devices ...")
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))

        val error = state.error
        val discoveredDevices = state.discoveredDevices
        if (error != null) {
            Text(error)
        } else if (discoveredDevices.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                discoveredDevices.forEach { (name, addr) ->
                    key(addr) {
                        BluetoothDevice(name, addr, viewModel)
                        Spacer(modifier = Modifier.requiredHeight(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BluetoothDevice(name: String, addr: String, viewModel: DeviceDiscoveryViewModel) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .border(BorderStroke(2.dp, Color.Red))
            .clickable(role = Role.Button) {
                scope.launch { viewModel.connectToDevice(addr) }
            },
    ) {
        Text(name)
    }
}
