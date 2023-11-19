package de.schweizer.bft

class BlueManager {

    private external fun initLogger()
    external fun discover(input: String): String

    init {
        initLogger()
    }
}