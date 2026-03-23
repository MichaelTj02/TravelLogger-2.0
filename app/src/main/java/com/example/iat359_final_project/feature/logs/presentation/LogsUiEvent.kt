package com.example.iat359_final_project.feature.logs.presentation

sealed interface LogsUiEvent {
    data object LoadLogs : LogsUiEvent
    data class SearchLogs(val query: String) : LogsUiEvent
    data class DeleteLog(val sessionTitle: String) : LogsUiEvent
    data object DeleteAllLogs : LogsUiEvent
    data object ConsumeMessage : LogsUiEvent
}
