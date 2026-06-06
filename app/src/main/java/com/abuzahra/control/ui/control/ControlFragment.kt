package com.abuzahra.control.ui.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.abuzahra.control.MainActivity
import com.abuzahra.control.adapter.ActionAdapter
import com.abuzahra.control.adapter.ActionItem
import com.abuzahra.control.databinding.FragmentControlBinding
import com.abuzahra.control.service.FirebaseService
import com.abuzahra.control.model.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ControlFragment : Fragment() {
    
    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!
    private var devices: List<Device> = emptyList()
    
    private val deviceActions = listOf(
        ActionItem("📡", "معلومات الجهاز", "get_info"),
        ActionItem("🔋", "البطارية", "get_battery"),
        ActionItem("📍", "الموقع", "get_location"),
        ActionItem("📱", "معلومات WiFi", "get_wifi_info"),
        ActionItem("📶", "معلومات الشبكة", "get_network_info"),
        ActionItem("💾", "معلومات التخزين", "get_storage_info"),
        ActionItem("📱", "معلومات SIM", "get_sim_info"),
        ActionItem("📋", "التقويم", "get_calendar"),
        ActionItem("📦", "التطبيقات المثبتة", "get_installed_apps"),
        ActionItem("⚙️", "التطبيقات المشغلة", "get_running_apps"),
    )
    
    private val cameraActions = listOf(
        ActionItem("📸", "لقطة شاشة", "screenshot"),
        ActionItem("📷", "الكاميرا الأمامية", "front_camera"),
        ActionItem("📹", "الكاميرا الخلفية", "back_camera"),
    )
    
    private val networkActions = listOf(
        ActionItem("📶", "تشغيل WiFi", "enable_wifi"),
        ActionItem("📡", "إيقاف WiFi", "disable_wifi"),
        ActionItem("🔵", "تشغيل البلوتوث", "enable_bluetooth"),
        ActionItem("🔵", "إيقاف البلوتوث", "disable_bluetooth"),
        ActionItem("✈️", "وضع الطيران", "airplane_on"),
        ActionItem("✈️", "إلغاء الطيران", "airplane_off"),
    )
    
    private val mediaActions = listOf(
        ActionItem("🔔", "تشغيل الرنين", "ring"),
        ActionItem("📳", "اهتزاز", "vibrate"),
        ActionItem("🔊", "تشغيل صوت", "play_sound"),
        ActionItem("🗣️", "نطق نص", "speak_text"),
        ActionItem("🔔", "إشعار تجريبي", "show_notification"),
        ActionItem("🔦", "تشغيل المصباح", "torch_on"),
        ActionItem("🔦", "إيقاف المصباح", "torch_off"),
        ActionItem("🔒", "قفل الهاتف", "lock_phone"),
        ActionItem("🔄", "إعادة تشغيل", "reboot"),
    )
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupActions()
        observeDevices()
    }
    
    private fun setupActions() {
        fun setupRecycler(rv: androidx.recyclerview.widget.RecyclerView, actions: List<ActionItem>) {
            rv.layoutManager = LinearLayoutManager(requireContext())
            rv.adapter = ActionAdapter(actions) { action ->
                sendCommand(action.command, action.params)
            }
        }
        
        setupRecycler(binding.rvDeviceActions, deviceActions)
        setupRecycler(binding.rvCameraActions, cameraActions)
        setupRecycler(binding.rvNetworkActions, networkActions)
        setupRecycler(binding.rvMediaActions, mediaActions)
    }
    
    private fun sendCommand(command: String, params: String = "") {
        (activity as? MainActivity)?.sendCommandToDevice(command, params)
    }
    
    private fun observeDevices() {
        CoroutineScope(Dispatchers.Main).launch {
            FirebaseService.getDevices().collect { deviceList ->
                devices = deviceList
                val names = deviceList.map { it.name.ifEmpty { it.model } }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerDevice.adapter = adapter
                
                // Auto-select matching device
                val selected = MainActivity.selectedDevice
                if (selected != null) {
                    val idx = deviceList.indexOfFirst { it.id == selected.id }
                    if (idx >= 0) binding.spinnerDevice.setSelection(idx)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
