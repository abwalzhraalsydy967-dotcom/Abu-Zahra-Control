package com.abuzahra.control

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.databinding.ActivityDeviceLinkBinding
import com.abuzahra.control.service.FirebaseService

class DeviceLinkActivity : AppCompatActivity() {
    private lateinit var b: ActivityDeviceLinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            b = ActivityDeviceLinkBinding.inflate(layoutInflater)
            setContentView(b.root)
        } catch (e: Exception) {
            Log.e("Link", "Binding error: ${e.message}"); finish(); return
        }

        b.ivBack.setOnClickListener { finish() }
        b.btnLink.setOnClickListener { doLink() }
    }

    private fun doLink() {
        val code = b.etCode.text.toString().trim().uppercase()
        if (code.isEmpty()) { Toast.makeText(this, "أدخل كود الربط", Toast.LENGTH_SHORT).show(); return }

        b.btnLink.isEnabled = false
        b.btnLink.text = "جاري التحقق..."

        FirebaseService.linkDevice(code) { ok, msg ->
            runOnUiThread {
                b.btnLink.isEnabled = true
                b.btnLink.text = "ربط جهاز"
                if (ok) {
                    b.tvStatus.setTextColor(getColor(R.color.success))
                    b.tvStatus.text = msg
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ finish() }, 1500)
                } else {
                    b.tvStatus.setTextColor(getColor(R.color.error))
                    b.tvStatus.text = msg
                }
            }
        }
    }
}
