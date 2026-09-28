package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Movie
import com.example.data.repository.MovieRepository
import com.example.ui.components.VideoPlayerView
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
    var currentSeason by remember { mutableIntStateOf(1) }
    var currentEpisode by remember { mutableIntStateOf(1) }

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
                    repository.buildDynamicStreamUrl(currentMovie, currentSeason, currentEpisode, serverIndex = 1)
                }
                StreamServer.SERVER_2 -> {
                    repository.buildDynamicStreamUrl(currentMovie, currentSeason, currentEpisode, serverIndex = 2)
                }
                StreamServer.SERVER_3 -> {
                    repository.buildDynamicStreamUrl(currentMovie, currentSeason, currentEpisode, serverIndex = 3)
                }
                StreamServer.TRAILER -> {
                    val trailerId = currentMovie.trailerYoutubeId.ifBlank { "dQw4w9WgXcQ" }
                    "https://www.youtube.com/embed/$trailerId?autoplay=1&playsinline=1"
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                VideoPlayerView(
                    videoUrl = playbackUrl,
                    title = if (currentMovie.isTvShow) "${currentMovie.title} (S$currentSeason:E$currentEpisode)" else currentMovie.title,
                    activeServer = activeServer,
                    onServerSelect = { activeServer = it },
                    customSubtitleUrl = repository.getCustomSubtitleUrl(),
                    isFullscreen = true,
                    onToggleFullscreen = onClose,
                    onClosePlayer = onClose,
                    modifier = Modifier.fillMaxSize()
                )

                // TV Show episode quick switcher (floating above bottom strip)
                if (currentMovie.isTvShow && activeServer != StreamServer.TRAILER) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 54.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الحلقات (Episodes): S$currentSeason:E$currentEpisode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    (1..3).forEach { s ->
                                        Surface(
                                            onClick = { currentSeason = s },
                                            color = if (currentSeason == s) ImmNetflixRed else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "S$s",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(end = 8.dp)
                            ) {
                                items((1..10).toList()) { ep ->
                                    val isSelected = currentEpisode == ep
                                    Surface(
                                        onClick = { currentEpisode = ep },
                                        color = if (isSelected) ImmNetflixRed else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Ep $ep",
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
