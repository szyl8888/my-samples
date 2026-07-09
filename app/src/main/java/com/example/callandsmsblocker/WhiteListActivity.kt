package com.example.callandsmsblocker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WhiteListActivity : AppCompatActivity() {
    private lateinit var mgr: WhiteListManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mgr = WhiteListManager.get(this)

        val et = EditText(this).apply { hint = "添加允许的完整号码 (E.164)，多个用逗号分隔" }
        val btnAdd = Button(this).apply { text = "添加到白名单" }
        val btnClear = Button(this).apply { text = "清空白名单" }
        val btnShow = Button(this).apply { text = "查看白名单" }

        btnAdd.setOnClickListener {
            val s = et.text.toString()
            val parts = s.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val cur = mgr.getWhiteList().toMutableSet()
            cur.addAll(parts)
            mgr.setWhiteList(cur.sorted())
            Toast.makeText(this, "已添加 ${parts.size} 个号码", Toast.LENGTH_SHORT).show()
        }
        btnClear.setOnClickListener { mgr.setWhiteList(emptyList()); Toast.makeText(this, "已清空白名单", Toast.LENGTH_SHORT).show() }
        btnShow.setOnClickListener { val list = mgr.getWhiteList(); Toast.makeText(this, list.joinToString(", ").take(200), Toast.LENGTH_LONG).show() }

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layout.addView(et); layout.addView(btnAdd); layout.addView(btnShow); layout.addView(btnClear)
        setContentView(layout)
    }
}
