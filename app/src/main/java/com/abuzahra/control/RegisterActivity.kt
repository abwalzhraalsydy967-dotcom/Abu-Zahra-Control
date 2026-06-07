package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.databinding.ActivityRegisterBinding
import com.abuzahra.control.service.FirebaseService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class RegisterActivity : AppCompatActivity() {
    private lateinit var b: ActivityRegisterBinding
    private lateinit var googleClient: GoogleSignInClient
    private val RC_GOOGLE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            b = ActivityRegisterBinding.inflate(layoutInflater)
            setContentView(b.root)
        } catch (e: Exception) {
            Log.e("Register", "Binding error: ${e.message}")
            finish(); return
        }

        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("7073076148-abcdef.apps.googleusercontent.com")
                .requestEmail().build()
            googleClient = GoogleSignIn.getClient(this, gso)
        } catch (_: Exception) {}

        b.btnRegister.setOnClickListener { doRegister() }
        b.tvGoLogin.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)); finish() }
        b.btnGoogleSignIn.setOnClickListener {
            try { startActivityForResult(googleClient.signInIntent, RC_GOOGLE) }
            catch (_: Exception) { Toast.makeText(this, "تأكد من خدمات Google", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun doRegister() {
        val email = b.etEmail.text.toString().trim()
        val pass = b.etPassword.text.toString()
        val confirm = b.etConfirmPassword.text.toString()

        if (email.isEmpty() || pass.isEmpty()) { Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT).show(); return }
        if (!email.contains("@") || !email.contains(".")) { Toast.makeText(this, "بريد غير صحيح", Toast.LENGTH_SHORT).show(); return }
        if (pass != confirm) { Toast.makeText(this, "كلمتا المرور غير متطابقتين", Toast.LENGTH_SHORT).show(); return }
        if (pass.length < 6) { Toast.makeText(this, "كلمة المرور 6 أحرف على الأقل", Toast.LENGTH_SHORT).show(); return }

        b.btnRegister.isEnabled = false
        b.btnRegister.text = "جاري التحميل..."

        FirebaseService.signUp(email, pass) { ok, err ->
            runOnUiThread {
                b.btnRegister.isEnabled = true
                b.btnRegister.text = "تسجيل"
                if (ok) {
                    Toast.makeText(this, "تم إنشاء الحساب ✅", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                } else {
                    Toast.makeText(this, err ?: "فشل", Toast.LENGTH_LONG).show()
                }
            }
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
                            startActivity(Intent(this, MainActivity::class.java)); finishAffinity()
                        } else {
                            Toast.makeText(this, "فشل", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (_: Exception) { Toast.makeText(this, "فشل Google", Toast.LENGTH_SHORT).show() }
        }
    }
}
