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

        // Check for previous crash and show it
        try {
            val prefs = getSharedPreferences("crash_info", MODE_PRIVATE)
            val crash = prefs.getString("last_crash", null)
            val crashTime = prefs.getLong("crash_time", 0)
            // Clear the crash info
            prefs.edit().remove("last_crash").remove("crash_time").apply()

            if (crash != null && (System.currentTimeMillis() - crashTime) < 60000) {
                // Show crash info for recent crashes (< 1 min ago)
                val shortMsg = crash.lines().firstOrNull() ?: "Unknown error"
                Toast.makeText(this, "خطأ سابق: $shortMsg", Toast.LENGTH_LONG).show()
                Log.e("Splash", "Previous crash: $shortMsg")
            }
        } catch (_: Throwable) {}

        try {
            setContentView(R.layout.activity_splash)
        } catch (t: Throwable) {
            Log.e("Splash", "setContentView failed: ${t.message}")
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
            } catch (t: Throwable) {
                Log.e("Splash", "Auth check failed: ${t.message}")
                goLogin()
            }
        }, 2000)
    }

    private fun goMain() {
        try {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        } catch (t: Throwable) {
            Log.e("Splash", "goMain failed: ${t.message}")
            Toast.makeText(this, "خطأ: ${t.message}", Toast.LENGTH_LONG).show()
            goLogin()
        }
    }

    private fun goLogin() {
        try {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } catch (t: Throwable) {
            Log.e("Splash", "goLogin failed: ${t.message}")
            finish()
        }
    }
}
