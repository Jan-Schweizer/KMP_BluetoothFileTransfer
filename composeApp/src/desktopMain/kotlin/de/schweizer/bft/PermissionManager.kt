package de.schweizer.bft

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

actual object PermissionManager {
    actual enum class Permission

    private val _deniedPermissions: MutableStateFlow<List<Permission>> = MutableStateFlow(emptyList())
    actual val deniedPermissions: StateFlow<List<Permission>> = _deniedPermissions.asStateFlow()

    // Permissions? What permissions?
    actual fun requestPermissions(permissions: List<Permission>, continuation: Continuation<Boolean>) {
        continuation.resume(true)
    }

    actual fun isPermissionGranted(permission: Permission): Boolean = true
}
