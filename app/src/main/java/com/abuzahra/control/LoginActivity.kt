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

    // Web client ID from google-services.json - use as fallback if R.string not available
    private val WEB_CLIENT_ID = "787676787951-20uf0a81hb0n5b95t9htb7cd073lu2bm.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_login)
        } catch (t: Throwable) {
            Log.e("Login", "setContentView CRASH: ${t.message}")
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
            val clientId = try {
                getString(R.string.default_web_client_id)
            } catch (_: Throwable) {
                WEB_CLIENT_ID
            }

            Log.d("Login", "Google Sign-In client ID: ${clientId.take(20)}...")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build()
            googleClient = GoogleSignIn.getClient(this, gso)
            Log.d("Login", "Google Sign-In client created OK")
        } catch (t: Throwable) {
            Log.e("Login", "Google Sign-In init failed: ${t.javaClass.simpleName}: ${t.message}")
        }

        // Email login
        btnLogin?.setOnClickListener {
            try {
                val email = etEmail?.text.toString().trim()
                val pass = etPassword?.text.toString().trim()
                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "أدخل البريد وكلمة المرور", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                btnLogin.isEnabled = false
                btnLogin.text = "جاري التحميل..."

                FirebaseService.signIn(email, pass) { ok, err ->
                    btnLogin?.isEnabled = true
                    btnLogin?.text = "دخول"
                    if (ok) {
                        Toast.makeText(this, "تم تسجيل الدخول", Toast.LENGTH_SHORT).show()
                        openMain()
                    } else {
                        Toast.makeText(this, err ?: "فشل تسجيل الدخول", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (t: Throwable) {
                Log.e("Login", "Login click error: ${t.message}")
                btnLogin?.isEnabled = true
                btnLogin?.text = "دخول"
            }
        }

        // Forgot password
        tvForgotPassword?.setOnClickListener {
            try {
                val email = etEmail?.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, "أدخل بريدك أولاً", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { t2 ->
                        Toast.makeText(this,
                            if (t2.isSuccessful) "تم إرسال رابط إعادة التعيين" else "فشل: ${t2.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
            } catch (t: Throwable) {
                Toast.makeText(this, "خطأ: ${t.message}", Toast.LENGTH_SHORT).show()
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
            } catch (t: Throwable) {
                Toast.makeText(this, "خطأ في خدمات Google: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE && data != null) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(Exception::class.java)
                if (account != null) {
                    val cred = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(cred)
                        .addOnCompleteListener { t2 ->
                            if (t2.isSuccessful) {
                                Toast.makeText(this, "تم تسجيل الدخول", Toast.LENGTH_SHORT).show()
                                openMain()
                            } else {
                                val errMsg = t2.exception?.message ?: "فشل"
                                Toast.makeText(this, "فشل: $errMsg", Toast.LENGTH_LONG).show()
                                Log.e("Login", "Google auth failed: $errMsg")
                            }
                        }
                }
            } catch (t: Throwable) {
                Log.e("Login", "Google Sign-In error: ${t.javaClass.simpleName}: ${t.message}")
                Toast.makeText(this, "فشل تسجيل Google: ${t.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openMain() {
        try {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } catch (t: Throwable) {
            Log.e("Login", "openMain: ${t.javaClass.simpleName}: ${t.message}")
            Toast.makeText(this, "خطأ: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }
}
