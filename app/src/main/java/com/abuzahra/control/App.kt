package com.abuzahra.control

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.PrintWriter
import java.io.StringWriter

class App : Application() {
    companion object {
        private const val TAG = "App"
        private var lastCrash: String = ""
        private var appContext: Context? = null

        fun getLastCrash(): String = lastCrash

        fun getAppContext(): Context? = appContext
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Enhanced crash handler
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                lastCrash = sw.toString()
                Log.e("CRASH", "=== UNCAUGHT EXCEPTION ===")
                Log.e("CRASH", "Thread: ${thread.name}")
                Log.e("CRASH", "Message: ${throwable.message}")
                Log.e("CRASH", "Stack:\n$lastCrash")
            } catch (_: Exception) {}

            // Show crash info before dying
            Handler(Looper.getMainLooper()).post {
                try {
                    Toast.makeText(appContext, "خطأ: ${throwable.message}", Toast.LENGTH_LONG).show()
                } catch (_: Exception) {}
            }

            // Delay before calling original handler to let the toast show
            try {
                Thread.sleep(1000)
            } catch (_: Exception) {}

            originalHandler?.uncaughtException(thread, throwable)
        }

        // Activity lifecycle monitor to catch activity-level crashes
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.d(TAG, "Activity created: ${activity.javaClass.simpleName}")
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                Log.d(TAG, "Activity destroyed: ${activity.javaClass.simpleName}")
            }
        })

        try {
            val apps = com.google.firebase.FirebaseApp.getApps(this)
            Log.d(TAG, "Firebase apps: ${apps.size}")
            if (apps.isNotEmpty()) {
                Log.d(TAG, "Firebase project: ${apps[0].options.projectId}")
                Log.d(TAG, "Firebase DB URL: ${apps[0].options.databaseUrl}")
            }

            try {
                Firebase.database.setPersistenceEnabled(true)
                Log.d(TAG, "Database persistence enabled")
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Persistence already set: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase init error: ${e.message}", e)
        }
    }
}
