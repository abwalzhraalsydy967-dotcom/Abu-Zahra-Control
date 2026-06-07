package com.abuzahra.control

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Check if Firebase is already initialized by google-services plugin
            val existingApp = FirebaseApp.getApps(this)
            if (existingApp.isNotEmpty()) {
                Log.d("App", "Firebase already initialized by plugin")
                // Ensure database URL is correct
                try {
                    Firebase.database.setPersistenceEnabled(true)
                } catch (e: Exception) {
                    Log.e("App", "DB persist error: ${e.message}")
                }
                return
            }

            // Manual initialization with correct project credentials
            val options = FirebaseOptions.Builder()
                .setApiKey("91b6cd08b16f5ad4cc62f88674bcff91fb5041e3")
                .setApplicationId("1:7073076148:android:c0a3e7f9d2b1a4e8")
                .setProjectId("studio-7073076148-6afe0")
                .setDatabaseUrl("https://studio-7073076148-6afe0-default-rtdb.firebaseio.com")
                .setStorageBucket("studio-7073076148-6afe0.appspot.com")
                .build()

            FirebaseApp.initializeApp(this, options)
            Log.d("App", "Firebase initialized manually")

            Firebase.database.setPersistenceEnabled(true)
            Log.d("App", "Database persistence enabled")
        } catch (e: Exception) {
            Log.e("App", "Firebase init error: ${e.message}")
        }
    }
}
