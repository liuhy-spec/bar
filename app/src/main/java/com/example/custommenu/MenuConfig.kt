package com.example.custommenu

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class MenuConfig(
    val id: Int,
    var x: Int = 50,
    var y: Int = 200,
    var width: Int = 60,
    var height: Int = 200,
    var iconWidth: Int = 50,
    var iconHeight: Int = 50,
    var iconSpacing: Int = 2,
    var isTop: Boolean = false,
    var appPackageNames: List<String> = emptyList()
) {
    companion object {
        const val MAX_MENUS = 10
        const val DEFAULT_WIDTH = 60
        const val DEFAULT_HEIGHT = 200
        const val DEFAULT_ICON_WIDTH = 50
        const val DEFAULT_ICON_HEIGHT = 50
        const val DEFAULT_ICON_SPACING = 2
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

data class MusicInfo(
    var isPlaying: Boolean = false,
    var title: String = "",
    var artist: String = "",
    var album: String = "",
    var albumArt: Bitmap? = null,
    var duration: Long = 0,
    var currentPosition: Long = 0
)