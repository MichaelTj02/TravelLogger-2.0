package com.example.iat359_final_project.feature.logs.presentation

import com.example.iat359_final_project.domain.model.LogEntry

data class LogsUiState(
    val logs: List<LogEntry> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)
