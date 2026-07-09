package com.example.callandsmsblocker

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProvinceSelectionActivity : AppCompatActivity() {
    private lateinit var mgr: BlacklistManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mgr = BlacklistManager.get(this)

        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(container)

        val map = mgr.getPrefixesByProvince(this)
        val checkBoxes = mutableListOf<Pair<String, CheckBox>>()
        for ((province, prefixes) in map.entries.sortedBy { it.key }) {
            val cb = CheckBox(this).apply { text = "$province (${prefixes.size})" }
            container.addView(cb)
            checkBoxes.add(province to cb)
        }

        val btnImportSelected = Button(this).apply { text = "导入选中省/市号段" }
        btnImportSelected.setOnClickListener {
            val selected = checkBoxes.filter { it.second.isChecked }.map { it.first }
            if (selected.isEmpty()) {
                Toast.makeText(this, "请先选择至少一个省/市", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // merge prefixes from selected provinces
            val current = mgr.getPrefixes().toMutableSet()
            val map2 = mgr.getPrefixesByProvince(this)
            for (p in selected) {
                val list = map2[p] ?: continue
                current.addAll(list)
            }
            mgr.setPrefixes(current.sorted())
            Toast.makeText(this, "已导入 ${selected.size} 个省/市的号段，共 ${current.size} 个前缀", Toast.LENGTH_LONG).show()
            finish()
        }

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layout.addView(scroll)
        layout.addView(btnImportSelected)
        setContentView(layout)
    }
}
