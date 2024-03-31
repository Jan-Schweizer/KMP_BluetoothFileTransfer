import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.schweizer.bft.ui.BftApp
import de.schweizer.bft.ui.theme.BftAppTheme

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        BftAppTheme {
            BftApp().run()
        }
    }
}
