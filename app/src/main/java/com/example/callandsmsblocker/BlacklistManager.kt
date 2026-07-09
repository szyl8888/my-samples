package com.example.callandsmsblocker

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class BlacklistManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("blk", Context.MODE_PRIVATE)

    companion object {
        @Volatile private var INSTANCE: BlacklistManager? = null
        fun get(context: Context): BlacklistManager =
            INSTANCE ?: synchronized(this) { INSTANCE ?: BlacklistManager(context.applicationContext).also { INSTANCE = it } }
    }

    // 存储为逗号分隔的字符串（简化）
    fun getPrefixes(): List<String> = prefs.getString("prefixes", null)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        ?: defaultInitialPrefixes()
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

    // Load default prefixes from bundled assets (china_prefixes.json)
    fun loadDefaultPrefixesFromAssets(context: Context, assetName: String = "china_prefixes.json") {
        try {
            val isr = context.assets.open(assetName)
            val json = isr.bufferedReader().use { it.readText() }
            val obj = JSONObject(json)
            val allPrefixes = mutableSetOf<String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = obj.optJSONArray(k)
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val p = arr.optString(i).trim()
                        if (p.isNotEmpty()) allPrefixes.add(p)
                    }
                }
            }
            // Save sorted
            val list = allPrefixes.sorted()
            setPrefixes(list)
            Log.i("BlacklistManager", "Imported ${list.size} prefixes from $assetName")
        } catch (e: Exception) {
            Log.e("BlacklistManager", "Failed to load default prefixes: ${e.message}")
        }
    }

    fun exportPrefixesToFile(context: Context): String? {
        return try {
            val prefixes = getPrefixes()
            val json = JSONObject()
            json.put("exported_at", System.currentTimeMillis())
            json.put("prefixes", prefixes)
            val exportsDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportsDir.exists()) exportsDir.mkdirs()
            val outFile = File(exportsDir, "blacklist_prefixes_${System.currentTimeMillis()}.json")
            FileOutputStream(outFile).use { it.write(json.toString(2).toByteArray(Charsets.UTF_8)) }
            outFile.absolutePath
        } catch (e: Exception) {
            Log.e("BlacklistManager", "Export failed: ${e.message}")
            null
        }
    }

    fun getPrefixesByProvince(context: Context, assetName: String = "china_prefixes.json"): Map<String, List<String>> {
        val map = mutableMapOf<String, List<String>>()
        try {
            val isr = context.assets.open(assetName)
            val json = isr.bufferedReader().use { it.readText() }
            val obj = JSONObject(json)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val arr = obj.optJSONArray(k)
                if (arr != null) {
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        val p = arr.optString(i).trim()
                        if (p.isNotEmpty()) list.add(p)
                    }
                    map[k] = list
                }
            }
        } catch (e: Exception) {
            Log.e("BlacklistManager", "Failed to read prefixes by province: ${e.message}")
        }
        return map
    }

    private fun defaultInitialPrefixes(): List<String> {
        // Fallback default (subset) if prefs empty
        return listOf(
            "+86130",
            "+86131",
            "+86132",
            "+86133",
            "+86134",
            "+86135",
            "+86136",
            "+86137",
            "+86138",
            "+86139",
            "+86150",
            "+86151",
            "+86152",
            "+86153",
            "+86155",
            "+86156",
            "+86157",
            "+86158",
            "+86159",
            "+8610"
        )
    }
}
