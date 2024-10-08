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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.schweizer.bft.PermissionManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

actual class RequestDeniedPermissionsScreen : Screen {
    @Composable
    override fun Content() {
        Toolbar(title = "Request Necessary Permissions", onBack = {}) {
            val navigator = LocalNavigator.currentOrThrow

            LaunchedEffect(Unit) {
                PermissionManager.deniedPermissions.onEach {
                    if (it.isEmpty()) {
                        navigator.pop()
                    }
                }.launchIn(this)
            }

            val deniedPermissions by PermissionManager.deniedPermissions.collectAsState()

            if (deniedPermissions.isNotEmpty()) {
                val nextDeniedPermission = deniedPermissions.first()

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val (permissionName, permissionDescription) = when (nextDeniedPermission) {
                        PermissionManager.Permission.Bluetooth -> "Nearby Devices" to "Please allow nearby devices access for this app"
                        PermissionManager.Permission.BackgroundLocation -> "Background Location" to "Please enable Background Location all the time"
                    }
                    DeniedPermission(permissionName, permissionDescription)
                }
            }
        }
    }

    @Composable
    private fun DeniedPermission(permissionName: String, permissionDescription: String) {
        val context = LocalContext.current

        // TODO: First request permission before directing the user to the app settings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary),
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = permissionName,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Text(
                    text = permissionDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Spacer(modifier = Modifier.requiredHeight(4.dp))
                Text(
                    text = "To system settings",
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable(role = Role.Button) {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", context.packageName, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                )
            }
        }
    }
}
