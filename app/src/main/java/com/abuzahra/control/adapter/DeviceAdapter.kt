package com.abuzahra.control.adapter

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.R
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.data.model.Device
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.parseColorSafe

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    private val devices = mutableListOf<Device>()
    var onDeviceClick: ((Device) -> Unit)? = null

    fun submitList(newList: List<Device>) {
        try {
            devices.clear()
            devices.addAll(newList)
            notifyDataSetChanged()
        } catch (_: Exception) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val ctx = parent.context

        // Card background
        val card = android.graphics.drawable.GradientDrawable()
        card.cornerRadius = ctx.dp(14).toFloat()
        card.setColor(ColorPalette.BG_CARD.parseColorSafe())

        // Root item layout: horizontal
        val itemLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = card
            setPadding(ctx.dp(16), ctx.dp(14), ctx.dp(16), ctx.dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, ctx.dp(6), 0, ctx.dp(6))
            }
        }

        // ── Left: Device icon circle ──
        val iconCircle = FrameLayout(ctx).apply {
            val iconBg = android.graphics.drawable.GradientDrawable()
            iconBg.shape = android.graphics.drawable.GradientDrawable.OVAL
            iconBg.setColor(ColorPalette.BG_INPUT.parseColorSafe())
            background = iconBg
            layoutParams = LinearLayout.LayoutParams(ctx.dp(40), ctx.dp(40))
        }

        val iconText = TextView(ctx).apply {
            text = "D"
            textSize = 18f
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

        // ── Middle: Device name + model (weight=1) ──
        val middleLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = ctx.dp(14)
            }
        }

        val tvDeviceName = TextView(ctx).apply {
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val tvDeviceModel = TextView(ctx).apply {
            textSize = 12f
            setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = ctx.dp(2)
            }
        }

        middleLayout.addView(tvDeviceName)
        middleLayout.addView(tvDeviceModel)

        // ── Right: Status dot + status text ──
        val rightLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val statusIndicator = View(ctx).apply {
            val dotBg = android.graphics.drawable.GradientDrawable()
            dotBg.shape = android.graphics.drawable.GradientDrawable.OVAL
            dotBg.setColor(ColorPalette.ERROR.parseColorSafe())
            background = dotBg
            layoutParams = LinearLayout.LayoutParams(ctx.dp(8), ctx.dp(8)).apply {
                gravity = Gravity.CENTER
            }
        }

        val tvStatus = TextView(ctx).apply {
            textSize = 11f
            setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = ctx.dp(2)
            }
        }

        rightLayout.addView(statusIndicator)
        rightLayout.addView(tvStatus)

        // Assemble
        itemLayout.addView(iconCircle)
        itemLayout.addView(middleLayout)
        itemLayout.addView(rightLayout)

        return ViewHolder(itemLayout, tvDeviceName, tvDeviceModel, statusIndicator, tvStatus)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        holder.tvDeviceName.text = device.name.ifEmpty { "جهاز غير معروف" }
        holder.tvDeviceModel.text = device.model.ifEmpty { device.brand.ifEmpty { "—" } }
        holder.tvStatus.text = device.statusText

        // Update status dot color
        val dotDrawable = android.graphics.drawable.GradientDrawable()
        dotDrawable.shape = android.graphics.drawable.GradientDrawable.OVAL
        try {
            val onlineDrawableRes = R.drawable.bg_status_online
            val offlineDrawableRes = R.drawable.bg_status_offline
            if (device.isOnline) {
                holder.itemView.context.getDrawable(onlineDrawableRes)?.let {
                    holder.statusIndicator.background = it
                } ?: run {
                    dotDrawable.setColor(ColorPalette.SUCCESS.parseColorSafe())
                    holder.statusIndicator.background = dotDrawable
                }
                holder.tvStatus.setTextColor(ColorPalette.SUCCESS.parseColorSafe())
            } else {
                holder.itemView.context.getDrawable(offlineDrawableRes)?.let {
                    holder.statusIndicator.background = it
                } ?: run {
                    dotDrawable.setColor(ColorPalette.ERROR.parseColorSafe())
                    holder.statusIndicator.background = dotDrawable
                }
                holder.tvStatus.setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
            }
        } catch (_: Exception) {
            dotDrawable.setColor(
                if (device.isOnline) ColorPalette.SUCCESS.parseColorSafe()
                else ColorPalette.ERROR.parseColorSafe()
            )
            holder.statusIndicator.background = dotDrawable
        }

        holder.itemView.setOnClickListener {
            try { onDeviceClick?.invoke(device) } catch (_: Exception) {}
        }
    }

    override fun getItemCount(): Int = devices.size

    class ViewHolder(
        itemView: View,
        val tvDeviceName: TextView,
        val tvDeviceModel: TextView,
        val statusIndicator: View,
        val tvStatus: TextView
    ) : RecyclerView.ViewHolder(itemView)
}
