package de.schweizer.bft

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

object PermissionManager {
    sealed class Permission(val permission: String) {
        data object BackgroundLocation : Permission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        @RequiresApi(Build.VERSION_CODES.S)
        data object BluetoothAdvertise : Permission(Manifest.permission.BLUETOOTH_ADVERTISE)

        @RequiresApi(Build.VERSION_CODES.S)
        data object BluetoothConnect : Permission(Manifest.permission.BLUETOOTH_CONNECT)

        @RequiresApi(Build.VERSION_CODES.S)
        data object BluetoothScan : Permission(Manifest.permission.BLUETOOTH_SCAN)
    }

    private val permissions: List<Permission> = listOf(
        Permission.BackgroundLocation,
        Permission.BluetoothAdvertise,
        Permission.BluetoothConnect,
        Permission.BluetoothScan,
    )

    private val _deniedPermissions: MutableStateFlow<List<Permission>> = MutableStateFlow(emptyList())
    val deniedPermissions: StateFlow<List<Permission>> = _deniedPermissions

    private var requestContinuation: Continuation<Boolean>? = null
    private lateinit var requestPermissionsDelegate: (List<Permission>) -> Unit

    fun setup(requestPermissionsDelegate: (List<Permission>) -> Unit) {
        this.requestPermissionsDelegate = requestPermissionsDelegate
    }

    fun updateDeniedPermissions(context: Context) {
        val deniedPerms = mutableListOf<Permission>()
        permissions.forEach {
            if (!isPermissionGranted(context, it)) {
                deniedPerms.add(it)
            }
        }
        _deniedPermissions.update { deniedPerms }
    }

    fun requestPermissions(permissions: List<Permission>, continuation: Continuation<Boolean>) {
        requestPermissionsDelegate(permissions)
        requestContinuation = continuation
    }

    fun onPermissionResult(permissionResult: List<Boolean>) {
        requestContinuation?.resume(permissionResult.all { it })
        requestContinuation = null
    }

    private fun isPermissionGranted(context: Context, permission: Permission): Boolean {
        return ContextCompat.checkSelfPermission(context, permission.permission) == PackageManager.PERMISSION_GRANTED
    }
}
