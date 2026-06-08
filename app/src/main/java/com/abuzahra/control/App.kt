package com.abuzahra.control

import android.app.Application
import android.util.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Do NOT access Firebase here - google-services plugin auto-initializes
        // Firebase via a ContentProvider that runs AFTER Application.onCreate().
        // Accessing Firebase in Application.onCreate() causes crash.
        Log.d("App", "App.onCreate() - Application started")
    }
}
