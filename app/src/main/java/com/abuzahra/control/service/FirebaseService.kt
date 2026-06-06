package com.abuzahra.control.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.abuzahra.control.model.CommandResult
import com.abuzahra.control.model.Device
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirebaseService {
    private const val TAG = "FirebaseService"
    private const val PREFS_NAME = "abu_zahra_control"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ID = "user_id"
    
    val database: DatabaseReference = Firebase.database.reference
    
    private var prefs: SharedPreferences? = null
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // --- Auth via RTDB (no Firebase Auth) ---
    
    data class UserInfo(
        val email: String = "",
        val password: String = "",
        val userId: String = "",
        val createdAt: Long = 0L,
        val role: String = "admin"
    )
    
    val currentUserEmail: String?
        get() = prefs?.getString(KEY_USER_EMAIL, null)
    
    val currentUserId: String?
        get() = prefs?.getString(KEY_USER_ID, null)
    
    val isLoggedIn: Boolean
        get() = prefs?.getBoolean(KEY_LOGGED_IN, false) == true
    
    private fun emailToKey(email: String): String {
        return email.replace(".", "_").replace("@", "_at_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")
    }
    
    private fun encodePassword(password: String): String {
        return Base64.encodeToString(password.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }
    
    private fun decodePassword(encoded: String): String {
        return String(Base64.decode(encoded, Base64.NO_WRAP), Charsets.UTF_8)
    }
    
    private fun generateUserId(email: String): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuilder()
        val base = email.hashCode().toString()
        for (i in base.indices) {
            val idx = Math.abs(base[i].code % chars.length)
            sb.append(chars[idx])
        }
        while (sb.length < 12) {
            sb.append(chars[Math.abs(email.length * System.currentTimeMillis().toInt() % chars.length)])
        }
        return sb.substring(0, 12)
    }
    
    fun signIn(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        val emailKey = emailToKey(email)
        val encodedPw = encodePassword(password)
        
        database.child("control_users").child(emailKey).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onComplete(false, "البريد الإلكتروني غير مسجل")
                    return@addOnSuccessListener
                }
                
                val storedPw = snapshot.child("password").getValue(String::class.java) ?: ""
                
                if (storedPw != encodedPw) {
                    onComplete(false, "كلمة المرور غير صحيحة")
                    return@addOnSuccessListener
                }
                
                val userId = snapshot.child("userId").getValue(String::class.java) ?: generateUserId(email)
                
                // Save login state
                prefs?.edit()
                    ?.putBoolean(KEY_LOGGED_IN, true)
                    ?.putString(KEY_USER_EMAIL, email)
                    ?.putString(KEY_USER_ID, userId)
                    ?.apply()
                
                Log.d(TAG, "User logged in: $email")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "SignIn failed: ${e.message}")
                onComplete(false, "خطأ في الاتصال: ${e.message}")
            }
    }
    
    fun signUp(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        val emailKey = emailToKey(email)
        val encodedPw = encodePassword(password)
        val userId = generateUserId(email)
        
        // Check if email already exists
        database.child("control_users").child(emailKey).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    onComplete(false, "البريد الإلكتروني مسجل بالفعل")
                    return@addOnSuccessListener
                }
                
                val userData = mapOf(
                    "email" to email,
                    "password" to encodedPw,
                    "userId" to userId,
                    "createdAt" to System.currentTimeMillis(),
                    "role" to "admin"
                )
                
                database.child("control_users").child(emailKey).setValue(userData)
                    .addOnSuccessListener {
                        // Save login state
                        prefs?.edit()
                            ?.putBoolean(KEY_LOGGED_IN, true)
                            ?.putString(KEY_USER_EMAIL, email)
                            ?.putString(KEY_USER_ID, userId)
                            ?.apply()
                        
                        Log.d(TAG, "User registered: $email")
                        onComplete(true, null)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "SignUp failed: ${e.message}")
                        onComplete(false, "خطأ: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, "خطأ: ${e.message}")
            }
    }
    
    fun signOut() {
        prefs?.edit()?.clear()?.apply()
    }
    
    // --- Device Management ---
    
    fun getDevices(): Flow<List<Device>> = callbackFlow {
        val userId = currentUserId ?: return@callbackFlow
        val ref = database.child("control_users").child(emailToKey(currentUserEmail ?: "")).child("devices")
        
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = mutableListOf<Device>()
                for (deviceSnap in snapshot.children) {
                    val deviceId = deviceSnap.key ?: continue
                    // Fetch actual device data
                    database.child("devices").child(deviceId).get()
                        .addOnSuccessListener { devSnap ->
                            val device = devSnap.getValue(Device::class.java)
                            if (device != null) {
                                device.id = deviceId
                                devices.add(device)
                            }
                        }
                }
                // Also check /devices directly for all linked devices
                database.child("devices").orderByChild("linked").equalTo(true).get()
                    .addOnSuccessListener { allDevices ->
                        val deviceList = mutableListOf<Device>()
                        for (devSnap in allDevices.children) {
                            val device = devSnap.getValue(Device::class.java)
                            device?.let {
                                it.id = devSnap.key ?: ""
                                deviceList.add(it)
                            }
                        }
                        trySend(deviceList)
                    }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getDevices cancelled: ${error.message}")
            }
        })
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    // --- Command Sending ---
    
    fun sendCommand(deviceId: String, command: String, params: String = "", onComplete: (Boolean) -> Unit) {
        val commandData = mapOf(
            "command" to command,
            "params" to params,
            "timestamp" to ServerValue.TIMESTAMP
        )
        
        database.child("devices").child(deviceId).child("command").setValue(commandData)
            .addOnCompleteListener { task ->
                Log.d(TAG, "Command sent: $command to $deviceId, success=${task.isSuccessful}")
                onComplete(task.isSuccessful)
            }
    }
    
    fun listenForResult(deviceId: String, callback: (CommandResult) -> Unit): ValueEventListener {
        val ref = database.child("devices").child(deviceId).child("result")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = snapshot.getValue(CommandResult::class.java)
                result?.let {
                    Log.d(TAG, "Result received: ${it.command} -> ${it.status}")
                    callback(it)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "listenForResult cancelled: ${error.message}")
            }
        }
        
        ref.addValueEventListener(listener)
        return listener
    }
    
    fun removeResultListener(deviceId: String, listener: ValueEventListener) {
        database.child("devices").child(deviceId).child("result").removeEventListener(listener)
    }
    
    // --- Device Linking ---
    
    fun linkDevice(code: String, onComplete: (Boolean, String?) -> Unit) {
        if (!isLoggedIn) {
            return onComplete(false, "لم يتم تسجيل الدخول")
        }
        
        val emailKey = emailToKey(currentUserEmail ?: "")
        
        database.child("linkCodes").child(code).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onComplete(false, "الكود غير صحيح")
                    return@addOnSuccessListener
                }
                
                val codeData = snapshot.value as Map<*, *>
                val expiresAt = codeData["expiresAt"] as? Long ?: 0
                val used = codeData["used"] as? Boolean ?: false
                
                if (used) {
                    onComplete(false, "الكود مستخدم بالفعل")
                    return@addOnSuccessListener
                }
                
                if (System.currentTimeMillis() > expiresAt) {
                    onComplete(false, "انتهت صلاحية الكود")
                    return@addOnSuccessListener
                }
                
                // Mark code as used
                database.child("linkCodes").child(code).child("used").setValue(true)
                
                // Add device to user's devices list
                val deviceId = (codeData["deviceId"] as? String) ?: ""
                if (deviceId.isNotEmpty()) {
                    database.child("control_users").child(emailKey).child("devices").child(deviceId).setValue(true)
                    database.child("devices").child(deviceId).child("linked").setValue(true)
                }
                
                onComplete(true, "تم ربط الجهاز بنجاح!")
            }
            .addOnFailureListener { e ->
                onComplete(false, "خطأ: ${e.message}")
            }
    }
}
