package com.example.custommenu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class FloatMenuView(context: Context, private val config: MenuConfig) : LinearLayout(context) {
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var params: WindowManager.LayoutParams
    private var xOffset = 0
    private var yOffset = 0
    private var isDragging = false
    private var screenWidth = 0
    private var screenHeight = 0
    private var parentService: FloatMenuService? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val padding = (5 * resources.displayMetrics.density).toInt()
        setPadding(padding, padding, padding, padding)
        background = ContextCompat.getDrawable(context, R.drawable.float_menu_bg)
        
        val displayMetrics = context.resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        
        params = WindowManager.LayoutParams().apply {
            width = (config.width * resources.displayMetrics.density).toInt()
            height = (config.height * resources.displayMetrics.density).toInt()
            x = config.x
            y = config.y
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = android.graphics.PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
        }

        loadAppIcons()
    }
    
    fun setParentService(service: FloatMenuService) {
        parentService = service
    }
    
    private fun updateSize() {
        val scale = config.width.toFloat() / MenuConfig.DEFAULT_WIDTH
        scaleX = scale
        scaleY = scale
    }
    
    private fun loadAppIcons() {
        removeAllViews()
        
        val iconWidth = (config.iconWidth * resources.displayMetrics.density).toInt()
        val iconHeight = (config.iconHeight * resources.displayMetrics.density).toInt()
        val iconSpacing = (config.iconSpacing * resources.displayMetrics.density).toInt()
        val count = config.appPackageNames.size
        
        for ((index, packageName) in config.appPackageNames.withIndex()) {
            val icon = getAppIcon(packageName)
            val imageView = ImageView(context).apply {
                layoutParams = LayoutParams(iconWidth, iconHeight).apply {
                    val bottomMargin = if (index < count - 1) iconSpacing else 0
                    setMargins(0, 0, 0, bottomMargin)
                    gravity = Gravity.CENTER
                }
                setImageDrawable(icon ?: ContextCompat.getDrawable(context, R.mipmap.ic_launcher))
                scaleType = ImageView.ScaleType.FIT_CENTER
                setOnClickListener { launchApp(packageName) }
            }
            addView(imageView)
        }
        
        if (config.appPackageNames.isEmpty()) {
            val emptyView = ImageView(context).apply {
                layoutParams = LayoutParams(iconWidth, iconHeight)
                setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_launcher))
                scaleType = ImageView.ScaleType.FIT_CENTER
                alpha = 0.3f
            }
            addView(emptyView)
        }
    }
    
    private fun getAppIcon(packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = false
                xOffset = event.rawX.toInt() - params.x
                yOffset = event.rawY.toInt() - params.y
                if (parentService != null) {
                    parentService!!.bringToFront(this)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val movedX = kotlin.math.abs(event.rawX.toInt() - xOffset - params.x)
                val movedY = kotlin.math.abs(event.rawY.toInt() - yOffset - params.y)
                if (movedX > 10 || movedY > 10) {
                    isDragging = true
                }
                if (isDragging) {
                    var newX = event.rawX.toInt() - xOffset
                    var newY = event.rawY.toInt() - yOffset
                    
                    newX = newX.coerceAtLeast(0)
                    newX = newX.coerceAtMost(screenWidth - width)
                    newY = newY.coerceAtLeast(0)
                    newY = newY.coerceAtMost(screenHeight - height)
                    
                    params.x = newX
                    params.y = newY
                    windowManager.updateViewLayout(this, params)
                    
                    config.x = newX
                    config.y = newY
                    parentService?.saveConfig()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return isDragging
    }
    
    fun show() {
        try {
            windowManager.addView(this, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun hide() {
        try {
            windowManager.removeView(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun updateConfig(newConfig: MenuConfig) {
        config.x = newConfig.x
        config.y = newConfig.y
        config.width = newConfig.width
        config.height = newConfig.height
        config.iconWidth = newConfig.iconWidth
        config.iconHeight = newConfig.iconHeight
        config.iconSpacing = newConfig.iconSpacing
        config.isTop = newConfig.isTop
        config.appPackageNames = newConfig.appPackageNames
        
        loadAppIcons()
        
        params.x = config.x
        params.y = config.y
        params.width = (config.width * resources.displayMetrics.density).toInt()
        params.height = (config.height * resources.displayMetrics.density).toInt()
        windowManager.updateViewLayout(this, params)
    }
    
    fun getConfig(): MenuConfig {
        return config.copy()
    }
    
    fun getMenuId(): Int {
        return config.id
    }
}