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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try {
            inflater.inflate(R.layout.fragment_dashboard, container, false)
        } catch (t: Throwable) {
            Log.e("Dashboard", "inflate error: ${t.javaClass.simpleName}: ${t.message}")
            TextView(requireContext()).apply { text = "خطأ في تحميل الواجهة" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            val rvDevices = view.findViewById<RecyclerView>(R.id.rvDevices)
            val emptyState = view.findViewById<LinearLayout>(R.id.emptyState)
            val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
            val btnLinkNewDevice = view.findViewById<Button>(R.id.btnLinkNewDevice)
            val btnEmptyLink = view.findViewById<Button>(R.id.btnEmptyLink)

            val adapter = DeviceAdapter(emptyList()) { device ->
                try { (activity as? MainActivity)?.selectDevice(device) } catch (_: Throwable) {}
            }
            rvDevices?.layoutManager = LinearLayoutManager(requireContext())
            rvDevices?.adapter = adapter

            try {
                val email = FirebaseService.userEmail
                tvWelcome?.text = if (email != null) "مرحباً، ${email.split("@").firstOrNull()}" else "مرحباً"
            } catch (_: Throwable) {}

            val openLink = View.OnClickListener {
                try { startActivity(Intent(requireContext(), DeviceLinkActivity::class.java)) }
                catch (_: Throwable) {}
            }
            btnLinkNewDevice?.setOnClickListener(openLink)
            btnEmptyLink?.setOnClickListener(openLink)

            try {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        FirebaseService.getDevices().collect { devices ->
                            try {
                                adapter.update(devices)
                                if (devices.isEmpty()) {
                                    emptyState?.visibility = View.VISIBLE
                                    rvDevices?.visibility = View.GONE
                                } else {
                                    emptyState?.visibility = View.GONE
                                    rvDevices?.visibility = View.VISIBLE
                                    if (MainActivity.selectedDevice == null) {
                                        try { (activity as? MainActivity)?.selectDevice(devices.first()) } catch (_: Throwable) {}
                                    }
                                }
                            } catch (t: Throwable) {
                                Log.e("Dashboard", "collect error: ${t.message}")
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.e("Dashboard", "lifecycleScope error: ${t.javaClass.simpleName}: ${t.message}")
            }
        } catch (t: Throwable) {
            Log.e("Dashboard", "onViewCreated CRASH: ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}
