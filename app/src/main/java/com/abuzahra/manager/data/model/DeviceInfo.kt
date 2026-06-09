package com.abuzahra.manager.data.model

data class DeviceInfo(
    val batteryLevel: String = "",
    val wifiName: String = "",
    val networkType: String = "",
    val storageTotal: String = "",
    val storageUsed: String = "",
    val simOperator: String = "",
    val simNumber: String = "",
    val androidVersion: String = "",
    val phoneModel: String = "",
    val screenResolution: String = "",
    val ipAddress: String = "",
    val gpsLocation: String = "",
    val imei: String = ""
)
