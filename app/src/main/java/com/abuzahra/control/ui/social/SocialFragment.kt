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
        return inflater.inflate(R.layout.fragment_generic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            val tvSectionTitle: TextView = view.findViewById(R.id.tvSectionTitle)
            val rvActions: RecyclerView = view.findViewById(R.id.rvActions)
            tvSectionTitle.text = "💬 التطبيقات الاجتماعية"
            rvActions.layoutManager = LinearLayoutManager(requireContext())
            rvActions.adapter = ActionAdapter(actions) { a ->
                (activity as? MainActivity)?.sendCommand(a.command)
            }
        } catch (e: Exception) { Log.e("Social", "onViewCreated: ${e.message}") }
    }
}
