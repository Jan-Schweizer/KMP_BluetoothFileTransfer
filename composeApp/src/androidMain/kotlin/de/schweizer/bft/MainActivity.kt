package de.schweizer.bft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import co.touchlab.kermit.Logger
import de.schweizer.bft.ui.DeviceDiscoveryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.setTag("Bft Android App")

        setContent {
            DeviceDiscoveryScreen()
        }
    }
}
