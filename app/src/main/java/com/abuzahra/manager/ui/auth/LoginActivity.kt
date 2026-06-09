package com.abuzahra.manager.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.manager.ui.main.MainActivity
import com.abuzahra.manager.R
import com.abuzahra.manager.constants.AppConstants
import com.abuzahra.manager.constants.ColorPalette
import com.abuzahra.manager.service.EventLogger
import com.abuzahra.manager.service.FirebaseManager
import com.abuzahra.manager.util.ViewUtils
import com.abuzahra.manager.util.dp
import com.abuzahra.manager.util.showToast
import com.abuzahra.manager.util.parseColorSafe
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    private lateinit var emailEditText: android.widget.EditText
    private lateinit var passwordEditText: android.widget.EditText
    private lateinit var loginButton: android.widget.Button
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    // Event log UI
    private lateinit var eventLogStatus: TextView
    private lateinit var eventLogContainer: LinearLayout
    private lateinit var eventLogScroll: ScrollView
    private val logUpdateListener = { _: List<com.abuzahra.manager.service.EventLogger.LogEntry> ->
        runOnUiThread { updateEventLog() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate")
            setupGoogleSignIn()
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

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventLogger.removeListener(logUpdateListener)
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
                    EventLogger.fail("تسجيل Google", "خطأ: ${e.message}")
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
            val rootLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }

            // Main scrollable content (weight=1 to take remaining space)
            val scrollView = ViewUtils.createScrollView(this)
            scrollView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )

            val container = ViewUtils.createVerticalLayout(this).apply {
                setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
                gravity = Gravity.CENTER
                setPadding(dp(32), dp(48), dp(32), dp(32))
            }

            // App name
            val appName = ViewUtils.createTitleText(
                this, "Abu Zahra Control",
                sizeSp = 26f, color = ColorPalette.PRIMARY
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Subtitle
            val subtitle = ViewUtils.createSubtitleText(
                this, "سجل دخولك للمتابعة",
                sizeSp = 14f, color = ColorPalette.TEXT_SECONDARY
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(8); bottomMargin = dp(24) }
            }

            // Email EditText
            emailEditText = ViewUtils.createEditText(
                this, "البريد الإلكتروني",
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT
            )

            // Password EditText
            passwordEditText = ViewUtils.createEditText(
                this, "كلمة المرور",
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
                ).apply { topMargin = dp(8); bottomMargin = dp(16) }
                setOnClickListener {
                    try {
                        val email = emailEditText.text.toString().trim()
                        if (email.isEmpty()) {
                            showToast("أدخل البريد الإلكتروني أولاً")
                            return@setOnClickListener
                        }
                        FirebaseManager.resetPassword(email) { success, error ->
                            if (success) {
                                EventLogger.success("إعادة تعيين كلمة المرور", "تم إرسال الرابط إلى $email")
                                showToast("تم إرسال رابط إعادة التعيين")
                            } else {
                                EventLogger.fail("إعادة تعيين كلمة المرور", error ?: "فشل")
                                showToast("خطأ: $error")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Forgot password error: ${e.message}")
                        EventLogger.fail("إعادة تعيين كلمة المرور", "خطأ: ${e.message}")
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
                this, "أو تابع باستخدام",
                sizeSp = 13f, color = ColorPalette.TEXT_HINT
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(20); bottomMargin = dp(8) }
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
                ).apply { topMargin = dp(24) }
            }

            val noAccountText = TextView(this).apply {
                text = "ليس لديك حساب؟ "
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                textSize = 14f
                gravity = Gravity.CENTER
            }

            val registerText = TextView(this).apply {
                text = "إنشاء حساب"
                setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                textSize = 14f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
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
            rootLayout.addView(scrollView)

            // ── Event Log Bar at the bottom ──
            rootLayout.addView(buildEventLogBar())

            setContentView(rootLayout)
        } catch (e: Exception) {
            Log.e(TAG, "buildLayout error: ${e.message}")
        }
    }

    /**
     * Build event log bar for login screen.
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

        // Divider
        barRoot.addView(View(this).apply {
            setBackgroundColor(ColorPalette.DIVIDER.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
        })

        // Status summary row
        val summaryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(6), dp(12), dp(4))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                EventLogger.clear()
                updateEventLog()
            }
        }
        summaryRow.addView(clearBtn)
        barRoot.addView(summaryRow)

        // Scrollable log area
        eventLogScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(100)
            )
            isVerticalScrollBarEnabled = true
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

    private fun performLogin() {
        try {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                EventLogger.fail("تسجيل الدخول", "البريد الإلكتروني فارغ")
                showToast("أدخل البريد الإلكتروني")
                return
            }
            if (password.isEmpty()) {
                EventLogger.fail("تسجيل الدخول", "كلمة المرور فارغة")
                showToast("أدخل كلمة المرور")
                return
            }

            loginButton.isEnabled = false
            loginButton.text = "جاري الدخول..."
            EventLogger.log("تسجيل الدخول", success = true, "جاري التحقق من $email...")

            FirebaseManager.signIn(email, password) { success, error ->
                try {
                    loginButton.isEnabled = true
                    loginButton.text = "دخول"
                    if (success) {
                        EventLogger.success("تسجيل الدخول", "تم بنجاح - $email")
                        showToast("تم تسجيل الدخول بنجاح")
                        openMain()
                    } else {
                        EventLogger.fail("تسجيل الدخول", error ?: "فشل تسجيل الدخول")
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
            EventLogger.fail("تسجيل الدخول", "خطأ: ${e.message}")
            showToast("خطأ في تسجيل الدخول")
        }
    }

    private fun performGoogleSignIn() {
        try {
            loginButton.isEnabled = false
            EventLogger.log("تسجيل Google", success = true, "جاري فتح نافذة Google...")
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Log.e(TAG, "performGoogleSignIn error: ${e.message}")
            loginButton.isEnabled = true
            EventLogger.fail("تسجيل Google", "خطأ: ${e.message}")
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
                                    EventLogger.success("تسجيل Google", "تم بنجاح - ${FirebaseManager.currentUser?.email}")
                                    showToast("تم تسجيل الدخول بنجاح")
                                    openMain()
                                } else {
                                    val errMsg = authTask.exception?.message ?: "فشل المصادقة"
                                    Log.e(TAG, "Google auth failed: $errMsg")
                                    EventLogger.fail("تسجيل Google", errMsg)
                                    showToast("فشل تسجيل الدخول بحساب Google")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Google auth callback error: ${e.message}")
                                loginButton.isEnabled = true
                            }
                        }
                } catch (e: ApiException) {
                    Log.e(TAG, "Google sign-in API error: ${e.statusCode} - ${e.message}")
                    EventLogger.fail("تسجيل Google", "خطأ API (${e.statusCode}): ${e.message}")
                    loginButton.isEnabled = true
                    showToast("فشل تسجيل الدخول بحساب Google")
                }
            } else {
                EventLogger.fail("تسجيل Google", "تم الإلغاء أو إغلاق النافذة")
                loginButton.isEnabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleGoogleSignInResult error: ${e.message}")
            loginButton.isEnabled = true
            EventLogger.fail("تسجيل Google", "خطأ: ${e.message}")
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
