package com.abuzahra.control.ui.settings

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.service.FirebaseManager
import com.abuzahra.control.ui.auth.LoginActivity
import com.abuzahra.control.ui.device.DeviceLinkActivity
import com.abuzahra.control.util.PrefsManager
import com.abuzahra.control.util.ViewUtils
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.parseColorSafe
import com.abuzahra.control.util.showToast
import com.abuzahra.control.util.toTimeAgo

class SettingsFragment : Fragment() {

    companion object {
        const val TAG = "SettingsFragment"
    }

    private lateinit var tvEmail: TextView
    private lateinit var tvUid: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return try {
            buildView()
        } catch (e: Exception) {
            Log.e(TAG, "onCreateView error: ${e.message}")
            View(requireContext()).apply {
                setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            populateUserInfo()
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated error: ${e.message}")
        }
    }

    private fun buildView(): View {
        val ctx = requireContext()
        val scrollView = ViewUtils.createScrollView(ctx)

        val container = ViewUtils.createVerticalLayout(ctx).apply {
            setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        // Email
        tvEmail = TextView(ctx).apply {
            text = ""
            setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // UID
        tvUid = TextView(ctx).apply {
            text = ""
            setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
            textSize = 12f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }

        // Divider
        val divider = ViewUtils.createDivider(ctx, marginTop = dp(24), marginBot = dp(16))

        // ── Settings Cards ──
        val btnLinkDevice = createSettingsCard(ctx, "ربط جهاز جديد") {
            try {
                startActivity(Intent(ctx, DeviceLinkActivity::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "btnLinkDevice click error: ${e.message}")
            }
        }

        val btnAppInfo = createSettingsCard(ctx, "حول التطبيق") {
            try {
                val versionName = try {
                    requireActivity().packageManager
                        .getPackageInfo(requireActivity().packageName, 0).versionName ?: "1.0"
                } catch (_: Exception) { "1.0" }
                ctx.showToast("Abu Zahra Control v$versionName")
            } catch (e: Exception) {
                Log.e(TAG, "btnAppInfo click error: ${e.message}")
            }
        }

        val btnCrashLog = createSettingsCard(ctx, "سجل الأخطاء") {
            try {
                val (crashMsg, crashTime) = PrefsManager.getCrash()
                if (crashMsg.isNotEmpty()) {
                    val timeAgo = try { crashTime.toTimeAgo() } catch (_: Exception) { "" }
                    ctx.showToast("$timeAgo: $crashMsg")
                } else {
                    ctx.showToast("لا توجد أخطاء مسجلة")
                }
            } catch (e: Exception) {
                Log.e(TAG, "btnCrashLog click error: ${e.message}")
            }
        }

        val btnClearData = createSettingsCard(ctx, "مسح البيانات المحلية") {
            try {
                PrefsManager.clearCrash()
                ctx.showToast("تم مسح البيانات المحلية")
            } catch (e: Exception) {
                Log.e(TAG, "btnClearData click error: ${e.message}")
            }
        }

        // Spacer
        val spacer = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(16)
            )
        }

        // Logout button
        val btnLogout = ViewUtils.createDangerButton(ctx, "تسجيل الخروج") {
            try {
                FirebaseManager.signOut()
                val intent = Intent(ctx, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "btnLogout click error: ${e.message}")
                ctx.showToast("خطأ في تسجيل الخروج")
            }
        }

        container.addView(tvEmail)
        container.addView(tvUid)
        container.addView(divider)
        container.addView(btnLinkDevice)
        container.addView(btnAppInfo)
        container.addView(btnCrashLog)
        container.addView(btnClearData)
        container.addView(spacer)
        container.addView(btnLogout)

        scrollView.addView(container)
        return scrollView
    }

    private fun createSettingsCard(
        ctx: android.content.Context,
        title: String,
        onClick: () -> Unit
    ): View {
        val card = android.graphics.drawable.GradientDrawable()
        card.cornerRadius = ctx.dp(14).toFloat()
        card.setColor(ColorPalette.BG_CARD.parseColorSafe())

        return TextView(ctx).apply {
            text = title
            setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
            textSize = 15f
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setPadding(ctx.dp(16), ctx.dp(16), ctx.dp(16), ctx.dp(16))
            background = card
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, ctx.dp(6), 0, ctx.dp(6))
            }
            setOnClickListener {
                try { onClick() } catch (_: Exception) {}
            }
        }
    }

    private fun populateUserInfo() {
        try {
            val email = FirebaseManager.userEmail
            val uid = FirebaseManager.userId
            tvEmail.text = email ?: "غير مسجل الدخول"
            tvUid.text = uid ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "populateUserInfo error: ${e.message}")
            tvEmail.text = "غير مسجل الدخول"
            tvUid.text = ""
        }
    }
}
