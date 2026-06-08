package com.abuzahra.control.service

import android.content.Context
import android.util.Log
import com.abuzahra.control.model.CommandResult
import com.abuzahra.control.model.Device
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebaseService {
    private const val TAG = "FirebaseService"

    val auth: FirebaseAuth by lazy { Firebase.auth }
    val database: DatabaseReference by lazy { Firebase.database.reference }

    val currentUser get() = try { auth.currentUser } catch (_: Exception) { null }
    val userId get() = try { currentUser?.uid } catch (_: Exception) { null }
    val userEmail get() = try { currentUser?.email } catch (_: Exception) { null }

    private var initialized = false

    fun init(ctx: Context) {
        if (initialized) return
        initialized = true
        try {
            Log.d(TAG, "FirebaseService initialized. User: ${currentUser?.email}")
        } catch (e: Exception) {
            Log.e(TAG, "Init error: ${e.message}")
        }
    }

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Signed in: ${currentUser?.email}")
                    callback(true, null)
                } else {
                    val msg = translateError(task.exception?.message ?: "فشل")
                    Log.e(TAG, "SignIn failed: ${task.exception?.message}")
                    callback(false, msg)
                }
            }
    }

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = userId ?: ""
                    Log.d(TAG, "Registered: $email uid=$uid")
                    try {
                        database.child("users").child(uid).setValue(mapOf(
                            "email" to email,
                            "createdAt" to ServerValue.TIMESTAMP,
                            "role" to "admin"
                        ))
                    } catch (_: Exception) {}
                    callback(true, null)
                } else {
                    val msg = translateError(task.exception?.message ?: "فشل")
                    Log.e(TAG, "SignUp failed: ${task.exception?.message}")
                    callback(false, msg)
                }
            }
    }

    private fun translateError(errMsg: String): String {
        return when {
            errMsg.contains("INVALID_LOGIN_CREDENTIALS", true) -> "البريد أو كلمة المرور غير صحيحة"
            errMsg.contains("wrong password", true) -> "البريد أو كلمة المرور غير صحيحة"
            errMsg.contains("user not found", true) -> "لا يوجد حساب بهذا البريد"
            errMsg.contains("email already in use", true) -> "هذا البريد مستخدم بالفعل"
            errMsg.contains("weak password", true) -> "كلمة المرور ضعيفة"
            errMsg.contains("invalid email", true) -> "البريد الإلكتروني غير صحيح"
            errMsg.contains("too many", true) -> "محاولات كثيرة، حاول لاحقاً"
            errMsg.contains("network", true) -> "خطأ في الاتصال"
            else -> "فشل: $errMsg"
        }
    }

    fun signOut() {
        try { auth.signOut() } catch (_: Exception) {}
    }

    fun getDevices(): Flow<List<Device>> = callbackFlow {
        val uid = userId ?: run { trySend(emptyList()); return@callbackFlow }
        val ref = database.child("users").child(uid).child("devices")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val deviceIds = snapshot.children.mapNotNull { it.key }
                    if (deviceIds.isEmpty()) { trySend(emptyList()); return }

                    val devices = mutableListOf<Device>()
                    var remaining = deviceIds.size

                    for (devId in deviceIds) {
                        database.child("devices").child(devId).get()
                            .addOnSuccessListener { devSnap ->
                                try {
                                    val device = devSnap.getValue(Device::class.java)
                                    if (device != null) { device.id = devId; devices.add(device) }
                                } catch (_: Exception) {}
                                remaining--
                                if (remaining == 0) trySend(devices.toList())
                            }
                            .addOnFailureListener {
                                remaining--
                                if (remaining == 0) trySend(devices.toList())
                            }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "getDevices error: ${e.message}")
                    trySend(emptyList())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getDevices cancelled: ${error.message}")
                trySend(emptyList())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun sendCommand(deviceId: String, command: String, params: String = "", callback: (Boolean) -> Unit) {
        database.child("devices").child(deviceId).child("command").setValue(mapOf(
            "command" to command,
            "params" to params,
            "timestamp" to ServerValue.TIMESTAMP
        )).addOnCompleteListener { callback(it.isSuccessful) }
    }

    fun listenForResult(deviceId: String, callback: (CommandResult) -> Unit): ValueEventListener {
        val ref = database.child("devices").child(deviceId).child("result")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val result = snapshot.getValue(CommandResult::class.java)
                    if (result != null && result.status.isNotEmpty()) callback(result)
                } catch (_: Exception) {}
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeResultListener(deviceId: String, listener: ValueEventListener) {
        try { database.child("devices").child(deviceId).child("result").removeEventListener(listener) }
        catch (_: Exception) {}
    }

    fun linkDevice(code: String, callback: (Boolean, String?) -> Unit) {
        val uid = userId ?: run { callback(false, "لم يتم تسجيل الدخول"); return }
        database.child("linkCodes").child(code).get()
            .addOnSuccessListener { snapshot ->
                try {
                    if (!snapshot.exists()) { callback(false, "الكود غير صحيح"); return@addOnSuccessListener }
                    val data = snapshot.value as? Map<*, *> ?: run { callback(false, "خطأ"); return@addOnSuccessListener }
                    if (data["used"] == true) { callback(false, "الكود مستخدم"); return@addOnSuccessListener }
                    val deviceId = data["deviceId"] as? String ?: ""
                    database.child("linkCodes").child(code).child("used").setValue(true)
                    database.child("users").child(uid).child("devices").child(deviceId).setValue(true)
                    callback(true, "تم ربط الجهاز بنجاح!")
                } catch (e: Exception) { callback(false, "خطأ: ${e.message}") }
            }
            .addOnFailureListener { callback(false, "خطأ: ${it.message}") }
    }
}
