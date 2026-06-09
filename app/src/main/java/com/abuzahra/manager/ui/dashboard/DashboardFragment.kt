package com.abuzahra.manager.ui.dashboard

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.manager.R
import com.abuzahra.manager.adapter.DeviceAdapter
import com.abuzahra.manager.constants.ColorPalette
import com.abuzahra.manager.service.FirebaseManager
import com.abuzahra.manager.ui.device.DeviceLinkActivity
import com.abuzahra.manager.ui.main.MainActivity
import com.abuzahra.manager.util.ViewUtils
import com.abuzahra.manager.util.dp
import com.abuzahra.manager.util.parseColorSafe
import com.abuzahra.manager.util.showToast
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    companion object {
        const val TAG = "DashboardFragment"
    }

    private lateinit var tvWelcome: TextView
    private lateinit var rvDevices: RecyclerView
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var noDeviceView: View
    private val deviceAdapter = DeviceAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return try {
            buildView()
        } catch (e: Exception) {
            Log.e(TAG, "onCreateView error: ${e.message}", e)
            createErrorView("خطأ في تحميل لوحة التحكم")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupViews()
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated error: ${e.message}", e)
        }
    }

    private fun createErrorView(message: String): View {
        val ctx = requireContext()
        return ViewUtils.createEmptyStateView(ctx, message, "إعادة المحاولة") {
            try {
                setupViews()
            } catch (_: Exception) {}
        }
    }

    private fun buildView(): View {
        val ctx = requireContext()
        val scrollView = ViewUtils.createScrollView(ctx)

        val container = ViewUtils.createVerticalLayout(ctx).apply {
            setBackgroundColor(ColorPalette.BG_PRIMARY.parseColorSafe())
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        // ── Welcome Card ──
        val welcomeCard = ViewUtils.createCard(ctx, padding = dp(16))

        tvWelcome = TextView(ctx).apply {
            val email = try { FirebaseManager.userEmail ?: "" } catch (_: Exception) { "" }
            text = "مرحباً"
            setTextColor(ColorPalette.TEXT_PRIMARY.parseColorSafe())
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
        }
        welcomeCard.addView(tvWelcome)

        val tvEmail = TextView(ctx).apply {
            val email = try { FirebaseManager.userEmail ?: "" } catch (_: Exception) { "" }
            text = email
            setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }
        welcomeCard.addView(tvEmail)

        // ── Quick Stats Row ──
        val statsRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        }

        val connectedCard = ViewUtils.createInfoCard(ctx, "متصل", "0", "\uD83D\uDD34")
        val devicesCard = ViewUtils.createInfoCard(ctx, "الأجهزة", "0", "\uD83D\uDCF1")
        val commandsCard = ViewUtils.createInfoCard(ctx, "الأوامر", "0", "\u26A1")

        statsRow.addView(connectedCard)
        statsRow.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(8), 1)
        })
        statsRow.addView(devicesCard)
        statsRow.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(8), 1)
        })
        statsRow.addView(commandsCard)

        // ── Link Device Button ──
        val btnLinkDevice = ViewUtils.createPrimaryButton(ctx, "+ ربط جهاز جديد") {
            try {
                startActivity(Intent(requireContext(), DeviceLinkActivity::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "btnLinkDevice click error: ${e.message}")
                showToast("خطأ في فتح صفحة الربط")
            }
        }

        // ── Section Title: Devices ──
        val sectionTitle = ViewUtils.createSectionHeader(ctx, "الأجهزة المرتبطة")

        // ── RecyclerView ──
        rvDevices = RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = deviceAdapter
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        }

        // ── Empty State ──
        emptyStateContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(48), dp(32), dp(32))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE

            val emptyIcon = TextView(ctx).apply {
                text = "\uD83D\uDCF1"
                textSize = 48f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val emptyMsg = TextView(ctx).apply {
                text = "لا توجد أجهزة مرتبطة بعد"
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                textSize = 15f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(16)
                }
            }

            val emptyHint = TextView(ctx).apply {
                text = "قم بتثبيت التطبيق المستهدف على الجهاز واربطه باستخدام الكود"
                setTextColor(ColorPalette.TEXT_HINT.parseColorSafe())
                textSize = 12f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                }
            }

            val emptyBtn = ViewUtils.createPrimaryButton(ctx, "ربط جهاز الآن") {
                try {
                    startActivity(Intent(requireContext(), DeviceLinkActivity::class.java))
                } catch (e: Exception) {
                    Log.e(TAG, "emptyBtn click error: ${e.message}")
                }
            }

            addView(emptyIcon)
            addView(emptyMsg)
            addView(emptyHint)
            addView(emptyBtn)
        }

        // ── No device selected view ──
        noDeviceView = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(24), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val warnText = TextView(ctx).apply {
                text = "\u26A0 لم يتم اختيار جهاز"
                setTextColor(ColorPalette.WARNING.parseColorSafe())
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
            addView(warnText)
        }

        // Assemble
        container.addView(welcomeCard)
        container.addView(statsRow)
        container.addView(btnLinkDevice)
        container.addView(sectionTitle)
        container.addView(noDeviceView)
        container.addView(rvDevices)
        container.addView(emptyStateContainer)

        scrollView.addView(container)
        return scrollView
    }

    private fun setupViews() {
        try {
            // Update welcome text
            val email = try { FirebaseManager.userEmail ?: "" } catch (_: Exception) { "" }
            tvWelcome.text = "مرحباً، $email"

            // Device click listener
            deviceAdapter.onDeviceClick = { device ->
                try {
                    (activity as? MainActivity)?.selectDevice(device)
                    showToast("تم اختيار: ${device.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Device click error: ${e.message}")
                }
            }

            // Observe devices
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    try {
                        FirebaseManager.getDevices().collect { devices ->
                            try {
                                deviceAdapter.submitList(devices)
                                if (devices.isEmpty()) {
                                    rvDevices.visibility = View.GONE
                                    noDeviceView.visibility = View.GONE
                                    emptyStateContainer.visibility = View.VISIBLE
                                } else {
                                    rvDevices.visibility = View.VISIBLE
                                    emptyStateContainer.visibility = View.GONE

                                    val selected = MainActivity.selectedDevice
                                    noDeviceView.visibility = if (selected != null) View.GONE else View.VISIBLE

                                    // Auto-select first device if none selected
                                    if (MainActivity.selectedDevice == null && devices.isNotEmpty()) {
                                        (activity as? MainActivity)?.selectDevice(devices[0])
                                        noDeviceView.visibility = View.GONE
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Device list update error: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Flow collection error: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupViews error: ${e.message}", e)
        }
    }
}
