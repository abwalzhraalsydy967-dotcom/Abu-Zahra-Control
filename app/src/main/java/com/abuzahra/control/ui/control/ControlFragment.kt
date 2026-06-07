package com.abuzahra.control.ui.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.abuzahra.control.MainActivity
import com.abuzahra.control.R
import com.abuzahra.control.adapter.ActionAdapter
import com.abuzahra.control.adapter.ActionItem
import com.abuzahra.control.databinding.FragmentGenericBinding

class ControlFragment : Fragment() {
    private var _b: FragmentGenericBinding? = null
    private val b get() = _b!!

    private val actions = listOf(
        ActionItem("📡", "معلومات الجهاز", "get_info"),
        ActionItem("🔋", "البطارية", "get_battery"),
        ActionItem("📍", "الموقع", "get_location"),
        ActionItem("📶", "معلومات WiFi", "get_wifi_info"),
        ActionItem("📶", "معلومات الشبكة", "get_network_info"),
        ActionItem("💾", "معلومات التخزين", "get_storage_info"),
        ActionItem("📱", "معلومات SIM", "get_sim_info"),
        ActionItem("📦", "التطبيقات المثبتة", "get_installed_apps"),
        ActionItem("⚙️", "التطبيقات المشغلة", "get_running_apps"),
        ActionItem("📸", "لقطة شاشة", "screenshot"),
        ActionItem("📷", "الكاميرا الأمامية", "front_camera"),
        ActionItem("📹", "الكاميرا الخلفية", "back_camera"),
        ActionItem("📶", "تشغيل WiFi", "enable_wifi"),
        ActionItem("📡", "إيقاف WiFi", "disable_wifi"),
        ActionItem("🔵", "تشغيل البلوتوث", "enable_bluetooth"),
        ActionItem("🔵", "إيقاف البلوتوث", "disable_bluetooth"),
        ActionItem("✈️", "وضع الطيران", "airplane_on"),
        ActionItem("✈️", "إلغاء الطيران", "airplane_off"),
        ActionItem("🔔", "تشغيل الرنين", "ring"),
        ActionItem("📳", "اهتزاز", "vibrate"),
        ActionItem("🔊", "تشغيل صوت", "play_sound"),
        ActionItem("🗣️", "نطق نص", "speak_text"),
        ActionItem("🔔", "إشعار تجريبي", "show_notification"),
        ActionItem("🔦", "تشغيل المصباح", "torch_on"),
        ActionItem("🔦", "إيقاف المصباح", "torch_off"),
        ActionItem("🔒", "قفل الهاتف", "lock_phone"),
        ActionItem("🔄", "إعادة تشغيل", "reboot"),
        ActionItem("📋", "التقويم", "get_calendar")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try {
            _b = FragmentGenericBinding.inflate(inflater, container, false)
            b.root
        } catch (e: Exception) {
            Log.e("Control", "Error: ${e.message}")
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (_b == null) return
        b.tvSectionTitle.text = "⚙️ التحكم بالجهاز"
        b.rvActions.layoutManager = LinearLayoutManager(requireContext())
        b.rvActions.adapter = ActionAdapter(actions) { a ->
            (activity as? MainActivity)?.sendCommand(a.command, a.params)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
