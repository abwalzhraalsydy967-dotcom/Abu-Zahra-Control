package com.abuzahra.tracker.service

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.abuzahra.tracker.utils.DeviceInfo
import com.abuzahra.tracker.utils.PermissionHelper
import com.google.firebase.database.FirebaseDatabase

/**
 * Comprehensive diagnostic tool for the Target Device (Tracker) app.
 * Checks every critical step and returns results for display.
 */
object DiagnosticTool {

    private const val TAG = "DiagnosticTracker"

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
        val totalChecks = 14
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
            results[1] = results[1].copy(
                status = if (app != null) CheckStatus.PASS else CheckStatus.FAIL,
                detail = if (app != null) "تم التهيئة بنجاح" else "لم يتم تهيئة Firebase"
            )
        } catch (e: Exception) {
            results[1] = results[1].copy(status = CheckStatus.FAIL, detail = "خطأ: ${e.message}")
        }
        onCheckDone()

        // 3. Firebase Database URL
        results.add(CheckItem("db_url", "رابط قاعدة البيانات", CheckStatus.RUNNING))
        try {
            val dbUrl = com.abuzahra.tracker.utils.Constants.FIREBASE_DB_URL
            results[2] = results[2].copy(
                status = if (dbUrl.contains("firebaseio")) CheckStatus.PASS else CheckStatus.FAIL,
                detail = dbUrl.take(40) + "..."
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

        // 5. Device ID
        results.add(CheckItem("device_id", "معرّف الجهاز", CheckStatus.RUNNING))
        try {
            val deviceId = DeviceInfo.getDeviceId(context)
            results[4] = results[4].copy(
                status = if (deviceId.isNotEmpty()) CheckStatus.PASS else CheckStatus.FAIL,
                detail = deviceId
            )
        } catch (e: Exception) {
            results[4] = results[4].copy(status = CheckStatus.FAIL, detail = "خطأ: ${e.message}")
        }
        onCheckDone()

        // 6. Device registration in Firebase
        results.add(CheckItem("device_reg", "تسجيل الجهاز في Firebase", CheckStatus.RUNNING))
        try {
            val deviceId = DeviceInfo.getDeviceId(context)
            FirebaseDatabase.getInstance().reference.child("devices").child(deviceId).get()
                .addOnSuccessListener { snap ->
                    results[5] = results[5].copy(
                        status = if (snap.exists()) CheckStatus.PASS else CheckStatus.FAIL,
                        detail = if (snap.exists()) "الجهاز مسجل" else "الجهاز غير مسجل - أدخل كود الربط"
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

        // 7. Usage Stats permission
        results.add(CheckItem("perm_usage", "صلاحية إحصائيات الاستخدام", CheckStatus.RUNNING))
        val usageOk = PermissionHelper.isUsageStatsEnabled(context)
        results[6] = results[6].copy(
            status = if (usageOk) CheckStatus.PASS else CheckStatus.FAIL,
            detail = if (usageOk) "ممنوحة" else "غير ممنوحة"
        )
        onCheckDone()

        // 8. Notification Access
        results.add(CheckItem("perm_notif", "صلاحية الإشعارات", CheckStatus.RUNNING))
        val notifOk = PermissionHelper.isNotificationAccessEnabled(context)
        results[7] = results[7].copy(
            status = if (notifOk) CheckStatus.PASS else CheckStatus.FAIL,
            detail = if (notifOk) "ممنوحة" else "غير ممنوحة"
        )
        onCheckDone()

        // 9. Install Unknown Apps
        results.add(CheckItem("perm_install", "صلاحية تثبيت تطبيقات", CheckStatus.RUNNING))
        val installOk = PermissionHelper.canInstallUnknownApps(context)
        results[8] = results[8].copy(
            status = if (installOk) CheckStatus.PASS else CheckStatus.FAIL,
            detail = if (installOk) "ممنوحة" else "غير ممنوحة"
        )
        onCheckDone()

        // 10. Device Admin
        results.add(CheckItem("perm_admin", "صلاحية إدارة الجهاز", CheckStatus.RUNNING))
        val adminOk = PermissionHelper.isDeviceAdminActive(context)
        results[9] = results[9].copy(
            status = if (adminOk) CheckStatus.PASS else CheckStatus.FAIL,
            detail = if (adminOk) "مفعّلة" else "غير مفعّلة"
        )
        onCheckDone()

        // 11. Battery Optimization
        results.add(CheckItem("perm_battery", "تجاهل تحسين البطارية", CheckStatus.RUNNING))
        val batteryOk = PermissionHelper.isBatteryOptimizationIgnored(context)
        results[10] = results[10].copy(
            status = if (batteryOk) CheckStatus.PASS else CheckStatus.FAIL,
            detail = if (batteryOk) "متجاهل" else "غير متجاهل"
        )
        onCheckDone()

        // 12. Overlay Permission
        results.add(CheckItem("perm_overlay", "صلاحية العرض فوق التطبيقات", CheckStatus.RUNNING))
        val overlayOk = PermissionHelper.canDrawOverOthers(context)
        results[11] = results[11].copy(
            status = if (overlayOk) CheckStatus.PASS else CheckStatus.FAIL,
            detail = if (overlayOk) "ممنوحة" else "غير ممنوحة"
        )
        onCheckDone()

        // 13. App info
        results.add(CheckItem("app_info", "معلومات التطبيق", CheckStatus.RUNNING))
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            results[12] = results[12].copy(
                status = CheckStatus.PASS,
                detail = "v${pInfo.versionName} (${pInfo.versionCode}) - ${context.packageName}"
            )
        } catch (e: Exception) {
            results[12] = results[12].copy(status = CheckStatus.WARN, detail = "تعذر قراءة معلومات التطبيق")
        }
        onCheckDone()

        // 14. Link codes table
        results.add(CheckItem("linkcodes_table", "جدول أكواد الربط", CheckStatus.RUNNING))
        try {
            FirebaseDatabase.getInstance().reference.child("linkCodes").get()
                .addOnSuccessListener { snap ->
                    results[13] = results[13].copy(
                        status = CheckStatus.PASS,
                        detail = "الجدول موجود (${snap.childrenCount} كود)"
                    )
                    onCheckDone()
                }.addOnFailureListener { e ->
                    results[13] = results[13].copy(status = CheckStatus.FAIL, detail = "فشل: ${e.message}")
                    onCheckDone()
                }
        } catch (e: Exception) {
            results[13] = results[13].copy(status = CheckStatus.FAIL, detail = "خطأ: ${e.message}")
            onCheckDone()
        }
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
