package com.example.callandsmsblocker

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var mgr: BlacklistManager

    private val requestRoleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // 用户返回后提示
        Toast.makeText(this, "请在系统中将本应用设为默认短信应用以使短信拦截生效", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mgr = BlacklistManager.get(this)

        val btnSetSms = Button(this).apply { text = "设为默认短信应用" }
        btnSetSms.setOnClickListener { requestSetDefaultSmsApp() }
        val btnCallScreening = Button(this).apply { text = "打开来电筛选设置" }
        btnCallScreening.setOnClickListener { openCallScreeningSettings() }

        val btnImportDefaults = Button(this).apply { text = "导入默认号段(按省市)" }
        btnImportDefaults.setOnClickListener {
            mgr.loadDefaultPrefixesFromAssets(this)
            Toast.makeText(this, "已导入默认号段（请在黑名单列表中查看）", Toast.LENGTH_SHORT).show()
        }

        val btnExport = Button(this).apply { text = "导出当前黑名单为 JSON" }
        btnExport.setOnClickListener {
            val path = mgr.exportPrefixesToFile(this)
            if (path != null) Toast.makeText(this, "已导出至 $path", Toast.LENGTH_LONG).show() else Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show()
        }

        val et = EditText(this).apply { hint = "添加前缀或完整号码，逗号分隔（如 +8610,+86137,+8613800138000）" }
        val btnSave = Button(this).apply { text = "保存黑名单/前缀" }
        btnSave.setOnClickListener {
            val s = et.text.toString()
            val parts = s.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            mgr.setPrefixes(parts)
            Toast.makeText(this, "已保存 ${parts.size} 个前缀/号段", Toast.LENGTH_SHORT).show()
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(btnSetSms)
            addView(btnCallScreening)
            addView(btnImportDefaults)
            addView(btnExport)
            addView(et)
            addView(btnSave)
        }
        setContentView(layout)
    }

    private fun requestSetDefaultSmsApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS) && !roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                requestRoleLauncher.launch(intent)
                return
            }
        }
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        startActivity(intent)
    }

    private fun openCallScreeningSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "在系统默认应用或电话应用设置中启用来电筛选服务", Toast.LENGTH_LONG).show()
    }
}
