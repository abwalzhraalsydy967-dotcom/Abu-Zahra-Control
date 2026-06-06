package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.databinding.ActivityRegisterBinding
import com.abuzahra.control.service.FirebaseService

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnRegister.setOnClickListener { registerWithEmail() }
        binding.tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "تسجيل بـ Google يتطلب إعدادات إضافية\nاستخدم البريد وكلمة المرور", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun registerWithEmail() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "أكمل جميع الحقول", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!email.contains("@") || !email.contains(".")) {
            Toast.makeText(this, "أدخل بريد إلكتروني صحيح", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password != confirmPassword) {
            Toast.makeText(this, "كلمتا المرور غير متطابقتين", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password.length < 6) {
            Toast.makeText(this, "كلمة المرور يجب أن تكون 6 أحرف على الأقل", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = getString(R.string.loading)
        
        FirebaseService.signUp(email, password) { success, error ->
            runOnUiThread {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = getString(R.string.sign_up)
                
                if (success) {
                    Toast.makeText(this, "تم إنشاء الحساب بنجاح", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                } else {
                    Toast.makeText(this, error ?: "فشل إنشاء الحساب", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
