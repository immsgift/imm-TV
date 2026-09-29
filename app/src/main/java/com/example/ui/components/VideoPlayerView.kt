package com.example.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.util.SubtitleProvider
import com.example.ui.viewmodel.StreamServer
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerView(
    videoUrl: String,
    title: String = "",
    activeServer: StreamServer = StreamServer.SERVER_1,
    onServerSelect: (StreamServer) -> Unit = {},
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

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var customFullscreenView by remember { mutableStateOf<android.view.View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // Load media into WebView directly with authentic Chrome headers
    fun loadTargetMedia(wv: WebView, url: String) {
        if (url.isBlank()) return
        val lower = url.trim().lowercase()
        if (lower.endsWith(".mp4") || lower.endsWith(".m3u8") || lower.endsWith(".webm") || lower.endsWith(".mkv")) {
            val html = SubtitleProvider.generatePlayerHtml(url, customSubtitleUrl, "ar")
            wv.loadDataWithBaseURL("https://imm.tv", html, "text/html", "UTF-8", null)
        } else {
            // Enable Cookies for Cloudflare Turnstile verification
            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(wv, true)

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

    // Hardware back press handler
    BackHandler {
        if (customFullscreenView != null) {
            customFullscreenView = null
            customViewCallback?.onCustomViewHidden()
            customViewCallback = null
        } else if (onClosePlayer != null) {
            onClosePlayer()
        } else {
            onToggleFullscreen()
        }
    }

    // Native Fullscreen Video View (if HTML5 video requests native fullscreen)
    if (customFullscreenView != null) {
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
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Embedded Android WebView: 100% full screen, in foreground, receiving all clicks directly
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("embedded_player_webview"),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    isClickable = true
                    isFocusable = true
                    isFocusableInTouchMode = true
                    requestFocus()

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
                        override fun onPageFinished(view: WebView?, url: String?) {
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

        // 2. Only Back Button in top corner (non-blocking for video clicks)
        if (onClosePlayer != null || isFullscreen) {
            IconButton(
                onClick = {
                    if (onClosePlayer != null) {
                        onClosePlayer()
                    } else {
                        onToggleFullscreen()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .testTag("player_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
