package com.example.callandsmsblocker

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object InterceptLogManager {
    fun addCallLog(context: Context, number: String, timestamp: Long) {
        try {
            val db = data.AppDatabase.get(context)
            val dao = db.interceptLogDao()
            dao.insert(data.InterceptLogEntity(type="call", number = number, timestamp = timestamp, content = null))
        } catch (e: Exception) {
            Log.e("InterceptLogManager", "addCallLog failed: ${e.message}")
        }
    }

    fun addSmsLog(context: Context, number: String, body: String, timestamp: Long) {
        try {
            val db = data.AppDatabase.get(context)
            val dao = db.interceptLogDao()
            dao.insert(data.InterceptLogEntity(type="sms", number = number, timestamp = timestamp, content = body))
        } catch (e: Exception) {
            Log.e("InterceptLogManager", "addSmsLog failed: ${e.message}")
        }
    }

    fun getLogs(context: Context): List<data.InterceptLogEntity> {
        return try {
            val db = data.AppDatabase.get(context)
            db.interceptLogDao().getAll()
        } catch (e: Exception) {
            Log.e("InterceptLogManager", "getLogs failed: ${e.message}")
            emptyList()
        }
    }

    fun clearLogs(context: Context) {
        try {
            val db = data.AppDatabase.get(context)
            db.interceptLogDao().clearAll()
        } catch (e: Exception) {
            Log.e("InterceptLogManager", "clearLogs failed: ${e.message}")
        }
    }

    fun exportLogsAndPrefixes(context: Context, prefixes: List<String>, typeFilter: String? = null, from: Long? = null, to: Long? = null): String? {
        try {
            val db = data.AppDatabase.get(context)
            val dao = db.interceptLogDao()
            val logs = when {
                typeFilter != null && from != null && to != null -> dao.getByTypeAndTime(typeFilter, from, to)
                typeFilter != null && from == null && to == null -> dao.getByType(typeFilter)
                typeFilter == null && from != null && to != null -> dao.getByTimeRange(from, to)
                else -> dao.getAll()
            }
            val root = JSONObject()
            root.put("exported_at", System.currentTimeMillis())
            val pArr = JSONArray()
            prefixes.forEach { pArr.put(it) }
            root.put("prefixes", pArr)
            val lArr = JSONArray()
            logs.forEach { e ->
                val o = JSONObject()
                o.put("type", e.type)
                o.put("number", e.number)
                o.put("timestamp", e.timestamp)
                if (e.content != null) o.put("content", e.content)
                lArr.put(o)
            }
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
