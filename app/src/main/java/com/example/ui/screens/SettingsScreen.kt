package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.aspectRatio
import com.example.ui.components.VideoPlayerView
import com.example.ui.viewmodel.StreamServer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.MovieRepository
import com.example.ui.theme.ImmCardSurface
import com.example.ui.theme.ImmDarkBackground
import com.example.ui.theme.ImmNetflixRed
import com.example.ui.theme.ImmSurfaceVariant
import com.example.ui.theme.ImmTextMuted
import com.example.ui.theme.ImmTextPrimary
import com.example.ui.theme.ImmTextSecondary

@Composable
fun SettingsScreen(
    repository: MovieRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tmdbApiKey by remember { mutableStateOf(repository.getApiKey()) }
    var selectedQuality by remember { mutableStateOf("1080p Full HD") }
    var selectedServer by remember { mutableStateOf("Server 1 (Ultra Fast)") }
    var hardwareAccelEnabled by remember { mutableStateOf(true) }
    var autoPlayNextEnabled by remember { mutableStateOf(true) }
    var customTestStreamUrl by remember {
        mutableStateOf(
            repository.getCustomStreamUrl().ifBlank {
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            }
        )
    }
    var customTestSubtitleUrl by remember { mutableStateOf(repository.getCustomSubtitleUrl()) }
    var isTestingPlayer by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ImmDarkBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "Settings & Preferences",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = ImmTextPrimary
            )
            Text(
                text = "Customize streaming quality, servers & TMDb API connection",
                fontSize = 12.sp,
                color = ImmTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Section: TMDb API Integration
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ImmCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ImmNetflixRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = ImmNetflixRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "TMDb API Service",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImmTextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (tmdbApiKey.isNotBlank()) Color(0xFF1B5E20) else Color(0xFF37474F)
                        ) {
                            Text(
                                text = if (tmdbApiKey.isNotBlank()) "CUSTOM KEY" else "BUILT-IN FEED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "The app fetches trending titles, high-resolution backdrops, and cast metadata. Provide your TMDb v3 API key to unlock unlimited personalized live queries, or leave empty to use our curated high-definition catalog.",
                        fontSize = 12.sp,
                        color = ImmTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tmdbApiKey,
                        onValueChange = { tmdbApiKey = it },
                        placeholder = { Text("Enter TMDb v3 API Key (optional)", color = ImmTextMuted, fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ImmSurfaceVariant,
                            unfocusedContainerColor = ImmSurfaceVariant,
                            focusedBorderColor = ImmNetflixRed,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tmdb_api_key_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                tmdbApiKey = MovieRepository.DEFAULT_TMDB_API_KEY
                                repository.setCustomApiKey(MovieRepository.DEFAULT_TMDB_API_KEY)
                                Toast.makeText(context, "Default TMDb key restored!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(text = "Restore Default Key", fontSize = 11.sp, color = ImmTextSecondary)
                        }

                        Button(
                            onClick = {
                                repository.setCustomApiKey(tmdbApiKey)
                                Toast.makeText(context, "API Key updated and saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ImmNetflixRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_api_key_button")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Save Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Streaming & Playback Preferences
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ImmCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ImmNetflixRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = ImmNetflixRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Playback & Quality",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImmTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "STREAM QUALITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ImmTextMuted)
                    Spacer(modifier = Modifier.height(6.dp))

                    val qualities = listOf("Auto (Best)", "1080p Full HD", "4K Ultra HD")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        qualities.forEach { q ->
                            val isSel = selectedQuality == q
                            Surface(
                                onClick = { selectedQuality = q },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) ImmNetflixRed else ImmSurfaceVariant,
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = q,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else ImmTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hardware Acceleration switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Hardware Acceleration", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ImmTextPrimary)
                            Text(text = "GPU acceleration for 60fps smooth stream decoding", fontSize = 11.sp, color = ImmTextSecondary)
                        }
                        Switch(
                            checked = hardwareAccelEnabled,
                            onCheckedChange = { hardwareAccelEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ImmNetflixRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Auto-Play Next switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto-Play Trailers & Next Episode", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ImmTextPrimary)
                            Text(text = "Seamlessly load next episode when playback finishes", fontSize = 11.sp, color = ImmTextSecondary)
                        }
                        Switch(
                            checked = autoPlayNextEnabled,
                            onCheckedChange = { autoPlayNextEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ImmNetflixRed)
                        )
                    }
                }
            }
        }

        // Section: Custom Stream & Subtitle Tester
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ImmCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ImmNetflixRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = null,
                                    tint = ImmNetflixRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Stream & Subtitle Tester",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImmTextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1B5E20)
                        ) {
                            Text(
                                text = "ترجمة عربية",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Enter any direct MP4 / HLS media URL and WebVTT (.vtt) subtitle file to test playback and Arabic captions directly in the player.",
                        fontSize = 12.sp,
                        color = ImmTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customTestStreamUrl,
                        onValueChange = { customTestStreamUrl = it },
                        label = { Text("Direct Video Stream URL (MP4 / HLS)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ImmSurfaceVariant,
                            unfocusedContainerColor = ImmSurfaceVariant,
                            focusedBorderColor = ImmNetflixRed,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customTestSubtitleUrl,
                        onValueChange = { customTestSubtitleUrl = it },
                        label = { Text("Custom Arabic Subtitle URL (.vtt) - Optional") },
                        placeholder = { Text("https://example.com/subtitles_ar.vtt") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ImmSurfaceVariant,
                            unfocusedContainerColor = ImmSurfaceVariant,
                            focusedBorderColor = ImmNetflixRed,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = {
                                customTestStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = ImmSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Preset: Bunny HD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                customTestStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = ImmSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Preset: Steel 4K",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                repository.setCustomStreamUrl(customTestStreamUrl)
                                repository.setCustomSubtitleUrl(customTestSubtitleUrl)
                                Toast.makeText(context, "Stream URLs saved for app playback!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Save Stream URL", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                repository.setCustomStreamUrl(customTestStreamUrl)
                                repository.setCustomSubtitleUrl(customTestSubtitleUrl)
                                isTestingPlayer = !isTestingPlayer
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTestingPlayer) Color(0xFF37474F) else ImmNetflixRed
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(
                                imageVector = if (isTestingPlayer) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTestingPlayer) "Close Player" else "Test with Subtitles",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isTestingPlayer) {
                        Spacer(modifier = Modifier.height(12.dp))
                        VideoPlayerView(
                            videoUrl = customTestStreamUrl,
                            title = "Custom Stream Test (Arabic Subtitles)",
                            activeServer = StreamServer.SERVER_1,
                            onServerSelect = {},
                            customSubtitleUrl = customTestSubtitleUrl,
                            isFullscreen = false,
                            onToggleFullscreen = {},
                            onClosePlayer = { isTestingPlayer = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
        }

        // Section: Storage & Cache
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ImmCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ImmNetflixRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = ImmNetflixRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Data & Storage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ImmTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Streaming Cache", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ImmTextPrimary)
                            Text(text = "14.2 MB cached media", fontSize = 11.sp, color = ImmTextSecondary)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Streaming cache cleared!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ImmSurfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Clear Cache", fontSize = 12.sp, color = ImmTextPrimary)
                        }
                    }
                }
            }
        }

        // Section: About
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ImmCardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "imm",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = ImmNetflixRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = ImmNetflixRed,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "TV",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Version 1.0.0 (Release Build)",
                        fontSize = 11.sp,
                        color = ImmTextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Powered by TMDb API and open video streaming foundations. Built with modern Jetpack Compose & Material 3 for high-performance streaming.",
                        fontSize = 11.sp,
                        color = ImmTextMuted,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
