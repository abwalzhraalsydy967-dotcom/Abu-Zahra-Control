package com.abuzahra.control.constants

import com.abuzahra.control.R

object NavItems {
    data class NavItem(val id: Int, val label: String, val icon: String)

    val items = listOf(
        NavItem(R.id.nav_dashboard, "الرئيسية", "D"),
        NavItem(R.id.nav_control, "التحكم", "C"),
        NavItem(R.id.nav_sms_calls, "الرسائل", "S"),
        NavItem(R.id.nav_files, "الملفات", "F"),
        NavItem(R.id.nav_social, "الدردشة", "P"),
        NavItem(R.id.nav_settings, "الإعدادات", "G")
    )
}
