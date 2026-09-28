package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.SubtitleTrack
import com.example.data.util.SubtitleProvider
import com.example.ui.theme.ImmDarkBackground
import com.example.ui.theme.ImmGold
import com.example.ui.theme.ImmNetflixRed
import com.example.ui.theme.ImmSurface
import com.example.ui.theme.ImmTextMuted
import com.example.ui.theme.ImmTextPrimary
import com.example.ui.theme.ImmTextSecondary
import com.example.ui.viewmodel.StreamServer
import kotlinx.coroutines.delay

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerView(
    videoUrl: String,
    title: String,
    activeServer: StreamServer,
    onServerSelect: (StreamServer) -> Unit,
    customSubtitleUrl: String = "",
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    onClosePlayer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentProgress by remember { mutableFloatStateOf(0.15f) }

    // Subtitle track selection state: Arabic selected by default
    var selectedSubtitleTrack by remember { mutableStateOf(SubtitleTrack.ARABIC) }
    var showSubtitleMenu by remember { mutableStateOf(false) }

    // Auto-hide controls after 4.5 seconds of inactivity
    LaunchedEffect(showControls, isPlaying, showSubtitleMenu) {
        if (showControls && isPlaying && !showSubtitleMenu) {
            delay(4500)
            showControls = false
        }
    }

    // HTML wrapper to play media seamlessly with HTML5 video player + WebVTT Arabic subtitles
    val htmlContent = remember(videoUrl, customSubtitleUrl) {
        SubtitleProvider.generatePlayerHtml(
            videoUrl = videoUrl,
            customSubtitleUrl = customSubtitleUrl,
            initialTrackId = selectedSubtitleTrack.id
        )
    }

    Box(
        modifier = modifier
            .background(ImmDarkBackground)
            .clickable { showControls = !showControls }
    ) {
        // Embedded Android WebView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowContentAccess = true
                        allowFileAccess = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    setBackgroundColor(android.graphics.Color.parseColor("#0E0E12"))
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isBuffering = true
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isBuffering = false
                            // Ensure default track is applied
                            view?.evaluateJavascript(
                                "if(window.setSubtitleTrack) { setSubtitleTrack('${selectedSubtitleTrack.id}'); }",
                                null
                            )
                        }
                    }
                    webChromeClient = object : WebChromeClient() {}
                    loadDataWithBaseURL("https://imm.tv", htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { wv ->
                webViewRef = wv
            }
        )

        // Buffering indicator
        if (isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ImmNetflixRed,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.88f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = ImmNetflixRed,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = activeServer.badge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Arabic Subtitles Badge (مترجم للعربية)
                            Surface(
                                color = Color(0xFF1B5E20),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("player_arabic_sub_badge")
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

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = ImmTextPrimary,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = "Streaming via ${activeServer.displayName} • ترجمة: ${selectedSubtitleTrack.name}",
                            fontSize = 11.sp,
                            color = ImmTextSecondary
                        )
                    }

                    if (onClosePlayer != null) {
                        IconButton(
                            onClick = onClosePlayer,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .testTag("close_player_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Player",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Center Playback Buttons (Rewind 10s, Play/Pause, Forward 10s)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = {
                            currentProgress = (currentProgress - 0.05f).coerceAtLeast(0f)
                            webViewRef?.evaluateJavascript(
                                "if(window.seekVideo) { seekVideo(-10); } else { var v = document.getElementById('player'); if(v) v.currentTime = Math.max(0, v.currentTime - 10); }",
                                null
                            )
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("rewind_10s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play / Pause Toggle
                    IconButton(
                        onClick = {
                            isPlaying = !isPlaying
                            if (isPlaying) {
                                webViewRef?.evaluateJavascript(
                                    "var v = document.getElementById('player'); if(v) v.play();",
                                    null
                                )
                            } else {
                                webViewRef?.evaluateJavascript(
                                    "var v = document.getElementById('player'); if(v) v.pause();",
                                    null
                                )
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ImmNetflixRed)
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = {
                            currentProgress = (currentProgress + 0.05f).coerceAtMost(1f)
                            webViewRef?.evaluateJavascript(
                                "if(window.seekVideo) { seekVideo(10); } else { var v = document.getElementById('player'); if(v) v.currentTime += 10; }",
                                null
                            )
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("forward_10s_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Controls: Progress bar, CC Subtitles, Fullscreen, Server Switcher
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    // Scrubbing Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "18:42",
                            fontSize = 11.sp,
                            color = ImmTextSecondary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Slider(
                            value = currentProgress,
                            onValueChange = {
                                currentProgress = it
                                webViewRef?.evaluateJavascript(
                                    "if(window.setVideoTime) { setVideoTime($it); }",
                                    null
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = ImmNetflixRed,
                                activeTrackColor = ImmNetflixRed,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        Text(
                            text = "2:24:10",
                            fontSize = 11.sp,
                            color = ImmTextSecondary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Action bar with Server Switcher, CC Subtitle Track Selector, and Fullscreen button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Server Switcher Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ServerChip(
                                label = "Server 1",
                                isSelected = activeServer == StreamServer.SERVER_1,
                                onClick = { onServerSelect(StreamServer.SERVER_1) }
                            )
                            ServerChip(
                                label = "Server 2",
                                isSelected = activeServer == StreamServer.SERVER_2,
                                onClick = { onServerSelect(StreamServer.SERVER_2) }
                            )
                            ServerChip(
                                label = "Trailer",
                                isSelected = activeServer == StreamServer.TRAILER,
                                onClick = { onServerSelect(StreamServer.TRAILER) }
                            )
                        }

                        // Right side icons: Subtitles (CC) and Fullscreen
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Subtitles (CC) Button & Menu
                            Box {
                                IconButton(
                                    onClick = { showSubtitleMenu = !showSubtitleMenu },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selectedSubtitleTrack != SubtitleTrack.OFF)
                                                ImmNetflixRed.copy(alpha = 0.25f)
                                            else Color.White.copy(alpha = 0.1f)
                                        )
                                        .testTag("subtitle_cc_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ClosedCaption,
                                        contentDescription = "Subtitles Track Selector",
                                        tint = if (selectedSubtitleTrack != SubtitleTrack.OFF) ImmNetflixRed else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSubtitleMenu,
                                    onDismissRequest = { showSubtitleMenu = false },
                                    modifier = Modifier.background(ImmSurface)
                                ) {
                                    Text(
                                        text = "اختر لغة الترجمة (Subtitles)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ImmTextMuted,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )

                                    SubtitleTrack.ALL.forEach { track ->
                                        val isSelected = selectedSubtitleTrack == track
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = track.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) ImmNetflixRed else ImmTextPrimary
                                                    )
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = ImmNetflixRed,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedSubtitleTrack = track
                                                showSubtitleMenu = false
                                                webViewRef?.evaluateJavascript(
                                                    "setSubtitleTrack('${track.id}');",
                                                    null
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Fullscreen Toggle
                            IconButton(
                                onClick = onToggleFullscreen,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("fullscreen_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ImmNetflixRed else Color.White.copy(alpha = 0.15f),
        modifier = Modifier.height(28.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = Color.White
            )
        }
    }
}
