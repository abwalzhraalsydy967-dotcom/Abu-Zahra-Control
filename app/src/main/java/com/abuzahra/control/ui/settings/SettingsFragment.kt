package com.abuzahra.control.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.abuzahra.control.DeviceLinkActivity
import com.abuzahra.control.LoginActivity
import com.abuzahra.control.R
import com.abuzahra.control.service.FirebaseService

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try {
            inflater.inflate(R.layout.fragment_settings, container, false)
        } catch (e: Exception) {
            Log.e("Settings", "inflate error: ${e.message}")
            TextView(requireContext()).apply { text = "خطأ" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
            val tvUid = view.findViewById<TextView>(R.id.tvUid)
            val btnLinkDevice = view.findViewById<TextView>(R.id.btnLinkDevice)
            val btnAppInfo = view.findViewById<TextView>(R.id.btnAppInfo)
            val btnLogout = view.findViewById<Button>(R.id.btnLogout)

            try {
                tvEmail?.text = FirebaseService.userEmail ?: "غير مسجل"
                tvUid?.text = "ID: ${FirebaseService.userId?.take(12) ?: "N/A"}"
            } catch (_: Exception) {}

            btnLinkDevice?.setOnClickListener {
                try { startActivity(Intent(requireContext(), DeviceLinkActivity::class.java)) }
                catch (_: Exception) {}
            }

            btnAppInfo?.setOnClickListener {
                Toast.makeText(requireContext(), "Abu Zahra Control v2.1", Toast.LENGTH_LONG).show()
            }

            btnLogout?.setOnClickListener {
                try {
                    FirebaseService.signOut()
                    Toast.makeText(requireContext(), "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    activity?.finishAffinity()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Settings", "onViewCreated error: ${e.message}")
        }
    }
}
