package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.ImmTvDatabase
import com.example.data.local.WatchlistDao
import com.example.data.local.WatchlistEntity
import com.example.data.model.CastMember
import com.example.data.model.Movie
import com.example.data.remote.MockMoviesData
import com.example.data.remote.TmdbApiService
import com.example.data.remote.TmdbMovieDetailsDto
import com.example.data.remote.TmdbMovieDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class MovieRepository(
    private val apiService: TmdbApiService,
    private val watchlistDao: WatchlistDao,
    private val context: Context? = null
) {
    companion object {
        const val DEFAULT_TMDB_API_KEY = "a9c9e11791d2f0e160b8350268603797"
        private const val PREFS_NAME = "imm_tv_prefs"
        private const val KEY_TMDB_API_KEY = "tmdb_api_key"
        private const val KEY_CUSTOM_STREAM_URL = "custom_stream_url"
        private const val KEY_CUSTOM_SUBTITLE_URL = "custom_subtitle_url"
        private const val KEY_BASE_STREAM_SERVER_URL = "base_stream_server_url"
        private const val DEFAULT_STREAM_1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        private const val DEFAULT_STREAM_2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"

        @Volatile
        private var INSTANCE: MovieRepository? = null

        fun getInstance(context: Context): MovieRepository {
            return INSTANCE ?: synchronized(this) {
                val db = ImmTvDatabase.getDatabase(context)
                val api = TmdbApiService.create()
                val instance = MovieRepository(api, db.watchlistDao(), context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // In-memory cache for all movies displayed or loaded across the app
    private val moviesCache = ConcurrentHashMap<Int, Movie>()

    init {
        // Pre-populate cache with initial mock items so they are always available
        MockMoviesData.allMovies.forEach { movie ->
            moviesCache[movie.id] = movie
        }
        moviesCache[MockMoviesData.heroMovie.id] = MockMoviesData.heroMovie

        // Ensure default TMDb API key is stored in preferences (localStorage) if not already set
        prefs?.let { p ->
            val savedKey = p.getString(KEY_TMDB_API_KEY, null)
            if (savedKey.isNullOrBlank()) {
                p.edit().putString(KEY_TMDB_API_KEY, DEFAULT_TMDB_API_KEY).apply()
            }
        }
    }

    fun getApiKey(): String {
        return prefs?.getString(KEY_TMDB_API_KEY, null)?.takeIf { it.isNotBlank() }
            ?: DEFAULT_TMDB_API_KEY
    }

    fun setCustomApiKey(key: String) {
        val trimmed = key.trim()
        val keyToSave = if (trimmed.isBlank()) DEFAULT_TMDB_API_KEY else trimmed
        prefs?.edit()?.putString(KEY_TMDB_API_KEY, keyToSave)?.apply()
    }

    fun getCustomStreamUrl(): String {
        return prefs?.getString(KEY_CUSTOM_STREAM_URL, "") ?: ""
    }

    fun setCustomStreamUrl(url: String) {
        prefs?.edit()?.putString(KEY_CUSTOM_STREAM_URL, url.trim())?.apply()
    }

    fun getCustomSubtitleUrl(): String {
        return prefs?.getString(KEY_CUSTOM_SUBTITLE_URL, "") ?: ""
    }

    fun setCustomSubtitleUrl(url: String) {
        prefs?.edit()?.putString(KEY_CUSTOM_SUBTITLE_URL, url.trim())?.apply()
    }

    fun getBaseStreamServerUrl(): String {
        val saved = prefs?.getString(KEY_BASE_STREAM_SERVER_URL, null)
        if (saved != null) return saved
        return "https://vidsrc.to/embed"
    }

    fun setBaseStreamServerUrl(url: String) {
        prefs?.edit()?.putString(KEY_BASE_STREAM_SERVER_URL, url.trim())?.apply()
    }

    fun buildDynamicStreamUrl(movie: Movie, season: Int = 1, episode: Int = 1, serverIndex: Int = 1): String {
        return when (serverIndex) {
            1 -> {
                // Server 1: vidsrc.pm with Arabic subtitles enabled
                if (movie.isTvShow) {
                    "https://vidsrc.pm/embed/tv/${movie.id}/$season/$episode?ds_lang=ar"
                } else {
                    "https://vidsrc.pm/embed/movie/${movie.id}?ds_lang=ar"
                }
            }
            2 -> {
                // Server 2: embed.su with Arabic subtitles enabled
                if (movie.isTvShow) {
                    "https://embed.su/embed/tv/${movie.id}/$season/$episode?sub=ar"
                } else {
                    "https://embed.su/embed/movie/${movie.id}?sub=ar"
                }
            }
            3 -> {
                // Server 3: vidcore.org with Arabic subtitles enabled
                if (movie.isTvShow) {
                    "https://vidcore.org/embed/series/${movie.id}/$season/$episode?sub=ar"
                } else {
                    "https://vidcore.org/embed/movie/${movie.id}?sub=ar"
                }
            }
            else -> {
                val customBase = getBaseStreamServerUrl().trim().removeSuffix("/")
                val baseUrl = if (customBase.isNotBlank()) customBase else "https://vidsrc.pm/embed"
                if (movie.isTvShow) {
                    "$baseUrl/tv/${movie.id}/$season/$episode?ds_lang=ar"
                } else {
                    "$baseUrl/movie/${movie.id}?ds_lang=ar"
                }
            }
        }
    }

    fun getTrendingMovies(): Flow<List<Movie>> = flow {
        val apiKey = getApiKey()
        val movies = try {
            val response = apiService.getTrendingMovies(apiKey)
            val list = mapDtosToMovies(response.results, "trending")
            list.forEach { moviesCache[it.id] = it }
            list.ifEmpty { MockMoviesData.trendingMovies }
        } catch (e: Exception) {
            MockMoviesData.trendingMovies
        }
        emit(movies)
    }.flowOn(Dispatchers.IO)

    fun getTopRatedMovies(): Flow<List<Movie>> = flow {
        val apiKey = getApiKey()
        val movies = try {
            val response = apiService.getTopRatedMovies(apiKey)
            val list = mapDtosToMovies(response.results, "top_rated")
            list.forEach { moviesCache[it.id] = it }
            list.ifEmpty { MockMoviesData.topRatedMovies }
        } catch (e: Exception) {
            MockMoviesData.topRatedMovies
        }
        emit(movies)
    }.flowOn(Dispatchers.IO)

    fun getActionMovies(): Flow<List<Movie>> = flow {
        val apiKey = getApiKey()
        val movies = try {
            // Genre 28 is Action in TMDb
            val response = apiService.discoverMoviesByGenre(apiKey, 28)
            val list = mapDtosToMovies(response.results, "action")
            list.forEach { moviesCache[it.id] = it }
            list.ifEmpty { MockMoviesData.actionMovies }
        } catch (e: Exception) {
            MockMoviesData.actionMovies
        }
        emit(movies)
    }.flowOn(Dispatchers.IO)

    fun getPopularTvShows(): Flow<List<Movie>> = flow {
        val apiKey = getApiKey()
        val movies = try {
            val response = apiService.getPopularTvShows(apiKey)
            val list = mapDtosToMovies(response.results, "tv_shows", isTv = true)
            list.forEach { moviesCache[it.id] = it }
            list.ifEmpty { MockMoviesData.popularTvShows }
        } catch (e: Exception) {
            MockMoviesData.popularTvShows
        }
        emit(movies)
    }.flowOn(Dispatchers.IO)

    fun searchMovies(query: String, selectedCategory: String): Flow<List<Movie>> = flow {
        val q = query.trim()
        val apiKey = getApiKey()

        val results = if (q.isNotEmpty()) {
            try {
                // Search live TMDb API using multi-search for movies and TV shows
                val multiResponse = apiService.searchMulti(apiKey, q)
                val dtos = multiResponse.results.filter { dto ->
                    dto.mediaType != "person" && (!dto.posterPath.isNullOrBlank() || !dto.backdropPath.isNullOrBlank())
                }

                val mapped = dtos.map { dto ->
                    val isTv = dto.mediaType == "tv" || dto.firstAirDate != null
                    mapSingleDtoToMovie(dto, "search", isTv)
                }

                mapped.forEach { moviesCache[it.id] = it }

                // Filter by category if selected
                filterMoviesByCategory(mapped, selectedCategory)
            } catch (e: Exception) {
                // Fallback to local filter if network query fails
                filterLocalMovies(q, selectedCategory)
            }
        } else {
            // When query is empty, show relevant feed based on selected category chip
            when (selectedCategory.lowercase()) {
                "tv shows" -> {
                    try {
                        val response = apiService.getPopularTvShows(apiKey)
                        val list = mapDtosToMovies(response.results, "tv_shows", isTv = true)
                        list.forEach { moviesCache[it.id] = it }
                        list
                    } catch (e: Exception) {
                        MockMoviesData.popularTvShows
                    }
                }
                "action" -> {
                    try {
                        val response = apiService.discoverMoviesByGenre(apiKey, 28)
                        val list = mapDtosToMovies(response.results, "action")
                        list.forEach { moviesCache[it.id] = it }
                        list
                    } catch (e: Exception) {
                        MockMoviesData.actionMovies
                    }
                }
                "drama" -> {
                    try {
                        val response = apiService.discoverMoviesByGenre(apiKey, 18)
                        val list = mapDtosToMovies(response.results, "drama")
                        list.forEach { moviesCache[it.id] = it }
                        list
                    } catch (e: Exception) {
                        MockMoviesData.allMovies.filter { it.genres.any { g -> g.contains("drama", ignoreCase = true) } }
                    }
                }
                "sci-fi" -> {
                    try {
                        val response = apiService.discoverMoviesByGenre(apiKey, 878)
                        val list = mapDtosToMovies(response.results, "sci-fi")
                        list.forEach { moviesCache[it.id] = it }
                        list
                    } catch (e: Exception) {
                        MockMoviesData.allMovies.filter { it.genres.any { g -> g.contains("sci-fi", ignoreCase = true) } }
                    }
                }
                "horror" -> {
                    try {
                        val response = apiService.discoverMoviesByGenre(apiKey, 27)
                        val list = mapDtosToMovies(response.results, "horror")
                        list.forEach { moviesCache[it.id] = it }
                        list
                    } catch (e: Exception) {
                        MockMoviesData.allMovies.filter { it.genres.any { g -> g.contains("horror", ignoreCase = true) } }
                    }
                }
                else -> {
                    try {
                        val response = apiService.getTrendingMovies(apiKey)
                        val list = mapDtosToMovies(response.results, "trending")
                        list.forEach { moviesCache[it.id] = it }
                        list
                    } catch (e: Exception) {
                        MockMoviesData.trendingMovies
                    }
                }
            }
        }

        emit(results)
    }.flowOn(Dispatchers.IO)

    private fun filterLocalMovies(q: String, selectedCategory: String): List<Movie> {
        val all = (moviesCache.values + MockMoviesData.allMovies).distinctBy { it.id }
        return all.filter { movie ->
            val matchesQuery = movie.title.contains(q, ignoreCase = true) ||
                    movie.arabicTitle.contains(q, ignoreCase = true) ||
                    movie.overview.contains(q, ignoreCase = true) ||
                    movie.arabicOverview.contains(q, ignoreCase = true) ||
                    movie.genres.any { it.contains(q, ignoreCase = true) }

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
    }

    private fun filterMoviesByCategory(list: List<Movie>, selectedCategory: String): List<Movie> {
        return when (selectedCategory.lowercase()) {
            "all" -> list
            "tv shows" -> list.filter { it.isTvShow }
            "action" -> list.filter { it.genres.any { g -> g.contains("action", ignoreCase = true) } }
            "drama" -> list.filter { it.genres.any { g -> g.contains("drama", ignoreCase = true) } }
            "sci-fi" -> list.filter { it.genres.any { g -> g.contains("sci-fi", ignoreCase = true) || g.contains("fantasy", ignoreCase = true) } }
            "horror" -> list.filter { it.genres.any { g -> g.contains("horror", ignoreCase = true) || g.contains("thriller", ignoreCase = true) } }
            else -> list
        }
    }

    suspend fun getMovieById(id: Int): Movie? = withContext(Dispatchers.IO) {
        val cached = moviesCache[id] ?: MockMoviesData.allMovies.find { it.id == id }
        val apiKey = getApiKey()

        // Fetch detailed metadata and official trailer videos from TMDb
        val detailedMovie = try {
            val details: TmdbMovieDetailsDto = if (cached?.isTvShow == true) {
                apiService.getTvDetails(id, apiKey)
            } else {
                try {
                    apiService.getMovieDetails(id, apiKey)
                } catch (e: Exception) {
                    apiService.getTvDetails(id, apiKey)
                }
            }

            enrichMovieWithDetails(cached, details)
        } catch (e: Exception) {
            cached ?: MockMoviesData.heroMovie
        }

        // Cache the enriched movie
        moviesCache[detailedMovie.id] = detailedMovie

        val isSaved = watchlistDao.isWatchlistedSync(detailedMovie.id)
        detailedMovie.copy(isWatchlisted = isSaved)
    }

    private fun enrichMovieWithDetails(baseMovie: Movie?, details: TmdbMovieDetailsDto): Movie {
        val posterBase = "https://image.tmdb.org/t/p/w780"
        val backdropBase = "https://image.tmdb.org/t/p/w1280"

        val title = details.title ?: details.name ?: baseMovie?.title ?: "Untitled"
        val overview = details.overview?.takeIf { it.isNotBlank() } ?: baseMovie?.overview ?: "No overview available."
        val poster = if (!details.posterPath.isNullOrBlank()) posterBase + details.posterPath else baseMovie?.posterPath ?: ""
        val backdrop = if (!details.backdropPath.isNullOrBlank()) backdropBase + details.backdropPath else baseMovie?.backdropPath ?: poster
        val releaseDate = details.releaseDate ?: details.firstAirDate ?: baseMovie?.releaseDate ?: "2024"
        val voteAvg = details.voteAverage ?: baseMovie?.voteAverage ?: 7.5
        val voteCount = details.voteCount ?: baseMovie?.voteCount ?: 1000

        // Extract runtime
        val runtimeStr = when {
            details.runtime != null && details.runtime > 0 -> {
                val hours = details.runtime / 60
                val mins = details.runtime % 60
                if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
            }
            !details.episodeRunTime.isNullOrEmpty() -> "${details.episodeRunTime[0]}m/ep"
            baseMovie?.runtime != null -> baseMovie.runtime
            else -> "2h 10m"
        }

        // Extract genres
        val genres = details.genres?.map { it.name }?.takeIf { it.isNotEmpty() }
            ?: baseMovie?.genres
            ?: listOf("Action", "Adventure")

        // Find official YouTube trailer from TMDb videos
        val youtubeTrailerKey = details.videos?.results?.let { videoList ->
            val trailer = videoList.firstOrNull { it.site.equals("YouTube", true) && it.type.equals("Trailer", true) && it.official == true }
                ?: videoList.firstOrNull { it.site.equals("YouTube", true) && it.type.equals("Trailer", true) }
                ?: videoList.firstOrNull { it.site.equals("YouTube", true) }
            trailer?.key
        } ?: baseMovie?.trailerYoutubeId ?: "dQw4w9WgXcQ"

        // Extract real cast members from credits
        val castList = details.credits?.cast?.take(8)?.map { c ->
            CastMember(
                id = c.id,
                name = c.name,
                character = c.character ?: "",
                profileUrl = if (!c.profilePath.isNullOrBlank()) "https://image.tmdb.org/t/p/w185${c.profilePath}" else ""
            )
        }?.takeIf { it.isNotEmpty() } ?: baseMovie?.cast ?: MockMoviesData.heroMovie.cast

        val customStream = getCustomStreamUrl()
        val s1 = if (customStream.isNotBlank()) customStream else DEFAULT_STREAM_1

        return Movie(
            id = details.id,
            title = title,
            overview = overview,
            posterPath = poster,
            backdropPath = backdrop,
            releaseDate = releaseDate,
            voteAverage = voteAvg,
            voteCount = voteCount,
            runtime = runtimeStr,
            genres = genres,
            category = baseMovie?.category ?: "trending",
            isTvShow = details.name != null || baseMovie?.isTvShow == true,
            trailerYoutubeId = youtubeTrailerKey,
            server1StreamUrl = s1,
            server2StreamUrl = DEFAULT_STREAM_2,
            cast = castList,
            arabicTitle = baseMovie?.arabicTitle ?: "",
            arabicOverview = baseMovie?.arabicOverview ?: "",
            hasArabicSubtitles = true
        )
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

    suspend fun getTvSeasons(tvId: Int): List<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        try {
            val tvDetails = apiService.getTvDetails(tvId, apiKey)
            val seasonList = tvDetails.seasons
                ?.filter { (it.seasonNumber ?: 0) > 0 }
                ?.map { Pair(it.seasonNumber, it.episodeCount ?: 10) }
            if (!seasonList.isNullOrEmpty()) {
                return@withContext seasonList
            }
            val numSeasons = tvDetails.numberOfSeasons ?: 1
            (1..numSeasons.coerceAtLeast(1)).map { Pair(it, 10) }
        } catch (e: Exception) {
            listOf(Pair(1, 10), Pair(2, 10), Pair(3, 8))
        }
    }

    suspend fun getTvEpisodesForSeason(tvId: Int, seasonNumber: Int): List<Int> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        try {
            val seasonDetail = apiService.getTvSeasonDetails(tvId, seasonNumber, apiKey)
            val epNumbers = seasonDetail.episodes.map { it.episodeNumber }
            if (epNumbers.isNotEmpty()) epNumbers else (1..10).toList()
        } catch (e: Exception) {
            (1..10).toList()
        }
    }

    private fun mapDtosToMovies(dtos: List<TmdbMovieDto>, category: String, isTv: Boolean = false): List<Movie> {
        return dtos.map { mapSingleDtoToMovie(it, category, isTv) }
    }

    private fun mapSingleDtoToMovie(dto: TmdbMovieDto, category: String, isTv: Boolean = false): Movie {
        val posterBase = "https://image.tmdb.org/t/p/w780"
        val backdropBase = "https://image.tmdb.org/t/p/w1280"

        val title = dto.title ?: dto.name ?: "Untitled"
        val releaseDate = dto.releaseDate ?: dto.firstAirDate ?: "2024"
        val poster = if (!dto.posterPath.isNullOrBlank()) posterBase + dto.posterPath else ""
        val backdrop = if (!dto.backdropPath.isNullOrBlank()) backdropBase + dto.backdropPath else poster

        // Convert genre IDs to readable genre names
        val genreNames = mapGenreIdsToNames(dto.genreIds)

        val customStream = getCustomStreamUrl()
        val s1 = if (customStream.isNotBlank()) customStream else DEFAULT_STREAM_1

        return Movie(
            id = dto.id,
            title = title,
            overview = dto.overview?.takeIf { it.isNotBlank() } ?: "No overview available for this title.",
            posterPath = poster,
            backdropPath = backdrop,
            releaseDate = releaseDate,
            voteAverage = dto.voteAverage ?: 7.5,
            voteCount = dto.voteCount ?: 1200,
            runtime = if (isTv) "45m/ep" else "2h 10m",
            genres = genreNames,
            category = category,
            isTvShow = isTv,
            server1StreamUrl = s1,
            server2StreamUrl = DEFAULT_STREAM_2,
            trailerYoutubeId = "dQw4w9WgXcQ", // will be enriched on detail load
            cast = MockMoviesData.heroMovie.cast,
            hasArabicSubtitles = true
        )
    }

    private fun mapGenreIdsToNames(genreIds: List<Int>?): List<String> {
        if (genreIds.isNullOrEmpty()) return listOf("Action", "Drama")
        val genreMap = mapOf(
            28 to "Action",
            12 to "Adventure",
            16 to "Animation",
            35 to "Comedy",
            80 to "Crime",
            99 to "Documentary",
            18 to "Drama",
            10751 to "Family",
            14 to "Fantasy",
            36 to "History",
            27 to "Horror",
            10402 to "Music",
            9648 to "Mystery",
            10749 to "Romance",
            878 to "Sci-Fi",
            10770 to "TV Movie",
            53 to "Thriller",
            10752 to "War",
            37 to "Western",
            10759 to "Action & Adventure",
            10765 to "Sci-Fi & Fantasy"
        )
        return genreIds.mapNotNull { genreMap[it] }.takeIf { it.isNotEmpty() } ?: listOf("Action", "Drama")
    }
}
