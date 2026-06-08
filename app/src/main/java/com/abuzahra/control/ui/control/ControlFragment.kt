package com.abuzahra.control.ui.control

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
// ActionAdapter is inner class below
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.data.model.ActionItem
import com.abuzahra.control.data.repository.CommandRepository
import com.abuzahra.control.ui.main.MainActivity
import com.abuzahra.control.util.ViewUtils
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.parseColorSafe

class ControlFragment : Fragment() {

    companion object {
        const val TAG = "ControlFragment"
    }

    private lateinit var rvActions: RecyclerView
    private val commandRepository = CommandRepository()
    private val actionAdapter = ControlAdapter(emptyList()) { item ->
        try { (activity as? MainActivity)?.sendCommand(item.command, item.params) } catch (_: Throwable) {}
    }

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
            setupActions()
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated error: ${e.message}")
        }
    }

    private fun buildView(): View {
        val ctx = requireContext()
        val scrollView = ViewUtils.createScrollView(ctx)

        val container = ViewUtils.createVerticalLayout(ctx).apply {
            setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        // Section title
        val tvSectionTitle = ViewUtils.createTitleText(
            ctx,
            "التحكم بالجهاز",
            sizeSp = 18f,
            color = ColorPalette.TEXT_PRIMARY
        )

        // RecyclerView for actions
        rvActions = RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = actionAdapter
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        }

        container.addView(tvSectionTitle)
        container.addView(rvActions)

        scrollView.addView(container)
        return scrollView
    }

    private fun setupActions() {
        try {
            val allActions = commandRepository.getAllControlActions()
            val displayItems = mutableListOf<Any>()

            for (action in allActions) {
                if (action.category == "section") {
                    // Add section header
                    displayItems.add(ControlSectionHeader(action.name))
                } else {
                    displayItems.add(action)
                }
            }

            val adapter = ControlAdapter(displayItems) { action ->
                try {
                    (activity as? MainActivity)?.sendCommand(action.command, action.params)
                } catch (e: Exception) {
                    Log.e(TAG, "Action click error: ${e.message}")
                }
            }
            rvActions.adapter = adapter
        } catch (e: Exception) {
            Log.e(TAG, "setupActions error: ${e.message}")
        }
    }

    // ── Section header data class ──
    private data class ControlSectionHeader(val title: String)

    // ── Adapter ──
    private inner class ControlAdapter(
        private val items: List<Any>,
        private val onActionClick: (ActionItem) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val typeHeader = 0
        private val typeAction = 1

        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is ControlSectionHeader -> typeHeader
                is ActionItem -> typeAction
                else -> typeAction
            }
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val ctx = parent.context
            return when (viewType) {
                typeHeader -> {
                    val tv = TextView(ctx).apply {
                        textSize = 15f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                        setPadding(dp(8), dp(16), dp(8), dp(8))
                    }
                    HeaderViewHolder(tv)
                }
                else -> {
                    val card = android.graphics.drawable.GradientDrawable()
                    card.cornerRadius = dp(12).toFloat()
                    card.setColor(ColorPalette.BG_CARD.parseColorSafe())

                    val itemLayout = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(dp(16), dp(14), dp(16), dp(14))
                        background = card
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, dp(4), 0, dp(4))
                        }
                    }

                    // Icon circle
                    val iconCircle = android.widget.FrameLayout(ctx).apply {
                        val iconBg = android.graphics.drawable.GradientDrawable()
                        iconBg.shape = android.graphics.drawable.GradientDrawable.OVAL
                        iconBg.setColor(ColorPalette.BG_INPUT.parseColorSafe())
                        background = iconBg
                        setPadding(dp(10), dp(10), dp(10), dp(10))
                        layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                    }

                    val iconText = TextView(ctx).apply {
                        textSize = 16f
                        setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                        typeface = Typeface.DEFAULT_BOLD
                        gravity = Gravity.CENTER
                    }
                    iconCircle.addView(iconText)

                    // Name
                    val nameText = TextView(ctx).apply {
                        textSize = 14f
                        setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        ).apply {
                            marginStart = dp(14)
                        }
                    }

                    // Arrow
                    val arrow = TextView(ctx).apply {
                        text = "›"
                        textSize = 20f
                        setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                        gravity = Gravity.CENTER
                    }

                    itemLayout.addView(iconCircle)
                    itemLayout.addView(nameText)
                    itemLayout.addView(arrow)

                    ActionViewHolder(itemLayout, iconText, nameText)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            if (holder is HeaderViewHolder && item is ControlSectionHeader) {
                holder.bind(item.title)
            } else if (holder is ActionViewHolder && item is ActionItem) {
                holder.bind(item)
                holder.itemView.setOnClickListener {
                    try { onActionClick(item) } catch (_: Exception) {}
                }
            }
        }

        override fun getItemCount(): Int = items.size
    }

    private class HeaderViewHolder(private val tv: TextView) : RecyclerView.ViewHolder(tv) {
        fun bind(title: String) { tv.text = title }
    }

    private class ActionViewHolder(
        itemView: View,
        private val iconText: TextView,
        private val nameText: TextView
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(action: ActionItem) {
            iconText.text = action.icon
            nameText.text = action.name
        }
    }
}
