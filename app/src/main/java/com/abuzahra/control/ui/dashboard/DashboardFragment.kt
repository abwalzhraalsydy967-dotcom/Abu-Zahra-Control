package com.abuzahra.control.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.abuzahra.control.DeviceLinkActivity
import com.abuzahra.control.MainActivity
import com.abuzahra.control.R
import com.abuzahra.control.adapter.DeviceAdapter
import com.abuzahra.control.databinding.FragmentDashboardBinding
import com.abuzahra.control.service.FirebaseService
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return try {
            _b = FragmentDashboardBinding.inflate(inflater, container, false)
            b.root
        } catch (e: Exception) {
            Log.e("Dash", "Inflate error: ${e.message}")
            inflater.inflate(R.layout.fragment_dashboard, container, false).also { _b = null }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (_b == null) return

        val adapter = DeviceAdapter(emptyList()) { device ->
            (activity as? MainActivity)?.selectDevice(device)
        }
        b.rvDevices.layoutManager = LinearLayoutManager(requireContext())
        b.rvDevices.adapter = adapter

        b.btnLinkNewDevice.setOnClickListener { openLink() }
        b.btnEmptyLink.setOnClickListener { openLink() }

        // Set welcome text
        try {
            val email = FirebaseService.userEmail
            b.tvWelcome.text = if (email != null) "مرحباً، ${email.split("@").first()}" else "مرحباً"
        } catch (_: Exception) {}

        // Observe devices
        try {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    FirebaseService.getDevices().collect { devices ->
                        try {
                            adapter.update(devices)
                            if (devices.isEmpty()) {
                                b.emptyState.visibility = View.VISIBLE
                                b.rvDevices.visibility = View.GONE
                            } else {
                                b.emptyState.visibility = View.GONE
                                b.rvDevices.visibility = View.VISIBLE
                                if (MainActivity.selectedDevice == null) {
                                    (activity as? MainActivity)?.selectDevice(devices.first())
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Dash", "Update error: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Dash", "Collect error: ${e.message}")
        }
    }

    private fun openLink() {
        try { startActivity(Intent(requireContext(), DeviceLinkActivity::class.java)) }
        catch (_: Exception) {}
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
