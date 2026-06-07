package com.abuzahra.control.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.databinding.ItemActionBinding

data class ActionItem(val emoji: String, val name: String, val command: String, val params: String = "")

class ActionAdapter(
    private val actions: List<ActionItem>,
    private val onClick: (ActionItem) -> Unit
) : RecyclerView.Adapter<ActionAdapter.VH>() {
    inner class VH(val b: ItemActionBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val a = actions[pos]
        h.b.tvEmoji.text = a.emoji
        h.b.tvActionName.text = a.name
        h.itemView.setOnClickListener { onClick(a) }
    }

    override fun getItemCount() = actions.size
}
