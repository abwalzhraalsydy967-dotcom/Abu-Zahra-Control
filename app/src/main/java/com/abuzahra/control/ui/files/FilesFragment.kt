package com.abuzahra.control.ui.files

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

class FilesFragment : Fragment() {
    private val actions = listOf(
        ActionItem("📂", "تصفح الملفات", "list_files"),
        ActionItem("🖼️", "الصور والمعرض", "get_gallery"),
        ActionItem("🔍", "بحث في الملفات", "search_files"),
        ActionItem("🕐", "الملفات الأخيرة", "recent_files")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try {
            inflater.inflate(R.layout.fragment_generic, container, false)
        } catch (t: Throwable) {
            Log.e("Files", "inflate error: ${t.message}")
            TextView(requireContext()).apply { text = "خطأ" }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            view.findViewById<TextView>(R.id.tvSectionTitle)?.text = "الملفات"
            val rv = view.findViewById<RecyclerView>(R.id.rvActions)
            rv?.layoutManager = LinearLayoutManager(requireContext())
            rv?.adapter = ActionAdapter(actions) { a ->
                try { (activity as? MainActivity)?.sendCommand(a.command) } catch (_: Throwable) {}
            }
        } catch (t: Throwable) {
            Log.e("Files", "onViewCreated CRASH: ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}
