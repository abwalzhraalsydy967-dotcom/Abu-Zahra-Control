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
    private var rvDevices: RecyclerView? = null
    private var emptyState: LinearLayout? = null
    private var tvWelcome: TextView? = null
    private var adapter: DeviceAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            rvDevices = view.findViewById(R.id.rvDevices)
            emptyState = view.findViewById(R.id.emptyState)
            tvWelcome = view.findViewById(R.id.tvWelcome)
            val btnLinkNewDevice: Button = view.findViewById(R.id.btnLinkNewDevice)
            val btnEmptyLink: Button = view.findViewById(R.id.btnEmptyLink)

            adapter = DeviceAdapter(emptyList()) { device ->
                (activity as? MainActivity)?.selectDevice(device)
            }
            rvDevices?.layoutManager = LinearLayoutManager(requireContext())
            rvDevices?.adapter = adapter

            btnLinkNewDevice.setOnClickListener { openLink() }
            btnEmptyLink.setOnClickListener { openLink() }

            try {
                val email = FirebaseService.userEmail
                tvWelcome?.text = if (email != null) "مرحباً، ${email.split("@").first()}" else "مرحباً"
            } catch (_: Exception) {}

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    FirebaseService.getDevices().collect { devices ->
                        try {
                            adapter?.update(devices)
                            if (devices.isEmpty()) {
                                emptyState?.visibility = View.VISIBLE
                                rvDevices?.visibility = View.GONE
                            } else {
                                emptyState?.visibility = View.GONE
                                rvDevices?.visibility = View.VISIBLE
                                if (MainActivity.selectedDevice == null && devices.isNotEmpty()) {
                                    (activity as? MainActivity)?.selectDevice(devices.first())
                                }
                            }
                        } catch (e: Exception) { Log.e("Dash", "Update: ${e.message}") }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Dash", "onViewCreated: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun openLink() {
        try { startActivity(Intent(requireContext(), DeviceLinkActivity::class.java)) } catch (_: Exception) {}
    }
}
