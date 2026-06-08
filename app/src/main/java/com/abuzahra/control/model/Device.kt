package com.abuzahra.control.model

data class Device(
    var id: String = "",
    var name: String = "",
    var model: String = "",
    var brand: String = "",
    var battery: String = "",
    var active: Boolean = false,
    var lastSeen: Long = 0L
) {
    val isOnline: Boolean
        get() = active && (System.currentTimeMillis() - lastSeen) < 300000

    val statusText: String
        get() = if (isOnline) "متصل" else "غير متصل"
}

data class CommandResult(
    val command: String = "",
    val status: String = "",
    val result: String = "",
    val timestamp: Long = 0L
)
