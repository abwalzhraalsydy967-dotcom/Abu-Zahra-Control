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
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private var binding: ActivityRegisterBinding? = null
    private val b get() = binding!!
    private var googleClient: GoogleSignInClient? = null
    private val RC_GOOGLE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityRegisterBinding.inflate(layoutInflater)
            setContentView(b.root)
        } catch (e: Exception) {
            Log.e("Register", "Binding error: ${e.message}")
            finish()
            return
        }

        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("787676787951-20uf0a81hb0n5b95t9htb7cd073lu2bm.apps.googleusercontent.com")
                .requestEmail().build()
            googleClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Log.w("Register", "Google Sign-In not available: ${e.message}")
        }

        try {
            b.btnRegister.setOnClickListener { doRegister() }
            b.tvGoLogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            b.btnGoogleSignIn.setOnClickListener {
                try {
                    googleClient?.let { startActivityForResult(it.signInIntent, RC_GOOGLE) }
                        ?: Toast.makeText(this, "تسجيل Google غير متاح", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "تأكد من خدمات Google", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Register", "UI setup error: ${e.message}")
        }
    }

    private fun doRegister() {
        try {
            val email = b.etEmail.text.toString().trim()
            val pass = b.etPassword.text.toString()
            val confirm = b.etConfirmPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT).show()
                return
            }
            if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(this, "بريد غير صحيح", Toast.LENGTH_SHORT).show()
                return
            }
            if (pass != confirm) {
                Toast.makeText(this, "كلمتا المرور غير متطابقتين", Toast.LENGTH_SHORT).show()
                return
            }
            if (pass.length < 6) {
                Toast.makeText(this, "كلمة المرور 6 أحرف على الأقل", Toast.LENGTH_SHORT).show()
                return
            }

            b.btnRegister.isEnabled = false
            b.btnRegister.text = "جاري التحميل..."

            FirebaseService.signUp(email, pass) { ok, err ->
                runOnUiThread {
                    try {
                        b.btnRegister.isEnabled = true
                        b.btnRegister.text = "تسجيل"
                    } catch (_: Exception) {}

                    if (ok) {
                        Toast.makeText(this, "تم إنشاء الحساب ✅", Toast.LENGTH_SHORT).show()
                        goToMain()
                    } else {
                        Toast.makeText(this, err ?: "فشل", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Register", "doRegister error: ${e.message}")
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
                            goToMain()
                        } else {
                            Toast.makeText(this, "فشل: ${t.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "فشل Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMain() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("Register", "goToMain error: ${e.message}")
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
