package com.abuzahra.manager.ui.device

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.manager.constants.ColorPalette
import com.abuzahra.manager.service.EventLogger
import com.abuzahra.manager.service.FirebaseManager
import com.abuzahra.manager.util.ViewUtils
import com.abuzahra.manager.util.dp
import com.abuzahra.manager.util.showToast
import com.abuzahra.manager.util.parseColorSafe
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviceLinkActivity : AppCompatActivity() {

    private val TAG = "DeviceLinkActivity"

    // Generate Code Mode Views
    private lateinit var btnGenerateCode: Button
    private lateinit var tvGeneratedCode: TextView
    private lateinit var tvCodeStatus: TextView
    private lateinit var tvCodeExpiry: TextView
    private var generatedCode: String = ""
    private var linkResultListener: ValueEventListener? = null
    private var expiryRunnable: Runnable? = null

    // Manual Link Mode Views
    private lateinit var codeEditText: EditText
    private lateinit var statusTextView: TextView
    private lateinit var linkButton: Button

    // Event log UI
    private lateinit var eventLogStatus: TextView
    private lateinit var eventLogContainer: LinearLayout
    private lateinit var eventLogScroll: ScrollView
    private val logUpdateListener: (List<com.abuzahra.manager.service.EventLogger.LogEntry>) -> Unit =
        { _ -> runOnUiThread { updateEventLog() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate")
            buildLayout()
            EventLogger.addListener(logUpdateListener)
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error: ${e.message}")
            showToast("خطأ في تهيئة الشاشة: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        updateEventLog()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventLogger.removeListener(logUpdateListener)
        if (generatedCode.isNotEmpty() && linkResultListener != null) {
            FirebaseManager.removeLinkCodeListener(generatedCode, linkResultListener!!)
        }
        expiryRunnable?.let { Handler(Looper.getMainLooper()).removeCallbacks(it) }
    }

    private fun buildLayout() {
        try {
            val rootLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }

            // Main scrollable content
            val scrollView = ViewUtils.createScrollView(this)
            scrollView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )

            val container = ViewUtils.createVerticalLayout(this).apply {
                setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
                gravity = Gravity.CENTER
                setPadding(dp(32), dp(48), dp(32), dp(32))
            }

            // Title
            container.addView(ViewUtils.createTitleText(
                this, "ربط جهاز جديد",
                sizeSp = 24f, color = ColorPalette.TEXT_PRIMARY
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })

            // Description
            container.addView(ViewUtils.createSubtitleText(
                this, "قم بتوليد كود الربط ثم أدخله في تطبيق الهاتف المستهدف",
                sizeSp = 14f, color = ColorPalette.TEXT_SECONDARY
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8); bottomMargin = dp(32) }
            })

            // ===== SECTION 1: GENERATE CODE =====
            val section1Header = TextView(this).apply {
                text = "1. توليد كود الربط"
                textSize = 18f
                setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            container.addView(section1Header)

            tvGeneratedCode = TextView(this).apply {
                text = "اضغط لتوليد كود"
                textSize = 36f
                setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
                setTypeface(android.graphics.Typeface.MONOSPACE)
                gravity = Gravity.CENTER
                letterSpacing = 0.2f
                setBackgroundColor(ColorPalette.BG_INPUT.parseColorSafe())
                setPadding(dp(24), dp(24), dp(24), dp(24))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(16) }
            }
            container.addView(tvGeneratedCode)

            tvCodeExpiry = TextView(this).apply {
                text = ""
                textSize = 12f
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(4) }
            }
            container.addView(tvCodeExpiry)

            tvCodeStatus = TextView(this).apply {
                text = ""
                textSize = 14f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8) }
            }
            container.addView(tvCodeStatus)

            btnGenerateCode = ViewUtils.createPrimaryButton(this, "توليد كود ربط جديد") {
                generateNewCode()
            }
            container.addView(btnGenerateCode)

            // Divider
            container.addView(TextView(this).apply {
                text = "─────────────────────────"
                textSize = 12f
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(32); bottomMargin = dp(8) }
            })

            // ===== SECTION 2: MANUAL LINK =====
            val section2Header = TextView(this).apply {
                text = "2. ربط يدوي (أدخل كود موجود)"
                textSize = 16f
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
                gravity = Gravity.CENTER
            }
            container.addView(section2Header)

            codeEditText = ViewUtils.createEditText(
                this, "كود الربط (6 أرقام)",
                InputType.TYPE_CLASS_NUMBER
            ).apply {
                gravity = Gravity.CENTER
                textSize = 20f
                letterSpacing = 0.15f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(16) }
            }
            container.addView(codeEditText)

            linkButton = ViewUtils.createPrimaryButton(this, "ربط جهاز") {
                performLink()
            }
            container.addView(linkButton)

            statusTextView = TextView(this).apply {
                text = ""
                textSize = 14f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(16) }
            }
            container.addView(statusTextView)

            scrollView.addView(container)
            rootLayout.addView(scrollView)

            // ── Event Log Bar at the bottom ──
            rootLayout.addView(buildEventLogBar())

            setContentView(rootLayout)
        } catch (e: Exception) {
            Log.e(TAG, "buildLayout error: ${e.message}")
        }
    }

    /**
     * Build event log bar for device link screen.
     */
    private fun buildEventLogBar(): View {
        val barRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ColorPalette.BG_SECONDARY.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        barRoot.addView(View(this).apply {
            setBackgroundColor(ColorPalette.DIVIDER.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
        })

        val summaryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(6), dp(12), dp(4))
        }

        eventLogStatus = TextView(this).apply {
            text = "سجل الأحداث"
            textSize = 11f
            setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
        }
        summaryRow.addView(eventLogStatus)

        val clearBtn = TextView(this).apply {
            text = "مسح"
            textSize = 10f
            setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
            setOnClickListener {
                EventLogger.clear()
                updateEventLog()
            }
        }
        summaryRow.addView(clearBtn)
        barRoot.addView(summaryRow)

        eventLogScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(100)
            )
        }

        eventLogContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(2), dp(8), dp(8))
        }
        eventLogScroll.addView(eventLogContainer)
        barRoot.addView(eventLogScroll)

        return barRoot
    }

    /**
     * Update the event log display.
     */
    private fun updateEventLog() {
        try {
            val logs = EventLogger.getRecentLogs(10)
            val lastEntry = logs.lastOrNull()

            if (lastEntry != null) {
                val icon = if (lastEntry.success) "\u2713" else "\u2717"
                eventLogStatus.text = "$icon ${lastEntry.action}"
                eventLogStatus.setTextColor(
                    if (lastEntry.success) ColorPalette.SUCCESS.parseColorSafe()
                    else ColorPalette.ERROR.parseColorSafe()
                )
            } else {
                eventLogStatus.text = "سجل الأحداث"
                eventLogStatus.setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
            }

            eventLogContainer.removeAllViews()
            for (entry in logs.reversed()) {
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(4), dp(1), dp(4), dp(1))
                }

                val dot = View(this).apply {
                    val d = android.graphics.drawable.GradientDrawable()
                    d.shape = android.graphics.drawable.GradientDrawable.OVAL
                    d.setSize(dp(6), dp(6))
                    d.setColor(
                        if (entry.success) ColorPalette.SUCCESS.parseColorSafe()
                        else ColorPalette.ERROR.parseColorSafe()
                    )
                    background = d
                    layoutParams = LinearLayout.LayoutParams(dp(6), dp(6)).apply { marginEnd = dp(6) }
                }
                row.addView(dot)

                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(Date(entry.timestamp))
                row.addView(TextView(this).apply {
                    text = timeStr
                    textSize = 9f
                    setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dp(4) }
                })

                row.addView(TextView(this).apply {
                    text = "${entry.action}: ${entry.message}"
                    textSize = 10f
                    setTextColor(
                        if (entry.success) ColorPalette.SUCCESS.parseColorSafe()
                        else ColorPalette.ERROR.parseColorSafe()
                    )
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                    maxLines = 2
                })

                eventLogContainer.addView(row)
            }

            if (eventLogContainer.childCount > 0) {
                eventLogScroll.post { eventLogScroll.scrollTo(0, 0) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateEventLog error: ${e.message}")
        }
    }

    private fun generateNewCode() {
        try {
            // Clean up previous listener
            if (generatedCode.isNotEmpty() && linkResultListener != null) {
                FirebaseManager.removeLinkCodeListener(generatedCode, linkResultListener!!)
            }
            expiryRunnable?.let { Handler(Looper.getMainLooper()).removeCallbacks(it) }

            btnGenerateCode.isEnabled = false
            btnGenerateCode.text = "جاري التوليد..."
            tvCodeStatus.text = "جاري الاتصال بقاعدة البيانات..."
            tvCodeStatus.setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())

            EventLogger.log("توليد كود الربط", success = true, "جاري الاتصال بقاعدة البيانات...")

            FirebaseManager.generateLinkCode { code, error ->
                Handler(Looper.getMainLooper()).post {
                    btnGenerateCode.isEnabled = true
                    btnGenerateCode.text = "توليد كود ربط جديد"

                    if (code != null) {
                        generatedCode = code
                        tvGeneratedCode.text = code
                        tvGeneratedCode.setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                        tvCodeStatus.text = "أدخل هذا الكود في تطبيق الهاتف المستهدف"
                        tvCodeStatus.setTextColor(ColorPalette.SUCCESS.parseColorSafe())

                        EventLogger.success("توليد كود الربط", "تم توليد الكود: $code")

                        startExpiryCountdown(10 * 60)
                        startListeningForLinkResult(code)

                        showToast("تم توليد الكود بنجاح!")
                    } else {
                        tvCodeStatus.text = error ?: "فشل توليد الكود"
                        tvCodeStatus.setTextColor(ColorPalette.ERROR.parseColorSafe())
                        tvGeneratedCode.text = "خطأ"
                        tvGeneratedCode.setTextColor(ColorPalette.ERROR.parseColorSafe())

                        EventLogger.fail("توليد كود الربط", error ?: "فشل توليد الكود")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "generateNewCode error: ${e.message}")
            btnGenerateCode.isEnabled = true
            btnGenerateCode.text = "توليد كود ربط جديد"
            EventLogger.fail("توليد كود الربط", "خطأ: ${e.message}")
        }
    }

    private fun startListeningForLinkResult(code: String) {
        linkResultListener = FirebaseManager.listenForLinkResult(code) { success, deviceId, message ->
            Handler(Looper.getMainLooper()).post {
                if (success) {
                    tvCodeStatus.text = "تم ربط الجهاز بنجاح! جاري التحديث..."
                    tvCodeStatus.setTextColor(ColorPalette.SUCCESS.parseColorSafe())
                    tvGeneratedCode.text = "متصل!"
                    tvGeneratedCode.setTextColor(ColorPalette.SUCCESS.parseColorSafe())

                    expiryRunnable?.let { Handler(Looper.getMainLooper()).removeCallbacks(it) }
                    tvCodeExpiry.text = ""

                    EventLogger.success("ربط جهاز", "تم ربط الجهاز بنجاح! معرف: ${deviceId?.take(12)}...")
                    showToast("تم ربط الجهاز بنجاح!")

                    Handler(Looper.getMainLooper()).postDelayed({
                        try { finish() } catch (_: Exception) {}
                    }, 2000)
                } else if (message != null && message.contains("صلاحية")) {
                    tvCodeStatus.text = message
                    tvCodeStatus.setTextColor(ColorPalette.WARNING.parseColorSafe())
                    EventLogger.fail("ربط جهاز", message)
                }
            }
        }
    }

    private fun startExpiryCountdown(secondsLeft: Int) {
        val handler = Handler(Looper.getMainLooper())
        var remaining = secondsLeft
        val minutes = remaining / 60
        val seconds = remaining % 60
        tvCodeExpiry.text = "ينتهي الكود بعد: ${minutes}:${String.format("%02d", seconds)}"

        expiryRunnable = object : Runnable {
            override fun run() {
                remaining--
                if (remaining <= 0) {
                    tvCodeExpiry.text = "انتهت صلاحية الكود"
                    tvCodeExpiry.setTextColor(ColorPalette.ERROR.parseColorSafe())
                    tvGeneratedCode.text = "منتهي الصلاحية"
                    tvGeneratedCode.setTextColor(ColorPalette.ERROR.parseColorSafe())
                    tvCodeStatus.text = "قم بتوليد كود جديد"
                    tvCodeStatus.setTextColor(ColorPalette.WARNING.parseColorSafe())
                    EventLogger.fail("كود الربط", "انتهت صلاحية الكود $generatedCode")
                    if (generatedCode.isNotEmpty() && linkResultListener != null) {
                        FirebaseManager.removeLinkCodeListener(generatedCode, linkResultListener!!)
                    }
                    return
                }
                val m = remaining / 60
                val s = remaining % 60
                tvCodeExpiry.text = "ينتهي الكود بعد: ${m}:${String.format("%02d", s)}"
                if (remaining <= 60) {
                    tvCodeExpiry.setTextColor(ColorPalette.WARNING.parseColorSafe())
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(expiryRunnable!!, 1000)
    }

    private fun performLink() {
        try {
            val code = codeEditText.text.toString().trim()

            if (code.isEmpty()) {
                showManualStatus("يرجى إدخال كود الربط", isError = true)
                EventLogger.fail("ربط يدوي", "لم يتم إدخال كود")
                return
            }

            linkButton.isEnabled = false
            linkButton.text = "جاري الربط..."
            showManualStatus("جاري التحقق من الكود...", isError = false)
            EventLogger.log("ربط يدوي", success = true, "جاري التحقق من الكود $code...")

            FirebaseManager.linkDevice(code) { success, message ->
                try {
                    linkButton.isEnabled = true
                    linkButton.text = "ربط جهاز"
                    if (success) {
                        showManualStatus(message ?: "تم ربط الجهاز بنجاح!", isError = false)
                        EventLogger.success("ربط يدوي", message ?: "تم ربط الجهاز بنجاح!")
                        showToast("تم ربط الجهاز بنجاح!")
                        Handler(Looper.getMainLooper()).postDelayed({
                            try { finish() } catch (e: Exception) {
                                Log.e(TAG, "Finish error: ${e.message}")
                            }
                        }, 1500)
                    } else {
                        showManualStatus(message ?: "فشل ربط الجهاز", isError = true)
                        EventLogger.fail("ربط يدوي", message ?: "فشل ربط الجهاز")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Link callback error: ${e.message}")
                    linkButton.isEnabled = true
                    linkButton.text = "ربط جهاز"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "performLink error: ${e.message}")
            linkButton.isEnabled = true
            linkButton.text = "ربط جهاز"
            showManualStatus("خطأ: ${e.message}", isError = true)
            EventLogger.fail("ربط يدوي", "خطأ: ${e.message}")
        }
    }

    private fun showManualStatus(message: String, isError: Boolean) {
        try {
            statusTextView.text = message
            if (isError) {
                statusTextView.setTextColor(ColorPalette.ERROR.parseColorSafe())
            } else {
                statusTextView.setTextColor(ColorPalette.SUCCESS.parseColorSafe())
            }
        } catch (e: Exception) {
            Log.e(TAG, "showManualStatus error: ${e.message}")
        }
    }
}
