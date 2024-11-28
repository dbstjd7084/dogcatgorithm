package com.example.application.structs

import java.util.Calendar

data class PetInfo(
    val name: String,
    var type: String,
    val like: String?,
    val hate: String?,
    val birth: Calendar?
)
