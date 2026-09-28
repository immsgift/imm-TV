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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val heroMovie: Movie = MockMoviesData.heroMovie,
    val isHeroWatchlisted: Boolean = false,
    val trendingMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val actionMovies: List<Movie> = emptyList(),
    val popularTvShows: List<Movie> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val hero = MockMoviesData.heroMovie

            combine(
                repository.getTrendingMovies(),
                repository.getTopRatedMovies(),
                repository.getActionMovies(),
                repository.getPopularTvShows(),
                repository.isWatchlisted(hero.id)
            ) { trending, topRated, action, tvShows, isHeroSaved ->
                HomeUiState(
                    isLoading = false,
                    heroMovie = hero,
                    isHeroWatchlisted = isHeroSaved,
                    trendingMovies = trending,
                    topRatedMovies = topRated,
                    actionMovies = action,
                    popularTvShows = tvShows,
                    errorMessage = null
                )
            }.catch { error ->
                _uiState.value = HomeUiState(
                    isLoading = false,
                    heroMovie = MockMoviesData.heroMovie,
                    trendingMovies = MockMoviesData.trendingMovies,
                    topRatedMovies = MockMoviesData.topRatedMovies,
                    actionMovies = MockMoviesData.actionMovies,
                    popularTvShows = MockMoviesData.popularTvShows,
                    errorMessage = error.localizedMessage
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleHeroWatchlist() {
        val hero = _uiState.value.heroMovie
        viewModelScope.launch {
            val isSaved = repository.toggleWatchlist(hero)
            _uiState.value = _uiState.value.copy(isHeroWatchlisted = isSaved)
        }
    }
}

class HomeViewModelFactory(
    private val repository: MovieRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}
