package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.abuzahra.control.model.Device
import com.abuzahra.control.service.FirebaseService
import com.abuzahra.control.ui.dashboard.DashboardFragment
import com.abuzahra.control.ui.control.ControlFragment
import com.abuzahra.control.ui.files.FilesFragment
import com.abuzahra.control.ui.settings.SettingsFragment
import com.abuzahra.control.ui.smscalls.SmsCallsFragment
import com.abuzahra.control.ui.social.SocialFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    companion object {
        var selectedDevice: Device? = null
        private const val TAG = "MainActivity"
    }

    private var resultListener: ValueEventListener? = null
    private var currentDevice: Device? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, ">>> onCreate START")

        // STEP 1: Try to inflate the layout
        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "setContentView OK")
        } catch (t: Throwable) {
            Log.e(TAG, "setContentView CRASH: ${t.javaClass.simpleName}: ${t.message}")
            // FALLBACK: Create a super simple layout manually
            try {
                setContentView(createFallbackLayout())
                Log.d(TAG, "Fallback layout OK")
            } catch (t2: Throwable) {
                Log.e(TAG, "Fallback also crashed: ${t2.message}")
                Toast.makeText(this, "خطأ حرج: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }

        // STEP 2: Find views
        try {
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
            val tvTopTitle = findViewById<TextView>(R.id.tvTopTitle)
            val tvDeviceCount = findViewById<TextView>(R.id.tvDeviceCount)

            Log.d(TAG, "All views found OK")

            // STEP 3: Set safe defaults first
            tvTopTitle?.text = "لوحة التحكم"
            tvDeviceCount?.text = "جاهز"

            // STEP 4: Try Firebase init
            try {
                FirebaseService.init(this)
                val email = FirebaseService.userEmail
                if (email != null) {
                    tvTopTitle?.text = email.split("@").firstOrNull() ?: "لوحة التحكم"
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Firebase init error: ${t.javaClass.simpleName}: ${t.message}")
            }

            // STEP 5: Bottom nav
            bottomNav?.setOnItemSelectedListener { item ->
                try {
                    switchFragment(item.itemId)
                } catch (t: Throwable) {
                    Log.e(TAG, "Nav item error: ${t.message}")
                }
                true
            }

            Log.d(TAG, "BottomNav set OK")

            // STEP 6: Load first fragment with delay to ensure layout is complete
            window.decorView.post {
                try {
                    switchFragment(R.id.nav_dashboard)
                    Log.d(TAG, ">>> onCreate COMPLETE")
                } catch (t: Throwable) {
                    Log.e(TAG, "Initial fragment error: ${t.javaClass.simpleName}: ${t.message}")
                }
            }

        } catch (t: Throwable) {
            Log.e(TAG, "View setup CRASH: ${t.javaClass.simpleName}: ${t.message}")
        }
    }

    /**
     * Fallback layout if XML inflation fails - creates everything in code
     */
    private fun createFallbackLayout(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0F0F1A.toInt())
            gravity = Gravity.CENTER

            addView(TextView(this@MainActivity).apply {
                text = "Abu Zahra Control"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 24f
                setTextAlignment(View.TEXT_ALIGNMENT_CENTER)
            })

            addView(TextView(this@MainActivity).apply {
                text = "لوحة التحكم - جاهز"
                setTextColor(0xFFB0B0CC.toInt())
                textSize = 16f
                setTextAlignment(View.TEXT_ALIGNMENT_CENTER)
                setPadding(0, 24, 0, 0)
            })
        }
    }

    private fun switchFragment(itemId: Int) {
        try {
            val frag: Fragment = when (itemId) {
                R.id.nav_dashboard -> DashboardFragment()
                R.id.nav_control -> ControlFragment()
                R.id.nav_sms_calls -> SmsCallsFragment()
                R.id.nav_files -> FilesFragment()
                R.id.nav_social -> SocialFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> DashboardFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, frag)
                .commitAllowingStateLoss()
        } catch (t: Throwable) {
            Log.e(TAG, "switchFragment CRASH: ${t.javaClass.simpleName}: ${t.message}")
        }
    }

    fun selectDevice(device: Device) {
        selectedDevice = device
        currentDevice = device

        try {
            val tvTopTitle = findViewById<TextView>(R.id.tvTopTitle)
            val tvDeviceCount = findViewById<TextView>(R.id.tvDeviceCount)
            val ivDeviceStatus = findViewById<View>(R.id.ivDeviceStatus)

            tvTopTitle?.text = device.name.ifEmpty { "جهاز" }
            tvDeviceCount?.text = device.statusText
            ivDeviceStatus?.setBackgroundResource(
                if (device.isOnline) R.drawable.bg_status_online else R.drawable.bg_status_offline
            )
        } catch (_: Throwable) {}

        // Listen for results
        try {
            resultListener?.let { currentDevice?.let { d -> FirebaseService.removeResultListener(d.id, it) } }
            resultListener = FirebaseService.listenForResult(device.id) { result ->
                if (result.status == "success" || result.status == "error") {
                    runOnUiThread {
                        Toast.makeText(this, result.result.take(200), Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    fun sendCommand(command: String, params: String = "") {
        val dev = selectedDevice
        if (dev == null) {
            Toast.makeText(this, "اختر جهازاً أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "جاري الإرسال: $command", Toast.LENGTH_SHORT).show()
        try {
            FirebaseService.sendCommand(dev.id, command, params) { ok ->
                runOnUiThread {
                    Toast.makeText(this, if (ok) "تم الإرسال" else "فشل", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "sendCommand error: ${t.message}")
            Toast.makeText(this, "خطأ: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            resultListener?.let { currentDevice?.let { d -> FirebaseService.removeResultListener(d.id, it) } }
        } catch (_: Throwable) {}
    }
}
