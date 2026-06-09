package com.abuzahra.control.data.repository

import com.abuzahra.control.service.FirebaseManager

class AuthRepository {
    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        FirebaseManager.signIn(email, password, callback)
    }

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        FirebaseManager.signUp(email, password, callback)
    }

    fun signOut() {
        FirebaseManager.signOut()
    }

    fun resetPassword(email: String, callback: (Boolean, String?) -> Unit) {
        FirebaseManager.resetPassword(email, callback)
    }

    fun isUserLoggedIn(): Boolean {
        return FirebaseManager.currentUser != null
    }

    fun getUserEmail(): String? {
        return FirebaseManager.userEmail
    }

    fun getUserId(): String? {
        return FirebaseManager.userId
    }
}
