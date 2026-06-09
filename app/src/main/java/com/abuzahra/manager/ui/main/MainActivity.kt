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
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.abuzahra.manager.R
import com.abuzahra.manager.constants.ColorPalette
import com.abuzahra.manager.constants.NavItems
import com.abuzahra.manager.data.model.Device
import com.abuzahra.manager.data.repository.DeviceRepository
import com.abuzahra.manager.service.EventLogger
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Event log UI
    private lateinit var eventLogBar: LinearLayout
    private lateinit var eventLogScroll: ScrollView
    private lateinit var eventLogContainer: LinearLayout
    private lateinit var eventLogStatus: TextView
    private lateinit var eventLogDetail: TextView
    private var eventLogExpanded = false
    private val logUpdateListener: (List<com.abuzahra.manager.service.EventLogger.LogEntry>) -> Unit =
        { _ -> runOnUiThread { updateEventLog() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate - building layout")
            buildLayout()
            setupTopBar()
            switchFragment(R.id.nav_dashboard)
            EventLogger.addListener(logUpdateListener)
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error: ${e.message}", e)
            showToast("خطأ في تهيئة الشاشة: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            refreshDeviceStatus()
            updateEventLog()
        } catch (e: Exception) {
            Log.e(TAG, "onResume error: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            EventLogger.removeListener(logUpdateListener)
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
        fragmentContainer = FrameLayout(this).apply {
            id = R.id.fragmentContainer
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        // ── Bottom Navigation Bar ──
        val bottomNav = buildBottomNav()

        // ── Event Log Bar (between content and bottom nav) ──
        eventLogBar = buildEventLogBar()

        // Assemble
        root.addView(topBar)
        root.addView(fragmentContainer)
        root.addView(eventLogBar)
        root.addView(bottomNav)

        setContentView(root)
    }

    /**
     * Build the event log bar that shows real-time action results.
     * Tap to expand/collapse and see full log history.
     */
    private fun buildEventLogBar(): LinearLayout {
        val barRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ColorPalette.BG_SECONDARY.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Divider line above log bar
        val divider = View(this).apply {
            setBackgroundColor(ColorPalette.DIVIDER.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
        }
        barRoot.addView(divider)

        // Summary row (always visible)
        val summaryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(6), dp(12), dp(6))
            isClickable = true
            setOnClickListener { toggleEventLog() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Status indicator dot
        val statusDot = View(this).apply {
            val dot = GradientDrawable()
            dot.shape = GradientDrawable.OVAL
            dot.setColor(ColorPalette.TEXT_HINT.parseColorSafe())
            background = dot
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                marginEnd = dp(6)
            }
        }
        summaryRow.addView(statusDot)

        eventLogStatus = TextView(this).apply {
            text = "سجل الأحداث"
            textSize = 11f
            setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
        }
        summaryRow.addView(eventLogStatus)

        eventLogDetail = TextView(this).apply {
            text = "اضغط للعرض"
            textSize = 10f
            setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginStart = dp(8) }
        }
        summaryRow.addView(eventLogDetail)
        barRoot.addView(summaryRow)

        // Expandable scrollable log container
        eventLogScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0
            )
            isVerticalScrollBarEnabled = true
            visibility = View.GONE
        }

        eventLogContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(8))
        }
        eventLogScroll.addView(eventLogContainer)
        barRoot.addView(eventLogScroll)

        return barRoot
    }

    /**
     * Toggle the event log between collapsed and expanded.
     */
    private fun toggleEventLog() {
        eventLogExpanded = !eventLogExpanded
        if (eventLogExpanded) {
            eventLogScroll.visibility = View.VISIBLE
            eventLogScroll.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(120)
            )
            eventLogDetail.text = "اضغط للإخفاء"
        } else {
            eventLogScroll.visibility = View.GONE
            eventLogScroll.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0
            )
            eventLogDetail.text = "اضغط للعرض"
        }
    }

    /**
     * Update the event log display with the latest entries.
     */
    private fun updateEventLog() {
        try {
            if (!::eventLogStatus.isInitialized) return

            val logs = EventLogger.getLogs()
            val hasFailures = logs.any { !it.success }
            val lastEntry = logs.lastOrNull()

            // Update summary row
            if (lastEntry != null) {
                val icon = if (lastEntry.success) "\u2713" else "\u2717"
                eventLogStatus.text = "$icon ${lastEntry.action}: ${lastEntry.message}"
                eventLogStatus.setTextColor(
                    if (lastEntry.success) ColorPalette.SUCCESS.parseColorSafe()
                    else ColorPalette.ERROR.parseColorSafe()
                )
            } else {
                eventLogStatus.text = "سجل الأحداث"
                eventLogStatus.setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
            }

            // Update status dot color
            val parent = eventLogBar.getChildAt(1) as? LinearLayout
            if (parent != null && parent.childCount > 0) {
                val dot = parent.getChildAt(0)
                val dotDrawable = GradientDrawable()
                dotDrawable.shape = GradientDrawable.OVAL
                dotDrawable.setColor(
                    when {
                        hasFailures -> ColorPalette.ERROR.parseColorSafe()
                        logs.isNotEmpty() -> ColorPalette.SUCCESS.parseColorSafe()
                        else -> ColorPalette.TEXT_HINT.parseColorSafe()
                    }
                )
                dot.background = dotDrawable
            }

            // Update expandable log content
            eventLogContainer.removeAllViews()
            val displayLogs = logs.takeLast(20).reversed()
            for (entry in displayLogs) {
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(4), dp(1), dp(4), dp(1))
                }

                // Colored dot
                val dot = View(this).apply {
                    val d = GradientDrawable()
                    d.shape = GradientDrawable.OVAL
                    d.setSize(dp(6), dp(6))
                    d.setColor(
                        if (entry.success) ColorPalette.SUCCESS.parseColorSafe()
                        else ColorPalette.ERROR.parseColorSafe()
                    )
                    background = d
                    layoutParams = LinearLayout.LayoutParams(dp(6), dp(6)).apply {
                        marginEnd = dp(6)
                    }
                }
                row.addView(dot)

                // Timestamp
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(Date(entry.timestamp))
                val timeView = TextView(this).apply {
                    text = timeStr
                    textSize = 9f
                    setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dp(4) }
                }
                row.addView(timeView)

                // Action name
                val actionView = TextView(this).apply {
                    text = entry.action
                    textSize = 10f
                    setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
                    typeface = Typeface.DEFAULT_BOLD
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dp(4) }
                }
                row.addView(actionView)

                // Message
                val msgView = TextView(this).apply {
                    text = entry.message
                    textSize = 10f
                    setTextColor(
                        if (entry.success) ColorPalette.SUCCESS.parseColorSafe()
                        else ColorPalette.ERROR.parseColorSafe()
                    )
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                    maxLines = 2
                }
                row.addView(msgView)

                eventLogContainer.addView(row)
            }

            // Auto-scroll to top (newest)
            if (eventLogContainer.childCount > 0) {
                eventLogScroll.post { eventLogScroll.scrollTo(0, 0) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateEventLog error: ${e.message}")
        }
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
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
        }
        navContainer.addView(divider)

        // Nav buttons row
        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60)
            )
        }

        for (item in NavItems.items) {
            val navItemView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
                setOnClickListener {
                    try { switchFragment(item.id) } catch (e: Exception) {
                        Log.e(TAG, "Nav click error: ${e.message}")
                    }
                }
            }

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
                ).apply { topMargin = dp(2) }
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

            val dotDrawable = GradientDrawable()
            dotDrawable.shape = GradientDrawable.OVAL
            dotDrawable.setColor(
                if (device.isOnline) ColorPalette.SUCCESS.parseColorSafe()
                else ColorPalette.ERROR.parseColorSafe()
            )
            ivDeviceStatus.background = dotDrawable

            EventLogger.success("اختيار جهاز", "${device.name} - ${device.statusText}")

            removeResultListener()
            resultListener = deviceRepository.listenForResult(device.id) { result ->
                try {
                    val msg = result.result
                    if (msg.isNotEmpty()) {
                        EventLogger.success("نتيجة أمر", msg)
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
                EventLogger.fail("إرسال أمر", "لم يتم اختيار جهاز")
                showToast("يرجى اختيار جهاز أولاً")
                return
            }
            deviceRepository.sendCommand(device.id, command, params) { success ->
                if (success) {
                    EventLogger.success("إرسال أمر", "تم إرسال $command بنجاح")
                    showToast("تم إرسال الأمر")
                } else {
                    EventLogger.fail("إرسال أمر", "فشل إرسال الأمر $command")
                    showToast("فشل إرسال الأمر")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendCommand error: ${e.message}")
            EventLogger.fail("إرسال أمر", "خطأ: ${e.message}")
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
