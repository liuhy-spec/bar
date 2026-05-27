package com.example.custommenu

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import android.text.TextWatcher
import android.text.Editable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    
    private lateinit var btnAddMenu: Button
    private lateinit var recyclerMenus: RecyclerView
    private lateinit var menuAdapter: MenuListAdapter
    
    private var menuConfigs = mutableListOf<MenuConfig>()
    private var selectedApps = mutableListOf<String>()
    private var editingMenuId: Int? = null
    
    private var service: FloatMenuService? = null
    private var isServiceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val localBinder = binder as FloatMenuService.LocalBinder
            service = localBinder.getService()
            isServiceBound = true
        }
        
        override fun onServiceDisconnected(arg0: ComponentName) {
            service = null
            isServiceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        requestNotificationPermission()
        initViews()
        loadMenuConfigs()
        bindService()
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION)
            }
        }
    }
    
    private fun initViews() {
        btnAddMenu = findViewById(R.id.btn_add_menu)
        recyclerMenus = findViewById(R.id.recycler_menus)
        
        btnAddMenu.setOnClickListener {
            if (menuConfigs.size >= MenuConfig.MAX_MENUS) {
                Toast.makeText(this, "最多支持${MenuConfig.MAX_MENUS}个菜单栏", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            checkOverlayPermissionAndAddMenu()
        }
        
        recyclerMenus.layoutManager = LinearLayoutManager(this)
        updateMenuList()
    }
    
    private fun checkOverlayPermissionAndAddMenu() {
        if (!PermissionHelper.hasOverlayPermission(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PermissionHelper.requestOverlayPermission(this, REQUEST_OVERLAY_PERMISSION)
            }
        } else {
            showAppSelectDialog(null)
        }
    }
    
    private fun loadMenuConfigs() {
        menuConfigs = ConfigManager.loadMenuConfigs(this).toMutableList()
        updateMenuList()
    }
    
    private fun updateMenuList() {
        menuAdapter = MenuListAdapter(this, menuConfigs,
            { menu ->
                showConfigDialog(menu)
            },
            { menu ->
                editingMenuId = menu.id
                selectedApps = menu.appPackageNames.toMutableList()
                showAppSelectDialog(menu)
            },
            { id ->
                AlertDialog.Builder(this)
                    .setTitle("确认删除")
                    .setMessage("确定要删除这个菜单栏吗？")
                    .setPositiveButton("确定") { _, _ ->
                        service?.removeMenuView(id)
                        menuConfigs.removeAll { it.id == id }
                        updateMenuList()
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        )
        recyclerMenus.adapter = menuAdapter
    }
    
    private fun showConfigDialog(menu: MenuConfig) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_menu_config, null)
        
        val etWidth = dialogView.findViewById<EditText>(R.id.et_width)
        val etHeight = dialogView.findViewById<EditText>(R.id.et_height)
        val etIconWidth = dialogView.findViewById<EditText>(R.id.et_icon_width)
        val etIconHeight = dialogView.findViewById<EditText>(R.id.et_icon_height)
        val etIconSpacing = dialogView.findViewById<EditText>(R.id.et_icon_spacing)
        val etPositionX = dialogView.findViewById<EditText>(R.id.et_position_x)
        val etPositionY = dialogView.findViewById<EditText>(R.id.et_position_y)
        
        etWidth.setText(menu.width.toString())
        etHeight.setText(menu.height.toString())
        etIconWidth.setText(menu.iconWidth.toString())
        etIconHeight.setText(menu.iconHeight.toString())
        etIconSpacing.setText(menu.iconSpacing.toString())
        etPositionX.setText(menu.x.toString())
        etPositionY.setText(menu.y.toString())
        
        AlertDialog.Builder(this)
            .setTitle("配置菜单栏 #${menu.id}")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                val newWidth = etWidth.text.toString().toIntOrNull() ?: menu.width
                val newHeight = etHeight.text.toString().toIntOrNull() ?: menu.height
                val newIconWidth = etIconWidth.text.toString().toIntOrNull() ?: menu.iconWidth
                val newIconHeight = etIconHeight.text.toString().toIntOrNull() ?: menu.iconHeight
                val newIconSpacing = etIconSpacing.text.toString().toIntOrNull() ?: menu.iconSpacing
                val newX = etPositionX.text.toString().toIntOrNull() ?: menu.x
                val newY = etPositionY.text.toString().toIntOrNull() ?: menu.y
                
                if (newWidth < 5) {
                    Toast.makeText(this, "菜单宽度不能小于5dp", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newHeight < 5) {
                    Toast.makeText(this, "菜单高度不能小于5dp", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newIconWidth < 5) {
                    Toast.makeText(this, "图标宽度不能小于5dp", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newIconHeight < 5) {
                    Toast.makeText(this, "图标高度不能小于5dp", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newIconSpacing < 0) {
                    Toast.makeText(this, "图标间距不能小于0dp", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val updatedMenu = menu.copy().apply {
                    width = newWidth
                    height = newHeight
                    iconWidth = newIconWidth
                    iconHeight = newIconHeight
                    iconSpacing = newIconSpacing
                    x = newX
                    y = newY
                }
                
                service?.updateMenuView(updatedMenu)
                
                val index = menuConfigs.indexOfFirst { it.id == menu.id }
                if (index != -1) {
                    menuConfigs[index] = updatedMenu
                }
                
                updateMenuList()
                Toast.makeText(this, "配置已更新", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showAppSelectDialog(menu: MenuConfig?) {
        val allApps = getInstalledApps()
        selectedApps = menu?.appPackageNames?.toMutableList() ?: mutableListOf()
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_app_select, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.et_search)
        val recyclerApps = dialogView.findViewById<RecyclerView>(R.id.recycler_apps)
        recyclerApps.layoutManager = LinearLayoutManager(this)
        
        val adapter = AppListAdapter(this, allApps, selectedApps) { app, isChecked ->
            if (isChecked) {
                selectedApps.add(app.packageName)
            } else {
                selectedApps.remove(app.packageName)
            }
        }
        recyclerApps.adapter = adapter
        
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                val filteredApps = if (query.isEmpty()) {
                    allApps
                } else {
                    allApps.filter { it.appName.contains(query, ignoreCase = true) }
                }
                adapter.updateApps(filteredApps)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        AlertDialog.Builder(this)
            .setTitle(if (menu == null) "选择应用" else "编辑应用")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                if (menu == null) {
                    addNewMenu()
                } else {
                    updateMenu(menu)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun getInstalledApps(): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (pkg in packages) {
            val appName = pm.getApplicationLabel(pkg).toString()
            if (appName.isEmpty()) {
                continue
            }
            
            try {
                val icon = pm.getApplicationIcon(pkg)
                apps.add(AppInfo(pkg.packageName, appName, icon))
            } catch (e: Exception) {
                continue
            }
        }
        
        return apps.distinctBy { it.packageName }.sortedBy { it.appName }
    }
    
    private fun isSystemApp(appInfo: android.content.pm.ApplicationInfo): Boolean {
        val packageName = appInfo.packageName
        
        if (packageName.startsWith("android.") || 
            packageName.startsWith("com.android.systemui") ||
            packageName == "android" ||
            packageName == "com.android.phone" ||
            packageName == "com.android.settings" ||
            packageName == "com.android.systemui") {
            return true
        }
        
        return false
    }
    
    private fun addNewMenu() {
        val config = MenuConfig(
            id = ConfigManager.getNextId(this),
            x = 50,
            y = 200,
            width = MenuConfig.DEFAULT_WIDTH,
            height = MenuConfig.DEFAULT_HEIGHT,
            isTop = false,
            appPackageNames = selectedApps.toList()
        )
        
        ConfigManager.addMenuConfig(this, config)
        menuConfigs.add(config)
        updateMenuList()
        
        service?.addMenuView(config)
        
        Toast.makeText(this, "已添加菜单栏", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateMenu(menu: MenuConfig) {
        menu.appPackageNames = selectedApps.toList()
        ConfigManager.updateMenuConfig(this, menu)
        service?.updateMenuView(menu)
        
        updateMenuList()
        editingMenuId = null
        
        Toast.makeText(this, "已更新", Toast.LENGTH_SHORT).show()
    }
    
    private fun bindService() {
        FloatMenuService.startService(this)
        val intent = Intent(this, FloatMenuService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (PermissionHelper.hasOverlayPermission(this)) {
                showAppSelectDialog(null)
            } else {
                Toast.makeText(this, "需要悬浮窗权限才能添加菜单栏", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
    
    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1001
        private const val REQUEST_NOTIFICATION_PERMISSION = 1002
    }
}