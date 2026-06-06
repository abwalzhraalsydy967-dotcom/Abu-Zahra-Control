package com.abuzahra.control

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        com.abuzahra.control.service.FirebaseService.init(this)
    }
}
