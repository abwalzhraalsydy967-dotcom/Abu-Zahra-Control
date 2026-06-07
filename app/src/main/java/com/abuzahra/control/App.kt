package com.abuzahra.control

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Global crash handler - catch ALL uncaught exceptions and log them
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH", "Uncaught exception in ${thread.name}: ${throwable.message}", throwable)
            throwable.printStackTrace()
            // Let the default handler finish the app
            defaultUEH?.uncaughtException(thread, throwable)
        }

        try {
            // Firebase is auto-initialized by the google-services plugin via ContentProvider
            // We just need to enable database persistence
            val apps = FirebaseApp.getApps(this)
            if (apps.isNotEmpty()) {
                Log.d("App", "Firebase initialized: ${apps.size} app(s)")
            }

            // Enable offline persistence (must be before any database operation)
            try {
                Firebase.database.setPersistenceEnabled(true)
                Log.d("App", "Database persistence enabled")
            } catch (e: IllegalStateException) {
                // Already called - this is fine
                Log.w("App", "Persistence already set: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("App", "Init error: ${e.message}")
        }
    }

    companion object {
        private val defaultUEH: Thread.UncaughtExceptionHandler? =
            Thread.getDefaultUncaughtExceptionHandler()
    }
}
