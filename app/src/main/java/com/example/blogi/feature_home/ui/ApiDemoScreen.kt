package com.example.blogi.feature_home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blogi.data.remote.TestPost
import com.example.blogi.feature_home.logic.ApiDemoViewModel

@Composable
fun ApiDemoScreen(
    viewModel: ApiDemoViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "API testandmed",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = { viewModel.loadPosts() },
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
        ) {
            Text("Lae testandmed")
        }

        when {
            viewModel.isLoading -> {
                CircularProgressIndicator()
            }

            viewModel.errorMessage != null -> {
                Text(
                    text = "Viga: ${viewModel.errorMessage}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            viewModel.posts.isEmpty() -> {
                Text("Andmeid ei leitud")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.posts, key = { it.id }) { post ->
                        ApiPostCard(post)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiPostCard(post: TestPost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "ID: ${post.id}",
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
            )

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}