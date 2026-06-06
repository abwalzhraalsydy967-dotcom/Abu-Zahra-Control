package com.abuzahra.control

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.databinding.ActivityDeviceLinkBinding
import com.abuzahra.control.service.FirebaseService

class DeviceLinkActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDeviceLinkBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.ivBack.setOnClickListener { finish() }
        binding.btnLink.setOnClickListener { linkDevice() }
    }
    
    private fun linkDevice() {
        val code = binding.etCode.text.toString().trim().uppercase()
        
        if (code.isEmpty()) {
            Toast.makeText(this, "أدخل كود الربط", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.btnLink.isEnabled = false
        binding.btnLink.text = "جاري التحقق..."
        binding.tvStatus.text = ""
        
        FirebaseService.linkDevice(code) { success, message ->
            binding.btnLink.isEnabled = true
            binding.btnLink.text = getString(R.string.link_device)
            
            if (success) {
                binding.tvStatus.setTextColor(getColor(R.color.success))
                binding.tvStatus.text = message
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                binding.etCode.text?.clear()
                
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1500)
            } else {
                binding.tvStatus.setTextColor(getColor(R.color.error))
                binding.tvStatus.text = message
            }
        }
    }
}
