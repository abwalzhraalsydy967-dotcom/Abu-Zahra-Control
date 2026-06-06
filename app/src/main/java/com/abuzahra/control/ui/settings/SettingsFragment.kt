package com.abuzahra.control.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.abuzahra.control.DeviceLinkActivity
import com.abuzahra.control.LoginActivity
import com.abuzahra.control.R
import com.abuzahra.control.databinding.FragmentSettingsBinding
import com.abuzahra.control.service.FirebaseService

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvEmail.text = FirebaseService.currentUser?.email ?: "غير مسجل"
        binding.tvUid.text = "UID: ${FirebaseService.currentUser?.uid?.take(16) ?: "N/A"}..."
        
        binding.btnLinkDevice.setOnClickListener {
            startActivity(Intent(requireContext(), DeviceLinkActivity::class.java))
        }
        
        binding.btnAppInfo.setOnClickListener {
            Toast.makeText(requireContext(), "Abu Zahra Control v1.0\nلوحة التحكم الاحترافية", Toast.LENGTH_LONG).show()
        }
        
        binding.btnLogout.setOnClickListener {
            FirebaseService.signOut()
            Toast.makeText(requireContext(), "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finishAffinity()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
