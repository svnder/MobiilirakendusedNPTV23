package com.example.blogiapp.feature_home.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.data.model.BlogPost
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



/* KOMMENTAAR
Home ekraan eraldi feature kaustas.
*/

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}


@Composable
fun HomeScreen(
    posts: List<BlogPost>,
    onPostClick: (Long) -> Unit
) {
    if (posts.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Postitusi veel pole",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Ava Create ja lisa oma esimene postitus.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }


    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    onClick = { onPostClick(post.id) }

                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = formatDate(post.createdAt),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        val preview = if (post.content.length > 220) {
                            post.content.take(220) + "..."
                        } else {
                            post.content
                        }

                        Text(
                            text = preview,
                            style = MaterialTheme.typography.bodyLarge
                        )


                    }
                }
            }

        }
    }

}