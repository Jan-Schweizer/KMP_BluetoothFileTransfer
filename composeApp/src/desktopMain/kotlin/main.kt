import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import de.schweizer.bft.ui.DeviceDiscoveryViewModel
import de.schweizer.bft.view.DeviceDiscoveryScreen
import java.nio.file.Paths

@Suppress("UnsafeDynamicallyLoadedCode")
private fun loadLibraries() {
    Logger.i { "Loading libraries" }
    val nativeLibraryPath =
        Paths.get(System.getProperty("user.dir"), "src", "desktopMain", "jniLibs", "linux-x86_64", "libblue_jni.so")
    System.load("$nativeLibraryPath")
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {

        loadLibraries()

        // TODO: move into Discover() and see if Logger init gets called with each recomposition
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
            viewModel.discoverDevices()
        }) {
            Text(text = "Discover")
        }

        Spacer(modifier = Modifier.requiredHeight(16.dp))

        if (state.isLoading) {
            Text("Discovering Devices ...")
        }

        val error = state.error
        val discoveredDevices = state.discoveredDevices
        if (error != null) {
            Text(error)
        } else if (discoveredDevices.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (device in state.discoveredDevices) {
                    Text(text = device)
                    Spacer(modifier = Modifier.requiredHeight(16.dp))
                }
            }
        }

    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}