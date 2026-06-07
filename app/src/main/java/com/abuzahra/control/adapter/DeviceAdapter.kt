package com.abuzahra.control.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.databinding.ItemDeviceBinding
import com.abuzahra.control.model.Device

class DeviceAdapter(
    private var devices: List<Device>,
    private val onDeviceClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.VH>() {
    inner class VH(val b: ItemDeviceBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val d = devices[pos]
        h.b.tvDeviceName.text = d.name.ifEmpty { "جهاز بدون اسم" }
        h.b.tvDeviceModel.text = "${d.brand} ${d.model}"
        h.b.tvStatus.text = if (d.isOnline) "متصل" else "غير متصل"
        h.b.statusIndicator.setBackgroundResource(
            if (d.isOnline) com.abuzahra.control.R.drawable.bg_status_online
            else com.abuzahra.control.R.drawable.bg_status_offline
        )
        h.itemView.setOnClickListener { onDeviceClick(d) }
    }

    override fun getItemCount() = devices.size

    fun update(newDevices: List<Device>) { devices = newDevices; notifyDataSetChanged() }
}
