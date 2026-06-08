package com.abuzahra.control

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import java.io.PrintWriter
import java.io.StringWriter

class App : Application() {

    companion object {
        private const val TAG = "App"
        var lastCrash: String = ""
    }

    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun onCreate() {
        super.onCreate()

        // Install global crash handler to catch ALL uncaught exceptions/errors
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "=== UNCAUGHT EXCEPTION ===")
            Log.e(TAG, "Thread: ${thread.name}")
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val trace = sw.toString()
            Log.e(TAG, trace)
            lastCrash = "${throwable.javaClass.simpleName}: ${throwable.message}\n$trace"

            // Save crash info to prefs so we can show it next launch
            try {
                getSharedPreferences("crash_info", Context.MODE_PRIVATE)
                    .edit()
                    .putString("last_crash", lastCrash)
                    .putLong("crash_time", System.currentTimeMillis())
                    .apply()
            } catch (_: Throwable) {}

            // Let the default handler kill the process
            defaultHandler?.uncaughtException(thread, throwable)
        }

        Log.d(TAG, "App.onCreate() complete")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
