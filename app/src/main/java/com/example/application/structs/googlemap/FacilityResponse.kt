package com.example.application.structs.googlemap

data class FacilityResponse(
    val currentCount: Int,
    val data: List<Facility>,
    val totalCount: Int
)