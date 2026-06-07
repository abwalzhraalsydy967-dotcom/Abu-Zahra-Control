package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.databinding.ActivityLoginBinding
import com.abuzahra.control.service.FirebaseService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var b: ActivityLoginBinding
    private lateinit var googleClient: GoogleSignInClient
    private val RC_GOOGLE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            b = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(b.root)
        } catch (e: Exception) {
            Log.e("Login", "Binding error: ${e.message}")
            finish()
            return
        }

        // Setup Google Sign-In
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("7073076148-abcdef.apps.googleusercontent.com")
                .requestEmail()
                .build()
            googleClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Log.e("Login", "Google client error: ${e.message}")
        }

        b.btnLogin.setOnClickListener { doLogin() }
        b.tvForgotPassword.setOnClickListener { doReset() }
        b.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        b.btnGoogleSignIn.setOnClickListener { doGoogleSignIn() }
    }

    private fun doLogin() {
        val email = b.etEmail.text.toString().trim()
        val pass = b.etPassword.text.toString().trim()
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "أدخل البريد وكلمة المرور", Toast.LENGTH_SHORT).show()
            return
        }
        b.btnLogin.isEnabled = false
        b.btnLogin.text = "جاري التحميل..."

        FirebaseService.signIn(email, pass) { ok, err ->
            runOnUiThread {
                b.btnLogin.isEnabled = true
                b.btnLogin.text = "دخول"
                if (ok) {
                    Toast.makeText(this, "تم تسجيل الدخول ✅", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, err ?: "فشل تسجيل الدخول", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun doGoogleSignIn() {
        try {
            startActivityForResult(googleClient.signInIntent, RC_GOOGLE)
        } catch (e: Exception) {
            Toast.makeText(this, "تأكد من تثبيت خدمات Google Play", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val cred = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(cred)
                    .addOnCompleteListener { t ->
                        if (t.isSuccessful) {
                            Toast.makeText(this, "تم تسجيل الدخول ✅", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "فشل: ${t.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "فشل تسجيل الدخول بـ Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doReset() {
        val email = b.etEmail.text.toString().trim()
        if (email.isEmpty()) { Toast.makeText(this, "أدخل بريدك", Toast.LENGTH_SHORT).show(); return }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { t ->
                Toast.makeText(this, if (t.isSuccessful) "تم إرسال رابط إعادة التعيين" else "فشل: ${t.exception?.message}", Toast.LENGTH_LONG).show()
            }
    }
}
