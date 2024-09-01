package de.schweizer.bft.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.schweizer.bft.BlueManager
import de.schweizer.bft.ui.DeviceDiscoveryViewModel.DeviceDiscoveryState
import de.schweizer.bft.ui.theme.Spacings
import de.schweizer.bft.ui.theme.VerticalSpacerM
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DeviceDiscoveryScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = viewModel { DeviceDiscoveryViewModel() }

        Toolbar(title = "Device Discovery Screen", onBack = {
            viewModel.cancelDiscovery() // TODO: Cancel receiving of files
        }) {
            val navigator = LocalNavigator.currentOrThrow

            LaunchedEffect(Unit) {
                BlueManager.deviceDiscoveredSharedFlow.onEach { viewModel.onDeviceDiscovered(it) }.launchIn(this)
                BlueManager.discoveryStoppedSharedFlow.onEach { viewModel.onDiscoveryStopped() }.launchIn(this)
                BlueManager.errorSharedFlow.onEach { viewModel.onError(it) }.launchIn(this)
                BlueManager.bluetoothState.onEach {
                    if (it == BlueManager.BluetoothState.Disabled) {
                        // TODO: Cancel any ongoing file transfers
                        viewModel.cancelDiscovery()
                        navigator.pop()
                    }
                }.launchIn(this)
            }

            Content(viewModel)
        }
    }

    @Composable
    fun Content(viewModel: DeviceDiscoveryViewModel) {
        val navigator = LocalNavigator.currentOrThrow
        val state by viewModel.uiState.collectAsState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacings.m),
        ) {
            Text("Start by discovering a device that you want to share a file with")

            // TODO: Remove permission handling here (permissions are handled before reaching this screen)
            Button(
                onClick = {
                    if (state is DeviceDiscoveryState.Loading) {
                        viewModel.cancelDiscovery()
                    } else {
                        viewModel.viewModelScope.launch {
                            if (viewModel.areAllPermissionsGranted()) {
                                viewModel.discoverDevices()
                            } else {
                                navigator.push(RequestDeniedPermissionsScreen())
                            }
                        }
                    }
                },
            ) {
                val text = if (state is DeviceDiscoveryState.Loading) "Cancel Discovery" else "Discover"
                Text(text)
            }

            VerticalSpacerM()

            if (state is DeviceDiscoveryState.Loading) {
                Text("Discovering Devices ...")
            }

            VerticalSpacerM()

            val discoveredDevices by viewModel.discoveredDevices.collectAsState()

            when (val s = state) {
                DeviceDiscoveryState.Init,
                DeviceDiscoveryState.Loading,
                -> if (discoveredDevices.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        discoveredDevices.forEach { (name, addr) ->
                            key(addr) {
                                BluetoothDevice(name, addr, viewModel)
                                Spacer(modifier = Modifier.requiredHeight(16.dp))
                            }
                        }
                    }
                }

                is DeviceDiscoveryState.Error -> Text(s.error.msg)
            }
        }
    }

    @Composable
    private fun BluetoothDevice(name: String, addr: String, viewModel: DeviceDiscoveryViewModel) {
        Box(
            modifier = Modifier
                .border(BorderStroke(2.dp, Color.Red))
                .clickable(role = Role.Button) {
                    viewModel.viewModelScope.launch { viewModel.connectToDevice(addr) }
                },
        ) {
            Text(name)
        }
    }
}
