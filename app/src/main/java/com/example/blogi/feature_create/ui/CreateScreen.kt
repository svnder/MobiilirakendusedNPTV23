package com.example.blogiapp.feature_create.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/* KOMMENTAAR
Create ekraan eraldi feature kaustas.
*/
@Composable
fun CreateScreen(
    onSavePost: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val titleCount = title.length
    val contentCount = content.length
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

        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 80) title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Pealkiri") },
            placeholder = { Text("Nt: Kuidas õppida Jetpack Compose'i") },
            singleLine = true,
            supportingText = { Text("$titleCount / 80") }
        )

        OutlinedTextField(
            value = content,
            onValueChange = { if (it.length <= 2000) content = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp),
            label = { Text("Sisu") },
            placeholder = { Text("Kirjuta postituse sisu siia...") },
            minLines = 8,
            maxLines = 16,
            singleLine = false,
            supportingText = { Text("${content.length} / 2000") }
        )


        Button(
            onClick = {
                onSavePost(title, content)
                title = ""
                content = ""
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("Salvesta postitus")
        }
    }


}