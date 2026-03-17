package com.example.blogi.feature_home.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogi.data.remote.RetrofitClient
import com.example.blogi.data.remote.TestPost
import kotlinx.coroutines.launch

class ApiDemoViewModel : ViewModel() {

    var posts by mutableStateOf<List<TestPost>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadPosts() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                posts = RetrofitClient.api.getPosts().take(10)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Tekkis viga"
            } finally {
                isLoading = false
            }
        }
    }
}