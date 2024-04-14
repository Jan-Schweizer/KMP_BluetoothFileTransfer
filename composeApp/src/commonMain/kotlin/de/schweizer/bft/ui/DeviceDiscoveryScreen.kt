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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.schweizer.bft.BlueManager
import de.schweizer.bft.ui.DeviceDiscoveryScreenModel.DeviceDiscoveryState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DeviceDiscoveryScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { DeviceDiscoveryScreenModel() }
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            BlueManager.deviceDiscoveredSharedFlow.onEach { screenModel.onDeviceDiscovered(it) }.launchIn(this)
            BlueManager.discoveryStoppedSharedFlow.onEach { screenModel.onDiscoveryStopped() }.launchIn(this)
            BlueManager.errorSharedFlow.onEach { screenModel.onError(it) }.launchIn(this)
            BlueManager.isBluetoothEnabled.onEach {
                if (it == BlueManager.BluetoothState.Disabled) {
                    navigator.push(RequestEnableBluetoothScreen())
                }
            }.launchIn(this)
        }

        Content(screenModel)
    }

    @Composable
    fun Content(screenModel: DeviceDiscoveryScreenModel) {
        val navigator = LocalNavigator.currentOrThrow
        val state by screenModel.state.collectAsState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Button(
                onClick = {
                    if (state is DeviceDiscoveryState.Loading) {
                        screenModel.cancelDiscovery()
                    } else {
                        screenModel.screenModelScope.launch {
                            if (screenModel.areAllPermissionsGranted()) {
                                screenModel.discoverDevices()
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

            Spacer(modifier = Modifier.requiredHeight(16.dp))

            if (state is DeviceDiscoveryState.Loading) {
                Text("Discovering Devices ...")
            }

            Spacer(modifier = Modifier.requiredHeight(16.dp))

            val discoveredDevices by screenModel.discoveredDevices.collectAsState()

            when (val s = state) {
                DeviceDiscoveryState.Init,
                DeviceDiscoveryState.Loading,
                -> if (discoveredDevices.isNotEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        discoveredDevices.forEach { (name, addr) ->
                            key(addr) {
                                BluetoothDevice(name, addr, screenModel)
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
    private fun BluetoothDevice(name: String, addr: String, screenModel: DeviceDiscoveryScreenModel) {
        Box(
            modifier = Modifier
                .border(BorderStroke(2.dp, Color.Red))
                .clickable(role = Role.Button) {
                    screenModel.screenModelScope.launch { screenModel.connectToDevice(addr) }
                },
        ) {
            Text(name)
        }
    }
}
