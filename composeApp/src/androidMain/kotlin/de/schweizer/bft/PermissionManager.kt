package de.schweizer.bft

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {

    private const val REQUEST_CODE = 0x1

    data class Permission(val permission: String)

    val backgroundLocation = Permission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    fun hasPermission(context: Context, permission: Permission): Boolean {
        return ContextCompat.checkSelfPermission(context, permission.permission) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(activity: Activity, permissions: List<Permission>) {
        ActivityCompat.requestPermissions(activity, permissions.map { it.permission }.toTypedArray(), REQUEST_CODE)
    }
}