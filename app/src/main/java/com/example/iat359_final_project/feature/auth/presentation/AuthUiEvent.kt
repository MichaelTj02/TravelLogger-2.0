package com.example.iat359_final_project.feature.auth.presentation

sealed interface AuthUiEvent {
    data class SubmitLogin(val username: String, val password: String) : AuthUiEvent
    data class SubmitSignup(val username: String, val password: String) : AuthUiEvent
    data object CheckSignupGate : AuthUiEvent
    data object ConsumeMessage : AuthUiEvent
    data object ConsumeNavigation : AuthUiEvent
}
