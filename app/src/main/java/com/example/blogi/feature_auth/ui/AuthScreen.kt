package com.example.blogi.feature_auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blogi.feature_auth.logic.AuthViewModel

private enum class AuthMode {
    LOGIN,
    REGISTER
}

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    val authModeState = remember { mutableStateOf(AuthMode.LOGIN) }

    when (authModeState.value) {
        AuthMode.LOGIN -> {
            LoginScreen(
                uiState = uiState,
                onEmailChange = authViewModel::onLoginEmailChange,
                onPasswordChange = authViewModel::onLoginPasswordChange,
                onLoginClick = authViewModel::login,
                onGoToRegister = {
                    authViewModel.clearError()
                    authModeState.value = AuthMode.REGISTER
                }
            )
        }

        AuthMode.REGISTER -> {
            RegisterScreen(
                uiState = uiState,
                onEmailChange = authViewModel::onRegisterEmailChange,
                onPasswordChange = authViewModel::onRegisterPasswordChange,
                onConfirmPasswordChange = authViewModel::onRegisterConfirmPasswordChange,
                onRegisterClick = authViewModel::register,
                onGoToLogin = {
                    authViewModel.clearError()
                    authModeState.value = AuthMode.LOGIN
                }
            )
        }
    }
}