package com.abuzahra.control.ui.main

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.abuzahra.control.R
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.constants.NavItems
import com.abuzahra.control.data.model.Device
import com.abuzahra.control.data.repository.DeviceRepository
import com.abuzahra.control.service.FirebaseManager
import com.abuzahra.control.ui.auth.LoginActivity
import com.abuzahra.control.ui.control.ControlFragment
import com.abuzahra.control.ui.dashboard.DashboardFragment
import com.abuzahra.control.ui.files.FilesFragment
import com.abuzahra.control.ui.settings.SettingsFragment
import com.abuzahra.control.ui.smscalls.SmsCallsFragment
import com.abuzahra.control.ui.social.SocialFragment
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.parseColorSafe
import com.abuzahra.control.util.showToast
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        var selectedDevice: Device? = null
    }

    private lateinit var tvTopTitle: TextView
    private lateinit var ivDeviceStatus: View
    private lateinit var tvDeviceCount: TextView
    private lateinit var fragmentContainer: FrameLayout
    private val navIconLabels = mutableMapOf<Int, TextView>()
    private var currentNavId: Int = R.id.nav_dashboard
    private var resultListener: ValueEventListener? = null
    private val deviceRepository = DeviceRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate")
            buildLayout()
            setupTopBar()
            setupBottomNav()
            switchFragment(R.id.nav_dashboard)
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error: ${e.message}")
            showToast("خطأ في تهيئة الشاشة: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        refreshDeviceStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        removeResultListener()
    }

    private fun buildLayout() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // ── Top Bar ──
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(ColorPalette.BG_SECONDARY.parseColorSafe())
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(4), dp(16), dp(4))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            )
        }

        tvTopTitle = TextView(this).apply {
            text = "لوحة التحكم"
            setTextColor(ColorPalette.WHITE.parseColorSafe())
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        ivDeviceStatus = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(10), dp(10)).apply {
                marginEnd = dp(6)
            }
        }

        tvDeviceCount = TextView(this).apply {
            text = "جاهز"
            setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        topBar.addView(tvTopTitle)
        topBar.addView(ivDeviceStatus)
        topBar.addView(tvDeviceCount)

        // ── Fragment Container ──
        fragmentContainer = FrameLayout(this).apply {
            id = R.id.fragmentContainer
            layoutParams = LinearLayout.LayoutParams(
                0,
                0,
                1f
            )
        }

        // ── Divider ──
        val divider = View(this).apply {
            setBackgroundColor(ColorPalette.DIVIDER.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
        }

        // ── Bottom Nav Bar ──
        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(ColorPalette.BG_SECONDARY.parseColorSafe())
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(60)
            )
        }

        for (item in NavItems.items) {
            val navItemView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                setOnClickListener {
                    try {
                        switchFragment(item.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Nav click error: ${e.message}")
                    }
                }
            }

            val iconView = TextView(this).apply {
                text = item.icon
                textSize = 18f
                setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val labelView = TextView(this).apply {
                text = item.label
                textSize = 9f
                setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(2)
                }
            }

            navItemView.addView(iconView)
            navItemView.addView(labelView)
            bottomNav.addView(navItemView)

            // Store the label (or icon) for highlight updates — store both icon + label
            // Use icon as the key indicator since label is also needed
            navIconLabels[item.id] = iconView
            // Store label separately with a different key pattern
            navIconLabels[item.id + 10000] = labelView
        }

        root.addView(topBar)
        root.addView(fragmentContainer)
        root.addView(divider)
        root.addView(bottomNav)

        setContentView(root)
    }

    private fun setupTopBar() {
        try {
            val email = FirebaseManager.userEmail
            if (!email.isNullOrEmpty()) {
                tvTopTitle.text = email
            } else {
                tvTopTitle.text = "لوحة التحكم"
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupTopBar error: ${e.message}")
            tvTopTitle.text = "لوحة التحكم"
        }
    }

    private fun setupBottomNav() {
        updateNavHighlight(R.id.nav_dashboard)
    }

    fun updateNavHighlight(selectedId: Int) {
        currentNavId = selectedId
        for (item in NavItems.items) {
            val isSelected = item.id == selectedId
            val color = if (isSelected) ColorPalette.PRIMARY.parseColorSafe()
                        else ColorPalette.TEXT_HINT.parseColorSafe()
            navIconLabels[item.id]?.setTextColor(color)
            navIconLabels[item.id + 10000]?.setTextColor(color)
        }
    }

    fun switchFragment(itemId: Int) {
        try {
            if (fragmentContainer == null) return
            val fragment: Fragment = when (itemId) {
                R.id.nav_dashboard -> DashboardFragment()
                R.id.nav_control -> ControlFragment()
                R.id.nav_sms_calls -> SmsCallsFragment()
                R.id.nav_files -> FilesFragment()
                R.id.nav_social -> SocialFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> DashboardFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commitAllowingStateLoss()
            updateNavHighlight(itemId)
        } catch (e: Exception) {
            Log.e(TAG, "switchFragment error: ${e.message}")
        }
    }

    fun selectDevice(device: Device) {
        try {
            selectedDevice = device
            tvTopTitle.text = device.name.ifEmpty { FirebaseManager.userEmail ?: "لوحة التحكم" }
            tvDeviceCount.text = device.statusText

            // Update status dot
            val dotDrawable = android.graphics.drawable.GradientDrawable()
            dotDrawable.shape = android.graphics.drawable.GradientDrawable.OVAL
            dotDrawable.setColor(
                if (device.isOnline) ColorPalette.SUCCESS.parseColorSafe()
                else ColorPalette.ERROR.parseColorSafe()
            )
            ivDeviceStatus.background = dotDrawable

            // Start listening for command results
            removeResultListener()
            resultListener = deviceRepository.listenForResult(device.id) { result ->
                try {
                    showToast("نتيجة: ${result.result}")
                } catch (e: Exception) {
                    Log.e(TAG, "Result callback error: ${e.message}")
                }
            }
            Log.d(TAG, "Selected device: ${device.name} (${device.id})")
        } catch (e: Exception) {
            Log.e(TAG, "selectDevice error: ${e.message}")
        }
    }

    fun sendCommand(command: String, params: String = "") {
        try {
            val device = selectedDevice
            if (device == null) {
                showToast("يرجى اختيار جهاز أولاً")
                return
            }
            deviceRepository.sendCommand(device.id, command, params) { success ->
                if (success) {
                    showToast("تم إرسال الأمر")
                } else {
                    showToast("فشل إرسال الأمر")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendCommand error: ${e.message}")
            showToast("خطأ في إرسال الأمر")
        }
    }

    private fun refreshDeviceStatus() {
        try {
            val device = selectedDevice
            if (device != null) {
                tvDeviceCount.text = device.statusText
                val dotDrawable = android.graphics.drawable.GradientDrawable()
                dotDrawable.shape = android.graphics.drawable.GradientDrawable.OVAL
                dotDrawable.setColor(
                    if (device.isOnline) ColorPalette.SUCCESS.parseColorSafe()
                    else ColorPalette.ERROR.parseColorSafe()
                )
                ivDeviceStatus.background = dotDrawable
            }
        } catch (e: Exception) {
            Log.e(TAG, "refreshDeviceStatus error: ${e.message}")
        }
    }

    private fun removeResultListener() {
        try {
            val listener = resultListener ?: return
            val device = selectedDevice ?: return
            deviceRepository.removeResultListener(device.id, listener)
            resultListener = null
        } catch (e: Exception) {
            Log.e(TAG, "removeResultListener error: ${e.message}")
        }
    }
}
