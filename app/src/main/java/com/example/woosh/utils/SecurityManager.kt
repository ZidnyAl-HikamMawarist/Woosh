package com.example.woosh.utils

import android.content.Context

class SecurityManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("woosh_security_prefs", Context.MODE_PRIVATE)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean("biometric_enabled", false)
    }

    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notification_enabled", enabled).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return prefs.getBoolean("notification_enabled", true)
    }

    fun setLanguage(language: String) {
        prefs.edit().putString("app_language", language).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("app_language", "ID") ?: "ID"
    }
}
