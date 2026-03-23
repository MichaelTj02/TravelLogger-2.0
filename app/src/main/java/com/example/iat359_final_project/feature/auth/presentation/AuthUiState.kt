package com.example.iat359_final_project.feature.auth.presentation

data class AuthUiState(
    val message: String? = null,
    val navigation: AuthNavigation = AuthNavigation.None
)
