package com.example.callandsmsblocker

import android.content.Context

class WhiteListManager private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("wl", Context.MODE_PRIVATE)

    companion object {
        @Volatile private var INSTANCE: WhiteListManager? = null
        fun get(context: Context): WhiteListManager = INSTANCE ?: synchronized(this) { INSTANCE ?: WhiteListManager(context.applicationContext).also { INSTANCE = it } }
    }

    fun getWhiteList(): List<String> = prefs.getString("white_numbers", "")!!.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    fun setWhiteList(list: List<String>) = prefs.edit().putString("white_numbers", list.joinToString(",")).apply()
    fun isWhitelisted(number: String): Boolean {
        if (number.isEmpty()) return false
        if (getWhiteList().any { it == number }) return true
        // optionally check contacts
        if (ContactsUtil.isNumberInContacts(prefs.context ?: return false, number)) return true
        return false
    }
}
