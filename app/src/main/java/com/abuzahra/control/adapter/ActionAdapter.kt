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

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvEmoji)
        val tvActionName: TextView = view.findViewById(R.id.tvActionName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_action, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val action = actions[position]
        holder.tvEmoji.text = action.emoji
        holder.tvActionName.text = action.name
        holder.itemView.setOnClickListener { onClick(action) }
    }

    override fun getItemCount() = actions.size
}
