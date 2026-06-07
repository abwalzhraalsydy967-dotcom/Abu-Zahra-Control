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
            val existingApps = FirebaseApp.getApps(this)
            if (existingApps.isNotEmpty()) {
                Log.d("App", "Firebase already initialized by plugin (${existingApps.size} apps)")
                try {
                    Firebase.database.setPersistenceEnabled(true)
                } catch (e: Exception) {
                    Log.e("App", "DB persist error: ${e.message}")
                }
                return
            }

            // Manual initialization with REAL project credentials
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyASBVIQ0AvrsLqAgbT9k6L7bCpZKoqdvjo")
                .setApplicationId("1:787676787951:android:e583411c93e5c76694171a")
                .setProjectId("studio-7073076148-6afe0")
                .setDatabaseUrl("https://studio-7073076148-6afe0-default-rtdb.firebaseio.com")
                .setStorageBucket("studio-7073076148-6afe0.firebasestorage.app")
                .setGcmSenderId("787676787951")
                .build()

            FirebaseApp.initializeApp(this, options)
            Log.d("App", "Firebase initialized manually with real credentials")

            Firebase.database.setPersistenceEnabled(true)
            Log.d("App", "Database persistence enabled")
        } catch (e: Exception) {
            Log.e("App", "Firebase init error: ${e.message}")
        }
    }
}
