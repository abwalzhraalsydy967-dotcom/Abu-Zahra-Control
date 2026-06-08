package com.abuzahra.control.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.MainActivity
import com.abuzahra.control.constants.AppConstants
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.service.FirebaseManager
import com.abuzahra.control.ui.auth.LoginActivity
import com.abuzahra.control.util.PrefsManager
import com.abuzahra.control.util.ViewUtils
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.showToast

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate - Building splash layout")
            buildLayout()
            checkPreviousCrash()
            navigateAfterDelay()
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error: ${e.message}")
            navigateToLogin()
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
            val root = ViewUtils.createVerticalLayout(this).apply {
                setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
                gravity = Gravity.CENTER
            }

            // App name / logo TextView
            val appName = android.widget.TextView(this).apply {
                text = "Abu Zahra Control"
                setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                textSize = 28f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dp(8))
                }
            }

            // Subtitle
            val subtitle = ViewUtils.createSubtitleText(
                this,
                "لوحة التحكم الذكية",
                sizeSp = 14f,
                color = ColorPalette.TEXT_SECONDARY
            ).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Center spacer to push progressBar to bottom
            val spacer = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }

            // ProgressBar at bottom
            val progressBar = ViewUtils.createProgressBar(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, dp(48))
                }
            }

            root.addView(appName)
            root.addView(subtitle)
            root.addView(spacer)
            root.addView(progressBar)

            setContentView(root)
        } catch (e: Exception) {
            Log.e(TAG, "buildLayout error: ${e.message}")
        }
    }

    private fun checkPreviousCrash() {
        try {
            val (crashMsg, crashTime) = PrefsManager.getCrash()
            if (crashMsg.isNotEmpty() && crashTime > 0) {
                val elapsed = System.currentTimeMillis() - crashTime
                if (elapsed < 60_000) {
                    showToast("حدث خطأ سابق: $crashMsg")
                }
                PrefsManager.clearCrash()
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkPreviousCrash error: ${e.message}")
        }
    }

    private fun navigateAfterDelay() {
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val currentUser = FirebaseManager.currentUser
                    if (currentUser != null) {
                        Log.d(TAG, "User logged in, navigating to MainActivity")
                        navigateToMain()
                    } else {
                        Log.d(TAG, "No user, navigating to LoginActivity")
                        navigateToLogin()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation error: ${e.message}")
                    navigateToLogin()
                }
            }, AppConstants.SPLASH_DELAY_MS)
        } catch (e: Exception) {
            Log.e(TAG, "navigateAfterDelay error: ${e.message}")
            navigateToLogin()
        }
    }

    private fun navigateToMain() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "navigateToMain error: ${e.message}")
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        try {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "navigateToLogin error: ${e.message}")
        }
    }
}
