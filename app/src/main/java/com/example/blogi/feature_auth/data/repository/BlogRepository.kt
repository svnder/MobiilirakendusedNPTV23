package com.example.blogi.data.repository

import com.example.blogi.data.model.BlogPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BlogRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postsCollection = firestore.collection("posts")

    suspend fun addPost(title: String, content: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Kasutaja pole sisse logitud")

        val postId = System.currentTimeMillis()

        val post = BlogPost(
            id = postId,
            title = title.trim(),
            content = content.trim(),
            createdAt = System.currentTimeMillis(),
            authorUid = user.uid
        )

        postsCollection
            .document(postId.toString())
            .set(post)
            .await()
    }

    suspend fun getPosts(): List<BlogPost> {
        val snapshot = postsCollection
            .orderBy("createdAt")
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { it.toObject(BlogPost::class.java) }
            .sortedByDescending { it.createdAt }
    }
}