package com.example.data.model

data class SubtitleTrack(
    val id: String,
    val name: String,
    val lang: String
) {
    companion object {
        val ARABIC = SubtitleTrack("ar", "العربية (Arabic)", "ar")
        val ENGLISH = SubtitleTrack("en", "English", "en")
        val OFF = SubtitleTrack("off", "Off (إيقاف)", "")

        val ALL = listOf(ARABIC, ENGLISH, OFF)
    }
}
