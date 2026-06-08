package com.abuzahra.control.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.MainActivity
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.service.FirebaseManager
import com.abuzahra.control.util.ViewUtils
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.isValidEmail
import com.abuzahra.control.util.showToast

class RegisterActivity : AppCompatActivity() {

    private val TAG = "RegisterActivity"

    private lateinit var emailEditText: android.widget.EditText
    private lateinit var passwordEditText: android.widget.EditText
    private lateinit var confirmEditText: android.widget.EditText
    private lateinit var registerButton: android.widget.Button

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
                setPadding(dp(32), dp(48), dp(32), dp(32))
            }

            // Title
            val title = ViewUtils.createTitleText(
                this,
                "إنشاء حساب جديد",
                sizeSp = 24f,
                color = ColorPalette.PRIMARY
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
                "أنشئ حسابك للبدء في استخدام التطبيق",
                sizeSp = 14f,
                color = ColorPalette.TEXT_SECONDARY
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                    bottomMargin = dp(24)
                }
            }

            // Email EditText
            emailEditText = ViewUtils.createEditText(
                this,
                "البريد الإلكتروني",
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT
            )

            // Password EditText
            passwordEditText = ViewUtils.createEditText(
                this,
                "كلمة المرور",
                InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            )

            // Confirm Password EditText
            confirmEditText = ViewUtils.createEditText(
                this,
                "تأكيد كلمة المرور",
                InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            )

            // Register button
            registerButton = ViewUtils.createPrimaryButton(this, "إنشاء حساب") {
                performRegister()
            }

            // Bottom row: "لديك حساب بالفعل؟" + "تسجيل الدخول"
            val bottomRow = ViewUtils.createHorizontalLayout(this).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(24)
                }
            }

            val hasAccountText = TextView(this).apply {
                text = "لديك حساب بالفعل؟ "
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                textSize = 14f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val loginText = TextView(this).apply {
                text = "تسجيل الدخول"
                setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                textSize = 14f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    try {
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } catch (e: Exception) {
                        Log.e(TAG, "Navigate to login error: ${e.message}")
                    }
                }
            }

            bottomRow.addView(hasAccountText)
            bottomRow.addView(loginText)

            container.addView(title)
            container.addView(subtitle)
            container.addView(emailEditText)
            container.addView(passwordEditText)
            container.addView(confirmEditText)
            container.addView(registerButton)
            container.addView(bottomRow)

            scrollView.addView(container)
            setContentView(scrollView)
        } catch (e: Exception) {
            Log.e(TAG, "buildLayout error: ${e.message}")
        }
    }

    private fun performRegister() {
        try {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirm = confirmEditText.text.toString().trim()

            // Validate empty fields
            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                showToast("يرجى ملء جميع الحقول")
                return
            }

            // Validate email format
            if (!email.isValidEmail()) {
                showToast("البريد الإلكتروني غير صحيح")
                return
            }

            // Validate password length
            if (password.length < 6) {
                showToast("كلمة المرور يجب أن تكون 6 أحرف على الأقل")
                return
            }

            // Validate password match
            if (password != confirm) {
                showToast("كلمتا المرور غير متطابقتين")
                return
            }

            registerButton.isEnabled = false
            registerButton.text = "جاري إنشاء الحساب..."

            FirebaseManager.signUp(email, password) { success, error ->
                try {
                    registerButton.isEnabled = true
                    registerButton.text = "إنشاء حساب"
                    if (success) {
                        showToast("تم إنشاء الحساب بنجاح")
                        openMain()
                    } else {
                        showToast("خطأ: $error")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Register callback error: ${e.message}")
                    registerButton.isEnabled = true
                    registerButton.text = "إنشاء حساب"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "performRegister error: ${e.message}")
            registerButton.isEnabled = true
            registerButton.text = "إنشاء حساب"
            showToast("خطأ في إنشاء الحساب")
        }
    }

    private fun openMain() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "openMain error: ${e.message}")
        }
    }
}
