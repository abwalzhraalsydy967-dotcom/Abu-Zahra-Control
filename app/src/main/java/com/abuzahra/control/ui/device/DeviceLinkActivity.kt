package com.abuzahra.control.ui.device

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.service.FirebaseManager
import com.abuzahra.control.util.ViewUtils
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.showToast
import com.abuzahra.control.util.parseColorSafe
import com.google.firebase.database.ValueEventListener

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate")
            buildLayout()
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error: ${e.message}")
            showToast("خطأ في تهيئة الشاشة: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up listener
        if (generatedCode.isNotEmpty() && linkResultListener != null) {
            FirebaseManager.removeLinkCodeListener(generatedCode, linkResultListener!!)
        }
        expiryRunnable?.let { Handler(Looper.getMainLooper()).removeCallbacks(it) }
    }

    private fun buildLayout() {
        try {
            val scrollView = ViewUtils.createScrollView(this)

            val container = ViewUtils.createVerticalLayout(this).apply {
                setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
                gravity = Gravity.CENTER
                setPadding(dp(32), dp(48), dp(32), dp(32))
            }

            // Title
            container.addView(ViewUtils.createTitleText(
                this,
                "ربط جهاز جديد",
                sizeSp = 24f,
                color = ColorPalette.TEXT_PRIMARY
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })

            // Description
            container.addView(ViewUtils.createSubtitleText(
                this,
                "قم بتوليد كود الربط ثم أدخله في تطبيق الهاتف المستهدف",
                sizeSp = 14f,
                color = ColorPalette.TEXT_SECONDARY
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                    bottomMargin = dp(32)
                }
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

            // Generated code display
            tvGeneratedCode = TextView(this).apply {
                text = "اضغط لتوليد كود"
                textSize = 36f
                setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
                setTypeface(android.graphics.Typeface.MONOSPACE)
                gravity = Gravity.CENTER
                letterSpacing = 0.2f
                setBackgroundColor(ColorPalette.SURFACE.parseColorSafe())
                setPadding(dp(24), dp(24), dp(24), dp(24))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(16)
                }
            }
            container.addView(tvGeneratedCode)

            // Expiry timer
            tvCodeExpiry = TextView(this).apply {
                text = ""
                textSize = 12f
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(4)
                }
            }
            container.addView(tvCodeExpiry)

            // Code status
            tvCodeStatus = TextView(this).apply {
                text = ""
                textSize = 14f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                }
            }
            container.addView(tvCodeStatus)

            // Generate button
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
                ).apply {
                    topMargin = dp(32)
                    bottomMargin = dp(8)
                }
            })

            // ===== SECTION 2: MANUAL LINK =====
            val section2Header = TextView(this).apply {
                text = "2. ربط يدوي (أدخل كود موجود)"
                textSize = 16f
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            container.addView(section2Header)

            // Code EditText
            codeEditText = ViewUtils.createEditText(
                this,
                "كود الربط (6 أرقام)",
                InputType.TYPE_CLASS_NUMBER
            ).apply {
                gravity = Gravity.CENTER
                textSize = 20f
                letterSpacing = 0.15f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(16)
                }
            }
            container.addView(codeEditText)

            // Link button
            linkButton = ViewUtils.createPrimaryButton(this, "ربط جهاز") {
                performLink()
            }
            container.addView(linkButton)

            // Status TextView
            statusTextView = TextView(this).apply {
                text = ""
                textSize = 14f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(16)
                }
            }
            container.addView(statusTextView)

            scrollView.addView(container)
            setContentView(scrollView)
        } catch (e: Exception) {
            Log.e(TAG, "buildLayout error: ${e.message}")
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

                        // Start expiry countdown
                        startExpiryCountdown(10 * 60) // 10 minutes

                        // Listen for the target device to link
                        startListeningForLinkResult(code)

                        showToast("تم توليد الكود بنجاح!")
                    } else {
                        tvCodeStatus.text = error ?: "فشل توليد الكود"
                        tvCodeStatus.setTextColor(ColorPalette.ERROR.parseColorSafe())
                        tvGeneratedCode.text = "خطأ"
                        tvGeneratedCode.setTextColor(ColorPalette.ERROR.parseColorSafe())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "generateNewCode error: ${e.message}")
            btnGenerateCode.isEnabled = true
            btnGenerateCode.text = "توليد كود ربط جديد"
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

                    // Clean up
                    expiryRunnable?.let { Handler(Looper.getMainLooper()).removeCallbacks(it) }
                    tvCodeExpiry.text = ""

                    showToast("تم ربط الجهاز بنجاح!")

                    // Finish after delay
                    Handler(Looper.getMainLooper()).postDelayed({
                        try { finish() } catch (_: Exception) {}
                    }, 2000)
                } else if (message != null && message.contains("صلاحية")) {
                    tvCodeStatus.text = message
                    tvCodeStatus.setTextColor(ColorPalette.WARNING.parseColorSafe())
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
                    // Clean up listener
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
                return
            }

            linkButton.isEnabled = false
            linkButton.text = "جاري الربط..."
            showManualStatus("جاري التحقق من الكود...", isError = false)

            FirebaseManager.linkDevice(code) { success, message ->
                try {
                    linkButton.isEnabled = true
                    linkButton.text = "ربط جهاز"
                    if (success) {
                        showManualStatus(message ?: "تم ربط الجهاز بنجاح!", isError = false)
                        showToast("تم ربط الجهاز بنجاح!")
                        Handler(Looper.getMainLooper()).postDelayed({
                            try { finish() } catch (e: Exception) {
                                Log.e(TAG, "Finish error: ${e.message}")
                            }
                        }, 1500)
                    } else {
                        showManualStatus(message ?: "فشل ربط الجهاز", isError = true)
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
