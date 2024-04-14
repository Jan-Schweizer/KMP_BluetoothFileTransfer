package de.schweizer.bft

sealed class BlueError(open val msg: String) {
    data object Unknown : BlueError("An unknown error occurred")
}
