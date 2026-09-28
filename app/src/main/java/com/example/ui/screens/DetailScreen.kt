package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CastMember
import com.example.data.model.Movie
import com.example.ui.components.CategoryRow
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.ImmCardSurface
import com.example.ui.theme.ImmDarkBackground
import com.example.ui.theme.ImmGold
import com.example.ui.theme.ImmNetflixRed
import com.example.ui.theme.ImmSurfaceVariant
import com.example.ui.theme.ImmTextMuted
import com.example.ui.theme.ImmTextPrimary
import com.example.ui.theme.ImmTextSecondary
import com.example.ui.viewmodel.DetailViewModel
import com.example.ui.viewmodel.StreamServer

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onOpenFullscreenPlayer: (movieId: Int, server: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isOverviewExpanded by remember { mutableStateOf(false) }
    var showCustomStreamDialog by remember { mutableStateOf(false) }
    var showArabicSynopsis by remember { mutableStateOf(true) }

    if (uiState.isLoading || uiState.movie == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ImmDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = ImmNetflixRed,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
        }
        return
    }

    val movie = uiState.movie!!

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ImmDarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            // Media Header: either Video Player (when active) or Backdrop Banner with Play Overlay
            item {
                if (uiState.isPlayerActive) {
                    // Built-in Embedded Video Player View with Subtitles & Arabic default
                    VideoPlayerView(
                        videoUrl = uiState.playbackUrl,
                        title = movie.title,
                        activeServer = uiState.activeServer,
                        onServerSelect = { viewModel.selectServer(it) },
                        customSubtitleUrl = uiState.customSubtitleUrl,
                        isFullscreen = false,
                        onToggleFullscreen = {
                            onOpenFullscreenPlayer(movie.id, uiState.activeServer.name)
                        },
                        onClosePlayer = { viewModel.togglePlayer(false) },
                        isTvShow = movie.isTvShow,
                        currentSeason = uiState.currentSeason,
                        currentEpisode = uiState.currentEpisode,
                        onNextEpisode = {
                            viewModel.selectSeasonAndEpisode(uiState.currentSeason, uiState.currentEpisode + 1)
                        },
                        onSelectEpisode = { s, ep ->
                            viewModel.selectSeasonAndEpisode(s, ep)
                        },
                        runtime = movie.runtime,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .testTag("inline_video_player")
                    )
                } else {
                    // Backdrop with Play Button overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(movie.backdropPath.ifBlank { movie.posterPath })
                                .crossfade(true)
                                .build(),
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dark gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.5f),
                                            Color.Transparent,
                                            ImmDarkBackground
                                        )
                                    )
                                )
                        )

                        // Center Play Button overlay to start playback
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ImmNetflixRed.copy(alpha = 0.95f))
                                .align(Alignment.Center)
                                .clickable {
                                    onOpenFullscreenPlayer(movie.id, uiState.activeServer.name)
                                }
                                .testTag("backdrop_play_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Movie",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            // Movie Info Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // English and Arabic Titles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = ImmTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (movie.arabicTitle.isNotBlank()) {
                                Text(
                                    text = movie.arabicTitle,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFD54F),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metadata row: Year, Duration, Rating, Quality, and Arabic Subtitles badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Rating Badge
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

                        Text(
                            text = movie.releaseYear,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ImmTextSecondary
                        )

                        Text(
                            text = "•",
                            fontSize = 13.sp,
                            color = ImmTextMuted
                        )

                        Text(
                            text = movie.runtime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ImmTextSecondary
                        )

                        // Arabic Subtitles Badge (مترجم للعربية)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1B5E20)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "مترجم للعربية",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "4K UHD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stream Server Action Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isBaseServerConfigured) "STREAM SERVER (ACTIVE):" else "PLAYBACK & TRAILER:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = if (uiState.isBaseServerConfigured) Color(0xFF81C784) else ImmTextMuted
                        )

                        // Quick Custom URL test button
                        TextButton(
                            onClick = { showCustomStreamDialog = true },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = ImmNetflixRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Custom Stream / Subtitles",
                                fontSize = 11.sp,
                                color = ImmNetflixRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic Stream Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E2028),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFF1B5E20),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "FULL MOVIE (فيلم كامل)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (movie.isTvShow) "حلقة كاملة • ${movie.runtime}" else "الفيلم كاملاً • ${movie.runtime}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    color = Color(0xFF263238),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "بدون إعلانات مزعجة",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF81C784),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // TV Show: Interactive Season & Episode Picker
                    if (movie.isTvShow) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "اختر الموسم والحلقة (EPISODES):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = ImmTextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Seasons Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(listOf(1, 2, 3, 4, 5)) { seasonNum ->
                                val isSelected = uiState.currentSeason == seasonNum
                                Surface(
                                    onClick = { viewModel.selectSeasonAndEpisode(seasonNum, uiState.currentEpisode) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ImmNetflixRed else Color(0xFF262638),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Text(
                                            text = "الموسم $seasonNum",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Episodes Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items((1..12).toList()) { epNum ->
                                val isSelected = uiState.currentEpisode == epNum
                                Surface(
                                    onClick = { viewModel.selectSeasonAndEpisode(uiState.currentSeason, epNum) },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) ImmNetflixRed else Color(0xFF1E2028),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    ) {
                                        Text(
                                            text = "الحلقة $epNum",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Watch Full Movie Button
                    Button(
                        onClick = {
                            onOpenFullscreenPlayer(movie.id, uiState.activeServer.name)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ImmNetflixRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("watch_fullscreen_stream_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (movie.isTvShow) "▶ شاهد الحلقة كاملاً (S${uiState.currentSeason}:E${uiState.currentEpisode})" else "▶ شاهد الفيلم كاملاً (${movie.runtime})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Server Selection Chips: Server 1, Server 2, Server 3, Inline Player
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { viewModel.selectServer(StreamServer.SERVER_1) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.activeServer == StreamServer.SERVER_1) ImmNetflixRed else Color(0xFF262638),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "سيرفر 1",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            onClick = { viewModel.selectServer(StreamServer.SERVER_2) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.activeServer == StreamServer.SERVER_2) ImmNetflixRed else Color(0xFF262638),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "سيرفر 2",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            onClick = { viewModel.selectServer(StreamServer.SERVER_3) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.activeServer == StreamServer.SERVER_3) ImmNetflixRed else Color(0xFF262638),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "سيرفر 3",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                viewModel.togglePlayer(true)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.isPlayerActive) Color(0xFF455A64) else Color(0xFF262638),
                            modifier = Modifier.weight(1.1f).height(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (uiState.isPlayerActive) "إغلاق المدمج" else "تشغيل مدمج",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary action buttons: + My List, Trailer, Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "+ My List" toggle button
                        OutlinedButton(
                            onClick = { viewModel.toggleWatchlist() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("detail_watchlist_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isWatchlisted) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = "Add to My List",
                                tint = if (uiState.isWatchlisted) ImmNetflixRed else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isWatchlisted) "In Watchlist" else "+ My List",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        // Trailer preview button
                        OutlinedButton(
                            onClick = { viewModel.selectServer(StreamServer.TRAILER) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("detail_trailer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "Trailer",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Trailer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        // Share
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Watch ${movie.title} with Arabic subtitles on imm TV!")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Movie"))
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Synopsis / Overview Header + Language Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showArabicSynopsis && movie.arabicOverview.isNotBlank()) "قصة الفيلم (Synopsis)" else "Synopsis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImmTextPrimary
                        )

                        if (movie.arabicOverview.isNotBlank()) {
                            Surface(
                                onClick = { showArabicSynopsis = !showArabicSynopsis },
                                shape = RoundedCornerShape(12.dp),
                                color = ImmSurfaceVariant
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = ImmNetflixRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showArabicSynopsis) "English" else "العربية",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val displaySynopsis = if (showArabicSynopsis && movie.arabicOverview.isNotBlank()) {
                        movie.arabicOverview
                    } else {
                        movie.overview
                    }

                    Text(
                        text = displaySynopsis,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ImmTextSecondary,
                        maxLines = if (isOverviewExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { isOverviewExpanded = !isOverviewExpanded }
                    )
                    if (displaySynopsis.length > 120) {
                        Text(
                            text = if (isOverviewExpanded) "Show Less" else "Read More",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImmNetflixRed,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isOverviewExpanded = !isOverviewExpanded }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Genres Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        movie.genres.forEach { genre ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = genre,
                                    fontSize = 12.sp,
                                    color = ImmTextSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Cast & Crew Section with Profile Bubbles
            if (movie.cast.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Cast & Crew",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ImmTextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(movie.cast, key = { it.id }) { castMember ->
                                CastProfileBubble(castMember = castMember)
                            }
                        }
                    }
                }
            }

            // More Like This Recommendation Section
            if (uiState.similarMovies.isNotEmpty()) {
                item {
                    CategoryRow(
                        title = "More Like This",
                        movies = uiState.similarMovies,
                        onMovieClick = onMovieClick
                    )
                }
            }
        }

        // Top Back Button Overlay
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .align(Alignment.TopStart)
                .testTag("detail_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go Back",
                tint = Color.White
            )
        }
    }

    // Custom Stream & Subtitle Configuration Dialog
    if (showCustomStreamDialog) {
        CustomStreamDialog(
            initialStreamUrl = uiState.playbackUrl,
            initialSubtitleUrl = uiState.customSubtitleUrl,
            onDismiss = { showCustomStreamDialog = false },
            onApply = { stream, sub ->
                viewModel.setCustomPlayback(stream, sub)
                showCustomStreamDialog = false
            }
        )
    }
}

@Composable
fun CustomStreamDialog(
    initialStreamUrl: String,
    initialSubtitleUrl: String,
    onDismiss: () -> Unit,
    onApply: (streamUrl: String, subtitleUrl: String) -> Unit
) {
    var streamUrl by remember { mutableStateOf(initialStreamUrl) }
    var subtitleUrl by remember { mutableStateOf(initialSubtitleUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ImmCardSurface,
        title = {
            Text(
                text = "Stream & Subtitle Tester",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ImmTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enter any direct MP4 / HLS streaming URL and custom WebVTT (.vtt) subtitle track to test:",
                    fontSize = 12.sp,
                    color = ImmTextSecondary
                )

                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("Direct Video Stream URL") },
                    placeholder = { Text("https://.../video.mp4") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmNetflixRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subtitleUrl,
                    onValueChange = { subtitleUrl = it },
                    label = { Text("Custom Subtitle URL (.vtt)") },
                    placeholder = { Text("https://.../subtitles_ar.vtt (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmNetflixRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick presets
                Text(text = "Quick Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImmTextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = {
                            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = ImmSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Sample 1 (HD)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        onClick = {
                            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = ImmSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Sample 2 (4K)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(streamUrl, subtitleUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = ImmNetflixRed)
            ) {
                Text("Play Stream")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ImmTextSecondary)
            }
        }
    )
}

@Composable
private fun CastProfileBubble(castMember: CastMember) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(castMember.profileUrl)
                .crossfade(true)
                .build(),
            contentDescription = castMember.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(ImmCardSurface)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = castMember.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = ImmTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = castMember.character,
            fontSize = 10.sp,
            color = ImmTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
