package de.schweizer.bft.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeviceDiscoveryScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
        ) {
        Button(onClick = { }) {
            Text("Discover Devices")
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))

        Column {
            Item("Item 1")
            Item("Item 2")
            Item("Item 3")
        }
    }
}

@Composable
private fun ColumnScope.Item(text: String) {
    Text(text)
    Spacer(modifier = Modifier.requiredHeight(16.dp))
}
