package com.abuzahra.tracker.service

import android.util.Log

/**
 * Real-time event logger for the Target Device (Tracker) app.
 * Records the result of every user action and displays it in the UI.
 *
 * Usage:
 *   EventLogger.log("ربط الجهاز", success = true, "تم ربط بنجاح")
 *   EventLogger.log("بدء الخدمة", success = false, "خطأ: صلاحية مفقودة")
 */
object EventLogger {

    private const val TAG = "EventLogger"

    data class LogEntry(
        val timestamp: Long = System.currentTimeMillis(),
        val action: String,
        val success: Boolean,
        val message: String
    )

    private val logs = mutableListOf<LogEntry>()
    private const val maxLogs = 50
    private val listeners = mutableListOf<(List<LogEntry>) -> Unit>()

    fun log(action: String, success: Boolean, message: String) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            action = action,
            success = success,
            message = message
        )
        synchronized(logs) {
            logs.add(entry)
            if (logs.size > maxLogs) logs.removeFirst()
        }
        Log.d(TAG, "[${if (success) "OK" else "FAIL"}] $action: $message")
        notifyListeners()
    }

    fun success(action: String, message: String = "تم بنجاح") {
        log(action, success = true, message = message)
    }

    fun fail(action: String, message: String) {
        log(action, success = false, message = message)
    }

    fun getLogs(): List<LogEntry> {
        synchronized(logs) { return logs.toList() }
    }

    fun getRecentLogs(count: Int = 5): List<LogEntry> {
        synchronized(logs) { return logs.takeLast(count) }
    }

    fun addListener(listener: (List<LogEntry>) -> Unit) {
        synchronized(listeners) { listeners.add(listener) }
        listener(getLogs())
    }

    fun removeListener(listener: (List<LogEntry>) -> Unit) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    fun clear() {
        synchronized(logs) { logs.clear() }
        notifyListeners()
    }

    fun hasFailures(): Boolean {
        synchronized(logs) { return logs.any { !it.success } }
    }

    private fun notifyListeners() {
        val snapshot = getLogs()
        for (listener in listeners) {
            try { listener(snapshot) } catch (_: Exception) {}
        }
    }
}
