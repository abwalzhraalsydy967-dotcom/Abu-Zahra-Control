package com.abuzahra.manager.ui.main

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.abuzahra.manager.R
import com.abuzahra.manager.constants.ColorPalette
import com.abuzahra.manager.constants.NavItems
import com.abuzahra.manager.data.model.Device
import com.abuzahra.manager.data.repository.DeviceRepository
import com.abuzahra.manager.service.FirebaseManager
import com.abuzahra.manager.ui.auth.LoginActivity
import com.abuzahra.manager.ui.control.ControlFragment
import com.abuzahra.manager.ui.dashboard.DashboardFragment
import com.abuzahra.manager.ui.files.FilesFragment
import com.abuzahra.manager.ui.settings.SettingsFragment
import com.abuzahra.manager.ui.smscalls.SmsCallsFragment
import com.abuzahra.manager.ui.social.SocialFragment
import com.abuzahra.manager.util.dp
import com.abuzahra.manager.util.parseColorSafe
import com.abuzahra.manager.util.showToast
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        var selectedDevice: Device? = null
    }

    private lateinit var tvTopTitle: TextView
    private lateinit var tvTopEmail: TextView
    private lateinit var ivDeviceStatus: View
    private lateinit var tvDeviceCount: TextView
    private lateinit var fragmentContainer: FrameLayout
    private val navIconViews = mutableMapOf<Int, TextView>()
    private val navLabelViews = mutableMapOf<Int, TextView>()
    private val navContainerViews = mutableMapOf<Int, View>()
    private var currentNavId: Int = R.id.nav_dashboard
    private var resultListener: ValueEventListener? = null
    private val deviceRepository = DeviceRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate - building layout")
            buildLayout()
            setupTopBar()
            switchFragment(R.id.nav_dashboard)
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error: ${e.message}", e)
            showToast("خطأ في تهيئة الشاشة: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            refreshDeviceStatus()
        } catch (e: Exception) {
            Log.e(TAG, "onResume error: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            removeResultListener()
        } catch (_: Exception) {}
    }

    private fun buildLayout() {
        // Root vertical layout
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
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Top bar left: Title
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

        // Top bar right: Device status indicator
        ivDeviceStatus = View(this).apply {
            val dot = GradientDrawable()
            dot.shape = GradientDrawable.OVAL
            dot.setColor(ColorPalette.TEXT_HINT.parseColorSafe())
            background = dot
            layoutParams = LinearLayout.LayoutParams(dp(10), dp(10)).apply {
                marginEnd = dp(6)
            }
        }

        tvDeviceCount = TextView(this).apply {
            text = "جاهز"
            setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
            textSize = 12f
        }

        // Email text below title
        tvTopEmail = TextView(this).apply {
            text = ""
            setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
            textSize = 10f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val topContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        topContent.addView(tvTopTitle)
        topContent.addView(tvTopEmail)

        val topRight = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        topRight.addView(ivDeviceStatus)
        topRight.addView(tvDeviceCount)

        topBar.addView(topContent)
        topBar.addView(topRight)

        // ── Fragment Container ──
        // CRITICAL FIX: Width must be MATCH_PARENT, height=0 with weight=1
        fragmentContainer = FrameLayout(this).apply {
            id = R.id.fragmentContainer
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,  // Width = MATCH_PARENT (was 0!)
                0,                                         // Height = 0
                1f                                         // Weight = 1 (fills remaining space)
            )
        }

        // ── Bottom Navigation Bar ──
        val bottomNav = buildBottomNav()

        // Assemble
        root.addView(topBar)
        root.addView(fragmentContainer)
        root.addView(bottomNav)

        setContentView(root)
    }

    private fun buildBottomNav(): View {
        val navContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ColorPalette.BG_SECONDARY.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Divider line above nav
        val divider = View(this).apply {
            setBackgroundColor(ColorPalette.DIVIDER.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
        }
        navContainer.addView(divider)

        // Nav buttons row
        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
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

            // Icon text (single letter with circle background)
            val iconBg = GradientDrawable()
            iconBg.shape = GradientDrawable.OVAL
            iconBg.setColor(ColorPalette.TRANSPARENT.parseColorSafe())

            val iconView = TextView(this).apply {
                text = item.icon
                textSize = 20f
                setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val labelView = TextView(this).apply {
                text = item.label
                textSize = 10f
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
            navRow.addView(navItemView)

            navIconViews[item.id] = iconView
            navLabelViews[item.id] = labelView
            navContainerViews[item.id] = navItemView
        }

        navContainer.addView(navRow)
        return navContainer
    }

    private fun setupTopBar() {
        try {
            val email = FirebaseManager.userEmail
            if (!email.isNullOrEmpty()) {
                tvTopEmail.text = email
            }
            tvTopTitle.text = "لوحة التحكم"
        } catch (e: Exception) {
            Log.e(TAG, "setupTopBar error: ${e.message}")
        }
    }

    private fun updateNavHighlight(selectedId: Int) {
        currentNavId = selectedId
        for (item in NavItems.items) {
            val isSelected = item.id == selectedId
            val color = if (isSelected) ColorPalette.PRIMARY.parseColorSafe()
                        else ColorPalette.TEXT_HINT.parseColorSafe()

            navIconViews[item.id]?.setTextColor(color)
            navLabelViews[item.id]?.setTextColor(color)

            // Highlight background for selected item
            val bgView = navContainerViews[item.id]
            if (bgView != null) {
                if (isSelected) {
                    val selBg = GradientDrawable()
                    selBg.shape = GradientDrawable.RECTANGLE
                    selBg.setColor(ColorPalette.TRANSPARENT.parseColorSafe())
                    selBg.cornerRadius = dp(8).toFloat()
                    bgView.background = selBg
                } else {
                    bgView.background = null
                }
            }
        }
    }

    fun switchFragment(itemId: Int) {
        try {
            if (!::fragmentContainer.isInitialized) return
            Log.d(TAG, "switchFragment: $itemId")
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
            Log.e(TAG, "switchFragment error: ${e.message}", e)
        }
    }

    fun selectDevice(device: Device) {
        try {
            selectedDevice = device
            tvTopTitle.text = device.name.ifEmpty { "جهاز" }
            tvDeviceCount.text = device.statusText
            tvTopEmail.text = "${device.model} ${device.brand}"

            // Update status dot
            val dotDrawable = GradientDrawable()
            dotDrawable.shape = GradientDrawable.OVAL
            dotDrawable.setColor(
                if (device.isOnline) ColorPalette.SUCCESS.parseColorSafe()
                else ColorPalette.ERROR.parseColorSafe()
            )
            ivDeviceStatus.background = dotDrawable

            // Start listening for command results
            removeResultListener()
            resultListener = deviceRepository.listenForResult(device.id) { result ->
                try {
                    val msg = result.result
                    if (msg.isNotEmpty()) {
                        showToast("نتيجة: $msg")
                    }
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
                val dotDrawable = GradientDrawable()
                dotDrawable.shape = GradientDrawable.OVAL
                dotDrawable.setColor(
                    if (device.isOnline) ColorPalette.SUCCESS.parseColorSafe()
                    else ColorPalette.ERROR.parseColorSafe()
                )
                ivDeviceStatus.background = dotDrawable
            } else {
                ivDeviceStatus.background = null
                val dotDrawable = GradientDrawable()
                dotDrawable.shape = GradientDrawable.OVAL
                dotDrawable.setColor(ColorPalette.TEXT_HINT.parseColorSafe())
                ivDeviceStatus.background = dotDrawable
                tvDeviceCount.text = "لا يوجد جهاز"
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
        } catch (_: Exception) {}
    }
}
