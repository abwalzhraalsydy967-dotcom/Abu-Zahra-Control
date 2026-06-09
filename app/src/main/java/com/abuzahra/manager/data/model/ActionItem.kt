package com.abuzahra.manager.data.model

data class ActionItem(
    val icon: String,
    val name: String,
    val command: String,
    val params: String = "",
    val category: String = ""
)
