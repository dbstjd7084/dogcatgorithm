package com.example.application.interfaces

import com.example.application.structs.googlemap.FacilityResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PetFriendlyService {
    @GET("15111389/v1/uddi:41944402-8249-4e45-9e9d-a52d0a7db1cc")
    fun getFacilities(
        @Query("page") page: Int,
        @Query("perPage") perPage: Int,
        @Query("returnType") returnType: String,
        @Query("serviceKey") serviceKey: String
    ): Call<FacilityResponse>
}