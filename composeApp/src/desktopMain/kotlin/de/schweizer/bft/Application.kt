package de.schweizer.bft

import androidx.compose.runtime.Composable
import de.schweizer.bft.ui.DesktopDeviceDiscoveryViewModel
import de.schweizer.bft.ui.DeviceDiscoveryScreenCommon
import de.schweizer.bft.ui.theme.BftAppTheme
import java.nio.file.Paths
import co.touchlab.kermit.Logger as L

class Application {

    private val viewModel = DesktopDeviceDiscoveryViewModel()

    @Composable
    private fun DeviceDiscoveryScreen() {
        DeviceDiscoveryScreenCommon(viewModel)
    }

    companion object {
        @Composable
        fun run() {
            val app = Application()
            BftAppTheme {
                app.DeviceDiscoveryScreen()
            }
        }

        @JvmStatic
        private external fun init()

        init {
            loadLibraries()

            init()
            NativeLogger.init(LogLevel.INFO.name)
        }

        @Suppress("UnsafeDynamicallyLoadedCode")
        private fun loadLibraries() {
            L.i { "Loading libraries" }
            val nativeLibraryPath =
                Paths.get(
                    System.getProperty("user.dir"),
                    "src",
                    "desktopMain",
                    "jniLibs",
                    "linux-x86_64",
                    "libblue_jni.so"
                )
            System.load("$nativeLibraryPath")
        }

    }
}