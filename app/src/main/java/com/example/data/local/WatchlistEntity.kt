package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CastMember
import com.example.data.model.Movie

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String,
    val backdropPath: String,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val runtime: String,
    val genresCsv: String,
    val isTvShow: Boolean,
    val trailerYoutubeId: String,
    val server1StreamUrl: String,
    val server2StreamUrl: String,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            voteCount = voteCount,
            runtime = runtime,
            genres = if (genresCsv.isBlank()) emptyList() else genresCsv.split(",").map { it.trim() },
            isTvShow = isTvShow,
            trailerYoutubeId = trailerYoutubeId,
            server1StreamUrl = server1StreamUrl,
            server2StreamUrl = server2StreamUrl,
            isWatchlisted = true
        )
    }

    companion object {
        fun fromMovie(movie: Movie): WatchlistEntity {
            return WatchlistEntity(
                id = movie.id,
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.posterPath,
                backdropPath = movie.backdropPath,
                releaseDate = movie.releaseDate,
                voteAverage = movie.voteAverage,
                voteCount = movie.voteCount,
                runtime = movie.runtime,
                genresCsv = movie.genres.joinToString(","),
                isTvShow = movie.isTvShow,
                trailerYoutubeId = movie.trailerYoutubeId,
                server1StreamUrl = movie.server1StreamUrl,
                server2StreamUrl = movie.server2StreamUrl
            )
        }
    }
}
