package de.schweizer.bft

sealed class BlueError(open val msg: String) {
    data class Generic(override val msg: String) : BlueError(msg)
    data object DiscoveryNotPossible : BlueError("Discovery not possible")
    data object AdapterNotAvailable : BlueError("No Bluetooth adapter available for this device")
    data object Unknown : BlueError("An unknown error occurred")
}
