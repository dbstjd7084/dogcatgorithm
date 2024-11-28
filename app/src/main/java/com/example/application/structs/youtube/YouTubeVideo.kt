package com.example.application.structs.youtube

data class YouTubeVideo(
    val title: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val channelName: String,
    val viewCount: String,
    val publishedAt: String
)