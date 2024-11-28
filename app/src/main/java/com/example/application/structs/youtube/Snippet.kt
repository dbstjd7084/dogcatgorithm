package com.example.application.structs.youtube

data class Snippet(
    val title: String,
    val description: String,
    val thumbnails: Thumbnails,
    val channelTitle: String,
    val publishedAt: String
)