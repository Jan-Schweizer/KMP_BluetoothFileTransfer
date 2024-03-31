package de.schweizer.bft.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import de.schweizer.bft.LogLevel
import de.schweizer.bft.NativeLogger
import java.nio.file.Paths
import co.touchlab.kermit.Logger as L

class BftApp {

    @Composable
    fun run() {
        Navigator(DeviceDiscoveryScreen())
    }

    companion object {
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
                    "libblue_jni.so",
                )
            System.load("$nativeLibraryPath")
        }
    }
}
