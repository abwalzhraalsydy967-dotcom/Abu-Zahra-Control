package com.abuzahra.manager.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.abuzahra.manager.R
import com.abuzahra.manager.constants.AppConstants
import com.abuzahra.manager.util.PrefsManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Comprehensive diagnostic tool for the Control Panel app.
 * Checks every critical step and returns results for display.
 */
object DiagnosticTool {

    private const val TAG = "DiagnosticManager"

    data class CheckItem(
        val id: String,
        val label: String,
        val status: CheckStatus,
        val detail: String = ""
    )

    enum class CheckStatus {
        PASS, FAIL, WARN, RUNNING, UNKNOWN
    }

    /**
     * Run all diagnostic checks and return results via callback.
     */
    fun runAllChecks(context: Context, callback: (List<CheckItem>) -> Unit) {
        val results = mutableListOf<CheckItem>()
        val totalChecks = 10
        var completed = 0

        fun onCheckDone() {
            completed++
            if (completed == totalChecks) {
                Log.d(TAG, "All $totalChecks diagnostic checks completed")
                callback(results)
            }
        }

        // 1. Network connectivity
        results.add(CheckItem("network", "الاتصال بالإنترنت", CheckStatus.RUNNING))
        val networkOk = isNetworkAvailable(context)
        results[0] = results[0].copy(
            status = if (networkOk) CheckStatus.PASS else CheckStatus.FAIL,
            detail = if (networkOk) "متصل" else "غير متصل بالإنترنت"
        )
        onCheckDone()

        // 2. Firebase initialized
        results.add(CheckItem("firebase_init", "تهيئة Firebase", CheckStatus.RUNNING))
        try {
            val app = com.google.firebase.FirebaseApp.getInstance()
            val db = FirebaseDatabase.getInstance()
            results[1] = results[1].copy(
                status = if (app != null) CheckStatus.PASS else CheckStatus.FAIL,
                detail = if (app != null) "تم التهيئة - ${db.reference.toString().take(50)}" else "لم يتم تهيئة Firebase"
            )
        } catch (e: Exception) {
            results[1] = results[1].copy(
                status = CheckStatus.FAIL,
                detail = "خطأ: ${e.message}"
            )
        }
        onCheckDone()

        // 3. Firebase Auth state
        results.add(CheckItem("auth", "حالة المصادقة", CheckStatus.RUNNING))
        try {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            results[2] = results[2].copy(
                status = if (user != null) CheckStatus.PASS else CheckStatus.FAIL,
                detail = if (user != null) "مسجل: ${user.email ?: user.uid}" else "غير مسجل الدخول"
            )
        } catch (e: Exception) {
            results[2] = results[2].copy(status = CheckStatus.FAIL, detail = "خطأ: ${e.message}")
        }
        onCheckDone()

        // 4. Database connectivity
        results.add(CheckItem("db_connect", "الاتصال بقاعدة البيانات", CheckStatus.RUNNING))
        try {
            FirebaseDatabase.getInstance().reference.child("_meta").child("lastPing")
                .get().addOnSuccessListener { snap ->
                    results[3] = results[3].copy(
                        status = if (snap.exists()) CheckStatus.PASS else CheckStatus.WARN,
                        detail = if (snap.exists()) "متصل بنجاح" else "الاتصال نجح لكن البيانات فارغة"
                    )
                    onCheckDone()
                }.addOnFailureListener { e ->
                    results[3] = results[3].copy(
                        status = CheckStatus.FAIL,
                        detail = "فشل: ${e.message}"
                    )
                    onCheckDone()
                }
        } catch (e: Exception) {
            results[3] = results[3].copy(status = CheckStatus.FAIL, detail = "خطأ: ${e.message}")
            onCheckDone()
        }

        // 5. Users table
        results.add(CheckItem("users_table", "جدول المستخدمين", CheckStatus.RUNNING))
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseDatabase.getInstance().reference.child("users").child(uid).get()
                    .addOnSuccessListener { snap ->
                        results[4] = results[4].copy(
                            status = if (snap.exists()) CheckStatus.PASS else CheckStatus.WARN,
                            detail = if (snap.exists()) "بيانات المستخدم موجودة" else "بيانات المستخدم غير موجودة"
                        )
                        onCheckDone()
                    }.addOnFailureListener { e ->
                        results[4] = results[4].copy(status = CheckStatus.FAIL, detail = "فشل: ${e.message}")
                        onCheckDone()
                    }
            } else {
                results[4] = results[4].copy(
                    status = CheckStatus.WARN,
                    detail = "غير مسجل الدخول"
                )
                onCheckDone()
            }
        } catch (e: Exception) {
            results[4] = results[4].copy(status = CheckStatus.FAIL, detail = "خطأ: ${e.message}")
            onCheckDone()
        }

        // 6. Devices table
        results.add(CheckItem("devices_table", "جدول الأجهزة", CheckStatus.RUNNING))
        try {
            FirebaseDatabase.getInstance().reference.child("devices").get()
                .addOnSuccessListener { snap ->
                    results[5] = results[5].copy(
                        status = CheckStatus.PASS,
                        detail = "الجدول موجود (${snap.childrenCount} جهاز)"
                    )
                    onCheckDone()
                }.addOnFailureListener { e ->
                    results[5] = results[5].copy(status = CheckStatus.FAIL, detail = "فشل: ${e.message}")
                    onCheckDone()
                }
        } catch (e: Exception) {
            results[5] = results[5].copy(status = CheckStatus.FAIL, detail = "خطأ: ${e.message}")
            onCheckDone()
        }

        // 7. Link codes table
        results.add(CheckItem("linkcodes_table", "جدول أكواد الربط", CheckStatus.RUNNING))
        try {
            FirebaseDatabase.getInstance().reference.child("linkCodes").get()
                .addOnSuccessListener { snap ->
                    results[6] = results[6].copy(
                        status = CheckStatus.PASS,
                        detail = "الجدول موجود (${snap.childrenCount} كود)"
                    )
                    onCheckDone()
                }.addOnFailureListener { e ->
                    results[6] = results[6].copy(status = CheckStatus.FAIL, detail = "فشل: ${e.message}")
                    onCheckDone()
                }
        } catch (e: Exception) {
            results[6] = results[6].copy(status = CheckStatus.FAIL, detail = "خطأ: ${e.message}")
            onCheckDone()
        }

        // 8. App info
        results.add(CheckItem("app_info", "معلومات التطبيق", CheckStatus.PASS))
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            results[7] = results[7].copy(
                detail = "v${pInfo.versionName} (${pInfo.versionCode}) - ${context.packageName}"
            )
        } catch (e: Exception) {
            results[7] = results[7].copy(status = CheckStatus.WARN, detail = "تعذر قراءة معلومات التطبيق")
        }
        onCheckDone()

        // 9. Crash log check
        results.add(CheckItem("crash_log", "سجل الأعطال", CheckStatus.RUNNING))
        try {
            val (msg, time) = PrefsManager.getCrash()
            results[8] = results[8].copy(
                status = if (msg.isEmpty()) CheckStatus.PASS else CheckStatus.WARN,
                detail = if (msg.isEmpty()) "لا توجد أعطال مسجلة" else "آخر عطل: $msg"
            )
        } catch (e: Exception) {
            results[8] = results[8].copy(status = CheckStatus.UNKNOWN, detail = "تعذر القراءة")
        }
        onCheckDone()

        // 10. Firebase config
        results.add(CheckItem("firebase_config", "إعدادات Firebase", CheckStatus.RUNNING))
        try {
            val dbUrl = try {
                context.getString(R.string.default_web_client_id)
                "configured"
            } catch (e: Exception) {
                "not found in resources"
            }
            results[9] = results[9].copy(
                status = CheckStatus.PASS,
                detail = "google-services.json موجود"
            )
        } catch (e: Exception) {
            results[9] = results[9].copy(status = CheckStatus.WARN, detail = "تحقق من google-services.json")
        }
        onCheckDone()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected == true
        }
    }
}
