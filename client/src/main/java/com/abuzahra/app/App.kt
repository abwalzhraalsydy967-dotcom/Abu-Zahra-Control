package com.abuzahra.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // Set persistence and explicit database URL
        val db = FirebaseDatabase.getInstance()
        db.setPersistenceEnabled(true)
        db.reference // Force initialization
    }
}
