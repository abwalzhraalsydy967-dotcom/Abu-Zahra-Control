package com.abuzahra.control.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.abuzahra.control.DeviceLinkActivity
import com.abuzahra.control.LoginActivity
import com.abuzahra.control.R
import com.abuzahra.control.service.FirebaseService

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            val tvEmail: TextView = view.findViewById(R.id.tvEmail)
            val tvUid: TextView = view.findViewById(R.id.tvUid)
            val btnLinkDevice: LinearLayout = view.findViewById(R.id.btnLinkDevice)
            val btnAppInfo: LinearLayout = view.findViewById(R.id.btnAppInfo)
            val btnLogout: Button = view.findViewById(R.id.btnLogout)

            tvEmail.text = FirebaseService.userEmail ?: "غير مسجل"
            tvUid.text = "ID: ${FirebaseService.userId?.take(12) ?: "N/A"}"

            btnLinkDevice.setOnClickListener {
                try { startActivity(Intent(requireContext(), DeviceLinkActivity::class.java)) } catch (_: Exception) {}
            }
            btnAppInfo.setOnClickListener {
                Toast.makeText(requireContext(), "Abu Zahra Control v1.5", Toast.LENGTH_LONG).show()
            }
            btnLogout.setOnClickListener {
                try {
                    FirebaseService.signOut()
                    Toast.makeText(requireContext(), "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    activity?.finishAffinity()
                } catch (e: Exception) { Log.e("Settings", "Logout: ${e.message}") }
            }
        } catch (e: Exception) { Log.e("Settings", "onViewCreated: ${e.message}") }
    }
}
