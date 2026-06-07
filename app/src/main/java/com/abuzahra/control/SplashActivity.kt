package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_splash)
        } catch (e: Exception) {
            Log.e("Splash", " setContentView error: ${e.message}")
            // Fallback - go to login directly
            goToLogin()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Log.d("Splash", "User logged in: ${user.email}")
                    goToMain()
                } else {
                    Log.d("Splash", "No user, go to login")
                    goToLogin()
                }
            } catch (e: Exception) {
                Log.e("Splash", "Auth check error: ${e.message}")
                goToLogin()
            }
        }, 1500)
    }

    private fun goToMain() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("Splash", "goToMain error: ${e.message}")
            Toast.makeText(this, "خطأ في تحميل التطبيق", Toast.LENGTH_LONG).show()
            goToLogin()
        }
    }

    private fun goToLogin() {
        try {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } catch (e: Exception) {
            Log.e("Splash", "goToLogin error: ${e.message}")
            finish()
        }
    }
}
