package com.abuzahra.control

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
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
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    companion object {
        var selectedDevice: Device? = null
        private const val TAG = "MainActivity"
    }

    private var resultListener: ValueEventListener? = null
    private var currentDevice: Device? = null
    private var tvTopTitle: TextView? = null
    private var tvDeviceCount: TextView? = null
    private var ivDeviceStatus: View? = null
    private var navButtons: List<TextView>? = null

    private val navItems = listOf(
        Triple(R.id.nav_dashboard, "\u{1F4CA}", "لوحة التحكم"),
        Triple(R.id.nav_control, "\u23F0", "التحكم"),
        Triple(R.id.nav_sms_calls, "\u2709", "الرسائل"),
        Triple(R.id.nav_files, "\u{1F4C1}", "الملفات"),
        Triple(R.id.nav_social, "\u{1F465}", "التواصل"),
        Triple(R.id.nav_settings, "\u2699", "الإعدادات")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, ">>> onCreate START")

        // Build layout programmatically - ZERO chance of InflateException
        setContentView(buildMainLayout())
        Log.d(TAG, "Layout built programmatically OK")

        try {
            FirebaseService.init(this)
            val email = FirebaseService.userEmail
            if (email != null) {
                tvTopTitle?.text = email.split("@").firstOrNull() ?: "لوحة التحكم"
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Firebase init error: ${t.javaClass.simpleName}: ${t.message}")
        }

        // Load first fragment
        window.decorView.post {
            try {
                switchFragment(R.id.nav_dashboard)
                Log.d(TAG, ">>> onCreate COMPLETE")
            } catch (t: Throwable) {
                Log.e(TAG, "Initial fragment error: ${t.javaClass.simpleName}: ${t.message}")
            }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
        ).toInt()
    }

    private fun buildMainLayout(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#FF0F0F1A"))
        }

        // === Top Bar ===
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#FF1A1A2E"))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), 0, dp(16), 0)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56)
            )
        }

        tvTopTitle = TextView(this).apply {
            text = "لوحة التحكم"
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        topBar.addView(tvTopTitle!!)

        ivDeviceStatus = View(this).apply {
            val dot = GradientDrawable()
            dot.shape = GradientDrawable.OVAL
            dot.setColor(Color.parseColor("#FFFF5252"))
            background = dot
            layoutParams = LinearLayout.LayoutParams(dp(10), dp(10)).apply {
                marginEnd = dp(6)
            }
        }
        topBar.addView(ivDeviceStatus!!)

        tvDeviceCount = TextView(this).apply {
            text = "جاهز"
            setTextColor(Color.parseColor("#FFB0B0CC"))
            textSize = 12f
        }
        topBar.addView(tvDeviceCount)
        root.addView(topBar)

        // === Fragment Container ===
        val fragmentContainer = FrameLayout(this).apply {
            id = R.id.fragmentContainer
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        root.addView(fragmentContainer)

        // === Divider ===
        val divider = View(this).apply {
            setBackgroundColor(Color.parseColor("#FF2A2A4A"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1
            )
        }
        root.addView(divider)

        // === Bottom Navigation (custom - no BottomNavigationView XML inflation) ===
        val navBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#FF1A1A2E"))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(60)
            )
        }

        val btnList = mutableListOf<TextView>()
        for ((navId, icon, label) in navItems) {
            val btn = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)

                val iv = TextView(this@MainActivity).apply {
                    text = icon
                    textSize = 18f
                    gravity = Gravity.CENTER
                    setTextColor(Color.parseColor("#FF6C6C8A"))
                }

                val tv = TextView(this@MainActivity).apply {
                    text = label
                    textSize = 9f
                    gravity = Gravity.CENTER
                    setTextColor(Color.parseColor("#FF6C6C8A"))
                    setPadding(0, dp(2), 0, 0)
                }

                addView(iv)
                addView(tv)

                setOnClickListener {
                    switchFragment(navId)
                    updateNavHighlight(navId)
                }
            }
            navBar.addView(btn)
            btnList.add(btn.findViewById<TextView>(1)) // the label textview
        }
        navButtons = btnList
        root.addView(navBar)

        // Highlight first tab
        updateNavHighlight(R.id.nav_dashboard)

        return root
    }

    private fun updateNavHighlight(selectedId: Int) {
        navButtons?.forEachIndexed { index, tv ->
            val isSelected = navItems[index].first == selectedId
            val color = if (isSelected) Color.parseColor("#FF6C63FF") else Color.parseColor("#FF6C6C8A")
            tv.setTextColor(color)
            // Also update the icon (parent LinearLayout's first child)
            val parent = tv.parent as? LinearLayout ?: return@forEachIndexed
            if (parent.childCount > 0) {
                (parent.getChildAt(0) as? TextView)?.setTextColor(color)
            }
        }
    }

    private fun switchFragment(itemId: Int) {
        try {
            val container = findViewById<View>(R.id.fragmentContainer)
            if (container == null) {
                Log.e(TAG, "switchFragment: fragmentContainer is NULL")
                return
            }

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

            Log.d(TAG, "Fragment switched OK: $itemId")
        } catch (t: Throwable) {
            Log.e(TAG, "switchFragment CRASH: ${t.javaClass.simpleName}: ${t.message}")
        }
    }

    fun selectDevice(device: Device) {
        selectedDevice = device
        currentDevice = device

        try {
            tvTopTitle?.text = device.name.ifEmpty { "جهاز" }
            tvDeviceCount?.text = device.statusText

            val isOnline = device.isOnline
            val color = if (isOnline) Color.parseColor("#FF4CAF50") else Color.parseColor("#FFFF5252")
            val dot = GradientDrawable()
            dot.shape = GradientDrawable.OVAL
            dot.setColor(color)
            ivDeviceStatus?.background = dot
        } catch (_: Throwable) {}

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
