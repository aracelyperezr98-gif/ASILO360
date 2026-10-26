package com.example.asilo360.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeOption {
    SYSTEM, LIGHT, DARK
}

/**
 * Gestor global del tema de la aplicación Asilo 360 (Modo Claro / Modo Oscuro / Sistema).
 * Funciona globalmente para todos los roles de usuario (Administrador, Personal, Familiar).
 */
object ThemeManager {
    private const val PREFS_NAME = "asilo360_theme_prefs"
    private const val KEY_THEME_OPTION = "theme_option"

    private val _themeOption = MutableStateFlow(ThemeOption.SYSTEM)
    val themeOption: StateFlow<ThemeOption> = _themeOption.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_THEME_OPTION, ThemeOption.SYSTEM.name) ?: ThemeOption.SYSTEM.name
        val option = try {
            ThemeOption.valueOf(savedName)
        } catch (_: Exception) {
            ThemeOption.SYSTEM
        }
        _themeOption.value = option
    }

    fun setThemeOption(context: Context, option: ThemeOption) {
        _themeOption.value = option
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_OPTION, option.name).apply()
    }

    fun toggleDarkMode(context: Context, isDark: Boolean) {
        val newOption = if (isDark) ThemeOption.DARK else ThemeOption.LIGHT
        setThemeOption(context, newOption)
    }
}
