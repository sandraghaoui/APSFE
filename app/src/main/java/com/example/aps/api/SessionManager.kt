package com.example.aps.api

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aps_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    // Save the Supabase JWT
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    // Called by AuthInterceptor when attaching token
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // Logout (clear session)
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // Check if logged in
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
