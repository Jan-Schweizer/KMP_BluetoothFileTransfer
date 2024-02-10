package de.schweizer.bft

import androidx.compose.runtime.Composable
import de.schweizer.bft.ui.DesktopDeviceDiscoveryViewModel
import de.schweizer.bft.ui.DeviceDiscoveryScreenCommon
import java.nio.file.Paths

class Application {

    private val viewModel = DesktopDeviceDiscoveryViewModel()

    @Composable
    fun DeviceDiscoveryScreen() {
        DeviceDiscoveryScreenCommon(viewModel)
    }

    companion object {
        @Suppress("UnsafeDynamicallyLoadedCode")
        private fun loadLibraries() {
            co.touchlab.kermit.Logger.i { "Loading libraries" }
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

        @JvmStatic
        private external fun init()

        init {
            loadLibraries()

            init()
            Logger.init(LogLevel.INFO.name)
        }
    }
}