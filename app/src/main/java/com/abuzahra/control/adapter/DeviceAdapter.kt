package com.abuzahra.control.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.R
import com.abuzahra.control.databinding.ItemDeviceBinding
import com.abuzahra.control.model.Device

class DeviceAdapter(
    private var devices: List<Device>,
    private val onDeviceClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    
    inner class DeviceViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        
        holder.binding.tvDeviceName.text = device.name.ifEmpty { "جهاز بدون اسم" }
        holder.binding.tvDeviceModel.text = "${device.brand} ${device.model}"
        holder.binding.tvDeviceLastSeen.text = device.lastSeenText
        holder.binding.tvStatus.text = if (device.isOnline) "متصل" else "غير متصل"
        
        holder.binding.statusIndicator.setBackgroundResource(
            if (device.isOnline) R.drawable.bg_status_online
            else R.drawable.bg_status_offline
        )
        
        holder.itemView.setOnClickListener { onDeviceClick(device) }
    }
    
    override fun getItemCount() = devices.size
    
    fun updateDevices(newDevices: List<Device>) {
        devices = newDevices
        notifyDataSetChanged()
    }
}
