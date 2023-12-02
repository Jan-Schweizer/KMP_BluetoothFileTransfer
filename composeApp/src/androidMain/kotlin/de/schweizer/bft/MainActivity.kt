package de.schweizer.bft

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import de.schweizer.bft.view.DeviceDiscoveryScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.setTag("App")

        System.loadLibrary("blue_jni")


        val blueManager = BlueManager()
        Logger.i { "Testing Android Logger" }

        setContent {
            Discover(blueManager, scope)
//            DeviceDiscoveryScreen()
        }
    }
}

@Composable
fun Discover(blueManager: BlueManager, scope: CoroutineScope) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(onClick = {
            scope.launch {
                blueManager.discover()
            }
        }) {
            Text(text = "Discover")
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
