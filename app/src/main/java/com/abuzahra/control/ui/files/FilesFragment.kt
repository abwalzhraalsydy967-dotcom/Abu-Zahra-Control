package com.abuzahra.control.ui.files

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.data.model.ActionItem
import com.abuzahra.control.data.repository.CommandRepository
import com.abuzahra.control.ui.main.MainActivity
import com.abuzahra.control.util.ViewUtils
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.parseColorSafe

class FilesFragment : Fragment() {

    companion object {
        const val TAG = "FilesFragment"
    }

    private lateinit var rvActions: RecyclerView
    private val commandRepository = CommandRepository()

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

        val tvSectionTitle = ViewUtils.createTitleText(
            ctx,
            "الملفات",
            sizeSp = 18f,
            color = ColorPalette.TEXT_PRIMARY
        )

        rvActions = RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
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
            val actions = commandRepository.getFileActions()
            val adapter = ActionListAdapter(actions) { action ->
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

    // ── Reusable action list adapter ──
    private class ActionListAdapter(
        private val items: List<ActionItem>,
        private val onActionClick: (ActionItem) -> Unit
    ) : RecyclerView.Adapter<ActionListAdapter.ViewHolder>() {

        class ViewHolder(
            itemView: View,
            val iconText: TextView,
            val nameText: TextView
        ) : RecyclerView.ViewHolder(itemView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val ctx = parent.context
            val card = android.graphics.drawable.GradientDrawable()
            card.cornerRadius = ctx.dp(12).toFloat()
            card.setColor(ColorPalette.BG_CARD.parseColorSafe())

            val itemLayout = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(14))
                background = card
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, ctx.dp(4), 0, ctx.dp(4))
                }
            }

            val iconCircle = FrameLayout(ctx).apply {
                val iconBg = android.graphics.drawable.GradientDrawable()
                iconBg.shape = android.graphics.drawable.GradientDrawable.OVAL
                iconBg.setColor(ColorPalette.BG_INPUT.parseColorSafe())
                background = iconBg
                layoutParams = LinearLayout.LayoutParams(ctx.dp(40), ctx.dp(40))
            }

            val iconText = TextView(ctx).apply {
                textSize = 16f
                setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            }
            iconCircle.addView(iconText)

            val nameText = TextView(ctx).apply {
                textSize = 14f
                setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginStart = ctx.dp(14)
                }
            }

            val arrow = TextView(ctx).apply {
                text = "›"
                textSize = 20f
                setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                gravity = Gravity.CENTER
            }

            itemLayout.addView(iconCircle)
            itemLayout.addView(nameText)
            itemLayout.addView(arrow)

            return ViewHolder(itemLayout, iconText, nameText)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val action = items[position]
            holder.iconText.text = action.icon
            holder.nameText.text = action.name
            holder.itemView.setOnClickListener {
                try { onActionClick(action) } catch (_: Exception) {}
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
