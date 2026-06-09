package com.abuzahra.manager.data.repository

import com.abuzahra.manager.data.model.CommandResult
import com.abuzahra.manager.data.model.Device
import com.abuzahra.manager.service.FirebaseManager
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow

class DeviceRepository {
    fun getDevices(): Flow<List<Device>> {
        return FirebaseManager.getDevices()
    }

    fun sendCommand(deviceId: String, command: String, params: String = "", callback: (Boolean) -> Unit) {
        FirebaseManager.sendCommand(deviceId, command, params, callback)
    }

    fun listenForResult(deviceId: String, callback: (CommandResult) -> Unit): ValueEventListener {
        return FirebaseManager.listenForResult(deviceId, callback)
    }

    fun removeResultListener(deviceId: String, listener: ValueEventListener) {
        FirebaseManager.removeResultListener(deviceId, listener)
    }

    fun linkDevice(code: String, callback: (Boolean, String?) -> Unit) {
        FirebaseManager.linkDevice(code, callback)
    }
}
