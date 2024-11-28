package com.example.application.interfaces

import com.example.application.structs.ai.OpenAIRequest
import com.example.application.structs.ai.OpenAIResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIApiService {
    @POST("v1/chat/completions")
    fun getChatCompletion(@Body request: OpenAIRequest): Call<OpenAIResponse>
}