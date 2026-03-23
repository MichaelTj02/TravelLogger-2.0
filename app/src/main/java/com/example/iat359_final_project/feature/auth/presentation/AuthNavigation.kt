package com.example.iat359_final_project.feature.auth.presentation

sealed interface AuthNavigation {
    data object None : AuthNavigation
    data object OpenMain : AuthNavigation
    data object OpenLogin : AuthNavigation
    data object OpenSignup : AuthNavigation
}
