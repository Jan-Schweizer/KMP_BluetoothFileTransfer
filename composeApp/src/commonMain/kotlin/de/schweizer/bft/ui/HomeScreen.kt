package de.schweizer.bft.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.schweizer.bft.BlueManager
import de.schweizer.bft.PermissionManager
import de.schweizer.bft.ui.theme.Spacings
import de.schweizer.bft.ui.theme.VerticalSpacerL
import de.schweizer.bft.ui.theme.VerticalSpacerS

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val bluetoothState by BlueManager.bluetoothState.collectAsState()
        val isBluetoothEnabled = bluetoothState == BlueManager.BluetoothState.Enabled
        val deniedPermissions by PermissionManager.deniedPermissions.collectAsState()
        val areAllPermissionsGranted = deniedPermissions.isEmpty()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = Spacings.l, horizontal = Spacings.m)
                .systemBarsPadding(),
        ) {
            Title()
            VerticalSpacerL()
            Description()
            VerticalSpacerL()
            when {
                !areAllPermissionsGranted -> GrantPermissionsNotice()
                !isBluetoothEnabled -> EnableBluetoothNotice()
                else -> StartDiscoveryButton()
            }
        }
    }

    @Composable
    private fun ColumnScope.Title() {
        Text(
            text = "BFT - Transfer files between devices via Bluetooth",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
    }

    @Composable
    private fun ColumnScope.Description() {
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(Spacings.s),
        ) {
            Text("This KMP app allows you to transfer files between devices over the Bluetooth protocol.")
            Text("It currently supports Android and Desktop (JVM).")
            Text(
                "Click the button below to first discover all devices in currently in range.\n" +
                    "Then, select a device that you want a file to transfer to.\n" +
                    "With click on the button, you will also be able to receive files.",
            )
            Text("After that, you can select a file to share.")
        }
    }

    @Composable
    private fun ColumnScope.StartDiscoveryButton() {
        val navigator = LocalNavigator.currentOrThrow
        val onDiscoverDeviceClick: () -> Unit = {
            navigator.push(DeviceDiscoveryScreen())
        }

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = onDiscoverDeviceClick,
        ) {
            Text("Start sharing files")
        }
    }

    @Composable
    private fun ColumnScope.EnableBluetoothNotice() {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "Bluetooth must be enabled for this app to work.\nPlease enable Bluetooth",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        VerticalSpacerS()
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { BlueManager.requestEnableBluetooth() },
        ) {
            Text("Enable Bluetooth")
        }
    }

    @Composable
    private fun ColumnScope.GrantPermissionsNotice() {
        val navigator = LocalNavigator.currentOrThrow

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "Permissions must be granted for this app to work.\nPlease grant necessary permissions.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        VerticalSpacerS()
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = { navigator.push(RequestDeniedPermissionsScreen()) },
        ) {
            Text("Request necessary permissions")
        }
    }
}
