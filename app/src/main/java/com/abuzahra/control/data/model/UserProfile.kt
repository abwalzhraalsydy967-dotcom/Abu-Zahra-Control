package com.abuzahra.control.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val role: String = "admin",
    val createdAt: Long = 0L,
    val deviceCount: Int = 0
)
