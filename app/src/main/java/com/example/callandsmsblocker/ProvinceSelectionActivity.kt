package com.example.callandsmsblocker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProvinceSelectionActivity : AppCompatActivity() {
    private lateinit var mgr: BlacklistManager
    private lateinit var adapter: ProvinceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mgr = BlacklistManager.get(this)

        val map = mgr.getPrefixesByProvince(this)
        val items = map.entries.sortedBy { it.key }.map { ProvinceItem(it.key, it.value) }

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val search = EditText(this).apply { hint = "搜索省/市" }
        layout.addView(search)

        val rv = RecyclerView(this)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ProvinceAdapter(items.toMutableList())
        rv.adapter = adapter
        layout.addView(rv)

        val btnPreview = Button(this).apply { text = "预览选中前缀" }
        val btnImportSelected = Button(this).apply { text = "导入选中省/市号段" }
        layout.addView(btnPreview)
        layout.addView(btnImportSelected)

        btnPreview.setOnClickListener {
            val selected = adapter.getSelectedProvinces()
            if (selected.isEmpty()) {
                Toast.makeText(this, "请先选择省/市", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            val merged = mutableSetOf<String>()
            for (p in selected) merged.addAll(p.prefixes)
            val preview = merged.sorted().joinToString("\n")
            AlertDialog.Builder(this).setTitle("将要导入的前缀 (${merged.size})").setMessage(preview.take(10000)).setPositiveButton("确定") { d, _ -> d.dismiss() }.show()
        }

        btnImportSelected.setOnClickListener {
            val selected = adapter.getSelectedProvinces()
            if (selected.isEmpty()) {
                Toast.makeText(this, "请先选择省/市", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            val current = mgr.getPrefixes().toMutableSet()
            for (p in selected) current.addAll(p.prefixes)
            mgr.setPrefixes(current.sorted())
            Toast.makeText(this, "已导入 ${selected.size} 个省/市的号段，共 ${current.size} 个前缀", Toast.LENGTH_LONG).show()
            finish()
        }

        setContentView(layout)

        search.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { adapter.filter(s?.toString() ?: "") }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
