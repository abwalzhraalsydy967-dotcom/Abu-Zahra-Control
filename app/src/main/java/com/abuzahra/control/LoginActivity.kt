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

class LoginActivity : AppCompatActivity() {
    private var googleClient: GoogleSignInClient? = null
    private val RC_GOOGLE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d("Login", "onCreate - layout set OK")

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val tvGoRegister = findViewById<TextView>(R.id.tvGoRegister)
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)

        // Google Sign-In setup
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("787676787951-20uf0a81hb0n5b95t9htb7cd073lu2bm.apps.googleusercontent.com")
                .requestEmail()
                .build()
            googleClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Log.w("Login", "Google Sign-In init: ${e.message}")
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "أدخل البريد وكلمة المرور", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnLogin.isEnabled = false
            btnLogin.text = "جاري التحميل..."

            FirebaseService.signIn(email, pass) { ok, err ->
                btnLogin.isEnabled = true
                btnLogin.text = "دخول"
                if (ok) {
                    Toast.makeText(this, "تم تسجيل الدخول ✅", Toast.LENGTH_SHORT).show()
                    openMain()
                } else {
                    Toast.makeText(this, err ?: "فشل تسجيل الدخول", Toast.LENGTH_LONG).show()
                }
            }
        }

        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "أدخل بريدك أولاً", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { t ->
                    Toast.makeText(this,
                        if (t.isSuccessful) "تم إرسال رابط إعادة التعيين" else "فشل: ${t.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnGoogleSignIn.setOnClickListener {
            try {
                val client = googleClient
                if (client != null) {
                    startActivityForResult(client.signInIntent, RC_GOOGLE)
                } else {
                    Toast.makeText(this, "تسجيل Google غير متاح", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "خطأ في خدمات Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE && data != null) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.result
                val cred = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)
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
                Toast.makeText(this, "فشل تسجيل Google", Toast.LENGTH_SHORT).show()
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
