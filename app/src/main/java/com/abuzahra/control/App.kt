package com.abuzahra.control

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class App : Application() {
    companion object {
        private const val TAG = "App"
        private var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        Log.d(TAG, "App.onCreate started")

        // Safe Firebase initialization
        try {
            val apps = com.google.firebase.FirebaseApp.getApps(this)
            Log.d(TAG, "Firebase apps count: ${apps.size}")
            if (apps.isNotEmpty()) {
                Log.d(TAG, "Firebase project: ${apps[0].options.projectId}")
                Log.d(TAG, "Firebase DB URL: ${apps[0].options.databaseUrl}")
                Log.d(TAG, "Firebase API Key: ${apps[0].options.apiKey}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase info error: ${e.message}")
        }

        // Enable database persistence safely
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            Log.d(TAG, "Database persistence enabled")
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Persistence already set: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Persistence enable error: ${e.message}")
        }

        Log.d(TAG, "App.onCreate completed successfully")
    }
}
