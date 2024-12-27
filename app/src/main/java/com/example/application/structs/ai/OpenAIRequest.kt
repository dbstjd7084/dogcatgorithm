package com.example.application.structs.ai

data class OpenAIRequest(
    val model: String,
    val messages: List<Message>
)