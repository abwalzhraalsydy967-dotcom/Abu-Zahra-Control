package com.abuzahra.control

import android.app.Application
import android.content.Context
import android.util.Log
import com.abuzahra.control.service.CrashHandler
import com.abuzahra.control.util.PrefsManager

class App : Application() {
    companion object {
        private const val TAG = "App"
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            CrashHandler.install(this)
            PrefsManager.init(this)
            Log.d(TAG, "App initialized successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "App init error: ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}
