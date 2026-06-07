package com.abuzahra.control.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try { _b = FragmentSettingsBinding.inflate(inflater, container, false); b.root }
        catch (e: Exception) { Log.e("Settings", "Error: ${e.message}"); null }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (_b == null) return
        try {
            b.tvEmail.text = FirebaseService.userEmail ?: "غير مسجل"
            b.tvUid.text = "ID: ${FirebaseService.userId?.take(12) ?: "N/A"}"
        } catch (_: Exception) {}

        b.btnLinkDevice.setOnClickListener {
            try { startActivity(Intent(requireContext(), DeviceLinkActivity::class.java)) } catch (_: Exception) {}
        }
        b.btnAppInfo.setOnClickListener {
            Toast.makeText(requireContext(), "Abu Zahra Control v1.2", Toast.LENGTH_LONG).show()
        }
        b.btnLogout.setOnClickListener {
            try {
                FirebaseService.signOut()
                Toast.makeText(requireContext(), "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finishAffinity()
            } catch (e: Exception) { Log.e("Settings", "Logout error: ${e.message}") }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
