package com.abuzahra.control.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.R

data class ActionItem(val emoji: String, val name: String, val command: String, val params: String = "")

class ActionAdapter(
    private val actions: List<ActionItem>,
    private val onClick: (ActionItem) -> Unit
) : RecyclerView.Adapter<ActionAdapter.VH>() {
    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvEmoji)
        val tvActionName: TextView = view.findViewById(R.id.tvActionName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_action, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val a = actions[pos]
        h.tvEmoji.text = a.emoji
        h.tvActionName.text = a.name
        h.itemView.setOnClickListener { onClick(a) }
    }

    override fun getItemCount() = actions.size
}
