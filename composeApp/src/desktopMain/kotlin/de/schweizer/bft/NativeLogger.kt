package de.schweizer.bft

import kotlin.jvm.JvmStatic

object NativeLogger {
    @JvmStatic
    external fun init(string: String)
}

enum class LogLevel {
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE,
}
