package com.abuzahra.control.ui.files

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

class FilesFragment : Fragment() {
    private var _b: FragmentGenericBinding? = null
    private val b get() = _b!!

    private val actions = listOf(
        ActionItem("📂", "تصفح الملفات", "list_files"),
        ActionItem("🖼️", "الصور والمعرض", "get_gallery"),
        ActionItem("🔍", "بحث في الملفات", "search_files"),
        ActionItem("🕐", "الملفات الأخيرة", "recent_files")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return try { _b = FragmentGenericBinding.inflate(inflater, container, false); b.root }
        catch (e: Exception) { Log.e("Files", "Error: ${e.message}"); null }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (_b == null) return
        b.tvSectionTitle.text = "📁 الملفات"
        b.rvActions.layoutManager = LinearLayoutManager(requireContext())
        b.rvActions.adapter = ActionAdapter(actions) { a ->
            (activity as? MainActivity)?.sendCommand(a.command)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
