package com.abuzahra.control.service

import android.content.Context
import android.util.Log
import com.abuzahra.control.util.PrefsManager
import java.io.PrintWriter
import java.io.StringWriter

object CrashHandler {
    private const val TAG = "CrashHandler"

    fun install(context: Context) {
        try {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                Log.e(TAG, "=== UNCAUGHT EXCEPTION ===")
                Log.e(TAG, "Thread: ${thread.name}")
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                Log.e(TAG, sw.toString())

                val message = buildCrashMessage(throwable)
                PrefsManager.saveCrash(message)
                Log.e(TAG, "Crash saved: $message")

                defaultHandler?.uncaughtException(thread, throwable)
            }
            Log.d(TAG, "CrashHandler installed")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to install CrashHandler: ${t.message}")
        }
    }

    private fun buildCrashMessage(throwable: Throwable): String {
        val sb = StringBuilder()
        var cause: Throwable? = throwable
        var depth = 0
        while (cause != null && depth < 5) {
            sb.append("${cause.javaClass.simpleName}: ${cause.message}")
            cause = cause.cause
            if (cause != null) sb.append(" | ")
            depth++
        }
        return sb.toString()
    }
}
