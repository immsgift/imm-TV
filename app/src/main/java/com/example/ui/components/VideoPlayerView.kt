package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.SubtitleTrack
import com.example.data.util.SubtitleProvider
import com.example.ui.theme.ImmDarkBackground
import com.example.ui.theme.ImmNetflixRed
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
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var loadError by remember { mutableStateOf(false) }

    val isDirectVideo = remember(videoUrl) {
        val lower = videoUrl.trim().lowercase()
        lower.endsWith(".mp4") || lower.endsWith(".m3u8") || lower.endsWith(".webm") || lower.endsWith(".mkv")
    }

    // Auto-hide controls after 5 seconds so user can watch comfortably
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    // Function to load url into webview correctly
    fun loadTargetMedia(wv: WebView, url: String) {
        if (url.isBlank()) return
        isBuffering = true
        loadError = false
        val lower = url.trim().lowercase()
        if (lower.endsWith(".mp4") || lower.endsWith(".m3u8") || lower.endsWith(".webm") || lower.endsWith(".mkv")) {
            val html = SubtitleProvider.generatePlayerHtml(url, customSubtitleUrl, "ar")
            wv.loadDataWithBaseURL("https://imm.tv", html, "text/html", "UTF-8", null)
        } else {
            // Direct web embed or YouTube - load URL directly so native scripts, controls, and media run smoothly
            wv.loadUrl(url)
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        // Embedded Android WebView (unobstructed so touches pass directly to player)
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
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowContentAccess = true
                        allowFileAccess = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportMultipleWindows(false)
                        javaScriptCanOpenWindowsAutomatically = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    setBackgroundColor(android.graphics.Color.BLACK)
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isBuffering = true
                            loadError = false
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isBuffering = false
                            // Suppress popup windows and ad scripts
                            view?.evaluateJavascript(
                                "(function(){ window.open = function(){ return null; }; })();",
                                null
                            )
                        }
                        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): android.webkit.WebResourceResponse? {
                            val reqUrl = request?.url?.toString()?.lowercase() ?: return null
                            val adPatterns = listOf(
                                "popads", "popcash", "adsterra", "doubleclick", "googlesyndication",
                                "bet365", "1xbet", "propellerads", "clickadu", "exoclick", "adtrue",
                                "adcash", "hilltopads", "monetag", "deloton", "trafficjunky"
                            )
                            if (adPatterns.any { reqUrl.contains(it) }) {
                                return android.webkit.WebResourceResponse("text/plain", "utf-8", java.io.ByteArrayInputStream(ByteArray(0)))
                            }
                            return super.shouldInterceptRequest(view, request)
                        }
                        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            isBuffering = false
                        }
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val nextUrl = request?.url?.toString() ?: return false
                            val lower = nextUrl.lowercase()
                            val adPatterns = listOf("popads", "adsterra", "bet365", "1xbet", "propellerads", "clickadu")
                            if (adPatterns.any { lower.contains(it) }) {
                                return true
                            }
                            if (!nextUrl.startsWith("http://") && !nextUrl.startsWith("https://")) {
                                return true
                            }
                            return false
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
                            // Block intrusive popup windows from stream servers
                            return false
                        }
                    }
                    tag = videoUrl
                    loadTargetMedia(this, videoUrl)
                    webViewRef = this
                }
            },
            update = { wv ->
                webViewRef = wv
                val currentTag = wv.tag as? String
                if (currentTag != videoUrl) {
                    wv.tag = videoUrl
                    loadTargetMedia(wv, videoUrl)
                }
            }
        )

        // Buffering Indicator
        if (isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ImmNetflixRed,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "جاري تحميل البث...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Discrete Floating Overlay Toggle Button (ALWAYS accessible, never blocks player center)
        IconButton(
            onClick = { showControls = !showControls },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .testTag("toggle_overlay_controls_button")
        ) {
            Icon(
                imageVector = if (showControls) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (showControls) "Hide Overlay" else "Show Overlay",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Top Navigation & Actions Bar (Floats at top, only occupies its own height)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.92f),
                                Color.Black.copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Back/Close & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
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
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = ImmNetflixRed,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = activeServer.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "اضغط على المشغل للتحكم الكامل • ${activeServer.displayName}",
                                fontSize = 10.sp,
                                color = ImmTextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    // Right: Actions (Reload, External, Fullscreen) - leave room for toggle button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 38.dp)
                    ) {
                        // Reload Stream
                        IconButton(
                            onClick = {
                                webViewRef?.let { loadTargetMedia(it, videoUrl) }
                                Toast.makeText(context, "إعادة تحميل المشغل...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Open in External Browser / Player
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح الرابط خارجياً", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open in External App",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Fullscreen Toggle
                        IconButton(
                            onClick = onToggleFullscreen,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .testTag("top_fullscreen_button")
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Server & Controls Strip (Floats at bottom, only occupies its own height)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Quick Server Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ServerChip(
                            label = "سيرفر 1",
                            isSelected = activeServer == StreamServer.SERVER_1,
                            onClick = { onServerSelect(StreamServer.SERVER_1) }
                        )
                        ServerChip(
                            label = "سيرفر 2",
                            isSelected = activeServer == StreamServer.SERVER_2,
                            onClick = { onServerSelect(StreamServer.SERVER_2) }
                        )
                        ServerChip(
                            label = "سيرفر 3",
                            isSelected = activeServer == StreamServer.SERVER_3,
                            onClick = { onServerSelect(StreamServer.SERVER_3) }
                        )
                        ServerChip(
                            label = "تريلر",
                            isSelected = activeServer == StreamServer.TRAILER,
                            onClick = { onServerSelect(StreamServer.TRAILER) }
                        )
                    }

                    // Direct Fullscreen Button in Bottom Corner
                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("fullscreen_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
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
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) ImmNetflixRed else Color.White.copy(alpha = 0.18f),
        modifier = Modifier.height(28.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
