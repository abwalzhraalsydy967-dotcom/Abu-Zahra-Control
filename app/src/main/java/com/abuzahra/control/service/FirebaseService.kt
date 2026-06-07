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

    private lateinit var _auth: FirebaseAuth
    private lateinit var _database: DatabaseReference

    val auth: FirebaseAuth
        get() {
            if (!::_auth.isInitialized) {
                try {
                    _auth = Firebase.auth
                } catch (e: Exception) {
                    Log.e(TAG, "Auth init error: ${e.message}")
                }
            }
            return _auth
        }

    val database: DatabaseReference
        get() {
            if (!::_database.isInitialized) {
                try {
                    _database = Firebase.database.reference
                } catch (e: Exception) {
                    Log.e(TAG, "Database init error: ${e.message}")
                }
            }
            return _database
        }

    val currentUser: FirebaseUser?
        get() = try { auth.currentUser } catch (e: Exception) { Log.e(TAG, "currentUser error: ${e.message}"); null }

    val userId: String?
        get() = try { currentUser?.uid } catch (e: Exception) { null }

    val userEmail: String?
        get() = try { currentUser?.email } catch (e: Exception) { null }

    private var prefs: SharedPreferences? = null

    fun init(ctx: Context) {
        try {
            prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Force init auth and database
            auth.currentUser // trigger lazy init
            database.child("_ping") // trigger lazy init
            Log.d(TAG, "FirebaseService initialized. User: ${userEmail ?: "null"}")
        } catch (e: Exception) {
            Log.e(TAG, "Init error: ${e.message}")
        }
    }

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Signed in: ${auth.currentUser?.email}")
                        callback(true, null)
                    } else {
                        val errMsg = task.exception?.message ?: "فشل تسجيل الدخول"
                        Log.e(TAG, "SignIn failed: $errMsg")
                        // Provide Arabic error message
                        val userMsg = when {
                            errMsg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                            errMsg.contains("wrong password", ignoreCase = true) ||
                            errMsg.contains("password is invalid", ignoreCase = true) ->
                                "البريد أو كلمة المرور غير صحيحة"
                            errMsg.contains("user not found", ignoreCase = true) ||
                            errMsg.contains("no user record", ignoreCase = true) ->
                                "لا يوجد حساب بهذا البريد"
                            errMsg.contains("too many", ignoreCase = true) ->
                                "محاولات كثيرة، حاول لاحقاً"
                            errMsg.contains("network", ignoreCase = true) ->
                                "خطأ في الاتصال بالإنترنت"
                            errMsg.contains("invalid api key", ignoreCase = true) ||
                            errMsg.contains("API key", ignoreCase = true) ->
                                "خطأ في إعدادات Firebase - تأكد من تفعيل المصادقة في Firebase Console"
                            else -> "فشل تسجيل الدخول: $errMsg"
                        }
                        callback(false, userMsg)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "SignIn failure: ${e.message}")
                    callback(false, "خطأ: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "SignIn exception: ${e.message}")
            callback(false, "خطأ داخلي: ${e.message}")
        }
    }

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: ""
                        Log.d(TAG, "Registered: $email uid=$uid")
                        // Save user profile in RTDB (non-blocking)
                        try {
                            database.child("users").child(uid).setValue(mapOf(
                                "email" to email,
                                "createdAt" to ServerValue.TIMESTAMP,
                                "role" to "admin"
                            )).addOnFailureListener { e ->
                                Log.e(TAG, "Save profile failed: ${e.message}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Save profile error: ${e.message}")
                        }
                        callback(true, null)
                    } else {
                        val errMsg = task.exception?.message ?: "فشل إنشاء الحساب"
                        Log.e(TAG, "SignUp failed: $errMsg")
                        val userMsg = when {
                            errMsg.contains("email already in use", ignoreCase = true) ->
                                "هذا البريد مستخدم بالفعل"
                            errMsg.contains("weak password", ignoreCase = true) ->
                                "كلمة المرور ضعيفة"
                            errMsg.contains("invalid email", ignoreCase = true) ->
                                "البريد الإلكتروني غير صحيح"
                            errMsg.contains("network", ignoreCase = true) ->
                                "خطأ في الاتصال بالإنترنت"
                            errMsg.contains("invalid api key", ignoreCase = true) ||
                            errMsg.contains("API key", ignoreCase = true) ->
                                "خطأ في إعدادات Firebase - تأكد من تفعيل المصادقة في Firebase Console"
                            else -> "فشل إنشاء الحساب: $errMsg"
                        }
                        callback(false, userMsg)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "SignUp failure: ${e.message}")
                    callback(false, "خطأ: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "SignUp exception: ${e.message}")
            callback(false, "خطأ داخلي: ${e.message}")
        }
    }

    fun signOut() {
        try {
            auth.signOut()
            Log.d(TAG, "Signed out")
        } catch (e: Exception) {
            Log.e(TAG, "SignOut error: ${e.message}")
        }
    }

    fun getDevices(): Flow<List<Device>> = callbackFlow {
        try {
            val uid = userId ?: run { trySend(emptyList()); return@callbackFlow }
            val ref = database.child("users").child(uid).child("devices")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val deviceIds = mutableListOf<String>()
                        for (ds in snapshot.children) {
                            ds.key?.let { deviceIds.add(it) }
                        }
                        if (deviceIds.isEmpty()) {
                            trySend(emptyList())
                            return
                        }

                        val devices = mutableListOf<Device>()
                        var remaining = deviceIds.size

                        for (devId in deviceIds) {
                            try {
                                database.child("devices").child(devId).get()
                                    .addOnSuccessListener { devSnap ->
                                        try {
                                            val device = devSnap.getValue(Device::class.java)
                                            if (device != null) {
                                                device.id = devId
                                                devices.add(device)
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Parse device error: ${e.message}")
                                        }
                                        remaining--
                                        if (remaining == 0) trySend(devices.toList())
                                    }
                                    .addOnFailureListener {
                                        Log.e(TAG, "Fetch device error: ${it.message}")
                                        remaining--
                                        if (remaining == 0) trySend(devices.toList())
                                    }
                            } catch (e: Exception) {
                                Log.e(TAG, "Get device error: ${e.message}")
                                remaining--
                                if (remaining == 0) trySend(devices.toList())
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange error: ${e.message}")
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
        } catch (e: Exception) {
            Log.e(TAG, "getDevices error: ${e.message}")
            trySend(emptyList())
        }
    }

    fun sendCommand(deviceId: String, command: String, params: String = "", callback: (Boolean) -> Unit) {
        try {
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
                .addOnFailureListener { e ->
                    Log.e(TAG, "Command failed: ${e.message}")
                    callback(false)
                }
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
                    if (result != null && result.status.isNotEmpty()) {
                        callback(result)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Parse result error: ${e.message}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "listenForResult cancelled: ${error.message}")
            }
        }
        try {
            ref.addValueEventListener(listener)
        } catch (e: Exception) {
            Log.e(TAG, "listenForResult error: ${e.message}")
        }
        return listener
    }

    fun removeResultListener(deviceId: String, listener: ValueEventListener) {
        try {
            database.child("devices").child(deviceId).child("result").removeEventListener(listener)
        } catch (e: Exception) {
            Log.e(TAG, "removeResultListener error: ${e.message}")
        }
    }

    fun linkDevice(code: String, callback: (Boolean, String?) -> Unit) {
        try {
            val uid = userId ?: return callback(false, "لم يتم تسجيل الدخول")
            database.child("linkCodes").child(code).get()
                .addOnSuccessListener { snapshot ->
                    try {
                        if (!snapshot.exists()) {
                            callback(false, "الكود غير صحيح")
                            return@addOnSuccessListener
                        }
                        val data = snapshot.value as? Map<*, *> ?: run {
                            callback(false, "خطأ في البيانات")
                            return@addOnSuccessListener
                        }
                        val used = data["used"] as? Boolean ?: false
                        if (used) {
                            callback(false, "الكود مستخدم")
                            return@addOnSuccessListener
                        }

                        val expiresAt = data["expiresAt"] as? Long ?: 0
                        if (System.currentTimeMillis() > expiresAt) {
                            callback(false, "الكود منتهي")
                            return@addOnSuccessListener
                        }

                        val deviceId = data["deviceId"] as? String ?: ""
                        database.child("linkCodes").child(code).child("used").setValue(true)
                        if (deviceId.isNotEmpty()) {
                            database.child("users").child(uid).child("devices").child(deviceId).setValue(true)
                            database.child("devices").child(deviceId).child("linked").setValue(true)
                        }
                        callback(true, "تم ربط الجهاز بنجاح!")
                    } catch (e: Exception) {
                        callback(false, "خطأ: ${e.message}")
                    }
                }
                .addOnFailureListener { e ->
                    callback(false, "خطأ: ${e.message}")
                }
        } catch (e: Exception) {
            callback(false, "خطأ داخلي: ${e.message}")
        }
    }
}
