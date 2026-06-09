package com.abuzahra.manager.ui.settings

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.abuzahra.manager.constants.ColorPalette
import com.abuzahra.manager.service.EventLogger
import com.abuzahra.manager.service.FirebaseManager
import com.abuzahra.manager.ui.auth.LoginActivity
import com.abuzahra.manager.ui.device.DeviceLinkActivity
import com.abuzahra.manager.util.PrefsManager
import com.abuzahra.manager.util.ViewUtils
import com.abuzahra.manager.util.dp
import com.abuzahra.manager.util.parseColorSafe
import com.abuzahra.manager.util.showToast
import com.abuzahra.manager.util.toTimeAgo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment() {

    companion object {
        const val TAG = "SettingsFragment"
    }

    private lateinit var contentContainer: LinearLayout
    private lateinit var tvEmail: TextView
    private lateinit var tvUid: TextView
    private lateinit var eventLogContainer: LinearLayout
    private val diagHandler = Handler(Looper.getMainLooper())
    private val logUpdateListener = { _: List<com.abuzahra.manager.service.EventLogger.LogEntry> ->
        diagHandler.post { updateEventLog() }
    }

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
            EventLogger.addListener(logUpdateListener)
            updateEventLog()
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated error: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventLogger.removeListener(logUpdateListener)
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
            ).apply { topMargin = dp(8) }
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
            ).apply { topMargin = dp(4) }
        }
        profileCard.addView(tvUid)

        contentContainer.addView(profileCard)

        // ── Device Section ──
        contentContainer.addView(ViewUtils.createSectionHeader(ctx, "الأجهزة"))

        contentContainer.addView(ViewUtils.createSettingsRow(ctx, "ربط جهاز جديد", "أدخل كود الربط من الجهاز المستهدف") {
            try {
                startActivity(Intent(requireContext(), DeviceLinkActivity::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "btnLinkDevice click error: ${e.message}")
                showToast("خطأ في فتح صفحة الربط")
            }
        })

        // ── App Section ──
        contentContainer.addView(ViewUtils.createSectionHeader(ctx, "التطبيق"))

        contentContainer.addView(ViewUtils.createSettingsRow(ctx, "حول التطبيق", "الإصدار والمعلومات") {
            try {
                val versionName = try {
                    requireActivity().packageManager
                        .getPackageInfo(requireActivity().packageName, 0).versionName ?: "1.0"
                } catch (_: Exception) { "1.0" }
                showToast("Abu Zahra Control v$versionName")
            } catch (e: Exception) {
                Log.e(TAG, "btnAppInfo click error: ${e.message}")
            }
        })

        // ── Debug Section ──
        contentContainer.addView(ViewUtils.createSectionHeader(ctx, "الصيانة"))

        contentContainer.addView(ViewUtils.createSettingsRow(ctx, "سجل الأخطاء", "عرض آخر خطأ حدث") {
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
        })

        contentContainer.addView(ViewUtils.createSettingsRow(ctx, "مسح البيانات المحلية", "حذف سجل الأخطاء والبيانات المؤقتة") {
            try {
                PrefsManager.clearCrash()
                showToast("تم مسح البيانات المحلية")
            } catch (e: Exception) {
                Log.e(TAG, "btnClearData click error: ${e.message}")
                showToast("خطأ في مسح البيانات")
            }
        })

        // ── Event Log Section ──
        contentContainer.addView(ViewUtils.createSectionHeader(ctx, "سجل الأحداث"))

        val clearLogBtn = ViewUtils.createSettingsRow(ctx, "مسح سجل الأحداث", "حذف جميع السجلات") {
            EventLogger.clear()
            updateEventLog()
            showToast("تم مسح سجل الأحداث")
        }

        eventLogContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ColorPalette.BG_INPUT.parseColorSafe())
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val eventLogScrollView = ScrollView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(200)
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
            addView(eventLogContainer)
        }

        // ── Spacer ──
        val spacer = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(16)
            )
        }

        // ── Logout Button ──
        val btnLogout = ViewUtils.createDangerButton(ctx, "تسجيل الخروج") {
            try {
                EventLogger.log("تسجيل الخروج", success = true, "تم تسجيل الخروج")
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
        contentContainer.addView(clearLogBtn)
        contentContainer.addView(eventLogScrollView)
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

    /**
     * Update the event log view in settings.
     */
    private fun updateEventLog() {
        try {
            if (!isAdded || !::eventLogContainer.isInitialized) return
            val ctx = requireContext()

            eventLogContainer.removeAllViews()
            val logs = EventLogger.getRecentLogs(15)

            if (logs.isEmpty()) {
                eventLogContainer.addView(TextView(ctx).apply {
                    text = "لا توجد أحداث مسجلة بعد"
                    textSize = 12f
                    setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                    setPadding(0, dp(8), 0, dp(8))
                })
                return
            }

            for (entry in logs.reversed()) {
                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(2), 0, dp(2))
                }

                val statusIcon = if (entry.success) "\u2713" else "\u2717"

                val iconView = TextView(ctx).apply {
                    text = statusIcon
                    textSize = 14f
                    setTextColor(
                        if (entry.success) ColorPalette.SUCCESS.parseColorSafe()
                        else ColorPalette.ERROR.parseColorSafe()
                    )
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dp(6) }
                }

                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(Date(entry.timestamp))

                val timeView = TextView(ctx).apply {
                    text = timeStr
                    textSize = 10f
                    setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dp(4) }
                }

                val labelView = TextView(ctx).apply {
                    text = entry.action
                    textSize = 12f
                    setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                }

                val detailView = TextView(ctx).apply {
                    text = entry.message
                    textSize = 10f
                    setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginStart = dp(6) }
                }

                row.addView(iconView)
                row.addView(timeView)
                row.addView(labelView)
                row.addView(detailView)
                eventLogContainer.addView(row)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateEventLog error: ${e.message}")
        }
    }
}
