package com.abuzahra.manager.util

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import com.abuzahra.manager.constants.ColorPalette

object ViewUtils {

    fun createVerticalLayout(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
    }

    fun createHorizontalLayout(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun createScrollView(context: Context): ScrollView {
        return ScrollView(context).apply {
            // Use MATCH_PARENT for both dimensions - will be added to FrameLayout
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
        }
    }

    fun createCard(context: Context, padding: Int = 16): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val drawable = GradientDrawable()
            drawable.cornerRadius = context.dp(16).toFloat()
            drawable.setColor(ColorPalette.BG_CARD.parseColorSafe())
            background = drawable
            setPadding(padding, padding, padding, padding)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, context.dp(4), 0, context.dp(4))
            }
        }
    }

    fun createTitleText(
        context: Context,
        text: String,
        sizeSp: Float = 18f,
        color: String = ColorPalette.TEXT_PRIMARY
    ): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(color.parseColorSafe())
            textSize = sizeSp
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun createSubtitleText(
        context: Context,
        text: String,
        sizeSp: Float = 14f,
        color: String = ColorPalette.TEXT_SECONDARY
    ): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(color.parseColorSafe())
            textSize = sizeSp
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun createBodyText(
        context: Context,
        text: String,
        sizeSp: Float = 13f,
        color: String = ColorPalette.TEXT_HINT
    ): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(color.parseColorSafe())
            textSize = sizeSp
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun createPrimaryButton(
        context: Context,
        text: String,
        onClick: (() -> Unit) = {}
    ): Button {
        return Button(context).apply {
            this.text = text
            setTextColor(ColorPalette.WHITE.parseColorSafe())
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            val drawable = GradientDrawable()
            drawable.cornerRadius = context.dp(14).toFloat()
            drawable.setColor(ColorPalette.PRIMARY.parseColorSafe())
            background = drawable
            setPadding(context.dp(24), 0, context.dp(24), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.dp(52)
            ).apply {
                setMargins(0, context.dp(12), 0, 0)
            }
            setOnClickListener {
                try { onClick() } catch (_: Throwable) {}
            }
        }
    }

    fun createOutlineButton(
        context: Context,
        text: String,
        onClick: (() -> Unit) = {}
    ): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(ColorPalette.PRIMARY.parseColorSafe())
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val drawable = GradientDrawable()
            drawable.cornerRadius = context.dp(14).toFloat()
            drawable.setColor(ColorPalette.TRANSPARENT.parseColorSafe())
            drawable.setStroke(context.dp(2), ColorPalette.PRIMARY.parseColorSafe())
            background = drawable
            setPadding(context.dp(16), context.dp(12), context.dp(16), context.dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, context.dp(8), 0, 0)
            }
            setOnClickListener {
                try { onClick() } catch (_: Throwable) {}
            }
        }
    }

    fun createDangerButton(
        context: Context,
        text: String,
        onClick: (() -> Unit) = {}
    ): Button {
        return Button(context).apply {
            this.text = text
            setTextColor(ColorPalette.WHITE.parseColorSafe())
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            val drawable = GradientDrawable()
            drawable.cornerRadius = context.dp(12).toFloat()
            drawable.setColor(ColorPalette.ERROR.parseColorSafe())
            background = drawable
            setPadding(context.dp(24), 0, context.dp(24), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.dp(48)
            ).apply {
                setMargins(0, context.dp(8), 0, 0)
            }
            setOnClickListener {
                try { onClick() } catch (_: Throwable) {}
            }
        }
    }

    fun createWarningButton(
        context: Context,
        text: String,
        onClick: (() -> Unit) = {}
    ): Button {
        return Button(context).apply {
            this.text = text
            setTextColor(ColorPalette.BLACK.parseColorSafe())
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            val drawable = GradientDrawable()
            drawable.cornerRadius = context.dp(12).toFloat()
            drawable.setColor(ColorPalette.WARNING.parseColorSafe())
            background = drawable
            setPadding(context.dp(24), 0, context.dp(24), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.dp(48)
            ).apply {
                setMargins(0, context.dp(8), 0, 0)
            }
            setOnClickListener {
                try { onClick() } catch (_: Throwable) {}
            }
        }
    }

    fun createEditText(
        context: Context,
        hint: String,
        inputType: Int = android.text.InputType.TYPE_CLASS_TEXT
    ): EditText {
        return EditText(context).apply {
            this.hint = hint
            setHintTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
            setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
            textSize = 15f
            this.inputType = inputType
            val drawable = GradientDrawable()
            drawable.cornerRadius = context.dp(12).toFloat()
            drawable.setColor(ColorPalette.BG_INPUT.parseColorSafe())
            background = drawable
            setPadding(context.dp(16), context.dp(14), context.dp(16), context.dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.dp(52)
            ).apply {
                setMargins(0, context.dp(8), 0, 0)
            }
        }
    }

    fun createDivider(context: Context, marginTop: Int = 0, marginBot: Int = 0): View {
        return View(context).apply {
            setBackgroundColor(ColorPalette.DIVIDER.parseColorSafe())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(0, marginTop, 0, marginBot)
            }
        }
    }

    fun createStatusDot(context: Context, isOnline: Boolean): View {
        return View(context).apply {
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(
                (if (isOnline) ColorPalette.SUCCESS else ColorPalette.ERROR).parseColorSafe()
            )
            background = drawable
            layoutParams = LinearLayout.LayoutParams(context.dp(10), context.dp(10))
        }
    }

    fun createEmptyStateView(
        context: Context,
        message: String,
        buttonText: String? = null,
        onButtonClick: (() -> Unit)? = null
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(context.dp(32), context.dp(64), context.dp(32), context.dp(32))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            addView(TextView(context).apply {
                text = message
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                textSize = 16f
                gravity = Gravity.CENTER
            })
            if (buttonText != null && onButtonClick != null) {
                addView(createPrimaryButton(context, buttonText, onButtonClick).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, context.dp(24), 0, 0)
                    }
                })
            }
        }
    }

    fun createProgressBar(context: Context): ProgressBar {
        return ProgressBar(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            indeterminateTintList = android.content.res.ColorStateList.valueOf(
                ColorPalette.PRIMARY.parseColorSafe()
            )
        }
    }

    /**
     * Creates a section header view for grouping actions
     */
    fun createSectionHeader(context: Context, title: String): TextView {
        return TextView(context).apply {
            text = title
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ColorPalette.PRIMARY.parseColorSafe())
            setPadding(context.dp(8), context.dp(16), context.dp(8), context.dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /**
     * Creates a settings row card
     */
    fun createSettingsRow(
        context: Context,
        title: String,
        subtitle: String = "",
        onClick: (() -> Unit) = {}
    ): LinearLayout {
        val card = GradientDrawable()
        card.cornerRadius = context.dp(14).toFloat()
        card.setColor(ColorPalette.BG_CARD.parseColorSafe())

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = card
            setPadding(context.dp(16), context.dp(14), context.dp(16), context.dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, context.dp(4), 0, context.dp(4))
            }

            val textContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }

            val tvTitle = TextView(context).apply {
                text = title
                setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
                textSize = 15f
            }
            textContainer.addView(tvTitle)

            if (subtitle.isNotEmpty()) {
                val tvSubtitle = TextView(context).apply {
                    text = subtitle
                    setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                    textSize = 12f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = context.dp(2)
                    }
                }
                textContainer.addView(tvSubtitle)
            }

            val arrow = TextView(context).apply {
                text = "\u203A"
                textSize = 22f
                setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                gravity = Gravity.CENTER
            }

            addView(textContainer)
            addView(arrow)

            setOnClickListener {
                try { onClick() } catch (_: Throwable) {}
            }
        }
    }

    /**
     * Creates an action item card with icon circle
     */
    fun createActionCard(
        context: Context,
        icon: String,
        title: String,
        subtitle: String = ""
    ): LinearLayout {
        val card = GradientDrawable()
        card.cornerRadius = context.dp(12).toFloat()
        card.setColor(ColorPalette.BG_CARD.parseColorSafe())

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = card
            setPadding(context.dp(16), context.dp(14), context.dp(16), context.dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, context.dp(4), 0, context.dp(4))
            }

            // Icon circle
            val iconCircle = FrameLayout(context).apply {
                val iconBg = GradientDrawable()
                iconBg.shape = GradientDrawable.OVAL
                iconBg.setColor(ColorPalette.BG_INPUT.parseColorSafe())
                background = iconBg
                setPadding(context.dp(4), context.dp(4), context.dp(4), context.dp(4))
                layoutParams = LinearLayout.LayoutParams(context.dp(44), context.dp(44))
            }

            val iconText = TextView(context).apply {
                text = icon
                textSize = 18f
                setTextColor(ColorPalette.PRIMARY.parseColorSafe())
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            }
            iconCircle.addView(iconText)

            // Text content
            val textContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).apply {
                    marginStart = context.dp(14)
                }
            }

            val nameText = TextView(context).apply {
                text = title
                setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
                textSize = 14f
            }
            textContainer.addView(nameText)

            if (subtitle.isNotEmpty()) {
                val subText = TextView(context).apply {
                    text = subtitle
                    setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                    textSize = 11f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = context.dp(2)
                    }
                }
                textContainer.addView(subText)
            }

            // Arrow
            val arrow = TextView(context).apply {
                text = "\u203A"
                textSize = 20f
                setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                gravity = Gravity.CENTER
            }

            addView(iconCircle)
            addView(textContainer)
            addView(arrow)
        }
    }

    /**
     * Creates a stats/info card for dashboard
     */
    fun createInfoCard(
        context: Context,
        label: String,
        value: String,
        icon: String = ""
    ): LinearLayout {
        val card = GradientDrawable()
        card.cornerRadius = context.dp(12).toFloat()
        card.setColor(ColorPalette.BG_CARD.parseColorSafe())

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = card
            setPadding(context.dp(12), context.dp(12), context.dp(12), context.dp(12))
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )

            if (icon.isNotEmpty()) {
                val iconTv = TextView(context).apply {
                    text = icon
                    textSize = 24f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = context.dp(4)
                    }
                }
                addView(iconTv)
            }

            val valueTv = TextView(context).apply {
                text = value
                setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
            addView(valueTv)

            val labelTv = TextView(context).apply {
                text = label
                setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                textSize = 11f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = context.dp(2)
                }
            }
            addView(labelTv)
        }
    }
}
