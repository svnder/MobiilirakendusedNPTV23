package com.example.blogi.data.model

data class BlogPost(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val authorUid: String = ""
)