import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import de.schweizer.bft.BlueManager
import de.schweizer.bft.view.DeviceDiscoveryScreen
import java.nio.file.Paths

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {

        loadLibraries()

        val blueManager = BlueManager()
        val res = blueManager.discover("Jan (from Desktop)")
        Text(text = res)
//        DeviceDiscoveryScreen()
    }
}

@Suppress("UnsafeDynamicallyLoadedCode")
private fun loadLibraries() {
    Logger.i { "Loading libraries" }
    val nativeLibraryPath =
        Paths.get(System.getProperty("user.dir"), "src", "desktopMain", "jniLibs", "linux-x86_64", "libblue_jni.so")
    System.load("$nativeLibraryPath")
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}