package de.schweizer.bft

import kotlin.jvm.JvmStatic

object Logger {
    @JvmStatic
    external fun init(string: String)
}

enum class LogLevel {
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE
}