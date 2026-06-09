package com.abuzahra.manager.data.model

import com.abuzahra.manager.constants.AppConstants

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
        get() = active && (System.currentTimeMillis() - lastSeen) < AppConstants.DEVICE_ONLINE_THRESHOLD_MS

    val statusText: String
        get() = if (isOnline) "متصل" else "غير متصل"
}
