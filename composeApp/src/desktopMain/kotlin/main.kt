import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import view.DeviceDiscoveryScreen

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        DeviceDiscoveryScreen()
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}