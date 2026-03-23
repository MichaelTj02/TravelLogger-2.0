package com.example.iat359_final_project.feature.auth.domain

/**
 * Auth data: MyData holds login credentials; MyPrefs used for signup-screen gate only (legacy behavior).
 */
interface AuthRepository {
    companion object {
        const val DEFAULT_CREDENTIAL_SENTINEL = "not available"
    }
    fun getLoginCredentials(): Pair<String, String>
    fun resetLoginCredentialsToDefault()
    fun saveSignupCredentials(username: String, password: String)
    fun getSignupScreenPrefs(): Pair<String, String>
}
