package com.example.iat359_final_project.feature.home.presentation

data class HomeUiState(
    val navigation: HomeNavigation = HomeNavigation.None,
    val toastMessage: String? = null
)
