package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val selectedCategory: String = "All",
    val categories: List<String> = listOf("All", "Action", "Drama", "Sci-Fi", "Horror", "TV Shows"),
    val results: List<Movie> = emptyList(),
    val isLoading: Boolean = false
)

class SearchViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        performSearch()
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        performSearch()
    }

    fun onCategorySelect(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        performSearch()
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(query = "", selectedCategory = "All")
        performSearch()
    }

    private fun performSearch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.searchMovies(_uiState.value.query, _uiState.value.selectedCategory)
                .collect { list ->
                    _uiState.value = _uiState.value.copy(
                        results = list,
                        isLoading = false
                    )
                }
        }
    }
}

class SearchViewModelFactory(
    private val repository: MovieRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(repository) as T
    }
}
