package com.example.blogi.feature_blog.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogi.data.model.BlogPost
import com.example.blogi.data.repository.BlogRepository
import kotlinx.coroutines.launch

class BlogViewModel : ViewModel() {

    private val repository = BlogRepository()

    var posts by mutableStateOf<List<BlogPost>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadPosts()
    }

    fun getPostById(postId: Long): BlogPost? {
        return posts.find { it.id == postId }
    }

    fun loadPosts() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                posts = repository.getPosts()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Postituste laadimine ebaõnnestus"
            } finally {
                isLoading = false
            }
        }
    }

    fun addPost(
        title: String,
        content: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()

        if (cleanTitle.isEmpty() || cleanContent.isEmpty()) {
            onError("Pealkiri ja sisu ei tohi olla tühjad")
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                repository.addPost(cleanTitle, cleanContent)
                loadPosts()
                onSuccess()
            } catch (e: Exception) {
                val message = e.message ?: "Postituse salvestamine ebaõnnestus"
                errorMessage = message
                onError(message)
            } finally {
                isLoading = false
            }
        }
    }
}