package com.abuzahra.manager.ui.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.abuzahra.manager.util.showToast

abstract class BaseActivity : AppCompatActivity() {

    protected val TAG: String by lazy { this::class.java.simpleName }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate")
            setupContent()
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error: ${e.message}")
            showCrashToast("خطأ في تهيئة الشاشة: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    protected abstract fun setupContent()

    protected fun showCrashToast(msg: String) {
        try {
            showToast(msg)
        } catch (_: Throwable) {}
    }
}
