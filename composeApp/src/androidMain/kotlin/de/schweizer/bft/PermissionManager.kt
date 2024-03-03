package de.schweizer.bft

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

object PermissionManager {

    // TODO: sealed class and all permissions objects?
    data class Permission(val permission: String)

    val backgroundLocation: Permission = Permission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    val bluetoothAdvertise: Permission? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Permission(Manifest.permission.BLUETOOTH_ADVERTISE) else null
    val bluetoothConnect: Permission? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Permission(Manifest.permission.BLUETOOTH_CONNECT) else null
    val bluetoothScan: Permission? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Permission(Manifest.permission.BLUETOOTH_SCAN) else null

    val permissions = listOfNotNull(
        backgroundLocation,
        bluetoothAdvertise,
        bluetoothConnect,
        bluetoothScan,
    )

    private var requestContinuation: Continuation<Boolean>? = null
    private lateinit var requestPermissionsDelegate: (List<Permission>) -> Unit

    fun setup(requestPermissionsDelegate: (List<Permission>) -> Unit) {
        this.requestPermissionsDelegate = requestPermissionsDelegate
    }

    fun requestPermissions(permissions: List<Permission>, continuation: Continuation<Boolean>) {
        requestPermissionsDelegate(permissions)
        requestContinuation = continuation
    }

    fun onPermissionResult(permissionResult: List<Boolean>) {
        requestContinuation?.resume(permissionResult.all { it })
        requestContinuation = null
    }

    fun isPermissionGranted(context: Context, permission: Permission): Boolean {
        return ContextCompat.checkSelfPermission(context, permission.permission) == PackageManager.PERMISSION_GRANTED
    }
}