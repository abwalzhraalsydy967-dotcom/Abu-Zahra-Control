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
            Log.e("Splash", "setContentView failed: ${e.message}")
            // If layout fails, just finish
            goLogin()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Log.d("Splash", "User exists: ${user.email}, go to Main")
                    goMain()
                } else {
                    Log.d("Splash", "No user, go to Login")
                    goLogin()
                }
            } catch (e: Exception) {
                Log.e("Splash", "Auth check failed: ${e.message}")
                goLogin()
            }
        }, 1500)
    }

    private fun goMain() {
        try {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } catch (e: Exception) {
            Log.e("Splash", "goMain failed: ${e.message}")
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            goLogin()
        }
    }

    private fun goLogin() {
        try {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } catch (e: Exception) {
            Log.e("Splash", "goLogin failed: ${e.message}")
            finish()
        }
    }
}
