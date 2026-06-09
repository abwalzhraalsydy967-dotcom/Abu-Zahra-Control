package com.abuzahra.tracker

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.tracker.service.EventLogger
import com.abuzahra.tracker.service.FirebaseListenerService
import com.abuzahra.tracker.utils.DeviceInfo
import com.abuzahra.tracker.utils.NotificationHelper
import com.abuzahra.tracker.utils.PermissionHelper
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvDeviceInfo: TextView
    private lateinit var btnStart: Button
    private lateinit var etLinkCode: EditText
    private lateinit var btnLink: Button
    private lateinit var btnPermissions: Button
    private lateinit var tvPermStatus: TextView
    private lateinit var permissionsContainer: LinearLayout

    // Event log UI
    private lateinit var eventLogContainer: LinearLayout
    private lateinit var eventLogScroll: ScrollView
    private lateinit var eventLogStatus: TextView
    private val logUpdateListener = { _: List<com.abuzahra.tracker.service.EventLogger.LogEntry> ->
        runOnUiThread { updateEventLog() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
        updatePermStatus()
        updateDeviceInfo()
        EventLogger.addListener(logUpdateListener)
    }

    override fun onResume() {
        super.onResume()
        updatePermStatus()
        updateServiceStatus()
        updateEventLog()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventLogger.removeListener(logUpdateListener)
    }

    private fun buildUI() {
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0xFF0A0A0F.toInt())
        }

        // Main scrollable content (takes remaining space)
        val scrollView = ScrollView(this)
        scrollView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        )

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 32)
        }

        // Title
        mainLayout.addView(TextView(this).apply {
            text = "\uD83D\uDEE1\uFE0F"
            textSize = 48f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = android.view.Gravity.CENTER
        })

        mainLayout.addView(TextView(this).apply {
            text = "Abu Zahra"
            textSize = 28f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            gravity = android.view.Gravity.CENTER
        })

        mainLayout.addView(TextView(this).apply {
            text = "Remote Device Management"
            textSize = 13f
            setTextColor(0xFF71717A.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 24)
        })

        // ===== LINKING SECTION =====
        tvDeviceInfo = createTextView("الجهاز: ---", 12f, 0xFF71717A.toInt())
        tvDeviceInfo.typeface = android.graphics.Typeface.MONOSPACE
        mainLayout.addView(tvDeviceInfo)

        tvStatus = createTextView("الحالة: متوقف \u274C", 18f, 0xFFE63946.toInt())
        tvStatus.setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
        tvStatus.gravity = android.view.Gravity.CENTER
        tvStatus.setPadding(0, 16, 16, 16)
        mainLayout.addView(tvStatus)

        mainLayout.addView(createSectionHeader("نظام الربط"))

        etLinkCode = EditText(this).apply {
            hint = "أدخل رمز الربط (6 أرقام)"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF52525B.toInt())
            setBackgroundColor(0xFF14141F.toInt())
            setPadding(20, 16, 20, 16)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
        }
        mainLayout.addView(etLinkCode)

        btnLink = Button(this).apply {
            text = "ربط الجهاز"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF3B82F6.toInt())
            setPadding(0, 12, 0, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            setOnClickListener { linkDevice() }
        }
        mainLayout.addView(btnLink)

        // ===== PERMISSIONS SECTION =====
        mainLayout.addView(createSectionHeader("الصلاحيات والأذونات"))

        tvPermStatus = createTextView("", 12f, 0xFF71717A.toInt())
        mainLayout.addView(tvPermStatus)

        permissionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8, 0, 8)
        }

        addPermButton(permissionsContainer, "صلاحية الوصول إلى الاستخدام", "usage_stats") {
            PermissionHelper.requestUsageStatsAccess(this)
        }
        addPermButton(permissionsContainer, "صلاحية الوصول إلى الإشعارات", "notification") {
            PermissionHelper.requestNotificationAccess(this)
        }
        addPermButton(permissionsContainer, "صلاحية تثبيت تطبيقات غير معروفة", "install_unknown") {
            PermissionHelper.requestInstallUnknownApps(this)
        }
        addPermButton(permissionsContainer, "صلاحية إدارة الجهاز (مشرف)", "device_admin") {
            PermissionHelper.requestDeviceAdmin(this)
        }
        addPermButton(permissionsContainer, "تجاهل تحسين البطارية", "battery") {
            PermissionHelper.requestBatteryOptimization(this)
        }
        addPermButton(permissionsContainer, "صلاحية العرض فوق التطبيقات", "overlay") {
            PermissionHelper.requestOverlayPermission(this)
        }

        mainLayout.addView(permissionsContainer)

        btnPermissions = Button(this).apply {
            text = "منح جميع الصلاحيات"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF22C55E.toInt())
            setPadding(0, 12, 0, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 24) }
            setOnClickListener { grantAllPermissions() }
        }
        mainLayout.addView(btnPermissions)

        btnStart = Button(this).apply {
            text = "بدء الخدمة"
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFE63946.toInt())
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { toggleService() }
        }
        mainLayout.addView(btnStart)

        scrollView.addView(mainLayout)
        rootLayout.addView(scrollView)

        // ── Event Log Bar at the bottom ──
        rootLayout.addView(buildEventLogBar())

        setContentView(rootLayout)
    }

    /**
     * Build the event log bar for the tracker app.
     */
    private fun buildEventLogBar(): LinearLayout {
        val barRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF1A1A2E.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Divider
        barRoot.addView(View(this).apply {
            setBackgroundColor(0xFF2A2A4A.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
        })

        // Status summary
        val summaryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 6, 12, 4)
        }

        eventLogStatus = TextView(this).apply {
            text = "سجل الأحداث"
            textSize = 11f
            setTextColor(0xFF71717A.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
        }
        summaryRow.addView(eventLogStatus)

        val clearBtn = TextView(this).apply {
            text = "مسح"
            textSize = 10f
            setTextColor(0xFF52525B.toInt())
            setOnClickListener {
                EventLogger.clear()
                updateEventLog()
            }
        }
        summaryRow.addView(clearBtn)
        barRoot.addView(summaryRow)

        // Scrollable log
        eventLogScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 100
            )
        }

        eventLogContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 2, 8, 8)
        }
        eventLogScroll.addView(eventLogContainer)
        barRoot.addView(eventLogScroll)

        return barRoot
    }

    /**
     * Update the event log display.
     */
    private fun updateEventLog() {
        try {
            val logs = EventLogger.getRecentLogs(10)
            val lastEntry = logs.lastOrNull()

            if (lastEntry != null) {
                val icon = if (lastEntry.success) "\u2713" else "\u2717"
                eventLogStatus.text = "$icon ${lastEntry.action}: ${lastEntry.message}"
                eventLogStatus.setTextColor(
                    if (lastEntry.success) 0xFF22C55E.toInt()
                    else 0xFFE63946.toInt()
                )
            } else {
                eventLogStatus.text = "سجل الأحداث"
                eventLogStatus.setTextColor(0xFF71717A.toInt())
            }

            eventLogContainer.removeAllViews()
            for (entry in logs.reversed()) {
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(4, 1, 4, 1)
                }

                val dot = View(this).apply {
                    val d = android.graphics.drawable.GradientDrawable()
                    d.shape = android.graphics.drawable.GradientDrawable.OVAL
                    d.setSize(dp(6), dp(6))
                    d.setColor(if (entry.success) 0xFF22C55E.toInt() else 0xFFE63946.toInt())
                    background = d
                    layoutParams = LinearLayout.LayoutParams(dp(6), dp(6)).apply { marginEnd = dp(6) }
                }
                row.addView(dot)

                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(Date(entry.timestamp))
                row.addView(TextView(this).apply {
                    text = timeStr
                    textSize = 9f
                    setTextColor(0xFF52525B.toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dp(4) }
                })

                row.addView(TextView(this).apply {
                    text = "${entry.action}: ${entry.message}"
                    textSize = 10f
                    setTextColor(if (entry.success) 0xFF22C55E.toInt() else 0xFFE63946.toInt())
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    maxLines = 2
                })

                eventLogContainer.addView(row)
            }

            if (eventLogContainer.childCount > 0) {
                eventLogScroll.post { eventLogScroll.scrollTo(0, 0) }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun createTextView(text: String, size: Float, color: Int): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
        }
    }

    private fun createSectionHeader(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            setPadding(0, 16, 0, 8)
        }
    }

    private fun addPermButton(container: LinearLayout, text: String, type: String, onClick: () -> Unit) {
        val btn = Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF1E1E2E.toInt())
            setPadding(16, 10, 16, 10)
            gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 4, 0, 4) }
            setOnClickListener {
                onClick()
                // Log the permission action
                EventLogger.log(text, success = true, "تم فتح إعدادات الصلاحية")
            }
        }
        container.addView(btn)
    }

    private fun updateDeviceInfo() {
        val deviceId = DeviceInfo.getDeviceId(this)
        tvDeviceInfo.text = "معرف: ${deviceId.take(16)}..."
    }

    private fun updatePermStatus() {
        val usageOk = PermissionHelper.isUsageStatsEnabled(this)
        val notifOk = PermissionHelper.isNotificationAccessEnabled(this)
        val installOk = PermissionHelper.canInstallUnknownApps(this)
        val adminOk = PermissionHelper.isDeviceAdminActive(this)
        val batteryOk = PermissionHelper.isBatteryOptimizationIgnored(this)
        val overlayOk = PermissionHelper.canDrawOverOthers(this)

        val total = 6
        val granted = listOf(usageOk, notifOk, installOk, adminOk, batteryOk, overlayOk).count { it }

        tvPermStatus.text = "تم منح $granted/$total صلاحيات"
        if (granted == total) {
            tvPermStatus.setTextColor(0xFF22C55E.toInt())
        } else if (granted >= 3) {
            tvPermStatus.setTextColor(0xFFF59E0B.toInt())
        } else {
            tvPermStatus.setTextColor(0xFFE63946.toInt())
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermStatus()
        updateServiceStatus()
    }

    private fun updateServiceStatus() {
        if (isServiceRunning()) {
            tvStatus.text = "الحالة: يعمل \u2705"
            tvStatus.setTextColor(0xFF22C55E.toInt())
            btnStart.text = "إيقاف الخدمة"
            btnStart.setBackgroundColor(0xFF71717A.toInt())
        } else {
            tvStatus.text = "الحالة: متوقف \u274C"
            tvStatus.setTextColor(0xFFE63946.toInt())
            btnStart.text = "بدء الخدمة"
            btnStart.setBackgroundColor(0xFFE63946.toInt())
        }
    }

    private fun toggleService() {
        if (isServiceRunning()) {
            stopService()
        } else {
            requestPermissionsAndStart()
        }
    }

    private fun requestPermissionsAndStart() {
        val missing = PermissionHelper.getMissingPermissions(this)
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            startService()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        updatePermStatus()
        startService()
    }

    private fun grantAllPermissions() {
        // 1. Usage Stats
        if (!PermissionHelper.isUsageStatsEnabled(this)) {
            PermissionHelper.requestUsageStatsAccess(this)
            EventLogger.log("صلاحية الاستخدام", success = true, "جاري فتح الإعدادات...")
            Toast.makeText(this, "افتح إعدادات الاستخدام وفعّل الصلاحية", Toast.LENGTH_LONG).show()
            return
        }
        // 2. Notification Access
        if (!PermissionHelper.isNotificationAccessEnabled(this)) {
            PermissionHelper.requestNotificationAccess(this)
            EventLogger.log("صلاحية الإشعارات", success = true, "جاري فتح إعدادات الإشعارات...")
            Toast.makeText(this, "فعّل الوصول إلى الإشعارات لأبو الزهراء", Toast.LENGTH_LONG).show()
            return
        }
        // 3. Install Unknown
        if (!PermissionHelper.canInstallUnknownApps(this)) {
            PermissionHelper.requestInstallUnknownApps(this)
            EventLogger.log("صلاحية التثبيت", success = true, "جاري فتح إعدادات التثبيت...")
            Toast.makeText(this, "اسمح بتثبيت التطبيقات غير المعروفة", Toast.LENGTH_LONG).show()
            return
        }
        // 4. Device Admin
        if (!PermissionHelper.isDeviceAdminActive(this)) {
            PermissionHelper.requestDeviceAdmin(this)
            EventLogger.log("إدارة الجهاز", success = true, "جاري فتح إعدادات المسؤول...")
            Toast.makeText(this, "فعّل إدارة الجهاز كمسؤول", Toast.LENGTH_LONG).show()
            return
        }
        // 5. Battery Optimization
        if (!PermissionHelper.isBatteryOptimizationIgnored(this)) {
            PermissionHelper.requestBatteryOptimization(this)
            EventLogger.success("تحسين البطارية", "تم تجاهل تحسين البطارية")
            Toast.makeText(this, "تجاهل تحسين البطارية", Toast.LENGTH_SHORT).show()
        }
        // 6. Overlay
        if (!PermissionHelper.canDrawOverOthers(this)) {
            PermissionHelper.requestOverlayPermission(this)
            EventLogger.log("العرض فوق التطبيقات", success = true, "جاري فتح الإعدادات...")
            Toast.makeText(this, "اسمح بالعرض فوق التطبيقات", Toast.LENGTH_SHORT).show()
        }

        EventLogger.success("الصلاحيات", "تم منح جميع الصلاحيات")

        // Also request dangerous permissions
        val missing = PermissionHelper.getMissingPermissions(this)
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun linkDevice() {
        val code = etLinkCode.text.toString().trim()
        if (code.length != 6) {
            EventLogger.fail("ربط الجهاز", "الكود غير صحيح - يجب أن يكون 6 أرقام")
            Toast.makeText(this, "أدخل رمز ربط صحيح (6 أرقام)", Toast.LENGTH_SHORT).show()
            return
        }

        EventLogger.log("ربط الجهاز", success = true, "جاري التحقق من الرمز $code...")
        Toast.makeText(this, "جاري التحقق من الرمز...", Toast.LENGTH_SHORT).show()

        val db = FirebaseDatabase.getInstance()
        db.reference.child("linkCodes").child(code).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val linkData = snapshot.value as? Map<*, *>
                val expiresAt = linkData?.get("expiresAt") as? Long ?: 0
                val used = linkData?.get("used") as? Boolean ?: false

                if (used) {
                    EventLogger.fail("ربط الجهاز", "الرمز مستخدم مسبقاً")
                    Toast.makeText(this, "هذا الرمز مستخدم مسبقاً", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                if (System.currentTimeMillis() > expiresAt) {
                    EventLogger.fail("ربط الجهاز", "انتهت صلاحية الرمز")
                    Toast.makeText(this, "انتهت صلاحية الرمز", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Link successful - register device
                val deviceId = DeviceInfo.getDeviceId(this)
                val deviceInfo = DeviceInfo.getFullDeviceInfo(this)

                val deviceData = mapOf(
                    "id" to deviceId,
                    "name" to "${DeviceInfo.getDeviceBrand()} ${DeviceInfo.getDeviceModel()}",
                    "model" to DeviceInfo.getDeviceModel(),
                    "brand" to DeviceInfo.getDeviceBrand(),
                    "os" to DeviceInfo.getDeviceOS(),
                    "battery" to getBatteryLevel(),
                    "network" to DeviceInfo.getIPAddress(),
                    "ip" to DeviceInfo.getIPAddress(),
                    "active" to true,
                    "lastSeen" to System.currentTimeMillis(),
                    "linkCode" to code,
                    "linkedAt" to System.currentTimeMillis(),
                    "info" to deviceInfo
                )

                // Register device in Firebase
                db.reference.child("devices").child(deviceId).setValue(deviceData)
                    .addOnSuccessListener {
                        EventLogger.success("تسجيل الجهاز", "تم تسجيل الجهاز في Firebase")
                    }
                    .addOnFailureListener { e ->
                        EventLogger.fail("تسجيل الجهاز", "خطأ: ${e.message}")
                    }

                // Write deviceId to the link code
                db.reference.child("linkCodes").child(code).child("deviceId").setValue(deviceId)
                db.reference.child("linkCodes").child(code).child("used").setValue(true)

                // Claim ownership if we know the ownerUid
                val ownerUid = linkData?.get("ownerUid") as? String
                if (!ownerUid.isNullOrEmpty()) {
                    db.reference.child("users").child(ownerUid).child("devices").child(deviceId)
                        .setValue(true)
                }

                EventLogger.success("ربط الجهاز", "تم ربط الجهاز بنجاح!")
                Toast.makeText(this, "تم ربط الجهاز بنجاح!", Toast.LENGTH_LONG).show()
                etLinkCode.text.clear()
                btnLink.text = "تم الربط"
                btnLink.setBackgroundColor(0xFF22C55E.toInt())

                // Save linked state
                getSharedPreferences("abu_zahra_prefs", MODE_PRIVATE)
                    .edit().putBoolean("registered", true).putString("link_code", code).apply()

                // Auto-start service after linking
                startService()

            } else {
                EventLogger.fail("ربط الجهاز", "رمز الربط غير صحيح")
                Toast.makeText(this, "رمز الربط غير صحيح", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            EventLogger.fail("ربط الجهاز", "خطأ: ${e.message}")
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startService() {
        NotificationHelper.createNotificationChannel(this)
        val intent = Intent(this, FirebaseListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        updateServiceStatus()
        EventLogger.success("بدء الخدمة", "تم بدء خدمة المراقبة")
        Toast.makeText(this, "تم بدء الخدمة", Toast.LENGTH_SHORT).show()
    }

    private fun stopService() {
        val intent = Intent(this, FirebaseListenerService::class.java)
        stopService(intent)
        updateServiceStatus()
        EventLogger.log("إيقاف الخدمة", success = true, "تم إيقاف خدمة المراقبة")
        Toast.makeText(this, "تم إيقاف الخدمة", Toast.LENGTH_SHORT).show()
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (service.service.className == FirebaseListenerService::class.java.name) {
                return true
            }
        }
        return false
    }

    private fun getBatteryLevel(): Int {
        return try {
            val filter = Intent.ACTION_BATTERY_CHANGED
            val batteryStatus = registerReceiver(null, android.content.IntentFilter(filter))
            val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: 0
            val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100) ?: 100
            (level * 100 / scale)
        } catch (e: Exception) { 0 }
    }
}
