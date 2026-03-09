package com.example.blogi.feature_auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.feature_auth.logic.AuthUiState
import com.example.blogi.ui.components.AppPrimaryButton
import com.example.blogi.ui.components.AppTextField

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Logi sisse",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Sisesta oma konto andmed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AppTextField(
            value = uiState.loginEmail,
            onValueChange = onEmailChange,
            placeholder = "Email"
        )

        AppTextField(
            value = uiState.loginPassword,
            onValueChange = onPasswordChange,
            placeholder = "Parool",
            isPassword = true
        )

        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        AppPrimaryButton(
            text = if (uiState.isLoading) "Laadib..." else "Logi sisse",
            enabled = !uiState.isLoading,
            onClick = onLoginClick
        )

        TextButton(onClick = onGoToRegister) {
            Text("Pole veel kontot? Registreeru")
        }
    }
}