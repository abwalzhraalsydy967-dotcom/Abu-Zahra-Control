package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.databinding.ActivityLoginBinding
import com.abuzahra.control.service.FirebaseService

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnLogin.setOnClickListener { loginWithEmail() }
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "تواصل مع المدير لإعادة تعيين كلمة المرور", Toast.LENGTH_SHORT).show()
        }
        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        // Google Sign-In button -> show message that it requires Firebase Console setup
        binding.btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "تسجيل الدخول بـ Google يتطلب إعدادات إضافية\nاستخدم البريد وكلمة المرور", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun loginWithEmail() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "أدخل البريد وكلمة المرور", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = getString(R.string.loading)
        
        FirebaseService.signIn(email, password) { success, error ->
            runOnUiThread {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = getString(R.string.sign_in)
                
                if (success) {
                    Toast.makeText(this, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, error ?: "فشل تسجيل الدخول", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
