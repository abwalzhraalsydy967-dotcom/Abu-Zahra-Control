package com.abuzahra.control.ui.dashboard

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
import com.abuzahra.control.R
import com.abuzahra.control.adapter.DeviceAdapter
import com.abuzahra.control.constants.ColorPalette
import com.abuzahra.control.data.model.Device
import com.abuzahra.control.service.FirebaseManager
import com.abuzahra.control.ui.device.DeviceLinkActivity
import com.abuzahra.control.ui.main.MainActivity
import com.abuzahra.control.util.ViewUtils
import com.abuzahra.control.util.dp
import com.abuzahra.control.util.parseColorSafe
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    companion object {
        const val TAG = "DashboardFragment"
    }

    private lateinit var tvWelcome: TextView
    private lateinit var btnLinkNewDevice: View
    private lateinit var rvDevices: RecyclerView
    private lateinit var emptyStateContainer: LinearLayout
    private val deviceAdapter = DeviceAdapter()

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
            setupViews()
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

        // Welcome text
        tvWelcome = TextView(ctx).apply {
            val email = try { FirebaseManager.userEmail ?: "" } catch (_: Exception) { "" }
            text = "مرحباً، $email"
            setTextColor(ColorPalette.PRIMARY.parseColorSafe())
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Link new device button
        btnLinkNewDevice = ViewUtils.createPrimaryButton(ctx, "ربط جهاز") {
            try {
                startActivity(Intent(requireContext(), DeviceLinkActivity::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "btnLinkNewDevice click error: ${e.message}")
            }
        }

        // Section title
        val sectionTitle = ViewUtils.createTitleText(
            ctx,
            "الأجهزة المرتبطة",
            sizeSp = 16f,
            color = ColorPalette.TEXT_PRIMARY
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        }

        // RecyclerView
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

        // Empty state container
        emptyStateContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(48), dp(32), dp(32))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE

            val emptyMsg = TextView(ctx).apply {
                text = "لا توجد أجهزة مرتبطة بعد"
                setTextColor(ColorPalette.TEXT_SECONDARY.parseColorSafe())
                textSize = 15f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val emptyBtn = ViewUtils.createPrimaryButton(ctx, "ربط جهاز") {
                try {
                    startActivity(Intent(requireContext(), DeviceLinkActivity::class.java))
                } catch (e: Exception) {
                    Log.e(TAG, "emptyBtn click error: ${e.message}")
                }
            }

            addView(emptyMsg)
            addView(emptyBtn)
        }

        container.addView(tvWelcome)
        container.addView(btnLinkNewDevice)
        container.addView(sectionTitle)
        container.addView(rvDevices)
        container.addView(emptyStateContainer)

        scrollView.addView(container)
        return scrollView
    }

    private fun setupViews() {
        try {
            // Update welcome with email
            val email = try { FirebaseManager.userEmail ?: "" } catch (_: Exception) { "" }
            tvWelcome.text = "مرحباً، $email"

            // Device click listener
            deviceAdapter.onDeviceClick = { device ->
                try {
                    (activity as? MainActivity)?.selectDevice(device)
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
                                    emptyStateContainer.visibility = View.VISIBLE
                                } else {
                                    rvDevices.visibility = View.VISIBLE
                                    emptyStateContainer.visibility = View.GONE
                                    // Auto-select first device if none selected
                                    if (MainActivity.selectedDevice == null && devices.isNotEmpty()) {
                                        (activity as? MainActivity)?.selectDevice(devices[0])
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
            Log.e(TAG, "setupViews error: ${e.message}")
        }
    }
}
