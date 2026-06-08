package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.service.FirebaseService

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        Log.d("Register", "onCreate - layout set OK")

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoLogin = findViewById<TextView>(R.id.tvGoLogin)
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString()
            val confirm = etConfirmPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(this, "بريد غير صحيح", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirm) {
                Toast.makeText(this, "كلمتا المرور غير متطابقتين", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass.length < 6) {
                Toast.makeText(this, "كلمة المرور 6 أحرف على الأقل", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            btnRegister.text = "جاري التحميل..."

            FirebaseService.signUp(email, pass) { ok, err ->
                btnRegister.isEnabled = true
                btnRegister.text = "إنشاء حساب"
                if (ok) {
                    Toast.makeText(this, "تم إنشاء الحساب ✅", Toast.LENGTH_SHORT).show()
                    openMain()
                } else {
                    Toast.makeText(this, err ?: "فشل", Toast.LENGTH_LONG).show()
                }
            }
        }

        tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "سجل دخول عبر البريد أولاً ثم اربط حساب Google من الإعدادات", Toast.LENGTH_LONG).show()
        }
    }

    private fun openMain() {
        try {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } catch (e: Exception) {
            Log.e("Register", "openMain: ${e.message}")
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
