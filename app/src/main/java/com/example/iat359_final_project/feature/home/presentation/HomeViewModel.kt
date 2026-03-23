package com.example.iat359_final_project.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.VoiceCommand -> handleVoiceCommand(event.text)
            HomeUiEvent.ConsumeNavigation -> _uiState.update { it.copy(navigation = HomeNavigation.None) }
            HomeUiEvent.ConsumeToast -> _uiState.update { it.copy(toastMessage = null) }
        }
    }

    private fun handleVoiceCommand(spokenText: String) {
        viewModelScope.launch {
            val command = spokenText.lowercase(Locale.getDefault())
            when {
                command.contains("check logs") -> {
                    _uiState.update { it.copy(navigation = HomeNavigation.OpenLogs) }
                }
                command.contains("view map") -> {
                    _uiState.update { it.copy(navigation = HomeNavigation.OpenMap) }
                }
                command.contains("start session") -> {
                    _uiState.update { it.copy(navigation = HomeNavigation.OpenTracking) }
                }
                command.contains("view information") -> {
                    _uiState.update { it.copy(navigation = HomeNavigation.OpenWeatherSearch) }
                }
                else -> {
                    _uiState.update { it.copy(toastMessage = "Command not recognized") }
                }
            }
        }
    }
}
