package com.example.callandsmsblocker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LogsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val filterType = EditText(this).apply { hint = "筛选类型: all / call / sms (默认 all)" }
        val filterFrom = EditText(this).apply { hint = "起始时间戳(可空)" }
        val filterTo = EditText(this).apply { hint = "结束时间戳(可空)" }
        val btnApply = Button(this).apply { text = "应用筛选" }
        val btnExport = Button(this).apply { text = "导出筛选结果" }
        val btnClear = Button(this).apply { text = "清空拦截日志" }

        val scroll = ScrollView(this)
        val tv = TextView(this)
        scroll.addView(tv)

        container.addView(filterType)
        container.addView(filterFrom)
        container.addView(filterTo)
        container.addView(btnApply)
        container.addView(btnExport)
        container.addView(btnClear)
        container.addView(scroll)

        setContentView(container)

        fun applyAndShow() {
            val t = filterType.text.toString().trim().ifEmpty { "all" }
            val from = filterFrom.text.toString().toLongOrNull()
            val to = filterTo.text.toString().toLongOrNull()
            val db = data.AppDatabase.get(this)
            val dao = db.interceptLogDao()
            val list = when {
                t == "all" && from == null && to == null -> dao.getAll()
                t == "all" && from != null && to != null -> dao.getByTimeRange(from, to)
                t != "all" && from == null && to == null -> dao.getByType(t)
                t != "all" && from != null && to != null -> dao.getByTypeAndTime(t, from, to)
                else -> dao.getAll()
            }
            val sb = StringBuilder()
            for (e in list) {
                val time = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(e.timestamp))
                sb.append("[${e.type.uppercase()}] $time ${e.number}\n")
                if (!e.content.isNullOrEmpty()) sb.append("   ${e.content}\n")
            }
            tv.text = sb.toString()
        }

        btnApply.setOnClickListener { applyAndShow() }
        btnClear.setOnClickListener { InterceptLogManager.clearLogs(this); tv.text = ""; Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show() }
        btnExport.setOnClickListener {
            val t = filterType.text.toString().trim().ifEmpty { null }
            val from = filterFrom.text.toString().toLongOrNull()
            val to = filterTo.text.toString().toLongOrNull()
            val path = InterceptLogManager.exportLogsAndPrefixes(this, BlacklistManager.get(this).getPrefixes(), t, from, to)
            if (path != null) Toast.makeText(this, "已导出至 $path", Toast.LENGTH_LONG).show() else Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show()
        }

        // initial load
        applyAndShow()
    }
}
