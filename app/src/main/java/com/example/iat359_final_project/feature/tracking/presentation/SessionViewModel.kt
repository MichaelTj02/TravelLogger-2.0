package com.example.iat359_final_project.feature.tracking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iat359_final_project.feature.tracking.domain.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionViewModel(
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    fun onEvent(event: SessionUiEvent) {
        when (event) {
            is SessionUiEvent.SaveSession -> saveSession(event)
            SessionUiEvent.ConsumeSaveUi -> _uiState.update {
                it.copy(saveJustFinished = false, finishedStepCount = 0)
            }
        }
    }

    private fun saveSession(event: SessionUiEvent.SaveSession) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertSession(event.location, event.steps, event.sessionTitle)
            }
            val stepsInt = event.steps.toIntOrNull() ?: 0
            _uiState.update {
                it.copy(saveJustFinished = true, finishedStepCount = stepsInt)
            }
        }
    }
}

class SessionViewModelFactory(
    private val repository: SessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
