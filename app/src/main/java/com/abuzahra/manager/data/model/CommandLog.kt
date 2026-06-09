package com.abuzahra.manager.data.model

data class CommandLog(
    val id: String = "",
    val command: String = "",
    val deviceId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "",
    val result: String = ""
)
