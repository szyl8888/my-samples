package com.example.callandsmsblocker

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LogsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val tv = TextView(this)
        scroll.addView(tv)

        val btnClear = Button(this).apply { text = "清空拦截日志" }
        btnClear.setOnClickListener {
            InterceptLogManager.clearLogs(this)
            tv.text = ""
            Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show()
        }

        val logs = InterceptLogManager.getLogs(this)
        val sb = StringBuilder()
        for (l in logs) {
            val t = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(l.timestamp))
            sb.append("[${l.type.uppercase()}] $t ${l.number}\n")
            if (!l.content.isNullOrEmpty()) sb.append("   ${l.content}\n")
        }
        tv.text = sb.toString()

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layout.addView(scroll)
        layout.addView(btnClear)
        setContentView(layout)
    }
}
