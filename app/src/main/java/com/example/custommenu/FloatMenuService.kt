package com.example.custommenu

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log

class FloatMenuService : Service() {

    private val binder = LocalBinder()
    private val menuViews = mutableListOf<FloatMenuView>()
    private var musicController: MusicControllerView? = null
    private var isMusicPlaying = false
    private var currentMusicInfo = MusicInfo()

    private lateinit var audioManager: AudioManager
    private var mediaController: MediaController? = null
    private var mediaSessionCallback: MediaController.Callback? = null

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                handleMusicStopped()
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): FloatMenuService = this@FloatMenuService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        registerBecomingNoisyReceiver()
        try {
            setupMediaSessionListener()
        } catch (e: Exception) {
            Log.w("FloatMenuService", "Media session not available: ${e.message}")
        }
        loadMenuConfigs()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "custom_menu_channel",
                "Custom Menu",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Custom Menu Service"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "custom_menu_channel")
                .setContentTitle("Custom Menu")
                .setContentText("悬浮菜单运行中")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Custom Menu")
                .setContentText("悬浮菜单运行中")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        }
    }

    private fun registerBecomingNoisyReceiver() {
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, filter)
    }

    private fun setupMediaSessionListener() {
        try {
            val mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val controllers = mediaSessionManager.getActiveSessions(null)

            if (controllers.isNotEmpty()) {
                val controller = controllers.firstOrNull {
                    it.packageName.contains("music") ||
                    it.packageName.contains("audio")
                } ?: controllers.first()

                mediaController = controller
                setupMediaControllerCallback()
                updateMusicInfo()
            }
        } catch (e: SecurityException) {
            Log.w("FloatMenuService", "Cannot access media sessions due to permission: ${e.message}")
        } catch (e: Exception) {
            Log.e("FloatMenuService", "Error setting up media session listener", e)
        }
    }

    private fun setupMediaControllerCallback() {
        mediaSessionCallback = object : MediaController.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackState?) {
                super.onPlaybackStateChanged(state)
                if (state != null) {
                    isMusicPlaying = state.state == PlaybackState.STATE_PLAYING
                    currentMusicInfo.isPlaying = isMusicPlaying
                    updateMusicDisplay()
                }
            }

            override fun onMetadataChanged(metadata: MediaMetadata?) {
                super.onMetadataChanged(metadata)
                if (metadata != null) {
                    currentMusicInfo.title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
                    currentMusicInfo.artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                    currentMusicInfo.album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
                    currentMusicInfo.duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)

                    val albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    currentMusicInfo.albumArt = albumArt

                    updateMusicDisplay()
                }
            }
        }

        mediaController?.registerCallback(mediaSessionCallback!!)
    }

    private fun updateMusicInfo() {
        val metadata = mediaController?.metadata
        val state = mediaController?.playbackState

        if (metadata != null) {
            currentMusicInfo.title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
            currentMusicInfo.artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
            currentMusicInfo.album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
            currentMusicInfo.duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
            currentMusicInfo.albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        }

        if (state != null) {
            isMusicPlaying = state.state == PlaybackState.STATE_PLAYING
            currentMusicInfo.isPlaying = isMusicPlaying
            currentMusicInfo.currentPosition = state.position
        }
    }

    private fun updateMusicDisplay() {
        if (isMusicPlaying && currentMusicInfo.title.isNotEmpty()) {
            hideAllMenus()
            showMusicController()
        } else {
            hideMusicController()
            showAllMenus()
        }

        musicController?.updateMusicInfo(currentMusicInfo)
    }

    private fun handleMusicStopped() {
        isMusicPlaying = false
        currentMusicInfo.isPlaying = false
        hideMusicController()
        showAllMenus()
    }

    private fun loadMenuConfigs() {
        val configs = ConfigManager.loadMenuConfigs(this)
        for (config in configs) {
            addMenuView(config)
        }
    }

    fun addMenuView(config: MenuConfig) {
        if (menuViews.size >= MenuConfig.MAX_MENUS) {
            return
        }

        val menuView = FloatMenuView(this, config)
        menuView.setParentService(this)
        menuViews.add(menuView)

        if (!isMusicPlaying) {
            menuView.show()
        }
    }

    fun removeMenuView(id: Int) {
        val menuView = menuViews.find { it.getMenuId() == id }
        if (menuView != null) {
            menuView.hide()
            menuViews.remove(menuView)
            ConfigManager.removeMenuConfig(this, id)
        }
    }

    fun updateMenuView(config: MenuConfig) {
        val menuView = menuViews.find { it.getMenuId() == config.id }
        if (menuView != null) {
            menuView.updateConfig(config)
            ConfigManager.updateMenuConfig(this, config)
        }
    }

    fun hideAllMenus() {
        menuViews.forEach { it.hide() }
    }

    fun showAllMenus() {
        menuViews.forEach { it.show() }
    }

    fun showMusicController() {
        if (musicController == null) {
            musicController = MusicControllerView(this).apply {
                setMusicCallback(object : MusicControllerView.MusicCallback {
                    override fun onPlayPause() {
                        if (currentMusicInfo.isPlaying) {
                            mediaController?.transportControls?.pause()
                        } else {
                            mediaController?.transportControls?.play()
                        }
                    }

                    override fun onPrev() {
                        mediaController?.transportControls?.skipToPrevious()
                    }

                    override fun onNext() {
                        mediaController?.transportControls?.skipToNext()
                    }

                    override fun onSeekTo(position: Long) {
                        mediaController?.transportControls?.seekTo(position)
                    }
                })
            }
        }

        musicController?.apply {
            updateMusicInfo(currentMusicInfo)
            if (!isVisible()) {
                show()
            }
        }
    }

    fun hideMusicController() {
        musicController?.hide()
    }

    fun bringToFront(menuView: FloatMenuView) {
        menuView.hide()
        menuView.show()
    }

    fun saveConfig() {
        val configs = menuViews.map { it.getConfig() }
        ConfigManager.saveMenuConfigs(this, configs)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(becomingNoisyReceiver)
        mediaSessionCallback?.let { mediaController?.unregisterCallback(it) }
        menuViews.forEach { it.hide() }
        musicController?.hide()
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, FloatMenuService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}