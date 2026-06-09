package com.abuzahra.manager.ui.smscalls

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.abuzahra.manager.constants.ColorPalette
import com.abuzahra.manager.data.repository.CommandRepository
import com.abuzahra.manager.ui.main.MainActivity
import com.abuzahra.manager.util.ViewUtils
import com.abuzahra.manager.util.dp
import com.abuzahra.manager.util.parseColorSafe
import com.abuzahra.manager.util.showToast

class SmsCallsFragment : Fragment() {

    companion object {
        const val TAG = "SmsCallsFragment"
    }

    private lateinit var contentContainer: LinearLayout
    private val commandRepository = CommandRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return try {
            buildView()
        } catch (e: Exception) {
            Log.e(TAG, "onCreateView error: ${e.message}", e)
            createErrorView("خطأ في تحميل الرسائل")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupActions()
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated error: ${e.message}", e)
        }
    }

    private fun createErrorView(message: String): View {
        val ctx = requireContext()
        return ViewUtils.createEmptyStateView(ctx, message, "إعادة المحاولة") {
            try {
                contentContainer.removeAllViews()
                setupActions()
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

        // Title
        val title = ViewUtils.createTitleText(ctx, "الرسائل والمكالمات", 20f, ColorPalette.TEXT_PRIMARY)

        // No device warning
        val noDeviceWarning = createNoDeviceWarning(ctx)

        contentContainer.addView(title)
        contentContainer.addView(noDeviceWarning)

        scrollView.addView(contentContainer)
        return scrollView
    }

    private fun createNoDeviceWarning(ctx: android.content.Context): LinearLayout {
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val cardBg = android.graphics.drawable.GradientDrawable()
            cardBg.cornerRadius = dp(12).toFloat()
            cardBg.setColor(ColorPalette.BG_CARD.parseColorSafe())
            background = cardBg
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(8)
            }

            addView(android.widget.TextView(ctx).apply {
                text = "\u26A0"
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(8) }
            })
            addView(android.widget.TextView(ctx).apply {
                text = "لم يتم اختيار جهاز - اختر جهازاً من تبويب الرئيسية"
                setTextColor(ColorPalette.WARNING.parseColorSafe())
                textSize = 13f
            })
        }
    }

    private fun setupActions() {
        try {
            val actions = commandRepository.getSmsCallsActions()

            for (action in actions) {
                val card = ViewUtils.createActionCard(
                    requireContext(), action.icon, action.name
                )
                card.setOnClickListener {
                    try {
                        val dev = MainActivity.selectedDevice
                        if (dev == null) {
                            showToast("يرجى اختيار جهاز أولاً")
                            return@setOnClickListener
                        }
                        (activity as? MainActivity)?.sendCommand(action.command, action.params)
                        showToast("تم إرسال: ${action.name}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Action click error: ${e.message}")
                        showToast("خطأ في إرسال الأمر")
                    }
                }
                contentContainer.addView(card)
            }

            // Bottom spacing
            contentContainer.addView(View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(32)
                )
            })
        } catch (e: Exception) {
            Log.e(TAG, "setupActions error: ${e.message}", e)
        }
    }
}
