package com.abuzahra.control

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.service.FirebaseService
import android.os.Handler
import android.os.Looper

class DeviceLinkActivity : AppCompatActivity() {
    private lateinit var etCode: EditText
    private lateinit var btnLink: Button
    private lateinit var tvStatus: TextView
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_device_link)
            etCode = findViewById(R.id.etCode)
            btnLink = findViewById(R.id.btnLink)
            tvStatus = findViewById(R.id.tvStatus)
            ivBack = findViewById(R.id.ivBack)
        } catch (e: Exception) {
            Log.e("Link", "Setup error: ${e.message}"); finish(); return
        }

        try { ivBack.setOnClickListener { finish() } } catch (_: Exception) {}
        try { btnLink.setOnClickListener { doLink() } } catch (_: Exception) {}
    }

    private fun doLink() {
        val code = etCode.text.toString().trim().uppercase()
        if (code.isEmpty()) { Toast.makeText(this, "أدخل كود الربط", Toast.LENGTH_SHORT).show(); return }
        btnLink.isEnabled = false
        btnLink.text = "جاري التحقق..."

        FirebaseService.linkDevice(code) { ok, msg ->
            runOnUiThread {
                try {
                    btnLink.isEnabled = true
                    btnLink.text = "ربط جهاز"
                    if (ok) {
                        tvStatus.setTextColor(getColor(R.color.success))
                        tvStatus.text = msg
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1500)
                    } else {
                        tvStatus.setTextColor(getColor(R.color.error))
                        tvStatus.text = msg
                    }
                } catch (e: Exception) { Log.e("Link", "doLink: ${e.message}") }
            }
        }
    }
}
