package com.abuzahra.control.ui.dashboard

import android.content.Intent
import android.os.Bundle
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
import com.abuzahra.control.model.Device
import com.abuzahra.control.service.FirebaseService
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var deviceAdapter: DeviceAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        observeDevices()
    }
    
    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(emptyList()) { device ->
            (activity as? MainActivity)?.selectDevice(device)
        }
        binding.rvDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLinkNewDevice.setOnClickListener {
            openLinkActivity()
        }
        binding.btnEmptyLink.setOnClickListener {
            openLinkActivity()
        }
    }
    
    private fun openLinkActivity() {
        startActivity(Intent(requireContext(), DeviceLinkActivity::class.java))
    }
    
    private fun observeDevices() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                FirebaseService.getDevices().collect { devices ->
                    deviceAdapter.updateDevices(devices)
                    
                    if (devices.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvDevices.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvDevices.visibility = View.VISIBLE
                        
                        // Update welcome
                        val userEmail = FirebaseService.currentUserEmail ?: "المدير"
                        binding.tvWelcome.text = "مرحباً، ${userEmail.split("@").first()}"
                        
                        // Auto-select first device if none selected
                        if (MainActivity.selectedDevice == null && devices.isNotEmpty()) {
                            val onlineDevice = devices.firstOrNull { it.isOnline }
                            (activity as? MainActivity)?.selectDevice(onlineDevice ?: devices.first())
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
