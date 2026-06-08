package com.abuzahra.control.model

data class Device(
    var id: String = "",
    var name: String = "",
    var model: String = "",
    var brand: String = "",
    var os: String = "",
    var battery: String = "",
    var network: String = "",
    var ip: String = "",
    var active: Boolean = false,
    var lastSeen: Long = 0L,
    var info: String = "",
    var linkCode: String = "",
    var linkedAt: Long = 0L
) {
    val isOnline: Boolean
        get() {
            if (!active) return false
            return (System.currentTimeMillis() - lastSeen) < 5 * 60 * 1000
        }

    val lastSeenText: String
        get() {
            if (lastSeen == 0L) return "لم يتصل بعد"
            val diff = System.currentTimeMillis() - lastSeen
            return when {
                diff < 60000 -> "متصل الآن"
                diff < 3600000 -> "منذ ${diff / 60000} دقيقة"
                diff < 86400000 -> "منذ ${diff / 3600000} ساعة"
                else -> "منذ ${diff / 86400000} يوم"
            }
        }
}

data class CommandResult(
    val command: String = "",
    val status: String = "",
    val result: String = "",
    val timestamp: Long = 0L
)
