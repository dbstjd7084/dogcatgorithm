package com.example.application.structs.ai

data class OpenAIRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>
)