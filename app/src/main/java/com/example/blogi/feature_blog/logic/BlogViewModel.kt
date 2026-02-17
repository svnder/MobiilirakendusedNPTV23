package com.example.blogi.feature_blog.logic

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.blogi.data.model.BlogPost

class BlogViewModel : ViewModel() {

    val posts = mutableStateListOf<BlogPost>()

    fun getPostById(postId: Long): BlogPost? {
        return posts.find { it.id == postId }
    }


    fun addPost(title: String, content: String) {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()
        if (cleanTitle.isEmpty() || cleanContent.isEmpty()) return

        posts.add(
            0,
            BlogPost(
                id = System.currentTimeMillis(),
                title = cleanTitle,
                content = cleanContent,
                createdAt = System.currentTimeMillis()
            )
        )
    }

}
