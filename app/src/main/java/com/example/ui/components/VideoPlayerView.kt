package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.util.SubtitleProvider
import com.example.ui.theme.ImmNetflixRed
import com.example.ui.theme.ImmTextMuted
import com.example.ui.theme.ImmTextSecondary
import com.example.ui.viewmodel.StreamServer
import kotlinx.coroutines.delay
import java.io.ByteArrayInputStream

@OptIn(ExperimentalMaterial3Api::class)
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
    isTvShow: Boolean = false,
    currentSeason: Int = 1,
    currentEpisode: Int = 1,
    onNextEpisode: (() -> Unit)? = null,
    onSelectEpisode: ((season: Int, episode: Int) -> Unit)? = null,
    runtime: String = "2h 15m",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Auto rotate to Landscape and hide battery, clock & navigation bar in fullscreen mode
    DisposableEffect(isFullscreen) {
        if (isFullscreen) {
            val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

            val window = activity?.window
            window?.let { w ->
                val controller = WindowCompat.getInsetsController(w, w.decorView)
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            }

            onDispose {
                activity?.requestedOrientation = originalOrientation
                val window = activity?.window
                window?.let { w ->
                    val controller = WindowCompat.getInsetsController(w, w.decorView)
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        } else {
            onDispose { }
        }
    }

    // UI States
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var isScreenLocked by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var customFullscreenView by remember { mutableStateOf<android.view.View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // Scrubber & Time
    var currentProgress by remember { mutableFloatStateOf(0.18f) }
    var isDraggingSlider by remember { mutableStateOf(false) }

    // Playback Speed
    val speeds = listOf("0.75x", "1.0x", "1.25x", "1.5x")
    var currentSpeedIndex by remember { mutableIntStateOf(1) }

    // Dialog sheets
    var showAudioSubtitleSheet by remember { mutableStateOf(false) }
    var showEpisodesSheet by remember { mutableStateOf(false) }
    var showServersSheet by remember { mutableStateOf(false) }

    var selectedAudioTrack by remember { mutableStateOf("English (Original)") }
    var selectedSubtitleTrack by remember { mutableStateOf("العربية [Arabic]") }

    // Auto-hide Netflix controls after 5 seconds of inactivity
    LaunchedEffect(showControls, isDraggingSlider, showAudioSubtitleSheet, showEpisodesSheet, showServersSheet, isScreenLocked) {
        if (showControls && !isDraggingSlider && !showAudioSubtitleSheet && !showEpisodesSheet && !showServersSheet && !isScreenLocked) {
            delay(5000)
            showControls = false
        }
    }

    // Build iframe HTML without allow-popups in sandbox to prevent all new ad pages on Play!
    fun buildSandboxedIframeHtml(streamUrl: String): String {
        return """
            <!DOCTYPE html>
            <html lang="ar">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body { width: 100%; height: 100%; overflow: hidden; background-color: #000; }
                    #player-container { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
                    iframe { width: 100%; height: 100%; border: 0; outline: none; }
                </style>
            </head>
            <body>
                <div id="player-container">
                    <iframe 
                        src="$streamUrl" 
                        width="100%" 
                        height="100%" 
                        frameborder="0" 
                        allowfullscreen 
                        referrerpolicy="origin"
                        sandbox="allow-scripts allow-same-origin allow-forms">
                    </iframe>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    // Load media into WebView directly with authentic Chrome headers
    fun loadTargetMedia(wv: WebView, url: String) {
        if (url.isBlank()) return
        isBuffering = true
        val lower = url.trim().lowercase()
        if (lower.endsWith(".mp4") || lower.endsWith(".m3u8") || lower.endsWith(".webm") || lower.endsWith(".mkv")) {
            val html = SubtitleProvider.generatePlayerHtml(url, customSubtitleUrl, "ar")
            wv.loadDataWithBaseURL("https://imm.tv", html, "text/html", "UTF-8", null)
        } else {
            // Enable Cookies for Cloudflare Turnstile verification
            android.webkit.CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(wv, true)
            }
            val headers = HashMap<String, String>().apply {
                put("Referer", "https://google.com/")
                put("Accept-Language", "ar,en-US,en;q=0.9")
                put("Sec-Ch-Ua", "\"Chromium\";v=\"128\", \"Not;A=Brand\";v=\"24\", \"Google Chrome\";v=\"128\"")
                put("Sec-Ch-Ua-Mobile", "?1")
                put("Sec-Ch-Ua-Platform", "\"Android\"")
            }
            wv.loadUrl(url, headers)
        }
    }

    // Execute video control commands inside player
    fun sendJsCommand(cmd: String) {
        webViewRef?.evaluateJavascript(cmd, null)
    }

    if (customFullscreenView != null) {
        BackHandler {
            customFullscreenView = null
            customViewCallback?.onCustomViewHidden()
            customViewCallback = null
        }
        Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { customFullscreenView!! },
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        // Embedded Android WebView with Ad-Blocking and Cloudflare bypass
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
                        userAgentString = "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    val cookieManager = android.webkit.CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)
                    setBackgroundColor(android.graphics.Color.BLACK)
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isBuffering = true
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isBuffering = false
                            // Suppress popup windows and auto-open scripts
                            view?.evaluateJavascript(
                                "(function(){ window.open = function(){ return null; }; })();",
                                null
                            )
                            // Auto-detect and enable Arabic subtitles
                            val autoArabicJs = """
                                (function() {
                                    function activateArabicSubs() {
                                        try {
                                            var vids = document.querySelectorAll('video');
                                            for (var i = 0; i < vids.length; i++) {
                                                var v = vids[i];
                                                if (v.textTracks) {
                                                    for (var j = 0; j < v.textTracks.length; j++) {
                                                        var t = v.textTracks[j];
                                                        var l = (t.label || '').toLowerCase();
                                                        var lang = (t.language || '').toLowerCase();
                                                        if (lang === 'ar' || lang.startsWith('ar') || l.includes('arabic') || l.includes('عرب')) {
                                                            t.mode = 'showing';
                                                        }
                                                    }
                                                }
                                            }
                                            var items = document.querySelectorAll('button, li, [role="menuitem"], [role="menuitemradio"], span, option');
                                            for (var k = 0; k < items.length; k++) {
                                                var txt = (items[k].textContent || '').trim().toLowerCase();
                                                if (txt === 'arabic' || txt === 'العربية' || txt === 'ar') {
                                                    items[k].click();
                                                }
                                            }
                                        } catch (e) {}
                                    }
                                    activateArabicSubs();
                                    setInterval(activateArabicSubs, 2000);
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(autoArabicJs, null)
                        }
                        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                            val reqUrl = request?.url?.toString()?.lowercase() ?: return null
                            // Never block Cloudflare challenges or Turnstile
                            if (reqUrl.contains("cloudflare") || reqUrl.contains("turnstile") || reqUrl.contains("challenges")) {
                                return super.shouldInterceptRequest(view, request)
                            }
                            val adPatterns = listOf(
                                "popads", "popcash", "adsterra", "doubleclick", "googlesyndication",
                                "bet365", "1xbet", "propellerads", "clickadu", "exoclick", "adtrue",
                                "adcash", "hilltopads", "monetag", "deloton", "trafficjunky"
                            )
                            if (adPatterns.any { reqUrl.contains(it) }) {
                                return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                            }
                            return super.shouldInterceptRequest(view, request)
                        }
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val nextUrl = request?.url?.toString() ?: return false
                            val lower = nextUrl.lowercase()
                            // Allow player and verification domains
                            if (lower.contains("vidsrc.pm") || lower.contains("embed.su") || lower.contains("vidcore.org") || lower.contains("cloudflare") || lower.contains("turnstile")) {
                                return false
                            }
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
                        override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                            super.onShowCustomView(view, callback)
                            customFullscreenView = view
                            customViewCallback = callback
                            onToggleFullscreen()
                        }

                        override fun onHideCustomView() {
                            super.onHideCustomView()
                            customFullscreenView = null
                            customViewCallback?.onCustomViewHidden()
                            customViewCallback = null
                        }

                        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
                            // لمنع فتح نوافذ إعلانية منبثقة جديدة
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

        // Buffering Spinner (Netflix Red)
        if (isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ImmNetflixRed,
                    strokeWidth = 3.5.dp,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        // Tap layer to toggle Netflix UI
        if (!isScreenLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showControls = !showControls
                    }
            )
        }

        // ================= NETFLIX SCREEN LOCKED HUD =================
        if (isScreenLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Keep locked state active
                    }
            ) {
                Surface(
                    onClick = {
                        isScreenLocked = false
                        showControls = true
                    },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Unlock",
                            tint = ImmNetflixRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "الشاشة مقفلة • اضغط لإلغاء القفل",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // ================= NETFLIX OVERLAY CONTROLS =================
        AnimatedVisibility(
            visible = showControls && !isScreenLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // ---------- TOP GRADIENT BAR ----------
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.95f),
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Back button + Netflix Logo + Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (onClosePlayer != null) {
                                IconButton(
                                    onClick = onClosePlayer,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .testTag("netflix_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            // Netflix "N" Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ImmNetflixRed,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "N",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontFamily = FontFamily.Serif,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    NetflixBadge(text = "4K ULTRA HD")
                                    NetflixBadge(text = "HDR")
                                    NetflixBadge(text = "5.1")
                                    if (isTvShow) {
                                        NetflixBadge(text = "S$currentSeason:E$currentEpisode")
                                    }
                                }
                            }
                        }

                        // Right: Top quick actions (Reload, External, Close)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Reload Stream
                            IconButton(
                                onClick = {
                                    webViewRef?.let { loadTargetMedia(it, videoUrl) }
                                    Toast.makeText(context, "إعادة تحميل المشغل...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Open in External App
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "External App",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // ---------- CENTER PLAYBACK CONTROLS (Floating Glassmorphism Capsule) ----------
                Surface(
                    shape = RoundedCornerShape(44.dp),
                    color = Color(0xDD0D0F14),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.2f),
                                ImmNetflixRed.copy(alpha = 0.85f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        )
                    ),
                    shadowElevation = 24.dp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("cinematic_center_controls")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        // Rewind 10s (-10s)
                        Surface(
                            onClick = {
                                sendJsCommand("var v = document.querySelector('video'); if (v) { v.currentTime = Math.max(0, v.currentTime - 10); }")
                                currentProgress = (currentProgress - 0.05f).coerceAtLeast(0f)
                                Toast.makeText(context, "⏪ 10- ثوانٍ", Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .size(54.dp)
                                .testTag("netflix_rewind_10")
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "Rewind 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "10-",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Main Radiant Play / Pause (68dp)
                        Surface(
                            onClick = {
                                isPlaying = !isPlaying
                                sendJsCommand("var v = document.querySelector('video'); if (v) { if (v.paused) v.play(); else v.pause(); }")
                            },
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF2222),
                                            ImmNetflixRed,
                                            Color(0xFF8B0000)
                                        )
                                    )
                                )
                                .testTag("netflix_center_play_pause")
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        // Forward 10s (+10s)
                        Surface(
                            onClick = {
                                sendJsCommand("var v = document.querySelector('video'); if (v) { v.currentTime += 10; }")
                                currentProgress = (currentProgress + 0.05f).coerceAtMost(1f)
                                Toast.makeText(context, "⏩ 10+ ثوانٍ", Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .size(54.dp)
                                .testTag("netflix_forward_10")
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "Forward 10s",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "10+",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // ---------- BOTTOM CONTROLS & SCRUBBER ----------
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Netflix Scrubber Bar (Signature Red Track)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = calculateElapsed(currentProgress),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Slider(
                            value = currentProgress,
                            onValueChange = {
                                isDraggingSlider = true
                                currentProgress = it
                            },
                            onValueChangeFinished = {
                                isDraggingSlider = false
                                sendJsCommand("var v = document.querySelector('video'); if (v && v.duration) { v.currentTime = v.duration * $currentProgress; }")
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = ImmNetflixRed,
                                activeTrackColor = ImmNetflixRed,
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                                .testTag("netflix_scrubber_slider")
                        )

                        Text(
                            text = calculateRemaining(currentProgress),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Netflix Bottom Actions Bar:
                    // [Lock] [Speed] [Audio & Subtitles] [Episodes / Next] [Servers] [Fullscreen]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. Lock Screen
                        NetflixActionItem(
                            icon = Icons.Default.Lock,
                            label = "قفل",
                            onClick = {
                                isScreenLocked = true
                                showControls = false
                            }
                        )

                        // 2. Playback Speed
                        NetflixActionItem(
                            icon = Icons.Default.Speed,
                            label = "السرعة (${speeds[currentSpeedIndex]})",
                            onClick = {
                                currentSpeedIndex = (currentSpeedIndex + 1) % speeds.size
                                val rate = when (currentSpeedIndex) {
                                    0 -> 0.75
                                    1 -> 1.0
                                    2 -> 1.25
                                    else -> 1.5
                                }
                                sendJsCommand("var v = document.querySelector('video'); if (v) { v.playbackRate = $rate; }")
                                Toast.makeText(context, "السرعة: ${speeds[currentSpeedIndex]}", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 3. Audio & Subtitles
                        NetflixActionItem(
                            icon = Icons.Default.Subtitles,
                            label = "الصوت والترجمة",
                            onClick = { showAudioSubtitleSheet = true }
                        )

                        // 4. TV Episodes / Next Episode
                        if (isTvShow) {
                            NetflixActionItem(
                                icon = Icons.Default.Tv,
                                label = "الحلقات",
                                onClick = { showEpisodesSheet = true }
                            )

                            if (onNextEpisode != null) {
                                NetflixActionItem(
                                    icon = Icons.Default.SkipNext,
                                    label = "التالية",
                                    onClick = onNextEpisode
                                )
                            }
                        }

                        // 5. Servers Selector
                        NetflixActionItem(
                            icon = Icons.Default.Layers,
                            label = "السيرفرات",
                            onClick = { showServersSheet = true }
                        )

                        // 6. Fullscreen Toggle
                        IconButton(
                            onClick = onToggleFullscreen,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("netflix_fullscreen_toggle")
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

        // ================= NETFLIX AUDIO & SUBTITLES SHEET =================
        if (showAudioSubtitleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAudioSubtitleSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF141414),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "الصوت والترجمة (Audio & Subtitles)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Audio Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الصوت (Audio)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImmNetflixRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            listOf("English (Original)", "العربية [Arabic]", "Français").forEach { audio ->
                                NetflixOptionRow(
                                    title = audio,
                                    isSelected = selectedAudioTrack == audio,
                                    onSelect = {
                                        selectedAudioTrack = audio
                                        showAudioSubtitleSheet = false
                                        Toast.makeText(context, "الصوت: $audio", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        // Subtitles Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الترجمة (Subtitles)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImmNetflixRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            listOf("العربية [Arabic]", "English", "إيقاف [Off]").forEach { sub ->
                                NetflixOptionRow(
                                    title = sub,
                                    isSelected = selectedSubtitleTrack == sub,
                                    onSelect = {
                                        selectedSubtitleTrack = sub
                                        showAudioSubtitleSheet = false
                                        if (sub.contains("العربية") || sub.contains("Arabic")) {
                                            sendJsCommand("""
                                                (function() {
                                                    var vids = document.querySelectorAll('video');
                                                    for (var i = 0; i < vids.length; i++) {
                                                        var v = vids[i];
                                                        if (v.textTracks) {
                                                            for (var j = 0; j < v.textTracks.length; j++) {
                                                                var t = v.textTracks[j];
                                                                var l = (t.label || '').toLowerCase();
                                                                var lang = (t.language || '').toLowerCase();
                                                                if (lang === 'ar' || lang.startsWith('ar') || l.includes('arabic') || l.includes('عرب')) {
                                                                    t.mode = 'showing';
                                                                }
                                                            }
                                                        }
                                                    }
                                                    var items = document.querySelectorAll('button, li, [role="menuitem"], [role="menuitemradio"], span, option');
                                                    for (var k = 0; k < items.length; k++) {
                                                        var txt = (items[k].textContent || '').trim().toLowerCase();
                                                        if (txt === 'arabic' || txt === 'العربية' || txt === 'ar') {
                                                            items[k].click();
                                                        }
                                                    }
                                                })();
                                            """.trimIndent())
                                            Toast.makeText(context, "تم تفعيل الترجمة العربية ✅", Toast.LENGTH_SHORT).show()
                                        } else if (sub.contains("إيقاف") || sub.contains("Off")) {
                                            sendJsCommand("var v = document.querySelector('video'); if (v && v.textTracks) { for(var i=0; i<v.textTracks.length; i++) v.textTracks[i].mode = 'disabled'; }")
                                            Toast.makeText(context, "تم إيقاف الترجمة", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "الترجمة: $sub", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // ================= NETFLIX EPISODES SHEET (For TV Shows) =================
        if (showEpisodesSheet && isTvShow) {
            ModalBottomSheet(
                onDismissRequest = { showEpisodesSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF141414),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "الحلقات (Episodes)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Seasons Tabs
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items((1..3).toList()) { s ->
                            val isSel = s == currentSeason
                            Surface(
                                onClick = { onSelectEpisode?.invoke(s, 1) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) ImmNetflixRed else Color(0xFF262626)
                            ) {
                                Text(
                                    text = "الموسم $s",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Episodes List
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((1..10).toList()) { ep ->
                            val isCurrent = currentEpisode == ep
                            Surface(
                                onClick = {
                                    onSelectEpisode?.invoke(currentSeason, ep)
                                    showEpisodesSheet = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) Color(0xFF2A1515) else Color(0xFF1E1E1E),
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, ImmNetflixRed) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "الحلقة $ep • الحلقة كاملة",
                                            fontSize = 13.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "48m • HD",
                                            fontSize = 11.sp,
                                            color = ImmTextMuted
                                        )
                                    }

                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Playing",
                                            tint = ImmNetflixRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // ================= NETFLIX SERVERS SHEET =================
        if (showServersSheet) {
            ModalBottomSheet(
                onDismissRequest = { showServersSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF141414),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "اختر سيرفر البث (Streaming Server)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    listOf(
                        StreamServer.SERVER_1 to "سيرفر 1 (VidSrc.pm - الأساسي)",
                        StreamServer.SERVER_2 to "سيرفر 2 (Embed.su - بديل 1)",
                        StreamServer.SERVER_3 to "سيرفر 3 (VidCore.org - بديل 2)",
                        StreamServer.TRAILER to "الإعلان الترويجي (Trailer)"
                    ).forEach { (srv, label) ->
                        val isSelected = activeServer == srv
                        Surface(
                            onClick = {
                                onServerSelect(srv)
                                showServersSheet = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF2A1515) else Color(0xFF1E1E1E),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ImmNetflixRed) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.White
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ImmNetflixRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun NetflixBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(2.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun NetflixActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun NetflixOptionRow(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = ImmNetflixRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun calculateElapsed(progress: Float): String {
    val totalSeconds = 7200 // 2 hours
    val elapsed = (totalSeconds * progress).toInt()
    val m = elapsed / 60
    val s = elapsed % 60
    return String.format("%02d:%02d", m, s)
}

private fun calculateRemaining(progress: Float): String {
    val totalSeconds = 7200 // 2 hours
    val remaining = (totalSeconds * (1f - progress)).toInt()
    val h = remaining / 3600
    val m = (remaining % 3600) / 60
    val s = remaining % 60
    return if (h > 0) {
        String.format("-%d:%02d:%02d", h, m, s)
    } else {
        String.format("-%02d:%02d", m, s)
    }
}
