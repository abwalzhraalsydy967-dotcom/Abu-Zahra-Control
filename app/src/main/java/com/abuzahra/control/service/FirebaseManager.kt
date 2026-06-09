package com.abuzahra.control.service

import android.util.Log
import com.abuzahra.control.data.model.CommandResult
import com.abuzahra.control.data.model.Device
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    val auth: FirebaseAuth by lazy {
        try {
            Firebase.auth
        } catch (e: Exception) {
            Log.e(TAG, "auth init: ${e.message}")
            FirebaseAuth.getInstance()
        }
    }

    val database: DatabaseReference by lazy {
        try {
            Firebase.database.reference
        } catch (e: Exception) {
            Log.e(TAG, "database init: ${e.message}")
            FirebaseDatabase.getInstance().reference
        }
    }

    val currentUser get() = try { auth.currentUser } catch (_: Exception) { null }
    val userId get() = try { currentUser?.uid } catch (_: Exception) { null }
    val userEmail get() = try { currentUser?.email } catch (_: Exception) { null }

    // ==================== AUTH ====================

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Signed in: ${currentUser?.email}")
                        callback(true, null)
                    } else {
                        callback(false, translateError(task.exception?.message ?: "فشل"))
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "SignIn error: ${e.message}")
            callback(false, "خطأ: ${e.message}")
        }
    }

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        try {
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
                        callback(false, translateError(task.exception?.message ?: "فشل"))
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "SignUp error: ${e.message}")
            callback(false, "خطأ: ${e.message}")
        }
    }

    fun signOut() {
        try { auth.signOut() } catch (_: Exception) {}
    }

    fun resetPassword(email: String, callback: (Boolean, String?) -> Unit) {
        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) callback(true, null)
                    else callback(false, task.exception?.message ?: "فشل")
                }
        } catch (e: Exception) {
            callback(false, "خطأ: ${e.message}")
        }
    }

    // ==================== DEVICES ====================

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
                                    if (device != null) {
                                        device.id = devId
                                        devices.add(device)
                                    }
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

    // ==================== COMMANDS ====================

    fun sendCommand(deviceId: String, command: String, params: String = "", callback: (Boolean) -> Unit) {
        try {
            database.child("devices").child(deviceId).child("command").setValue(mapOf(
                "command" to command,
                "params" to params,
                "timestamp" to ServerValue.TIMESTAMP
            )).addOnCompleteListener { callback(it.isSuccessful) }
        } catch (e: Exception) {
            Log.e(TAG, "sendCommand error: ${e.message}")
            callback(false)
        }
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
        try {
            database.child("devices").child(deviceId).child("result").removeEventListener(listener)
        } catch (_: Exception) {}
    }

    // ==================== DEVICE LINKING ====================

    /**
     * Generate a new 6-digit link code and store it in Firebase.
     * The target device app will read this code and register itself.
     */
    fun generateLinkCode(callback: (String?, String?) -> Unit) {
        val uid = userId ?: run { callback(null, "لم يتم تسجيل الدخول"); return }
        try {
            // Generate random 6-digit code
            val code = (100000..999999).random().toString()
            val expiresAt = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes expiry

            val linkCodeData = mapOf(
                "code" to code,
                "ownerUid" to uid,
                "used" to false,
                "expiresAt" to expiresAt,
                "createdAt" to System.currentTimeMillis()
            )

            database.child("linkCodes").child(code).setValue(linkCodeData)
                .addOnSuccessListener {
                    Log.d(TAG, "Link code generated: $code")
                    callback(code, null)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to generate code: ${e.message}")
                    callback(null, "فشل توليد الكود: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Generate code error: ${e.message}")
            callback(null, "خطأ: ${e.message}")
        }
    }

    /**
     * Listen for a generated link code to be used by a target device.
     * Once the target device links, this callback fires with the deviceId.
     */
    fun listenForLinkResult(code: String, callback: (Boolean, String?, String?) -> Unit): ValueEventListener {
        val uid = userId ?: run { callback(false, null, "لم يتم تسجيل الدخول"); throw IllegalStateException("Not signed in") }
        val ref = database.child("linkCodes").child(code)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) {
                        callback(false, null, "انتهت صلاحية الكود")
                        return
                    }
                    val data = snapshot.value as? Map<*, *> ?: return
                    val used = data["used"] as? Boolean ?: false
                    val deviceId = data["deviceId"] as? String ?: ""

                    if (used && deviceId.isNotEmpty()) {
                        // Target device has linked! Claim ownership
                        database.child("users").child(uid).child("devices").child(deviceId).setValue(true)
                            .addOnSuccessListener {
                                callback(true, deviceId, "تم ربط الجهاز بنجاح!")
                            }
                            .addOnFailureListener { e ->
                                callback(false, deviceId, "فشل في ربط الملكية: ${e.message}")
                            }
                    }
                } catch (_: Exception) {}
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, null, "خطأ: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeLinkCodeListener(code: String, listener: ValueEventListener) {
        try {
            database.child("linkCodes").child(code).removeEventListener(listener)
        } catch (_: Exception) {}
    }

    fun linkDevice(code: String, callback: (Boolean, String?) -> Unit) {
        val uid = userId ?: run { callback(false, "لم يتم تسجيل الدخول"); return }
        try {
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
        } catch (e: Exception) { callback(false, "خطأ: ${e.message}") }
    }

    // ==================== HELPERS ====================

    private fun translateError(msg: String): String {
        return when {
            msg.contains("INVALID_LOGIN_CREDENTIALS", true) -> "البريد أو كلمة المرور غير صحيحة"
            msg.contains("wrong password", true) -> "البريد أو كلمة المرور غير صحيحة"
            msg.contains("user not found", true) -> "لا يوجد حساب بهذا البريد"
            msg.contains("email already in use", true) -> "هذا البريد مستخدم بالفعل"
            msg.contains("weak password", true) -> "كلمة المرور ضعيفة"
            msg.contains("invalid email", true) -> "البريد الإلكتروني غير صحيح"
            msg.contains("too many", true) -> "محاولات كثيرة، حاول لاحقاً"
            msg.contains("network", true) -> "خطأ في الاتصال"
            else -> "فشل: $msg"
        }
    }
}
