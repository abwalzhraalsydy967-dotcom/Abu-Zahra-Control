package com.abuzahra.control.service

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
    
    val auth: FirebaseAuth = Firebase.auth
    val database: DatabaseReference = Firebase.database.reference
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    fun signIn(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message ?: "فشل تسجيل الدخول")
                }
            }
    }
    
    fun signUp(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userData = mapOf(
                        "email" to email,
                        "createdAt" to System.currentTimeMillis(),
                        "role" to "admin"
                    )
                    database.child("users").child(user!!.uid).setValue(userData)
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message ?: "فشل إنشاء الحساب")
                }
            }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    fun sendPasswordReset(email: String, onComplete: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, "تم إرسال رابط إعادة تعيين كلمة المرور")
                } else {
                    onComplete(false, task.exception?.message ?: "فشل")
                }
            }
    }
    
    // Get all devices linked to current user
    fun getDevices(): Flow<List<Device>> = callbackFlow {
        val userId = currentUser?.uid ?: return@callbackFlow
        val ref = database.child("users").child(userId).child("devices")
        
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = mutableListOf<Device>()
                for (deviceSnap in snapshot.children) {
                    val device = deviceSnap.getValue(Device::class.java)
                    device?.let { devices.add(it) }
                }
                trySend(devices)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getDevices cancelled: ${error.message}")
            }
        })
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    // Send command to a device
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
    
    // Listen for results from a device
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
    
    // Link device with code
    fun linkDevice(code: String, onComplete: (Boolean, String?) -> Unit) {
        val userId = currentUser?.uid ?: return onComplete(false, "لم يتم تسجيل الدخول")
        
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
                    database.child("users").child(userId).child("devices").child(deviceId).setValue(true)
                }
                
                // Also link in the device side
                database.child("devices").child(deviceId).child("linked").setValue(true)
                
                onComplete(true, "تم ربط الجهاز بنجاح!")
            }
            .addOnFailureListener { e ->
                onComplete(false, "خطأ: ${e.message}")
            }
    }
    
    // Check if user has any linked devices
    fun hasLinkedDevices(): Flow<Boolean> = callbackFlow {
        val userId = currentUser?.uid ?: return@callbackFlow
        val ref = database.child("users").child(userId).child("devices")
        
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists() && snapshot.childrenCount > 0)
            }
            
            override fun onCancelled(error: DatabaseError) {
                trySend(false)
            }
        })
        
        awaitClose { ref.removeEventListener(listener) }
    }
}
