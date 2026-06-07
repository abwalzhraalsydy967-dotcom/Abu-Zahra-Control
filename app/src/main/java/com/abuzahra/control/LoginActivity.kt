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

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvGoRegister: TextView
    private lateinit var btnGoogleSignIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_login)
            etEmail = findViewById(R.id.etEmail)
            etPassword = findViewById(R.id.etPassword)
            btnLogin = findViewById(R.id.btnLogin)
            tvForgotPassword = findViewById(R.id.tvForgotPassword)
            tvGoRegister = findViewById(R.id.tvGoRegister)
            btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        } catch (e: Exception) {
            Log.e("Login", "Setup error: ${e.message}")
            e.printStackTrace()
            finish()
            return
        }

        // Google Sign-In
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("787676787951-20uf0a81hb0n5b95t9htb7cd073lu2bm.apps.googleusercontent.com")
                .requestEmail()
                .build()
            googleClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Log.w("Login", "Google Sign-In not available: ${e.message}")
        }

        try {
            btnLogin.setOnClickListener { doLogin() }
            tvForgotPassword.setOnClickListener { doReset() }
            tvGoRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
            btnGoogleSignIn.setOnClickListener { doGoogleSignIn() }
        } catch (e: Exception) { Log.e("Login", "UI setup: ${e.message}") }
    }

    private fun doLogin() {
        try {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "أدخل البريد وكلمة المرور", Toast.LENGTH_SHORT).show()
                return
            }
            btnLogin.isEnabled = false
            btnLogin.text = "جاري التحميل..."

            FirebaseService.signIn(email, pass) { ok, err ->
                runOnUiThread {
                    try {
                        btnLogin.isEnabled = true
                        btnLogin.text = "دخول"
                    } catch (_: Exception) {}
                    if (ok) {
                        Toast.makeText(this, "تم تسجيل الدخول ✅", Toast.LENGTH_SHORT).show()
                        goToMain()
                    } else {
                        Toast.makeText(this, err ?: "فشل تسجيل الدخول", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Login", "doLogin: ${e.message}")
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun doGoogleSignIn() {
        try {
            val client = googleClient
            if (client != null) startActivityForResult(client.signInIntent, RC_GOOGLE)
            else Toast.makeText(this, "تسجيل Google غير متاح - استخدم البريد وكلمة المرور", Toast.LENGTH_LONG).show()
        } catch (e: Exception) { Toast.makeText(this, "تأكد من تثبيت خدمات Google Play", Toast.LENGTH_LONG).show() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.result
                val cred = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(cred)
                    .addOnCompleteListener { t ->
                        if (t.isSuccessful) {
                            Toast.makeText(this, "تم تسجيل الدخول ✅", Toast.LENGTH_SHORT).show()
                            goToMain()
                        } else {
                            Toast.makeText(this, "فشل: ${t.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } catch (e: Exception) { Toast.makeText(this, "فشل تسجيل الدخول بـ Google", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun doReset() {
        try {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) { Toast.makeText(this, "أدخل بريدك أولاً", Toast.LENGTH_SHORT).show(); return }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { t ->
                    val msg = if (t.isSuccessful) "تم إرسال رابط إعادة التعيين" else "فشل: ${t.exception?.message}"
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) { Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    private fun goToMain() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("Login", "goToMain: ${e.message}")
            Toast.makeText(this, "خطأ في فتح الصفحة الرئيسية: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
