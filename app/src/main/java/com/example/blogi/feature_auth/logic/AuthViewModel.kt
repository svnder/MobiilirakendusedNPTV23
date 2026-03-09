package com.example.blogi.feature_auth.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogi.feature_auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loginEmail: String = "",
    val loginPassword: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isLoggedIn = repository.isLoggedIn())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onLoginEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(loginEmail = value)
    }

    fun onLoginPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(loginPassword = value)
    }

    fun onRegisterEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(registerEmail = value)
    }

    fun onRegisterPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(registerPassword = value)
    }

    fun onRegisterConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(registerConfirmPassword = value)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun login() {
        val email = _uiState.value.loginEmail.trim()
        val password = _uiState.value.loginPassword

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sisesta email ja parool"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = repository.login(email, password)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Sisselogimine ebaõnnestus"
                )
            }
        }
    }

    fun register() {
        val email = _uiState.value.registerEmail.trim()
        val password = _uiState.value.registerPassword
        val confirmPassword = _uiState.value.registerConfirmPassword

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Täida kõik väljad"
            )
            return
        }

        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Parool peab olema vähemalt 6 tähemärki"
            )
            return
        }

        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Paroolid ei kattu"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = repository.register(email, password)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Konto loomine ebaõnnestus"
                )
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = _uiState.value.copy(isLoggedIn = false)
    }
}