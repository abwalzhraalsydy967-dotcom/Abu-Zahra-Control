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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private var googleClient: GoogleSignInClient? = null
    private val RC_GOOGLE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_login)
        } catch (e: Exception) {
            Log.e("Login", "setContentView CRASH: ${e.message}")
            finish()
            return
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvGoRegister = findViewById<TextView>(R.id.tvGoRegister)
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)

        // Google Sign-In setup
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            googleClient = GoogleSignIn.getClient(this, gso)
            Log.d("Login", "Google Sign-In client created OK")
        } catch (e: Exception) {
            Log.w("Login", "Google Sign-In init failed: ${e.message}")
        }

        // Email login
        btnLogin.setOnClickListener {
            val email = etEmail?.text.toString().trim()
            val pass = etPassword?.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "أدخل البريد وكلمة المرور", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnLogin?.isEnabled = false
            btnLogin?.text = "جاري التحميل..."

            FirebaseService.signIn(email, pass) { ok, err ->
                btnLogin?.isEnabled = true
                btnLogin?.text = "دخول"
                if (ok) {
                    Toast.makeText(this, "تم تسجيل الدخول ✅", Toast.LENGTH_SHORT).show()
                    openMain()
                } else {
                    Toast.makeText(this, err ?: "فشل تسجيل الدخول", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Forgot password
        tvForgotPassword?.setOnClickListener {
            val email = etEmail?.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "أدخل بريدك أولاً", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { t ->
                        Toast.makeText(this,
                            if (t.isSuccessful) "تم إرسال رابط إعادة التعيين" else "فشل: ${t.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Go to register
        tvGoRegister?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Google sign in
        btnGoogleSignIn?.setOnClickListener {
            try {
                val client = googleClient
                if (client != null) {
                    startActivityForResult(client.signInIntent, RC_GOOGLE)
                } else {
                    Toast.makeText(this, "تسجيل Google غير متاح", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "خطأ في خدمات Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE && data != null) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.result
                val cred = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(cred)
                    .addOnCompleteListener { t ->
                        if (t.isSuccessful) {
                            Toast.makeText(this, "تم تسجيل الدخول ✅", Toast.LENGTH_SHORT).show()
                            openMain()
                        } else {
                            Toast.makeText(this, "فشل: ${t.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "فشل تسجيل Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openMain() {
        try {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } catch (e: Exception) {
            Log.e("Login", "openMain: ${e.message}")
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
