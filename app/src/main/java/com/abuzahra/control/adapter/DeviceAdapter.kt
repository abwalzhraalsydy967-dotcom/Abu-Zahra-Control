package com.abuzahra.control.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.R
import com.abuzahra.control.model.Device

class DeviceAdapter(
    private var devices: List<Device>,
    private val onDeviceClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.VH>() {
    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val tvDeviceName: TextView = view.findViewById(R.id.tvDeviceName)
        val tvDeviceModel: TextView = view.findViewById(R.id.tvDeviceModel)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val statusIndicator: ImageView = view.findViewById(R.id.statusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val d = devices[pos]
        h.tvDeviceName.text = d.name.ifEmpty { "جهاز بدون اسم" }
        h.tvDeviceModel.text = "${d.brand} ${d.model}"
        h.tvStatus.text = if (d.isOnline) "متصل" else "غير متصل"
        h.statusIndicator.setBackgroundResource(
            if (d.isOnline) R.drawable.bg_status_online else R.drawable.bg_status_offline
        )
        h.itemView.setOnClickListener { onDeviceClick(d) }
    }

    override fun getItemCount() = devices.size

    fun update(newDevices: List<Device>) { devices = newDevices; notifyDataSetChanged() }
}
