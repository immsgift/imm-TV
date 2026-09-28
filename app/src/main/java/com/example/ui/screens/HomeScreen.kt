package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Movie
import com.example.ui.components.CategoryRow
import com.example.ui.theme.ImmCardElevated
import com.example.ui.theme.ImmDarkBackground
import com.example.ui.theme.ImmGold
import com.example.ui.theme.ImmNetflixRed
import com.example.ui.theme.ImmRedGlow
import com.example.ui.theme.ImmSurface
import com.example.ui.theme.ImmTextMuted
import com.example.ui.theme.ImmTextPrimary
import com.example.ui.theme.ImmTextSecondary
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (Movie) -> Unit,
    onWatchNow: (Movie) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ImmDarkBackground)
    ) {
        if (uiState.isLoading && uiState.trendingMovies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ImmNetflixRed,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Hero Banner
                item {
                    HeroFeaturedBanner(
                        movie = uiState.heroMovie,
                        isWatchlisted = uiState.isHeroWatchlisted,
                        onWatchNow = { onWatchNow(uiState.heroMovie) },
                        onToggleWatchlist = { viewModel.toggleHeroWatchlist() },
                        onDetails = { onMovieClick(uiState.heroMovie) }
                    )
                }

                // Carousels
                item {
                    CategoryRow(
                        title = "Trending Now",
                        movies = uiState.trendingMovies,
                        onMovieClick = onMovieClick
                    )
                }

                item {
                    CategoryRow(
                        title = "Top Rated Movies",
                        movies = uiState.topRatedMovies,
                        onMovieClick = onMovieClick
                    )
                }

                item {
                    CategoryRow(
                        title = "Action & Thrillers",
                        movies = uiState.actionMovies,
                        onMovieClick = onMovieClick
                    )
                }

                item {
                    CategoryRow(
                        title = "Popular TV Shows",
                        movies = uiState.popularTvShows,
                        onMovieClick = onMovieClick
                    )
                }
            }
        }

        // Top Header Overlay (imm tv logo, search, profile avatar)
        TopHeaderBar(
            onSearchClick = onSearchClick,
            onProfileClick = onProfileClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun TopHeaderBar(
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.9f),
                        Color.Black.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // "imm tv" bold logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("app_logo")
            ) {
                Text(
                    text = "imm",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = ImmNetflixRed,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Surface(
                    color = ImmNetflixRed,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "TV",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }

            // Right side icons: Search and User Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .testTag("search_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Movies",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // User Profile Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ImmNetflixRed, Color(0xFFE040FB))
                            )
                        )
                        .clickable(onClick = onProfileClick)
                        .testTag("user_profile_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroFeaturedBanner(
    movie: Movie,
    isWatchlisted: Boolean,
    onWatchNow: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(490.dp)
    ) {
        // High-res backdrop artwork
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movie.backdropPath.ifBlank { movie.posterPath })
                .crossfade(true)
                .build(),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient fade to background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.5f),
                            0.35f to Color.Transparent,
                            0.70f to ImmDarkBackground.copy(alpha = 0.85f),
                            1.0f to ImmDarkBackground
                        )
                    )
                )
        )

        // Banner Content at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Genre Pills & Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ImmGold.copy(alpha = 0.2f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = ImmGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = movie.ratingFormatted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmGold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1B5E20)
                ) {
                    Text(
                        text = "مترجم للعربية",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                movie.genres.take(2).forEach { genre ->
                    Text(
                        text = "• $genre",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ImmTextSecondary
                    )
                }
            }

            // Title
            Text(
                text = movie.title,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = ImmTextPrimary,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            if (movie.arabicTitle.isNotBlank()) {
                Text(
                    text = movie.arabicTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Overview snippet
            Text(
                text = movie.overview,
                fontSize = 12.sp,
                color = ImmTextMuted,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 16.dp)
            )

            // Action Buttons: "Watch Now / Play", "+ My List", "Details"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "+ My List" toggle
                OutlinedButton(
                    onClick = onToggleWatchlist,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("hero_watchlist_button")
                ) {
                    Icon(
                        imageVector = if (isWatchlisted) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "My List",
                        tint = if (isWatchlisted) ImmNetflixRed else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isWatchlisted) "In List" else "My List",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // "Watch Now / Play" primary button
                Button(
                    onClick = onWatchNow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmNetflixRed
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("hero_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Watch Now",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Watch Now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info / Details Button
                IconButton(
                    onClick = onDetails,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .testTag("hero_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Details",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
