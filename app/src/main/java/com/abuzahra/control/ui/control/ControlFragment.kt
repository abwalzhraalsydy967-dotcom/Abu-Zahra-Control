package com.abuzahra.control.ui.control

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.MainActivity
import com.abuzahra.control.R
import com.abuzahra.control.adapter.ActionAdapter
import com.abuzahra.control.adapter.ActionItem

class ControlFragment : Fragment() {
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
            inflater.inflate(R.layout.fragment_generic, container, false)
        } catch (t: Throwable) {
            Log.e("Control", "inflate error: ${t.message}")
            TextView(requireContext()).apply { text = "خطأ" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            view.findViewById<TextView>(R.id.tvSectionTitle)?.text = "التحكم بالجهاز"
            val rv = view.findViewById<RecyclerView>(R.id.rvActions)
            rv?.layoutManager = LinearLayoutManager(requireContext())
            rv?.adapter = ActionAdapter(actions) { a ->
                try { (activity as? MainActivity)?.sendCommand(a.command, a.params) } catch (_: Throwable) {}
            }
        } catch (t: Throwable) {
            Log.e("Control", "onViewCreated CRASH: ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}
