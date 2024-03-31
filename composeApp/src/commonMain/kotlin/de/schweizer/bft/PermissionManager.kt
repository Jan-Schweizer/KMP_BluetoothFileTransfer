package de.schweizer.bft

import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.Continuation

expect object PermissionManager {

    enum class Permission

    val deniedPermissions: StateFlow<List<Permission>>

    fun requestPermissions(permissions: List<Permission>, continuation: Continuation<Boolean>)
    fun isPermissionGranted(permission: Permission): Boolean
}
