package com.example.application.structs

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiaryEntry(
    val date: String,
    var mood: Mood,
    var title: String? = null,
    var comment: String? = null,
    var imageUriList: List<String>? = null
) {
    fun getFormattedDate(): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.parse(date)  // 변환 실패 시 null 반환
    }
}

enum class Mood {
    GOOD, BAD
}
