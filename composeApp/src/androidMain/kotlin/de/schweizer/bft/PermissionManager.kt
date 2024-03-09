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
    enum class Permission(val permissions: Array<String>) {
        BackgroundLocation(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)),
        Bluetooth(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
            ) else emptyArray()
        )
    }

    private val permissions: List<Permission> = listOf(
        Permission.BackgroundLocation,
        Permission.Bluetooth,
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
            if (it.permissions.any { permission -> !isPermissionGranted(context, permission) }) {
                deniedPerms.add(it)
            }
        }
        _deniedPermissions.update { deniedPerms }
    }

    fun requestPermissions(permissions: List<Permission>, continuation: Continuation<Boolean>) {
        requestContinuation = continuation
        requestPermissionsDelegate(permissions)
    }

    fun onPermissionResult(permissionResult: List<Boolean>) {
        requestContinuation?.resume(permissionResult.all { it })
        requestContinuation = null
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
