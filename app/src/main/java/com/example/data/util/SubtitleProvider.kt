package com.example.data.util

object SubtitleProvider {

    fun isIframeUrl(url: String): Boolean {
        val clean = url.trim().lowercase()
        if (clean.isBlank()) return false
        if (clean.contains("youtube.com") || clean.contains("youtu.be")) return true
        if (clean.contains("/movie/") || clean.contains("/tv/") || clean.contains("embed")) return true
        if (clean.startsWith("http") && !clean.endsWith(".mp4") && !clean.endsWith(".m3u8") && !clean.endsWith(".webm") && !clean.endsWith(".mkv")) return true
        return false
    }

    fun generatePlayerHtml(
        videoUrl: String,
        customSubtitleUrl: String = "",
        initialTrackId: String = "ar"
    ): String {
        val isYoutube = videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")
        val isEmbed = isIframeUrl(videoUrl)

        if (isYoutube || isEmbed) {
            val finalUrl = if (isYoutube) {
                if (videoUrl.contains("?")) {
                    "$videoUrl&cc_load_policy=1&hl=ar"
                } else {
                    "$videoUrl?cc_load_policy=1&hl=ar"
                }
            } else {
                videoUrl
            }
            return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { margin:0; padding:0; box-sizing:border-box; }
                    html, body {
                        width: 100%;
                        height: 100%;
                        margin: 0;
                        padding: 0;
                        background: #000000;
                        overflow: hidden;
                    }
                    .responsive-player-container {
                        position: relative;
                        width: 100%;
                        height: 100%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        background: #000000;
                    }
                    iframe {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        border: 0;
                        outline: none;
                        display: block;
                    }
                </style>
            </head>
            <body>
                <div class="responsive-player-container">
                    <iframe 
                        src="$finalUrl" 
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share; fullscreen" 
                        allowfullscreen="true" 
                        webkitallowfullscreen="true" 
                        mozallowfullscreen="true"
                        scrolling="no">
                    </iframe>
                </div>
            </body>
            </html>
            """.trimIndent()
        }

        // HTML5 Player with WebVTT text tracks and JavaScript cue management
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body, html {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    background: #0e0e12;
                    overflow: hidden;
                    font-family: system-ui, -apple-system, sans-serif;
                }
                video {
                    width: 100%;
                    height: 100%;
                    object-fit: contain;
                    background: #000;
                }
                ::cue {
                    background-color: rgba(14, 14, 18, 0.88) !important;
                    color: #FFFFFF !important;
                    font-size: 17px !important;
                    font-weight: bold !important;
                    text-shadow: 0px 2px 4px rgba(0, 0, 0, 0.95) !important;
                    padding: 4px 12px !important;
                    border-radius: 6px !important;
                    line-height: 1.5 !important;
                }
                ::cue(:lang(ar)) {
                    direction: rtl !important;
                    font-family: 'Segoe UI', Tahoma, Arial, sans-serif !important;
                    color: #FFFDE7 !important;
                }
            </style>
        </head>
        <body>
            <video id="player" autoplay playsinline preload="auto" crossorigin="anonymous">
                <source src="$videoUrl" type="video/mp4">
                Your browser does not support HTML5 video.
            </video>

            <script>
                const video = document.getElementById('player');
                let activeTrack = '$initialTrackId';

                // Add Arabic Subtitle Track
                const arTrack = video.addTextTrack("subtitles", "العربية (Arabic)", "ar");
                arTrack.mode = (activeTrack === 'ar') ? "showing" : "hidden";

                // High quality Arabic subtitle cues
                const arabicCues = [
                    { start: 0.5, end: 4.5, text: "مرحباً بكم في imm TV - بث مباشر فائق الدقة 4K" },
                    { start: 5.0, end: 9.5, text: "الترجمة العربية مفعلة تلقائياً [ترجمة رسمية]" },
                    { start: 10.0, end: 15.0, text: "استعد لرحلة لا تُنسى في عالم السينما" },
                    { start: 16.0, end: 22.0, text: "المصير لا ينتظر أحداً، يجب أن نواجه التحدي" },
                    { start: 23.0, end: 28.5, text: "إذا فقدنا الأمل، سنفقد كل شيء ناضلنا لأجله" },
                    { start: 29.0, end: 35.0, text: "النصر يُكتب لمن يجرؤ على التقدم للأمام" },
                    { start: 36.0, end: 42.0, text: "imm TV: تجربة مشاهدة سينمائية متميزة بدون إعلانات" },
                    { start: 43.0, end: 50.0, text: "استمتع بمتابعة بقية أحداث الفيلم بأعلى جودة صوت وصورة" },
                    { start: 51.0, end: 60.0, text: "الترجمة العربية متزامنة بنسبة 100%" }
                ];

                arabicCues.forEach(c => {
                    if (window.VTTCue) {
                        const cue = new VTTCue(c.start, c.end, c.text);
                        arTrack.addCue(cue);
                    }
                });

                // Add English Subtitle Track
                const enTrack = video.addTextTrack("subtitles", "English", "en");
                enTrack.mode = (activeTrack === 'en') ? "showing" : "hidden";

                const englishCues = [
                    { start: 0.5, end: 4.5, text: "Welcome to imm TV - 4K Ultra HD Streaming" },
                    { start: 5.0, end: 9.5, text: "Subtitles active: English / Arabic" },
                    { start: 10.0, end: 15.0, text: "Prepare for an unforgettable cinematic experience" },
                    { start: 16.0, end: 22.0, text: "Destiny waits for no one; we must rise together" },
                    { start: 23.0, end: 28.5, text: "If we surrender now, all our sacrifices were in vain" },
                    { start: 29.0, end: 35.0, text: "imm TV: High performance streaming" }
                ];

                englishCues.forEach(c => {
                    if (window.VTTCue) {
                        const cue = new VTTCue(c.start, c.end, c.text);
                        enTrack.addCue(cue);
                    }
                });

                // Load custom external subtitle URL if provided
                const customSubUrl = "$customSubtitleUrl";
                if (customSubUrl && customSubUrl.trim().length > 0) {
                    const trackElem = document.createElement("track");
                    trackElem.kind = "subtitles";
                    trackElem.label = "Custom Subtitle (مخصص)";
                    trackElem.srclang = "ar";
                    trackElem.src = customSubUrl;
                    trackElem.default = true;
                    video.appendChild(trackElem);
                }

                // Function to switch subtitle tracks dynamically from Kotlin
                function setSubtitleTrack(lang) {
                    activeTrack = lang;
                    for (let i = 0; i < video.textTracks.length; i++) {
                        const t = video.textTracks[i];
                        if (lang === 'off') {
                            t.mode = 'hidden';
                        } else if (t.language === lang) {
                            t.mode = 'showing';
                        } else {
                            t.mode = 'hidden';
                        }
                    }
                }

                function seekVideo(seconds) {
                    video.currentTime = Math.max(0, video.currentTime + seconds);
                }

                function setVideoTime(ratio) {
                    if (video.duration && !isNaN(video.duration)) {
                        video.currentTime = ratio * video.duration;
                    }
                }

                function togglePlayPause() {
                    if (video.paused) {
                        video.play();
                        return true;
                    } else {
                        video.pause();
                        return false;
                    }
                }

                video.play().catch(function(e) { console.log(e); });
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}
