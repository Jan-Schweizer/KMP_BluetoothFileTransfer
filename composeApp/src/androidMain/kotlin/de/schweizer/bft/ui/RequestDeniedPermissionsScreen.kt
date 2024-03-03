package de.schweizer.bft.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import de.schweizer.bft.PermissionManager

@Composable
fun RequestDeniedPermissionsScreen() {
    // TODO: if nextDeniedPermission == null -> popBackstack

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!PermissionManager.isPermissionGranted(context, PermissionManager.bluetoothAdvertise!!) ||
            !PermissionManager.isPermissionGranted(context, PermissionManager.bluetoothConnect!!) ||
            !PermissionManager.isPermissionGranted(context, PermissionManager.bluetoothScan!!)
        ) {
            DeniedPermission(
                permissionName = "Bluetooth",
                permissionDescription = "Please provide access to Bluetooth",
            )
        }
    }
}

@Composable
private fun DeniedPermission(permissionName: String, permissionDescription: String) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
        ) {
            Text(text = permissionName, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.requiredHeight(16.dp))
            Text(text = permissionDescription, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.requiredHeight(16.dp))
            Box(
                modifier = Modifier
                    .clickable(role = Role.Button) {
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", context.packageName, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
            ) {
                Text(
                    text = "To system settings",
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
