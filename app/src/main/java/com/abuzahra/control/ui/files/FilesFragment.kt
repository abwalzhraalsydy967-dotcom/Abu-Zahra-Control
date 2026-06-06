package com.abuzahra.control.ui.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.abuzahra.control.MainActivity
import com.abuzahra.control.adapter.ActionAdapter
import com.abuzahra.control.adapter.ActionItem
import com.abuzahra.control.databinding.FragmentFilesBinding
import com.abuzahra.control.model.Device
import com.abuzahra.control.service.FirebaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilesFragment : Fragment() {
    
    private var _binding: FragmentFilesBinding? = null
    private val binding get() = _binding!!
    private var devices: List<Device> = emptyList()
    
    private val actions = listOf(
        ActionItem("📂", "تصفح الملفات", "list_files"),
        ActionItem("🖼️", "الصور والمعرض", "get_gallery"),
        ActionItem("🔍", "بحث في الملفات", "search_files"),
        ActionItem("🕐", "الملفات الأخيرة", "recent_files"),
    )
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFilesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.rvActions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActions.adapter = ActionAdapter(actions) { action ->
            (activity as? MainActivity)?.sendCommandToDevice(action.command)
        }
        
        observeDevices()
    }
    
    private fun observeDevices() {
        CoroutineScope(Dispatchers.Main).launch {
            FirebaseService.getDevices().collect { deviceList ->
                devices = deviceList
                val names = deviceList.map { it.name.ifEmpty { it.model } }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerDevice.adapter = adapter
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
