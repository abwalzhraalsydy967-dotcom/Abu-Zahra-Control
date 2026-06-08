package com.abuzahra.control

import android.app.Application
import android.util.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val apps = com.google.firebase.FirebaseApp.getApps(this)
            Log.d("App", "Firebase apps: ${apps.size}")
            if (apps.isNotEmpty()) {
                Log.d("App", "Project: ${apps[0].options.projectId}")
                Log.d("App", "DB URL: ${apps[0].options.databaseUrl}")
            }
        } catch (e: Exception) {
            Log.e("App", "Firebase check: ${e.message}")
        }
    }
}
