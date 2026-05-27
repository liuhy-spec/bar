package com.example.custommenu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat

class MusicControllerView(context: Context) : View(context) {
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var params: WindowManager.LayoutParams
    private var view: View
    private var xOffset = 0
    private var yOffset = 0
    private var isDragging = false
    private var screenWidth = 0
    private var screenHeight = 0
    
    private lateinit var ivAlbumArt: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var btnPrev: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var seekBar: SeekBar
    
    private var musicInfo = MusicInfo()
    private var musicCallback: MusicCallback? = null
    
    interface MusicCallback {
        fun onPlayPause()
        fun onPrev()
        fun onNext()
        fun onSeekTo(position: Long)
    }
    
    init {
        val displayMetrics = context.resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        
        view = LayoutInflater.from(context).inflate(R.layout.music_controller, null)
        initViews()
        
        params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = screenWidth - 60
            y = screenHeight / 2 - 100
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = android.graphics.PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
        }
        
        setupListeners()
    }
    
    private fun initViews() {
        ivAlbumArt = view.findViewById(R.id.iv_album_art)
        tvTitle = view.findViewById(R.id.tv_title)
        tvArtist = view.findViewById(R.id.tv_artist)
        btnPrev = view.findViewById(R.id.btn_prev)
        btnPlayPause = view.findViewById(R.id.btn_play_pause)
        btnNext = view.findViewById(R.id.btn_next)
        seekBar = view.findViewById(R.id.seek_bar)
    }
    
    private fun setupListeners() {
        btnPrev.setOnClickListener {
            musicCallback?.onPrev()
        }
        
        btnPlayPause.setOnClickListener {
            musicCallback?.onPlayPause()
        }
        
        btnNext.setOnClickListener {
            musicCallback?.onNext()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && musicInfo.duration > 0) {
                    val position = (progress.toLong() * musicInfo.duration) / 100
                    musicCallback?.onSeekTo(position)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        view.setOnTouchListener { _, event ->
            handleTouchEvent(event)
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private fun handleTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                xOffset = event.rawX.toInt() - params.x
                yOffset = event.rawY.toInt() - params.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    var newX = event.rawX.toInt() - xOffset
                    var newY = event.rawY.toInt() - yOffset
                    
                    newX = newX.coerceAtLeast(0)
                    newX = newX.coerceAtMost(screenWidth - view.width)
                    newY = newY.coerceAtLeast(0)
                    newY = newY.coerceAtMost(screenHeight - view.height)
                    
                    params.x = newX
                    params.y = newY
                    windowManager.updateViewLayout(view, params)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return isDragging
    }
    
    fun setMusicCallback(callback: MusicCallback) {
        musicCallback = callback
    }
    
    fun updateMusicInfo(info: MusicInfo) {
        musicInfo = info
        
        tvTitle.text = info.title
        tvArtist.text = info.artist
        
        if (info.albumArt != null) {
            ivAlbumArt.setImageBitmap(info.albumArt)
        } else {
            ivAlbumArt.setImageResource(R.mipmap.ic_launcher)
        }
        
        btnPlayPause.setImageResource(if (info.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        
        if (info.duration > 0) {
            val progress = ((info.currentPosition * 100) / info.duration).toInt()
            seekBar.progress = progress
        }
    }
    
    fun show() {
        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun hide() {
        try {
            windowManager.removeView(view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun isVisible(): Boolean {
        return view.parent != null
    }
}