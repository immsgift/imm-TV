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
    SERVER_1("Server 1 (Ultra Fast)", "HD"),
    SERVER_2("Server 2 (StreamMax)", "4K"),
    TRAILER("Official Trailer", "TRAILER")
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
    val customSubtitleUrl: String = ""
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

            repository.isWatchlisted(movie.id).collect { isSaved ->
                val currentUrl = getUrlForServer(movie, _uiState.value.activeServer)
                _uiState.value = _uiState.value.copy(
                    movie = movie,
                    isLoading = false,
                    isWatchlisted = isSaved,
                    similarMovies = similar,
                    playbackUrl = currentUrl
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

    private fun getUrlForServer(movie: Movie, server: StreamServer): String {
        return when (server) {
            StreamServer.SERVER_1 -> {
                if (movie.server1StreamUrl.isNotBlank()) movie.server1StreamUrl
                else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            }
            StreamServer.SERVER_2 -> {
                if (movie.server2StreamUrl.isNotBlank()) movie.server2StreamUrl
                else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
            }
            StreamServer.TRAILER -> {
                "https://www.youtube.com/embed/${movie.trailerYoutubeId}?autoplay=1&playsinline=1"
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
