package com.example.blogiapp.feature_create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.ui.components.AppPrimaryButton
import com.example.blogi.ui.components.AppTextField
import com.example.blogiapp.ui.components.AppTextArea

@Composable
fun CreateScreen(
    onSavePost: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val isValid = title.trim().isNotEmpty() && content.trim().isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Uus postitus",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Kirjuta pealkiri ja sisu. Vajuta Salvesta, et postitus lisada avalehele.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        AppTextField(
            value = title,
            onValueChange = { if (it.length <= 100) title = it },
            placeholder = "Pealkiri"
        )

        AppTextArea(
            value = content,
            onValueChange = { if (it.length <= 2000) content = it },
            placeholder = "Kirjuta postituse sisu siia..."
        )

        Text(
            text = "${content.length} / 2000",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AppPrimaryButton(
            text = "Salvesta postitus",
            enabled = isValid,
            onClick = {
                onSavePost(title, content)
                title = ""
                content = ""
            }
        )
    }
}