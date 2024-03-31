package de.schweizer.bft.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.schweizer.bft.BlueManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RequestEnableBluetoothScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            BlueManager.isBluetoothEnabled.onEach {
                if (it == BlueManager.BluetoothState.Enabled) {
                    navigator.pop()
                }
            }.launchIn(this)
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column {
                Text(
                    text = "Please enable Bluetooth",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.requiredHeight(4.dp))
                Button(
                    onClick = { BlueManager.requestEnableBluetooth() },
                ) {
                    Text("Enable Bluetooth")
                }
            }
        }
    }
}
