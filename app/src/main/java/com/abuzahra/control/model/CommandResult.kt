package com.abuzahra.control.model

data class CommandResult(
    val command: String = "",
    val status: String = "",
    val result: String = "",
    val timestamp: Long = 0L
)
