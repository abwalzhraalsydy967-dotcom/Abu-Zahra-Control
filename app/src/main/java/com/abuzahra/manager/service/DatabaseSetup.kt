package com.abuzahra.manager.service

import android.util.Log
import com.google.firebase.database.*

/**
 * Sets up the Firebase Realtime Database structure on first launch.
 * Creates all required nodes/tables so both apps can communicate properly.
 */
object DatabaseSetup {

    private const val TAG = "DatabaseSetup"

    /**
     * Initialize database structure. Call this from App.onCreate() or first launch.
     */
    fun initialize() {
        try {
            val db = FirebaseManager.database
            Log.d(TAG, "Initializing Firebase database structure...")

            // 1. Ensure root structure exists
            setupRootStructure(db)

            // 2. Setup security rules placeholder (metadata)
            setupMetadata(db)

            // 3. Setup link codes cleanup node
            setupLinkCodesNode(db)

            Log.d(TAG, "Database structure initialization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Database setup error: ${e.message}")
        }
    }

    /**
     * Verify database connectivity and structure by reading root nodes.
     * Returns a list of check results.
     */
    fun verifyStructure(callback: (List<DbCheckResult>) -> Unit) {
        val results = mutableListOf<DbCheckResult>()
        val checks = listOf(
            "users" to "جدول المستخدمين",
            "devices" to "جدول الأجهزة",
            "linkCodes" to "جدول أكواد الربط"
        )

        var remaining = checks.size
        for ((node, label) in checks) {
            FirebaseManager.database.child(node).get()
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

    private fun setupRootStructure(db: DatabaseReference) {
        // Ensure the key root nodes exist
        val rootNodes = mapOf(
            "_meta" to mapOf(
                "app" to "manager",
                "version" to "3.2",
                "structureVersion" to 1,
                "initializedAt" to ServerValue.TIMESTAMP
            )
        )

        db.child("_meta").updateChildren(rootNodes)
            .addOnSuccessListener {
                Log.d(TAG, "Root metadata created")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Root metadata creation: ${e.message}")
            }
    }

    private fun setupMetadata(db: DatabaseReference) {
        // Write a connectivity test value
        db.child("_meta").child("lastPing").setValue(ServerValue.TIMESTAMP)
            .addOnSuccessListener {
                Log.d(TAG, "Database connectivity confirmed")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Database connectivity failed: ${e.message}")
            }
    }

    private fun setupLinkCodesNode(db: DatabaseReference) {
        // Ensure linkCodes node exists with an index marker
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
        val status: String, // "exists", "empty", "error"
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
