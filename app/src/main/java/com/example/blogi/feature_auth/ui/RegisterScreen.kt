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
fun RegisterScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Loo konto",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Registreeru emaili ja parooliga.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AppTextField(
            value = uiState.registerEmail,
            onValueChange = onEmailChange,
            placeholder = "Email"
        )
        AppTextField(
            value = uiState.registerPassword,
            onValueChange = onPasswordChange,
            placeholder = "Parool",
            isPassword = true
        )

        AppTextField(
            value = uiState.registerConfirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "Korda parooli"
        )

        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        AppPrimaryButton(
            text = if (uiState.isLoading) "Laadib..." else "Registreeru",
            enabled = !uiState.isLoading,
            onClick = onRegisterClick
        )

        TextButton(onClick = onGoToLogin) {
            Text("Konto on olemas? Logi sisse")
        }
    }
}
