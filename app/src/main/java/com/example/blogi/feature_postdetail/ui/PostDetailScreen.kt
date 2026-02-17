package com.example.blogi.feature_postdetail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.data.model.BlogPost
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun PostDetailScreen(post: BlogPost?) {
    if (post == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Postitust ei leitud",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = post.title,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = formatDate(post.createdAt),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
