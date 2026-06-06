package com.abuzahra.control.model

data class Device(
    val id: String = "",
    val name: String = "",
    val model: String = "",
    val brand: String = "",
    val os: String = "",
    val battery: String = "",
    val network: String = "",
    val ip: String = "",
    val active: Boolean = false,
    val lastSeen: Long = 0L,
    val info: String = "",
    val linkCode: String = "",
    val linkedAt: Long = 0L
) {
    val isOnline: Boolean
        get() {
            if (!active) return false
            val fiveMinutesAgo = System.currentTimeMillis() - 5 * 60 * 1000
            return lastSeen > fiveMinutesAgo
        }
    
    val lastSeenText: String
        get() {
            if (lastSeen == 0L) return "لم يتصل بعد"
            val diff = System.currentTimeMillis() - lastSeen
            return when {
                diff < 60 * 1000 -> "متصل الآن"
                diff < 60 * 60 * 1000 -> "منذ ${diff / (60 * 1000)} دقيقة"
                diff < 24 * 60 * 60 * 1000 -> "منذ ${diff / (60 * 60 * 1000)} ساعة"
                else -> "منذ ${diff / (24 * 60 * 60 * 1000)} يوم"
            }
        }
}
