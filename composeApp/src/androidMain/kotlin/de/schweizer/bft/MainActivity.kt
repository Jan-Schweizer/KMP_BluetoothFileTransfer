package de.schweizer.bft

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import co.touchlab.kermit.Logger
import de.schweizer.bft.ui.BftApp
import de.schweizer.bft.ui.theme.BftAppTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        PermissionManager.onPermissionResult(it.values.toList())
    }

    private val startActivityForResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        it.data?.action?.let { action ->
            Logger.i { "Received Activity result with action=$action" }
        }
        if (it.resultCode == Activity.RESULT_OK) {
            Logger.i { "Activity result OK" }
        } else {
            Logger.i { "Activity result Not OK" }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.setTag("Bft Android App")

        setupPermissionHandling()
        setupBlueManager(this)

        setContent {
            BftAppTheme {
                BftApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PermissionManager.updateDeniedPermissions(this)
        BlueManager.registerBluetoothBroadcastReceiver(this)
    }

    override fun onDestroy() {
        BlueManager.unregisterBluetoothBroadcastReceiver(this)
        super.onDestroy()
    }

    private fun setupPermissionHandling() {
        PermissionManager.setup { permissions ->
            requestPermissionsLauncher.launch(permissions.flatMap { it.permissions.toList() }.toTypedArray())
        }
    }

    private fun setupBlueManager(context: Context) {
        BlueManager.setup(context) {
            val intent = Intent().apply {
                action = BluetoothAdapter.ACTION_REQUEST_ENABLE
            }
            startActivityForResultLauncher.launch(intent)
        }
    }
}
