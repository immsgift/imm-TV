package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.ImmDarkBackground
import com.example.ui.theme.ImmNetflixRed
import com.example.ui.viewmodel.StreamServer

@Composable
fun PlayerScreen(
    movieId: Int,
    initialServerName: String,
    repository: MovieRepository,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var movie by remember { mutableStateOf<Movie?>(null) }
    var activeServer by remember {
        mutableStateOf(
            try {
                StreamServer.valueOf(initialServerName)
            } catch (e: Exception) {
                StreamServer.SERVER_1
            }
        )
    }

    LaunchedEffect(movieId) {
        movie = repository.getMovieById(movieId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val currentMovie = movie
        if (currentMovie == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ImmNetflixRed,
                    strokeWidth = 3.dp
                )
            }
        } else {
            val playbackUrl = when (activeServer) {
                StreamServer.SERVER_1 -> {
                    if (currentMovie.server1StreamUrl.isNotBlank()) currentMovie.server1StreamUrl
                    else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                }
                StreamServer.SERVER_2 -> {
                    if (currentMovie.server2StreamUrl.isNotBlank()) currentMovie.server2StreamUrl
                    else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
                }
                StreamServer.TRAILER -> {
                    "https://www.youtube.com/embed/${currentMovie.trailerYoutubeId}?autoplay=1&playsinline=1"
                }
            }

            VideoPlayerView(
                videoUrl = playbackUrl,
                title = currentMovie.title,
                activeServer = activeServer,
                onServerSelect = { activeServer = it },
                isFullscreen = true,
                onToggleFullscreen = onClose,
                onClosePlayer = onClose,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
