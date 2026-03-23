package com.example.iat359_final_project.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iat359_final_project.feature.auth.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.SubmitLogin -> submitLogin(event.username, event.password)
            is AuthUiEvent.SubmitSignup -> submitSignup(event.username, event.password)
            AuthUiEvent.CheckSignupGate -> checkSignupGate()
            AuthUiEvent.ConsumeMessage -> _uiState.update { it.copy(message = null) }
            AuthUiEvent.ConsumeNavigation -> _uiState.update { it.copy(navigation = AuthNavigation.None) }
        }
    }

    private fun submitLogin(username: String, password: String) {
        viewModelScope.launch {
            val (storedUser, storedPass) = repository.getLoginCredentials()
            if (username == storedUser && password == storedPass) {
                _uiState.update {
                    it.copy(
                        message = "Login Success",
                        navigation = AuthNavigation.OpenMain
                    )
                }
            } else {
                repository.resetLoginCredentialsToDefault()
                _uiState.update {
                    it.copy(
                        message = "Credentials Reset",
                        navigation = AuthNavigation.OpenSignup
                    )
                }
            }
        }
    }

    private fun submitSignup(username: String, password: String) {
        viewModelScope.launch {
            repository.saveSignupCredentials(username, password)
            _uiState.update {
                it.copy(navigation = AuthNavigation.OpenLogin)
            }
        }
    }

    private fun checkSignupGate() {
        viewModelScope.launch {
            val (u, p) = repository.getSignupScreenPrefs()
            if (u != AuthRepository.DEFAULT_CREDENTIAL_SENTINEL &&
                p != AuthRepository.DEFAULT_CREDENTIAL_SENTINEL
            ) {
                _uiState.update { it.copy(navigation = AuthNavigation.OpenLogin) }
            }
        }
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
