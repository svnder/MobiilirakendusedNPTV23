package com.example.blogi.feature_profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.ui.components.AppPrimaryButton

@Composable
fun ProfileScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onApiDemoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = if (darkTheme) "Dark mode: ON" else "Dark mode: OFF",
            color = MaterialTheme.colorScheme.onBackground
        )

        Switch(
            checked = darkTheme,
            onCheckedChange = onDarkThemeChange
        )

        AppPrimaryButton(
            text = "Logi välja",
            onClick = onLogoutClick
        )
        Button(onClick = onApiDemoClick) {
            Text("Ava API demo")
        }
    }
}