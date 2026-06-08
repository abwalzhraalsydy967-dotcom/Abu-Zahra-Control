package com.abuzahra.control.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abuzahra.control.R
import com.abuzahra.control.model.Device

class DeviceAdapter(
    private var devices: List<Device>,
    private val onDeviceClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvDeviceName: TextView? = view.findViewById(R.id.tvDeviceName)
        val tvDeviceModel: TextView? = view.findViewById(R.id.tvDeviceModel)
        val tvStatus: TextView? = view.findViewById(R.id.tvStatus)
        val statusIndicator: View? = view.findViewById(R.id.statusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val device = devices[position]
        holder.tvDeviceName?.text = device.name.ifEmpty { "جهاز" }
        holder.tvDeviceModel?.text = device.brand.ifEmpty { "" }
        holder.tvStatus?.text = device.statusText
        holder.statusIndicator?.setBackgroundResource(
            if (device.isOnline) R.drawable.bg_status_online else R.drawable.bg_status_offline
        )
        holder.itemView.setOnClickListener { onDeviceClick(device) }
    }

    override fun getItemCount() = devices.size

    fun update(newDevices: List<Device>) {
        devices = newDevices
        notifyDataSetChanged()
    }
}
