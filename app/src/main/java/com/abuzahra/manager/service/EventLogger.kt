package com.abuzahra.manager.service

import android.util.Log

/**
 * Real-time event logger that records the result of every user action.
 * Each log entry shows whether an action succeeded or failed with details.
 * The log bar in the UI auto-updates when new entries are added.
 *
 * Usage:
 *   EventLogger.log("تسجيل الدخول", success = true, "تم بنجاح")
 *   EventLogger.log("توليد كود الربط", success = false, "خطأ: فشل الاتصال بقاعدة البيانات")
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

    /**
     * Log an event with its result.
     * @param action The name of the action (e.g., "تسجيل الدخول")
     * @param success Whether the action succeeded
     * @param message Detail message (error description or success confirmation)
     */
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

    /**
     * Log a success event (shortcut).
     */
    fun success(action: String, message: String = "تم بنجاح") {
        log(action, success = true, message = message)
    }

    /**
     * Log a failure event (shortcut).
     */
    fun fail(action: String, message: String) {
        log(action, success = false, message = message)
    }

    /**
     * Get all log entries (thread-safe copy).
     */
    fun getLogs(): List<LogEntry> {
        synchronized(logs) { return logs.toList() }
    }

    /**
     * Get the most recent N log entries.
     */
    fun getRecentLogs(count: Int = 5): List<LogEntry> {
        synchronized(logs) { return logs.takeLast(count) }
    }

    /**
     * Register a listener to receive updates when new log entries are added.
     * The listener is immediately called with the current logs.
     */
    fun addListener(listener: (List<LogEntry>) -> Unit) {
        synchronized(listeners) { listeners.add(listener) }
        listener(getLogs())
    }

    /**
     * Unregister a listener.
     */
    fun removeListener(listener: (List<LogEntry>) -> Unit) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    /**
     * Clear all log entries.
     */
    fun clear() {
        synchronized(logs) { logs.clear() }
        notifyListeners()
    }

    /**
     * Check if there are any failure entries in the log.
     */
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
