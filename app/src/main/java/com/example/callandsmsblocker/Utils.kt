package com.example.callandsmsblocker

object Utils {
    // 规范化号码：使用 libphonenumber 更稳健的解析，但也保留简单 fallback
    fun normalizeNumber(raw: String?, defaultCountry: String = "CN"): String {
        if (raw == null) return ""
        val s = raw.replace(Regex("[^+0-9]"), "")
        if (s.isEmpty()) return ""
        try {
            val phoneUtil = com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance()
            val number = phoneUtil.parse(raw, defaultCountry)
            return phoneUtil.format(number, com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: Exception) {
            // fallback
            if (s.startsWith("+")) return s
            // if starts with 0 (local trunk), return as-is
            if (s.startsWith("0")) return s
            return "+86" + s
        }
    }
}
