package com.example.blogi.data.remote

data class PostDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)