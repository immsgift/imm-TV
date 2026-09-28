package com.example.data.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String,
    val backdropPath: String,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val runtime: String,
    val genres: List<String>,
    val category: String = "trending",
    val isTvShow: Boolean = false,
    val trailerYoutubeId: String = "dQw4w9WgXcQ",
    val server1StreamUrl: String = "",
    val server2StreamUrl: String = "",
    val cast: List<CastMember> = emptyList(),
    val isWatchlisted: Boolean = false,
    val arabicTitle: String = "",
    val arabicOverview: String = "",
    val arabicSubtitleUrl: String = "",
    val hasArabicSubtitles: Boolean = true
) {
    val releaseYear: String
        get() = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else "2024"

    val ratingFormatted: String
        get() = String.format("%.1f", voteAverage)
}

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String
)
