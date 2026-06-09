package com.abuzahra.manager

import android.app.Application
import android.content.Context
import android.util.Log
import com.abuzahra.manager.service.CrashHandler
import com.abuzahra.manager.service.DatabaseSetup
import com.abuzahra.manager.util.PrefsManager

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
            DatabaseSetup.initialize()
            Log.d(TAG, "App initialized successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "App init error: ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}
