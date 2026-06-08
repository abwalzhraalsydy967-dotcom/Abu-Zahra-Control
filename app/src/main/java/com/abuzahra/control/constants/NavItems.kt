package com.abuzahra.control.constants

object NavItems {
    data class NavItem(val id: Int, val label: String, val icon: String)
    
    val items = listOf(
        NavItem(R.id.nav_dashboard, "لوحة التحكم", "D"),
        NavItem(R.id.nav_control, "التحكم", "C"),
        NavItem(R.id.nav_sms_calls, "الرسائل", "S"),
        NavItem(R.id.nav_files, "الملفات", "F"),
        NavItem(R.id.nav_social, "التواصل", "P"),
        NavItem(R.id.nav_settings, "الإعدادات", "G")
    )
}
