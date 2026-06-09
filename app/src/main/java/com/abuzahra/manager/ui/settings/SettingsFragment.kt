package com.abuzahra.manager.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.abuzahra.manager.constants.ColorPalette
import com.abuzahra.manager.service.FirebaseManager
import com.abuzahra.manager.ui.auth.LoginActivity
import com.abuzahra.manager.ui.device.DeviceLinkActivity
import com.abuzahra.manager.util.PrefsManager
import com.abuzahra.manager.util.ViewUtils
import com.abuzahra.manager.util.dp
import com.abuzahra.manager.util.parseColorSafe
import com.abuzahra.manager.util.showToast
import com.abuzahra.manager.util.toTimeAgo

class SettingsFragment : Fragment() {

    companion object {
        const val TAG = "SettingsFragment"
    }

    private lateinit var contentContainer: LinearLayout
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
            Log.e(TAG, "onCreateView error: ${e.message}", e)
            createErrorView("خطأ في تحميل الإعدادات")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            populateUserInfo()
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated error: ${e.message}", e)
        }
    }

    private fun createErrorView(message: String): View {
        val ctx = requireContext()
        return ViewUtils.createEmptyStateView(ctx, message, "إعادة المحاولة") {
            try {
                contentContainer.removeAllViews()
                populateUserInfo()
            } catch (_: Exception) {}
        }
    }

    private fun buildView(): View {
        val ctx = requireContext()
        val scrollView = ViewUtils.createScrollView(ctx)

        contentContainer = ViewUtils.createVerticalLayout(ctx).apply {
            setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        // ── User Profile Card ──
        val profileCard = ViewUtils.createCard(ctx, padding = dp(20))
        profileCard.gravity = Gravity.CENTER

        val profileIcon = TextView(ctx).apply {
            text = "\uD83D\uDC64"
            textSize = 48f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        profileCard.addView(profileIcon)

        tvEmail = TextView(ctx).apply {
            text = "جاري التحميل..."
            setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        }
        profileCard.addView(tvEmail)

        tvUid = TextView(ctx).apply {
            text = ""
            setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
            textSize = 11f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }
        profileCard.addView(tvUid)

        // ── Device Section ──
        contentContainer.addView(profileCard)

        val deviceSectionHeader = ViewUtils.createSectionHeader(ctx, "الأجهزة")

        val btnLinkDevice = ViewUtils.createSettingsRow(ctx, "ربط جهاز جديد", "أدخل كود الربط من الجهاز المستهدف") {
            try {
                startActivity(Intent(requireContext(), DeviceLinkActivity::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "btnLinkDevice click error: ${e.message}")
                showToast("خطأ في فتح صفحة الربط")
            }
        }

        // ── App Section ──
        val appSectionHeader = ViewUtils.createSectionHeader(ctx, "التطبيق")

        val btnAppInfo = ViewUtils.createSettingsRow(ctx, "حول التطبيق", "الإصدار والمعلومات") {
            try {
                val versionName = try {
                    requireActivity().packageManager
                        .getPackageInfo(requireActivity().packageName, 0).versionName ?: "1.0"
                } catch (_: Exception) { "1.0" }
                showToast("Abu Zahra Control v$versionName")
            } catch (e: Exception) {
                Log.e(TAG, "btnAppInfo click error: ${e.message}")
            }
        }

        // ── Debug Section ──
        val debugSectionHeader = ViewUtils.createSectionHeader(ctx, "الصيانة")

        val btnCrashLog = ViewUtils.createSettingsRow(ctx, "سجل الأخطاء", "عرض آخر خطأ حدث") {
            try {
                val (crashMsg, crashTime) = PrefsManager.getCrash()
                if (crashMsg.isNotEmpty()) {
                    val timeAgo = try { crashTime.toTimeAgo() } catch (_: Exception) { "" }
                    showToast("$timeAgo: $crashMsg")
                } else {
                    showToast("لا توجد أخطاء مسجلة")
                }
            } catch (e: Exception) {
                Log.e(TAG, "btnCrashLog click error: ${e.message}")
            }
        }

        val btnClearData = ViewUtils.createSettingsRow(ctx, "مسح البيانات المحلية", "حذف سجل الأخطاء والبيانات المؤقتة") {
            try {
                PrefsManager.clearCrash()
                showToast("تم مسح البيانات المحلية")
            } catch (e: Exception) {
                Log.e(TAG, "btnClearData click error: ${e.message}")
                showToast("خطأ في مسح البيانات")
            }
        }

        // ── Spacer ──
        val spacer = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(16)
            )
        }

        // ── Logout Button ──
        val btnLogout = ViewUtils.createDangerButton(ctx, "تسجيل الخروج") {
            try {
                FirebaseManager.signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "btnLogout click error: ${e.message}")
                showToast("خطأ في تسجيل الخروج")
            }
        }

        // Assemble
        contentContainer.addView(deviceSectionHeader)
        contentContainer.addView(btnLinkDevice)
        contentContainer.addView(appSectionHeader)
        contentContainer.addView(btnAppInfo)
        contentContainer.addView(debugSectionHeader)
        contentContainer.addView(btnCrashLog)
        contentContainer.addView(btnClearData)
        contentContainer.addView(spacer)
        contentContainer.addView(btnLogout)

        scrollView.addView(contentContainer)
        return scrollView
    }

    private fun populateUserInfo() {
        try {
            val email = FirebaseManager.userEmail
            val uid = FirebaseManager.userId
            tvEmail.text = email ?: "غير مسجل الدخول"
            tvUid.text = if (!uid.isNullOrEmpty()) "UID: $uid" else ""
        } catch (e: Exception) {
            Log.e(TAG, "populateUserInfo error: ${e.message}")
            tvEmail.text = "غير مسجل الدخول"
            tvUid.text = ""
        }
    }
}
