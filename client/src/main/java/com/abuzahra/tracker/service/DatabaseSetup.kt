package com.abuzahra.tracker.service

import android.util.Log
import com.google.firebase.database.*

/**
 * Sets up the Firebase Realtime Database structure on first launch.
 * Ensures all required nodes exist for the target device app.
 */
object DatabaseSetup {

    private const val TAG = "DatabaseSetup"

    /**
     * Initialize database structure. Call this from App.onCreate().
     */
    fun initialize() {
        try {
            val db = FirebaseDatabase.getInstance().reference
            Log.d(TAG, "Initializing Firebase database structure (tracker)...")

            // 1. Setup tracker metadata
            setupTrackerMetadata(db)

            // 2. Ensure devices node exists
            setupDevicesNode(db)

            // 3. Setup link codes node
            setupLinkCodesNode(db)

            Log.d(TAG, "Tracker database structure initialization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Database setup error: ${e.message}")
        }
    }

    /**
     * Verify database connectivity and structure.
     */
    fun verifyStructure(callback: (List<DbCheckResult>) -> Unit) {
        val results = mutableListOf<DbCheckResult>()
        val checks = listOf(
            "devices" to "جدول الأجهزة",
            "linkCodes" to "جدول أكواد الربط",
            "_meta" to "بيانات النظام"
        )

        var remaining = checks.size
        val db = FirebaseDatabase.getInstance().reference
        for ((node, label) in checks) {
            db.child(node).get()
                .addOnSuccessListener { snapshot ->
                    results.add(DbCheckResult(
                        name = label,
                        node = node,
                        status = if (snapshot.exists()) "exists" else "empty",
                        children = snapshot.childrenCount
                    ))
                    remaining--
                    if (remaining == 0) callback(results)
                }
                .addOnFailureListener { e ->
                    results.add(DbCheckResult(
                        name = label,
                        node = node,
                        status = "error",
                        error = e.message
                    ))
                    remaining--
                    if (remaining == 0) callback(results)
                }
        }
    }

    private fun setupTrackerMetadata(db: DatabaseReference) {
        db.child("_meta").child("lastTrackerPing").setValue(ServerValue.TIMESTAMP)
            .addOnSuccessListener {
                Log.d(TAG, "Tracker ping successful")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Tracker ping failed: ${e.message}")
            }
    }

    private fun setupDevicesNode(db: DatabaseReference) {
        db.child("devices").child("_schema").setValue(mapOf(
            "type" to "devices",
            "description" to "Registered target devices",
            "fields" to listOf("id", "name", "model", "brand", "os", "battery", "network", "ip", "active", "lastSeen", "heartbeat", "command", "result")
        )).addOnSuccessListener {
            Log.d(TAG, "Devices schema initialized")
        }.addOnFailureListener { e ->
            Log.w(TAG, "Devices schema init: ${e.message}")
        }
    }

    private fun setupLinkCodesNode(db: DatabaseReference) {
        db.child("linkCodes").child("_index").setValue(mapOf(
            "type" to "linkCodes",
            "description" to "Temporary 6-digit codes for device linking"
        )).addOnSuccessListener {
            Log.d(TAG, "Link codes node initialized")
        }.addOnFailureListener { e ->
            Log.w(TAG, "Link codes node init: ${e.message}")
        }
    }

    data class DbCheckResult(
        val name: String,
        val node: String,
        val status: String,
        val children: Long = 0,
        val error: String? = null
    ) {
        fun statusText(): String = when (status) {
            "exists" -> "موجود ($children عنصر)"
            "empty" -> "فارغ (جاهز)"
            "error" -> "خطأ: $error"
            else -> status
        }

        fun isOk(): Boolean = status == "exists" || status == "empty"
    }
}
