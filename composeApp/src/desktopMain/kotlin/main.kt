import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.schweizer.bft.Application

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {

        val app = Application()
        app.Discover()
    }
}

