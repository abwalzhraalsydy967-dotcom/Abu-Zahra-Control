package com.abuzahra.control

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.control.service.FirebaseService
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    Log.d("Splash", "User logged in: ${user.email}")
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Log.d("Splash", "No user, go to login")
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            } catch (e: Exception) {
                Log.e("Splash", "Error: ${e.message}")
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1500)
    }
}
