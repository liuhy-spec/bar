package com.example.custommenu

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ConfigManager {
    private const val PREF_NAME = "custom_menu_prefs"
    private const val KEY_MENU_CONFIGS = "menu_configs"
    private const val KEY_NEXT_ID = "next_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveMenuConfigs(context: Context, configs: List<MenuConfig>) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(configs)
        editor.putString(KEY_MENU_CONFIGS, json)
        editor.apply()
    }

    fun loadMenuConfigs(context: Context): List<MenuConfig> {
        val prefs = getPreferences(context)
        val json = prefs.getString(KEY_MENU_CONFIGS, null)
        return if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<List<MenuConfig>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun getNextId(context: Context): Int {
        val prefs = getPreferences(context)
        val id = prefs.getInt(KEY_NEXT_ID, 1)
        prefs.edit().putInt(KEY_NEXT_ID, id + 1).apply()
        return id
    }

    fun addMenuConfig(context: Context, config: MenuConfig) {
        val configs = loadMenuConfigs(context).toMutableList()
        if (configs.size < MenuConfig.MAX_MENUS) {
            configs.add(config)
            saveMenuConfigs(context, configs)
        }
    }

    fun updateMenuConfig(context: Context, config: MenuConfig) {
        val configs = loadMenuConfigs(context).toMutableList()
        val index = configs.indexOfFirst { it.id == config.id }
        if (index != -1) {
            configs[index] = config
            saveMenuConfigs(context, configs)
        }
    }

    fun removeMenuConfig(context: Context, id: Int) {
        val configs = loadMenuConfigs(context).toMutableList()
        configs.removeAll { it.id == id }
        saveMenuConfigs(context, configs)
    }
}