package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d("Splash", "onCreate")

        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("Splash", "Checking auth...")
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                Log.d("Splash", "User logged in: ${user.email}")
                openMain()
            } else {
                Log.d("Splash", "No user, go to login")
                openLogin()
            }
        }, 1500)
    }

    private fun openMain() {
        try {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } catch (e: Exception) {
            Log.e("Splash", "openMain error: ${e.message}")
            openLogin()
        }
    }

    private fun openLogin() {
        try {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } catch (e: Exception) {
            Log.e("Splash", "openLogin error: ${e.message}")
            finish()
        }
    }
}
