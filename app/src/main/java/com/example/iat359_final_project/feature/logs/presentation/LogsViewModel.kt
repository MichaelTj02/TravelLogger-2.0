package com.example.iat359_final_project.feature.logs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iat359_final_project.feature.logs.domain.LogsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LogsViewModel(
    private val repository: LogsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    fun onEvent(event: LogsUiEvent) {
        when (event) {
            LogsUiEvent.LoadLogs -> loadLogs()
            is LogsUiEvent.SearchLogs -> searchLogs(event.query)
            is LogsUiEvent.DeleteLog -> deleteLog(event.sessionTitle)
            LogsUiEvent.DeleteAllLogs -> deleteAllLogs()
            LogsUiEvent.ConsumeMessage -> {
                _uiState.update { it.copy(message = null) }
            }
        }
    }

    private fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val logs = repository.getAllLogs()
            _uiState.update {
                it.copy(
                    logs = logs,
                    isLoading = false
                )
            }
        }
    }

    private fun searchLogs(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val logs = repository.searchLogs(query)
            _uiState.update {
                it.copy(
                    logs = logs,
                    isLoading = false,
                    message = if (logs.isEmpty()) "No logs found for this location" else null
                )
            }
        }
    }

    private fun deleteLog(sessionTitle: String) {
        viewModelScope.launch {
            repository.deleteLog(sessionTitle)
            val logs = repository.getAllLogs()
            _uiState.update { it.copy(logs = logs) }
        }
    }

    private fun deleteAllLogs() {
        viewModelScope.launch {
            repository.deleteAllLogs()
            _uiState.update {
                it.copy(
                    logs = emptyList(),
                    message = "All logs deleted"
                )
            }
        }
    }
}

class LogsViewModelFactory(
    private val repository: LogsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
