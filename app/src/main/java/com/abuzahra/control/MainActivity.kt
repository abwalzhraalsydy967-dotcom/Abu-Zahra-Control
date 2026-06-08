package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
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
        Log.d(TAG, "onCreate START")

        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            Log.e(TAG, "setContentView CRASH: ${e.message}")
            Toast.makeText(this, "خطأ في تحميل الواجهة", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d(TAG, "setContentView OK")

        // Find views safely
        val fragmentContainer = findViewById<View>(R.id.fragmentContainer)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val tvTopTitle = findViewById<TextView>(R.id.tvTopTitle)
        val tvDeviceCount = findViewById<TextView>(R.id.tvDeviceCount)

        Log.d(TAG, "All views found OK")

        // Init FirebaseService
        try {
            FirebaseService.init(this)
            val email = FirebaseService.userEmail
            tvTopTitle?.text = if (email != null) {
                email.split("@").firstOrNull() ?: "لوحة التحكم"
            } else {
                "لوحة التحكم"
            }
            tvDeviceCount?.text = "جاهز"
        } catch (e: Exception) {
            Log.e(TAG, "Firebase init: ${e.message}")
            tvTopTitle?.text = "لوحة التحكم"
            tvDeviceCount?.text = "جاهز"
        }

        Log.d(TAG, "Welcome text set")

        // Bottom nav
        bottomNav?.setOnItemSelectedListener { item ->
            switchFragment(item.itemId)
            true
        }
        Log.d(TAG, "BottomNav set OK")

        // Load first fragment
        switchFragment(R.id.nav_dashboard)
        Log.d(TAG, "onCreate COMPLETE")
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
        } catch (e: Exception) {
            Log.e(TAG, "switchFragment error: ${e.message}")
        }
    }

    fun selectDevice(device: Device) {
        selectedDevice = device
        currentDevice = device

        val tvTopTitle = findViewById<TextView>(R.id.tvTopTitle)
        val tvDeviceCount = findViewById<TextView>(R.id.tvDeviceCount)
        val ivDeviceStatus = findViewById<View>(R.id.ivDeviceStatus)

        tvTopTitle?.text = device.name.ifEmpty { "جهاز" }
        tvDeviceCount?.text = device.statusText
        ivDeviceStatus?.setBackgroundResource(
            if (device.isOnline) R.drawable.bg_status_online else R.drawable.bg_status_offline
        )

        // Listen for results
        resultListener?.let { currentDevice?.let { d -> FirebaseService.removeResultListener(d.id, it) } }
        resultListener = FirebaseService.listenForResult(device.id) { result ->
            if (result.status == "success" || result.status == "error") {
                runOnUiThread {
                    Toast.makeText(this, result.result.take(200), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun sendCommand(command: String, params: String = "") {
        val dev = selectedDevice
        if (dev == null) {
            Toast.makeText(this, "اختر جهازاً أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "جاري الإرسال: $command", Toast.LENGTH_SHORT).show()
        FirebaseService.sendCommand(dev.id, command, params) { ok ->
            runOnUiThread {
                Toast.makeText(this, if (ok) "تم الإرسال ✅" else "فشل ❌", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            resultListener?.let { currentDevice?.let { d -> FirebaseService.removeResultListener(d.id, it) } }
        } catch (_: Exception) {}
    }
}
