package com.example.callandsmsblocker

import android.content.Context
import android.content.SharedPreferences

class BlacklistManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("blk", Context.MODE_PRIVATE)

    companion object {
        @Volatile private var INSTANCE: BlacklistManager? = null
        fun get(context: Context): BlacklistManager =
            INSTANCE ?: synchronized(this) { INSTANCE ?: BlacklistManager(context.applicationContext).also { INSTANCE = it } }
    }

    // 存储为逗号分隔的字符串（简化）
    fun getPrefixes(): List<String> = prefs.getString("prefixes", "+86137,+86138,+86139,+86150,+86151,+86152,+86153,+86155,+86156,+86157,+86158,+86159,+86130,+86131,+86132,+86133,+86134,+86135,+86136,+86170,+86171,+86172,+86173,+86174,+86175,+86176,+86177,+86178,+86179,+8610")!!.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    fun setPrefixes(list: List<String>) = prefs.edit().putString("prefixes", list.joinToString(",")).apply()

    fun getExactNumbers(): List<String> = prefs.getString("numbers", "")!!.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    fun setExactNumbers(list: List<String>) = prefs.edit().putString("numbers", list.joinToString(",")).apply()

    fun isBlockedNumber(normalized: String): Boolean {
        if (normalized.isEmpty()) return false
        // 精确匹配
        if (getExactNumbers().any { normalized == it }) return true
        // 前缀匹配
        if (getPrefixes().any { normalized.startsWith(it) }) return true
        return false
    }
}
