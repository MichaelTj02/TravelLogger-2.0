package com.example.iat359_final_project.feature.tracking.presentation

sealed interface SessionUiEvent {
    data class SaveSession(
        val location: String,
        val steps: String,
        val sessionTitle: String
    ) : SessionUiEvent

    data object ConsumeSaveUi : SessionUiEvent
}
