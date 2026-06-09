package com.abuzahra.manager.ui.control

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
import com.abuzahra.manager.constants.ColorPalette
import com.abuzahra.manager.data.model.ActionItem
import com.abuzahra.manager.data.repository.CommandRepository
import com.abuzahra.manager.ui.main.MainActivity
import com.abuzahra.manager.util.ViewUtils
import com.abuzahra.manager.util.dp
import com.abuzahra.manager.util.parseColorSafe
import com.abuzahra.manager.util.showToast

class ControlFragment : Fragment() {

    companion object {
        const val TAG = "ControlFragment"
    }

    private lateinit var contentContainer: LinearLayout
    private lateinit var noDeviceWarning: View
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
            createErrorView("خطأ في تحميل التحكم")
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
        val title = ViewUtils.createTitleText(ctx, "التحكم بالجهاز", 20f, ColorPalette.TEXT_PRIMARY)

        // No device warning
        noDeviceWarning = LinearLayout(ctx).apply {
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

            val warnIcon = TextView(ctx).apply {
                text = "\u26A0"
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(8) }
            }

            val warnText = TextView(ctx).apply {
                text = "لم يتم اختيار جهاز - اختر جهازاً من تبويب الرئيسية"
                setTextColor(ColorPalette.WARNING.parseColorSafe())
                textSize = 13f
            }

            addView(warnIcon)
            addView(warnText)
        }

        contentContainer.addView(title)
        contentContainer.addView(noDeviceWarning)

        scrollView.addView(contentContainer)
        return scrollView
    }

    private fun setupActions() {
        try {
            // Check if a device is selected
            val device = MainActivity.selectedDevice
            if (device == null) {
                noDeviceWarning.visibility = View.VISIBLE
            } else {
                noDeviceWarning.visibility = View.GONE
            }

            val allActions = commandRepository.getAllControlActions()

            for (action in allActions) {
                if (action.category == "section") {
                    // Section header
                    val sectionHeader = ViewUtils.createSectionHeader(
                        requireContext(), action.name
                    )
                    contentContainer.addView(sectionHeader)
                } else {
                    // Action card
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
            }

            // Add bottom spacing
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
