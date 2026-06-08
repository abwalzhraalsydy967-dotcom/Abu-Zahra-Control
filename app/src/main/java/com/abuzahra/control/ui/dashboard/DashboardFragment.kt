package com.abuzahra.control.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.DeviceLinkActivity
import com.abuzahra.control.MainActivity
import com.abuzahra.control.R
import com.abuzahra.control.adapter.DeviceAdapter
import com.abuzahra.control.service.FirebaseService
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Dashboard", "onViewCreated START")

        val rvDevices = view.findViewById<RecyclerView>(R.id.rvDevices)
        val emptyState = view.findViewById<LinearLayout>(R.id.emptyState)
        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val btnLinkNewDevice = view.findViewById<Button>(R.id.btnLinkNewDevice)
        val btnEmptyLink = view.findViewById<Button>(R.id.btnEmptyLink)

        Log.d("Dashboard", "All views found OK")

        val adapter = DeviceAdapter(emptyList()) { device ->
            (activity as? MainActivity)?.selectDevice(device)
        }
        rvDevices.layoutManager = LinearLayoutManager(requireContext())
        rvDevices.adapter = adapter

        // Set welcome text
        val email = FirebaseService.userEmail
        tvWelcome.text = if (email != null) "مرحباً، ${email.split("@").firstOrNull()}" else "مرحباً"

        // Link buttons
        val openLink = {
            try { startActivity(Intent(requireContext(), DeviceLinkActivity::class.java)) }
            catch (_: Exception) {}
        }
        btnLinkNewDevice.setOnClickListener(openLink)
        btnEmptyLink.setOnClickListener(openLink)

        // Observe devices
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                FirebaseService.getDevices().collect { devices ->
                    Log.d("Dashboard", "Got ${devices.size} devices")
                    adapter.update(devices)
                    if (devices.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        rvDevices.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        rvDevices.visibility = View.VISIBLE
                        if (MainActivity.selectedDevice == null) {
                            (activity as? MainActivity)?.selectDevice(devices.first())
                        }
                    }
                }
            }
        }

        Log.d("Dashboard", "onViewCreated COMPLETE")
    }
}
