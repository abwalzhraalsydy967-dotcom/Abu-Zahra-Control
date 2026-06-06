package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.databinding.ActivityLoginBinding
import com.abuzahra.control.service.FirebaseService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupGoogleSignIn()
        
        binding.btnLogin.setOnClickListener { loginWithEmail() }
        binding.tvForgotPassword.setOnClickListener { resetPassword() }
        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnGoogleSignIn.setOnClickListener { signInWithGoogle() }
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
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
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = getString(R.string.sign_in)
            
            if (success) {
                Toast.makeText(this, "تم تسجيل الدخول", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, error ?: "فشل تسجيل الدخول", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val acct = account.result
                firebaseAuthWithGoogle(acct.idToken!!)
            } catch (e: Exception) {
                Toast.makeText(this, "فشل تسجيل الدخول بـ Google", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseService.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "تم تسجيل الدخول", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "فشل المصادقة", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun resetPassword() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "أدخل بريدك الإلكتروني", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseService.sendPasswordReset(email) { success, msg ->
            Toast.makeText(this, msg ?: "", Toast.LENGTH_LONG).show()
        }
    }
}
