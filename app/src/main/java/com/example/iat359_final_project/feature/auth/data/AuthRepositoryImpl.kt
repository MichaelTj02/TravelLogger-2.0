package com.example.iat359_final_project.feature.auth.data

import android.content.Context
import com.example.iat359_final_project.feature.auth.domain.AuthRepository

class AuthRepositoryImpl(private val context: Context) : AuthRepository {

    private val defaultSentinel = AuthRepository.DEFAULT_CREDENTIAL_SENTINEL

    override fun getLoginCredentials(): Pair<String, String> {
        val prefs = context.getSharedPreferences(PREFS_MY_DATA, Context.MODE_PRIVATE)
        val u = prefs.getString(KEY_USERNAME, defaultSentinel) ?: defaultSentinel
        val p = prefs.getString(KEY_PASSWORD, defaultSentinel) ?: defaultSentinel
        return Pair(u, p)
    }

    override fun resetLoginCredentialsToDefault() {
        context.getSharedPreferences(PREFS_MY_DATA, Context.MODE_PRIVATE).edit()
            .putString(KEY_USERNAME, defaultSentinel)
            .putString(KEY_PASSWORD, defaultSentinel)
            .commit()
    }

    override fun saveSignupCredentials(username: String, password: String) {
        context.getSharedPreferences(PREFS_MY_DATA, Context.MODE_PRIVATE).edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .commit()
    }

    override fun getSignupScreenPrefs(): Pair<String, String> {
        val prefs = context.getSharedPreferences(PREFS_MY_PREFS, Context.MODE_PRIVATE)
        val u = prefs.getString(KEY_USERNAME, defaultSentinel) ?: defaultSentinel
        val p = prefs.getString(KEY_PASSWORD, defaultSentinel) ?: defaultSentinel
        return Pair(u, p)
    }

    companion object {
        private const val PREFS_MY_DATA = "MyData"
        private const val PREFS_MY_PREFS = "MyPrefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
    }
}
