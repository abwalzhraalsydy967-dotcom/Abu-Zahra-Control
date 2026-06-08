package com.abuzahra.control.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.ui.main.MainActivity
import com.abuzahra.control.R
import com.abuzahra.control.constants.AppConstants
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.service.FirebaseManager
import com.abuzahra.control.util.ViewUtils
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    private lateinit var emailEditText: android.widget.EditText
    private lateinit var passwordEditText: android.widget.EditText
    private lateinit var loginButton: android.widget.Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate")
            setupGoogleSignIn()
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

    private fun setupGoogleSignIn() {
        try {
            val webClientId = try {
                getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get web client id from resources, using fallback")
                "787676787951-20uf0a81hb0n5b95t9htb7cd073lu2bm.apps.googleusercontent.com"
            }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            googleSignInLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                try {
                    handleGoogleSignInResult(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Google sign-in result error: ${e.message}")
                    showToast("فشل تسجيل الدخول بحساب Google")
                    loginButton.isEnabled = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupGoogleSignIn error: ${e.message}")
        }
    }

    private fun buildLayout() {
        try {
            val scrollView = ViewUtils.createScrollView(this)

            val container = ViewUtils.createVerticalLayout(this).apply {
                setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
                gravity = Gravity.CENTER
                setPadding(dp(32), dp(48), dp(32), dp(32))
            }

            // App name
            val appName = ViewUtils.createTitleText(
                this,
                "Abu Zahra Control",
                sizeSp = 26f,
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
                "سجل دخولك للمتابعة",
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

            // Forgot password
            val forgotPassword = TextView(this).apply {
                text = "نسيت كلمة المرور؟"
                setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                textSize = 13f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                    bottomMargin = dp(16)
                }
                setOnClickListener {
                    try {
                        val email = emailEditText.text.toString().trim()
                        if (email.isEmpty()) {
                            showToast("أدخل البريد الإلكتروني أولاً")
                            return@setOnClickListener
                        }
                        FirebaseManager.resetPassword(email) { success, error ->
                            if (success) {
                                showToast("تم إرسال رابط إعادة التعيين")
                            } else {
                                showToast("خطأ: $error")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Forgot password error: ${e.message}")
                        showToast("خطأ في إرسال رابط إعادة التعيين")
                    }
                }
            }

            // Login button
            loginButton = ViewUtils.createPrimaryButton(this, "دخول") {
                performLogin()
            }

            // "أو تابع باستخدام" divider
            val orDivider = ViewUtils.createSubtitleText(
                this,
                "أو تابع باستخدام",
                sizeSp = 13f,
                color = ColorPalette.TEXT_HINT
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(20)
                    bottomMargin = dp(8)
                }
            }

            // Google sign-in outline button
            val googleButton = ViewUtils.createOutlineButton(this, "تسجيل الدخول بحساب Google") {
                performGoogleSignIn()
            }

            // Bottom row: "ليس لديك حساب؟" + "إنشاء حساب"
            val bottomRow = ViewUtils.createHorizontalLayout(this).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(24)
                }
            }

            val noAccountText = TextView(this).apply {
                text = "ليس لديك حساب؟ "
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                textSize = 14f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val registerText = TextView(this).apply {
                text = "إنشاء حساب"
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
                        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                    } catch (e: Exception) {
                        Log.e(TAG, "Navigate to register error: ${e.message}")
                    }
                }
            }

            bottomRow.addView(noAccountText)
            bottomRow.addView(registerText)

            container.addView(appName)
            container.addView(subtitle)
            container.addView(emailEditText)
            container.addView(passwordEditText)
            container.addView(forgotPassword)
            container.addView(loginButton)
            container.addView(orDivider)
            container.addView(googleButton)
            container.addView(bottomRow)

            scrollView.addView(container)
            setContentView(scrollView)
        } catch (e: Exception) {
            Log.e(TAG, "buildLayout error: ${e.message}")
        }
    }

    private fun performLogin() {
        try {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                showToast("أدخل البريد الإلكتروني")
                return
            }
            if (password.isEmpty()) {
                showToast("أدخل كلمة المرور")
                return
            }

            loginButton.isEnabled = false
            loginButton.text = "جاري الدخول..."

            FirebaseManager.signIn(email, password) { success, error ->
                try {
                    loginButton.isEnabled = true
                    loginButton.text = "دخول"
                    if (success) {
                        showToast("تم تسجيل الدخول بنجاح")
                        openMain()
                    } else {
                        showToast("خطأ: $error")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Login callback error: ${e.message}")
                    loginButton.isEnabled = true
                    loginButton.text = "دخول"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "performLogin error: ${e.message}")
            loginButton.isEnabled = true
            loginButton.text = "دخول"
            showToast("خطأ في تسجيل الدخول")
        }
    }

    private fun performGoogleSignIn() {
        try {
            loginButton.isEnabled = false
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Log.e(TAG, "performGoogleSignIn error: ${e.message}")
            loginButton.isEnabled = true
            showToast("فشل في بدء تسجيل الدخول بحساب Google")
        }
    }

    private fun handleGoogleSignInResult(result: androidx.activity.result.ActivityResult) {
        try {
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseManager.auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            try {
                                loginButton.isEnabled = true
                                if (authTask.isSuccessful) {
                                    Log.d(TAG, "Google sign-in success: ${FirebaseManager.currentUser?.email}")
                                    showToast("تم تسجيل الدخول بنجاح")
                                    openMain()
                                } else {
                                    Log.e(TAG, "Google auth failed: ${authTask.exception?.message}")
                                    showToast("فشل تسجيل الدخول بحساب Google")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Google auth callback error: ${e.message}")
                                loginButton.isEnabled = true
                            }
                        }
                } catch (e: ApiException) {
                    Log.e(TAG, "Google sign-in API error: ${e.statusCode}")
                    loginButton.isEnabled = true
                    showToast("فشل تسجيل الدخول بحساب Google")
                }
            } else {
                loginButton.isEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleGoogleSignInResult error: ${e.message}")
            loginButton.isEnabled = true
            showToast("فشل تسجيل الدخول بحساب Google")
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
