package com.abuzahra.control.ui.social

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

class SocialFragment : Fragment() {
    private var _b: FragmentGenericBinding? = null
    private val b get() = _b!!

    private val actions = listOf(
        ActionItem("💚", "واتساب", "get_whatsapp"),
        ActionItem("🔵", "تليجرام", "get_telegram"),
        ActionItem("🟣", "إنستجرام", "get_instagram"),
        ActionItem("💬", "ماسنجر", "get_messenger"),
        ActionItem("💛", "سناب شات", "get_snapchat"),
        ActionItem("🎵", "تيك توك", "get_tiktok"),
        ActionItem("🐦", "تويتر / X", "get_twitter"),
        ActionItem("💜", "فايبر", "get_viber"),
        ActionItem("🔵", "سيجنال", "get_signal"),
        ActionItem("🔵", "فيسبوك", "get_facebook")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try { _b = FragmentGenericBinding.inflate(inflater, container, false); b.root }
        catch (e: Exception) { Log.e("Social", "Error: ${e.message}"); null }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (_b == null) return
        b.tvSectionTitle.text = "💬 التطبيقات الاجتماعية"
        b.rvActions.layoutManager = LinearLayoutManager(requireContext())
        b.rvActions.adapter = ActionAdapter(actions) { a ->
            (activity as? MainActivity)?.sendCommand(a.command)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
