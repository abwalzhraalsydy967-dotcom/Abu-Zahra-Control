package com.abuzahra.control

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // Ensure Firebase Database uses the correct URL
        Firebase.database.setPersistenceEnabled(true)
    }
}
