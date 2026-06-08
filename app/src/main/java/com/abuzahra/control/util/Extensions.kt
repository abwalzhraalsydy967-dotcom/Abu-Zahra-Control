package com.abuzahra.control.util

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.dp(value: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics
    ).toInt()
}

fun Context.dp(value: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics
    ).toInt()
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    try {
        Toast.makeText(this, message, duration).show()
    } catch (_: Throwable) {}
}

fun String.parseColorSafe(): Int {
    return try {
        Color.parseColor(this)
    } catch (_: Throwable) {
        Color.TRANSPARENT
    }
}

fun Long.toFormattedDate(): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(this))
    } catch (_: Throwable) {
        ""
    }
}

fun Long.toTimeAgo(): String {
    val diff = System.currentTimeMillis() - this
    return when {
        diff < 60000 -> "الآن"
        diff < 3600000 -> "${diff / 60000} دقيقة"
        diff < 86400000 -> "${diff / 3600000} ساعة"
        else -> "${diff / 86400000} يوم"
    }
}

fun String.toSafeEmail(): String {
    return this.trim().lowercase()
}

fun String.isValidEmail(): Boolean {
    return this.contains("@") && this.contains(".") && this.length > 5
}
