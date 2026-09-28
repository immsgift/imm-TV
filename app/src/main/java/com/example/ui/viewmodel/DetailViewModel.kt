package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Movie
import com.example.data.remote.MockMoviesData
import com.example.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StreamServer(val displayName: String, val badge: String) {
    SERVER_1("سيرفر البث 1 (VidSrc)", "FULL MOVIE"),
    SERVER_2("سيرفر البث 2 (Embed.su)", "FULL MOVIE"),
    SERVER_3("سيرفر البث 3 (VidSrc.xyz)", "FULL MOVIE"),
    TRAILER("الإعلان الترويجي (Trailer)", "TRAILER")
}

data class DetailUiState(
    val movie: Movie? = null,
    val isLoading: Boolean = true,
    val isWatchlisted: Boolean = false,
    val activeServer: StreamServer = StreamServer.SERVER_1,
    val isPlayerActive: Boolean = false,
    val isFullscreen: Boolean = false,
    val similarMovies: List<Movie> = emptyList(),
    val playbackUrl: String = "",
    val customSubtitleUrl: String = "",
    val isBaseServerConfigured: Boolean = true,
    val baseServerUrl: String = "",
    val currentSeason: Int = 1,
    val currentEpisode: Int = 1
)

class DetailViewModel(
    private val movieId: Int,
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadMovie()
    }

    private fun loadMovie() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val movie = repository.getMovieById(movieId) ?: MockMoviesData.heroMovie
            val similar = MockMoviesData.allMovies.filter { it.id != movie.id }.take(6)

            val baseServer = repository.getBaseStreamServerUrl().trim()
            val initialUrl = getUrlForServer(movie, StreamServer.SERVER_1, season = 1, episode = 1)

            repository.isWatchlisted(movie.id).collect { isSaved ->
                _uiState.value = _uiState.value.copy(
                    movie = movie,
                    isLoading = false,
                    isWatchlisted = isSaved,
                    similarMovies = similar,
                    activeServer = StreamServer.SERVER_1,
                    isBaseServerConfigured = true,
                    baseServerUrl = baseServer,
                    playbackUrl = initialUrl
                )
            }
        }
    }

    fun selectServer(server: StreamServer) {
        val movie = _uiState.value.movie ?: return
        val url = getUrlForServer(movie, server)
        _uiState.value = _uiState.value.copy(
            activeServer = server,
            playbackUrl = url,
            isPlayerActive = true
        )
    }

    fun selectSeasonAndEpisode(season: Int, episode: Int) {
        val movie = _uiState.value.movie ?: return
        _uiState.value = _uiState.value.copy(
            currentSeason = season,
            currentEpisode = episode
        )
        val url = getUrlForServer(movie, _uiState.value.activeServer, season, episode)
        _uiState.value = _uiState.value.copy(playbackUrl = url)
    }

    fun setCustomPlayback(streamUrl: String, subtitleUrl: String) {
        if (streamUrl.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                playbackUrl = streamUrl.trim(),
                customSubtitleUrl = subtitleUrl.trim(),
                isPlayerActive = true
            )
        }
    }

    fun togglePlayer(active: Boolean? = null) {
        val current = _uiState.value.isPlayerActive
        _uiState.value = _uiState.value.copy(isPlayerActive = active ?: !current)
    }

    fun toggleFullscreen() {
        _uiState.value = _uiState.value.copy(isFullscreen = !_uiState.value.isFullscreen)
    }

    fun toggleWatchlist() {
        val movie = _uiState.value.movie ?: return
        viewModelScope.launch {
            val saved = repository.toggleWatchlist(movie)
            _uiState.value = _uiState.value.copy(isWatchlisted = saved)
        }
    }

    fun getUrlForServer(
        movie: Movie,
        server: StreamServer,
        season: Int = _uiState.value.currentSeason,
        episode: Int = _uiState.value.currentEpisode
    ): String {
        return when (server) {
            StreamServer.SERVER_1 -> {
                repository.buildDynamicStreamUrl(movie, season, episode, serverIndex = 1)
            }
            StreamServer.SERVER_2 -> {
                repository.buildDynamicStreamUrl(movie, season, episode, serverIndex = 2)
            }
            StreamServer.SERVER_3 -> {
                repository.buildDynamicStreamUrl(movie, season, episode, serverIndex = 3)
            }
            StreamServer.TRAILER -> {
                val trailerId = movie.trailerYoutubeId.ifBlank { "dQw4w9WgXcQ" }
                "https://www.youtube.com/embed/$trailerId?autoplay=1&playsinline=1"
            }
        }
    }
}

class DetailViewModelFactory(
    private val movieId: Int,
    private val repository: MovieRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetailViewModel(movieId, repository) as T
    }
}
