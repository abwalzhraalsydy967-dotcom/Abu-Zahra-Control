package com.abuzahra.control.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.abuzahra.control.model.CommandResult
import com.abuzahra.control.model.Device
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
    private const val PREFS_NAME = "abu_zahra_control_prefs"

    val auth: FirebaseAuth = Firebase.auth
    val database: DatabaseReference = Firebase.database.reference

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val userId: String?
        get() = currentUser?.uid

    val userEmail: String?
        get() = currentUser?.email

    private var prefs: SharedPreferences? = null

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Signed in: ${auth.currentUser?.email}")
                    callback(true, null)
                } else {
                    Log.e(TAG, "SignIn failed: ${task.exception?.message}")
                    callback(false, task.exception?.message ?: "فشل تسجيل الدخول")
                }
            }
    }

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    // Save user profile in RTDB
                    database.child("users").child(uid).setValue(mapOf(
                        "email" to email,
                        "createdAt" to System.currentTimeMillis(),
                        "role" to "admin"
                    ))
                    Log.d(TAG, "Registered: $email")
                    callback(true, null)
                } else {
                    Log.e(TAG, "SignUp failed: ${task.exception?.message}")
                    callback(false, task.exception?.message ?: "فشل إنشاء الحساب")
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getDevices(): Flow<List<Device>> = callbackFlow {
        val uid = userId ?: return@callbackFlow
        val ref = database.child("users").child(uid).child("devices")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deviceIds = mutableListOf<String>()
                for (ds in snapshot.children) {
                    ds.key?.let { deviceIds.add(it) }
                }
                if (deviceIds.isEmpty()) {
                    trySend(emptyList())
                    return
                }
                // Fetch each device
                val devices = mutableListOf<Device>()
                var remaining = deviceIds.size
                if (remaining == 0) { trySend(devices); return }

                for (devId in deviceIds) {
                    database.child("devices").child(devId).get()
                        .addOnSuccessListener { devSnap ->
                            val device = devSnap.getValue(Device::class.java)
                            if (device != null) {
                                device.id = devId
                                devices.add(device)
                            }
                            remaining--
                            if (remaining == 0) trySend(devices.toList())
                        }
                        .addOnFailureListener {
                            remaining--
                            if (remaining == 0) trySend(devices.toList())
                        }
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
        val data = mapOf(
            "command" to command,
            "params" to params,
            "timestamp" to ServerValue.TIMESTAMP
        )
        database.child("devices").child(deviceId).child("command").setValue(data)
            .addOnCompleteListener { task ->
                Log.d(TAG, "Command $command -> $deviceId: ${task.isSuccessful}")
                callback(task.isSuccessful)
            }
    }

    fun listenForResult(deviceId: String, callback: (CommandResult) -> Unit): ValueEventListener {
        val ref = database.child("devices").child(deviceId).child("result")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val result = snapshot.getValue(CommandResult::class.java)
                    if (result != null && result.status.isNotEmpty()) {
                        callback(result)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing result: ${e.message}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "listenForResult: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeResultListener(deviceId: String, listener: ValueEventListener) {
        database.child("devices").child(deviceId).child("result").removeEventListener(listener)
    }

    fun linkDevice(code: String, callback: (Boolean, String?) -> Unit) {
        val uid = userId ?: return callback(false, "لم يتم تسجيل الدخول")
        database.child("linkCodes").child(code).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    callback(false, "الكود غير صحيح")
                    return@addOnSuccessListener
                }
                val data = snapshot.value as? Map<*, *> ?: run { callback(false, "خطأ في البيانات"); return@addOnSuccessListener }
                val used = data["used"] as? Boolean ?: false
                if (used) { callback(false, "الكود مستخدم"); return@addOnSuccessListener }

                val expiresAt = data["expiresAt"] as? Long ?: 0
                if (System.currentTimeMillis() > expiresAt) { callback(false, "الكود منتهي"); return@addOnSuccessListener }

                database.child("linkCodes").child(code).child("used").setValue(true)
                val deviceId = data["deviceId"] as? String ?: ""
                if (deviceId.isNotEmpty()) {
                    database.child("users").child(uid).child("devices").child(deviceId).setValue(true)
                    database.child("devices").child(deviceId).child("linked").setValue(true)
                }
                callback(true, "تم ربط الجهاز بنجاح!")
            }
            .addOnFailureListener { e -> callback(false, "خطأ: ${e.message}") }
    }
}
