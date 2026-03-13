package com.simats.ocularsurfaceai.data

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("ocular_session", Context.MODE_PRIVATE)

    fun saveLogin(userId: Int, name: String, email: String, token: String) {
        prefs.edit()
            .putInt("user_id", userId)
            .putString("name", name)
            .putString("email", email)
            .putString("jwt_token", token)
            .putBoolean("logged_in", true)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("logged_in", false)

    fun getToken(): String? = prefs.getString("jwt_token", null)

    fun getUserId(): Int {
        return try {
            prefs.getInt("user_id", 0)
        } catch (e: ClassCastException) {
            prefs.getString("user_id", "0")?.toIntOrNull() ?: 0
        }
    }

    fun getName(): String = prefs.getString("name", "") ?: ""
    fun getEmail(): String = prefs.getString("email", "") ?: ""

    fun clear() {
        prefs.edit().clear().apply()
    }

    // --- NEW SETTINGS TOGGLE SAVES ---

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("DARK_MODE", isDark).apply()
    }

    fun isDarkMode(): Boolean {
        // false is the default (Light Mode)
        return prefs.getBoolean("DARK_MODE", false)
    }

    fun setNotificationsEnabled(isEnabled: Boolean) {
        prefs.edit().putBoolean("NOTIFICATIONS", isEnabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        // true is the default
        return prefs.getBoolean("NOTIFICATIONS", true) 
    }

    fun setAutoSave(isEnabled: Boolean) {
        prefs.edit().putBoolean("AUTO_SAVE", isEnabled).apply()
    }

    fun isAutoSaveEnabled(): Boolean {
        // true is the default
        return prefs.getBoolean("AUTO_SAVE", true) 
    }

    fun setShowConfidence(isEnabled: Boolean) {
        prefs.edit().putBoolean("SHOW_CONFIDENCE", isEnabled).apply()
    }

    fun isShowConfidenceEnabled(): Boolean {
        // true is the default
        return prefs.getBoolean("SHOW_CONFIDENCE", true)
    }
}
