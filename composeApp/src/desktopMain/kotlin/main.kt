import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import de.schweizer.bft.Logger as MyLogger
import de.schweizer.bft.LogLevel
import de.schweizer.bft.ui.DeviceDiscoveryViewModel
import de.schweizer.bft.view.DeviceDiscoveryScreen
import java.nio.file.Paths

private fun init() {
    loadLibraries()

    MyLogger.init(LogLevel.INFO.name)
}

@Suppress("UnsafeDynamicallyLoadedCode")
private fun loadLibraries() {
    Logger.i { "Loading libraries" }
    val nativeLibraryPath =
        Paths.get(System.getProperty("user.dir"), "src", "desktopMain", "jniLibs", "linux-x86_64", "libblue_jni.so")
    System.load("$nativeLibraryPath")
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {

        init()

        val viewModel = remember { DeviceDiscoveryViewModel() }
        Discover(viewModel)


//        DeviceDiscoveryScreen()
    }
}

@Composable
fun Discover(viewModel: DeviceDiscoveryViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(onClick = {
            if (state.isLoading) {
                viewModel.cancelDiscovery()
            } else {
                viewModel.discoverDevices()
            }
        }) {
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
                discoveredDevices.forEach { device ->
                    // TODO: Make key device addr
                    key(device) {
                        BluetoothDevice(device, viewModel)
                        Spacer(modifier = Modifier.requiredHeight(16.dp))
                    }
                }
            }
        }

    }
}

@Composable
fun BluetoothDevice(name: String, viewModel: DeviceDiscoveryViewModel) {
    Box(
        modifier = Modifier
            .border(BorderStroke(2.dp, Color.Red))
            .clickable(role = Role.Button) {
                viewModel.connectToDevice(name)
            },
    ) {
        Text(name);
    }
}
