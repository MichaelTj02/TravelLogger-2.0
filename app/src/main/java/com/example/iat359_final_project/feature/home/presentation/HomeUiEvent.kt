package com.example.iat359_final_project.feature.home.presentation

sealed interface HomeUiEvent {
    data class VoiceCommand(val text: String) : HomeUiEvent
    data object ConsumeNavigation : HomeUiEvent
    data object ConsumeToast : HomeUiEvent
}
