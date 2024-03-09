import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.schweizer.bft.ui.BftApp

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        BftApp.run()
    }
}
