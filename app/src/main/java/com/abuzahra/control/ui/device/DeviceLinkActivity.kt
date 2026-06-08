package com.abuzahra.control.ui.device

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.Gravity
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

class DeviceLinkActivity : AppCompatActivity() {

    private val TAG = "DeviceLinkActivity"

    private lateinit var codeEditText: EditText
    private lateinit var statusTextView: TextView
    private lateinit var linkButton: android.widget.Button

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

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private fun buildLayout() {
        try {
            val scrollView = ViewUtils.createScrollView(this)

            val container = ViewUtils.createVerticalLayout(this).apply {
                setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
                gravity = Gravity.CENTER
                setPadding(dp(32), dp(64), dp(32), dp(32))
            }

            // Title
            val title = ViewUtils.createTitleText(
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
            }

            // Subtitle
            val subtitle = ViewUtils.createSubtitleText(
                this,
                "أدخل كود الربط الذي يظهر على الجهاز المستهدف",
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
            }

            // Code EditText - centered, textCapCharacters, textSize 20
            codeEditText = ViewUtils.createEditText(
                this,
                "كود الربط",
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
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

            // Link button
            linkButton = ViewUtils.createPrimaryButton(this, "ربط جهاز") {
                performLink()
            }

            // Status TextView - centered
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

            container.addView(title)
            container.addView(subtitle)
            container.addView(codeEditText)
            container.addView(linkButton)
            container.addView(statusTextView)

            scrollView.addView(container)
            setContentView(scrollView)
        } catch (e: Exception) {
            Log.e(TAG, "buildLayout error: ${e.message}")
        }
    }

    private fun performLink() {
        try {
            val code = codeEditText.text.toString().trim().uppercase()

            if (code.isEmpty()) {
                showStatus("يرجى إدخال كود الربط", isError = true)
                return
            }

            linkButton.isEnabled = false
            linkButton.text = "جاري الربط..."
            showStatus("جاري التحقق من الكود...", isError = false)

            FirebaseManager.linkDevice(code) { success, message ->
                try {
                    linkButton.isEnabled = true
                    linkButton.text = "ربط جهاز"
                    if (success) {
                        showStatus(message ?: "تم ربط الجهاز بنجاح!", isError = false)
                        showToast("تم ربط الجهاز بنجاح!")
                        // Finish after 1.5 second delay
                        Handler(Looper.getMainLooper()).postDelayed({
                            try {
                                finish()
                            } catch (e: Exception) {
                                Log.e(TAG, "Finish error: ${e.message}")
                            }
                        }, 1500)
                    } else {
                        showStatus(message ?: "فشل ربط الجهاز", isError = true)
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
            showStatus("خطأ: ${e.message}", isError = true)
        }
    }

    private fun showStatus(message: String, isError: Boolean) {
        try {
            statusTextView.text = message
            if (isError) {
                statusTextView.setTextColor(ColorPalette.ERROR.parseColorSafe())
            } else {
                statusTextView.setTextColor(ColorPalette.SUCCESS.parseColorSafe())
            }
        } catch (e: Exception) {
            Log.e(TAG, "showStatus error: ${e.message}")
        }
    }
}
