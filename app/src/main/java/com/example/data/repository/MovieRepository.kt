package com.example.data.repository

import android.content.Context
import com.example.data.local.ImmTvDatabase
import com.example.data.local.WatchlistDao
import com.example.data.local.WatchlistEntity
import com.example.data.model.CastMember
import com.example.data.model.Movie
import com.example.data.remote.MockMoviesData
import com.example.data.remote.TmdbApiService
import com.example.data.remote.TmdbMovieDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MovieRepository(
    private val apiService: TmdbApiService,
    private val watchlistDao: WatchlistDao
) {
    // Current user configured TMDb API Key (or default sample key)
    private var customApiKey: String = ""

    fun setCustomApiKey(key: String) {
        customApiKey = key.trim()
    }

    fun getApiKey(): String = customApiKey

    fun getTrendingMovies(): Flow<List<Movie>> = flow {
        val movies = if (customApiKey.isNotBlank()) {
            try {
                val response = apiService.getTrendingMovies(customApiKey)
                mapDtosToMovies(response.results, "trending")
            } catch (e: Exception) {
                MockMoviesData.trendingMovies
            }
        } else {
            MockMoviesData.trendingMovies
        }
        emit(movies)
    }.flowOn(Dispatchers.IO)

    fun getTopRatedMovies(): Flow<List<Movie>> = flow {
        val movies = if (customApiKey.isNotBlank()) {
            try {
                val response = apiService.getTopRatedMovies(customApiKey)
                mapDtosToMovies(response.results, "top_rated")
            } catch (e: Exception) {
                MockMoviesData.topRatedMovies
            }
        } else {
            MockMoviesData.topRatedMovies
        }
        emit(movies)
    }.flowOn(Dispatchers.IO)

    fun getActionMovies(): Flow<List<Movie>> = flow {
        val movies = if (customApiKey.isNotBlank()) {
            try {
                // Genre 28 is Action in TMDb
                val response = apiService.discoverMoviesByGenre(customApiKey, 28)
                mapDtosToMovies(response.results, "action")
            } catch (e: Exception) {
                MockMoviesData.actionMovies
            }
        } else {
            MockMoviesData.actionMovies
        }
        emit(movies)
    }.flowOn(Dispatchers.IO)

    fun getPopularTvShows(): Flow<List<Movie>> = flow {
        val movies = if (customApiKey.isNotBlank()) {
            try {
                val response = apiService.getPopularTvShows(customApiKey)
                mapDtosToMovies(response.results, "tv_shows", isTv = true)
            } catch (e: Exception) {
                MockMoviesData.popularTvShows
            }
        } else {
            MockMoviesData.popularTvShows
        }
        emit(movies)
    }.flowOn(Dispatchers.IO)

    fun searchMovies(query: String, selectedCategory: String): Flow<List<Movie>> = flow {
        val q = query.trim()
        val all = MockMoviesData.allMovies

        val filtered = all.filter { movie ->
            val matchesQuery = if (q.isEmpty()) true else {
                movie.title.contains(q, ignoreCase = true) ||
                movie.overview.contains(q, ignoreCase = true) ||
                movie.genres.any { it.contains(q, ignoreCase = true) }
            }

            val matchesCategory = when (selectedCategory.lowercase()) {
                "all" -> true
                "action" -> movie.genres.any { it.contains("action", ignoreCase = true) }
                "drama" -> movie.genres.any { it.contains("drama", ignoreCase = true) }
                "sci-fi" -> movie.genres.any { it.contains("sci-fi", ignoreCase = true) || it.contains("fantasy", ignoreCase = true) }
                "horror" -> movie.genres.any { it.contains("horror", ignoreCase = true) || it.contains("thriller", ignoreCase = true) }
                "tv shows" -> movie.isTvShow
                else -> true
            }

            matchesQuery && matchesCategory
        }

        emit(filtered)
    }.flowOn(Dispatchers.IO)

    suspend fun getMovieById(id: Int): Movie? = withContext(Dispatchers.IO) {
        val movie = MockMoviesData.allMovies.find { it.id == id }
            ?: MockMoviesData.heroMovie
        val isSaved = watchlistDao.isWatchlistedSync(movie.id)
        movie.copy(isWatchlisted = isSaved)
    }

    fun isWatchlisted(movieId: Int): Flow<Boolean> = watchlistDao.isWatchlisted(movieId)

    suspend fun toggleWatchlist(movie: Movie): Boolean = withContext(Dispatchers.IO) {
        val currentlySaved = watchlistDao.isWatchlistedSync(movie.id)
        if (currentlySaved) {
            watchlistDao.deleteById(movie.id)
            false
        } else {
            watchlistDao.insert(WatchlistEntity.fromMovie(movie))
            true
        }
    }

    fun getWatchlist(): Flow<List<Movie>> {
        return watchlistDao.getAll().map { list ->
            list.map { it.toMovie() }
        }
    }

    private fun mapDtosToMovies(dtos: List<TmdbMovieDto>, category: String, isTv: Boolean = false): List<Movie> {
        val posterBase = "https://image.tmdb.org/t/p/w780"
        val backdropBase = "https://image.tmdb.org/t/p/w1280"

        return dtos.map { dto ->
            val title = dto.title ?: dto.name ?: "Untitled"
            val releaseDate = dto.releaseDate ?: dto.firstAirDate ?: "2024"
            val poster = if (!dto.posterPath.isNullOrBlank()) posterBase + dto.posterPath else ""
            val backdrop = if (!dto.backdropPath.isNullOrBlank()) backdropBase + dto.backdropPath else poster

            Movie(
                id = dto.id,
                title = title,
                overview = dto.overview ?: "No overview available.",
                posterPath = poster,
                backdropPath = backdrop,
                releaseDate = releaseDate,
                voteAverage = dto.voteAverage ?: 7.5,
                voteCount = dto.voteCount ?: 1200,
                runtime = if (isTv) "45m/ep" else "2h 10m",
                genres = listOf("Action", "Drama"),
                category = category,
                isTvShow = isTv,
                server1StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                server2StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                cast = MockMoviesData.heroMovie.cast
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MovieRepository? = null

        fun getInstance(context: Context): MovieRepository {
            return INSTANCE ?: synchronized(this) {
                val db = ImmTvDatabase.getDatabase(context)
                val api = TmdbApiService.create()
                val instance = MovieRepository(api, db.watchlistDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
