package de.schweizer.bft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import de.schweizer.bft.ui.BftApp
import de.schweizer.bft.ui.theme.BftAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.setTag("Bft Android App")

        setContent {
            SetupPermissionHandling()

            BftAppTheme {
                BftApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PermissionManager.updateDeniedPermissions(this)
    }

    @Composable
    fun SetupPermissionHandling() {
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
            PermissionManager.onPermissionResult(it.values.toList())
        }
        PermissionManager.setup { permissions ->
            requestPermissionLauncher.launch(permissions.map { it.permission }.toTypedArray())
        }
    }
}
