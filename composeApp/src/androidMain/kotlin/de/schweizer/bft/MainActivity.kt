package de.schweizer.bft

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import view.DeviceDiscoveryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.loadLibrary("blue_jni")

        val blueManager = BlueManager()
        val res = blueManager.discover("Jan (from Android)")

        setContent {
            Text(text = res)
//            DeviceDiscoveryScreen()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
