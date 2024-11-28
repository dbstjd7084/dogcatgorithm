package com.example.application.structs

import com.example.application.structs.youtube.Snippet
import com.example.application.structs.youtube.VideoId

data class Item(
    val snippet: Snippet,
    val id: VideoId
)
