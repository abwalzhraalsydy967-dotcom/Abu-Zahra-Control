package com.abuzahra.manager.util

import android.content.Context
import android.content.SharedPreferences
import com.abuzahra.manager.constants.AppConstants

object PrefsManager {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        try {
            prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        } catch (_: Throwable) {}
    }

    fun saveCrash(msg: String) {
        try {
            prefs.edit()
                .putString(AppConstants.KEY_CRASH_MSG, msg)
                .putLong(AppConstants.KEY_CRASH_TIME, System.currentTimeMillis())
                .apply()
        } catch (_: Throwable) {}
    }

    fun getCrash(): Pair<String, Long> {
        return try {
            val msg = prefs.getString(AppConstants.KEY_CRASH_MSG, "") ?: ""
            val time = prefs.getLong(AppConstants.KEY_CRASH_TIME, 0)
            Pair(msg, time)
        } catch (_: Throwable) {
            Pair("", 0L)
        }
    }

    fun clearCrash() {
        try {
            prefs.edit()
                .remove(AppConstants.KEY_CRASH_MSG)
                .remove(AppConstants.KEY_CRASH_TIME)
                .apply()
        } catch (_: Throwable) {}
    }

    fun saveString(key: String, value: String) {
        try { prefs.edit().putString(key, value).apply() } catch (_: Throwable) {}
    }

    fun getString(key: String, default: String = ""): String {
        return try { prefs.getString(key, default) ?: default } catch (_: Throwable) { default }
    }
}
