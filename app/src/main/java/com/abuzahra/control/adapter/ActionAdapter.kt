package com.abuzahra.control.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.databinding.ItemActionButtonBinding

data class ActionItem(
    val emoji: String,
    val name: String,
    val command: String,
    val params: String = ""
)

class ActionAdapter(
    private val actions: List<ActionItem>,
    private val onActionClick: (ActionItem) -> Unit
) : RecyclerView.Adapter<ActionAdapter.ActionViewHolder>() {
    
    inner class ActionViewHolder(val binding: ItemActionButtonBinding) : RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val binding = ItemActionButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        val action = actions[position]
        
        holder.binding.tvEmoji.text = action.emoji
        holder.binding.tvActionName.text = action.name
        
        holder.binding.actionRoot.setOnClickListener { onActionClick(action) }
    }
    
    override fun getItemCount() = actions.size
}
