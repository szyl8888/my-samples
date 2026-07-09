package com.example.callandsmsblocker

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

data class InterceptLogEntry(val type: String, val number: String, val timestamp: Long, val content: String? = null) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("type", type)
        obj.put("number", number)
        obj.put("timestamp", timestamp)
        if (content != null) obj.put("content", content)
        return obj
    }
    companion object {
        fun fromJson(o: JSONObject): InterceptLogEntry = InterceptLogEntry(
            type = o.optString("type", "call"),
            number = o.optString("number", ""),
            timestamp = o.optLong("timestamp", 0L),
            content = if (o.has("content")) o.optString("content") else null
        )
    }
}

object InterceptLogManager {
    private const val PREFS = "blk"
    private const val KEY_LOGS = "intercept_logs"

    fun addCallLog(context: Context, number: String, timestamp: Long) {
        addLog(context, InterceptLogEntry("call", number, timestamp, null))
    }

    fun addSmsLog(context: Context, number: String, body: String, timestamp: Long) {
        addLog(context, InterceptLogEntry("sms", number, timestamp, body))
    }

    private fun addLog(context: Context, entry: InterceptLogEntry) {
        try {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_LOGS, null)
            val arr = if (raw.isNullOrEmpty()) JSONArray() else JSONArray(raw)
            arr.put(entry.toJson())
            prefs.edit().putString(KEY_LOGS, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e("InterceptLogManager", "addLog failed: ${e.message}")
        }
    }

    fun getLogs(context: Context): List<InterceptLogEntry> {
        try {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_LOGS, null) ?: return emptyList()
            val arr = JSONArray(raw)
            val out = mutableListOf<InterceptLogEntry>()
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                out.add(InterceptLogEntry.fromJson(o))
            }
            return out.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e("InterceptLogManager", "getLogs failed: ${e.message}")
            return emptyList()
        }
    }

    fun clearLogs(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_LOGS).apply()
    }

    // Export logs (and optionally prefixes) to a JSON file in app external files exports dir
    fun exportLogsAndPrefixes(context: Context, prefixes: List<String>): String? {
        try {
            val logs = getLogs(context)
            val root = JSONObject()
            root.put("exported_at", System.currentTimeMillis())
            val pArr = JSONArray()
            prefixes.forEach { pArr.put(it) }
            root.put("prefixes", pArr)
            val lArr = JSONArray()
            logs.forEach { lArr.put(it.toJson()) }
            root.put("intercept_logs", lArr)

            val exportsDir = java.io.File(context.getExternalFilesDir(null), "exports")
            if (!exportsDir.exists()) exportsDir.mkdirs()
            val outFile = java.io.File(exportsDir, "blacklist_and_logs_${System.currentTimeMillis()}.json")
            java.io.FileOutputStream(outFile).use { it.write(root.toString(2).toByteArray(Charsets.UTF_8)) }
            return outFile.absolutePath
        } catch (e: Exception) {
            Log.e("InterceptLogManager", "export failed: ${e.message}")
            return null
        }
    }
}
