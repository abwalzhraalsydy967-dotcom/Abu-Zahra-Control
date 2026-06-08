package com.abuzahra.control

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.service.FirebaseService

class DeviceLinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_device_link)
        } catch (t: Throwable) {
            Log.e("DeviceLink", "setContentView CRASH: ${t.message}")
            finish()
            return
        }

        val etCode = findViewById<EditText>(R.id.etCode)
        val btnLink = findViewById<Button>(R.id.btnLink)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnLink?.setOnClickListener {
            try {
                val code = etCode?.text.toString().trim().uppercase()
                if (code.isEmpty()) {
                    Toast.makeText(this, "أدخل كود الربط", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                btnLink.isEnabled = false
                btnLink.text = "جاري التحقق..."

                FirebaseService.linkDevice(code) { ok, msg ->
                    btnLink?.isEnabled = true
                    btnLink?.text = "ربط جهاز"
                    if (ok) {
                        tvStatus?.setTextColor(getColor(R.color.success))
                        tvStatus?.text = msg
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1500)
                    } else {
                        tvStatus?.setTextColor(getColor(R.color.error))
                        tvStatus?.text = msg
                    }
                }
            } catch (t: Throwable) {
                Log.e("DeviceLink", "Link error: ${t.message}")
                btnLink?.isEnabled = true
                btnLink?.text = "ربط جهاز"
            }
        }
    }
}
