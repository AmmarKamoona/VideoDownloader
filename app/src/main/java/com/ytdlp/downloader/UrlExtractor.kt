package com.ytdlp.downloader

/**
 * Pure-Kotlin URL extraction & validation utilities.
 * Used by the share-receive flow and the clipboard service.
 */
object UrlExtractor {

    // Catches everything from "https://youtu.be/abc" to "http://www.tiktok.com/@user/video/123?si=xyz"
    // Allows any non-whitespace character after the scheme so we don't lose query params.
    private val URL_REGEX = Regex("""https?://[^\s]+""", RegexOption.IGNORE_CASE)

    /**
     * Hosts the app actively supports for video downloads.
     * Adding a new platform = just append its host(s) here. No manifest changes needed —
     * the existing ACTION_SEND text/plain filter already catches every share from every
     * social app since they all share URLs as plain text.
     */
    val SUPPORTED_HOSTS = listOf(
        // YouTube
        "youtube.com", "youtu.be", "m.youtube.com", "music.youtube.com",
        // TikTok
        "tiktok.com", "vm.tiktok.com", "vt.tiktok.com",
        // Instagram
        "instagram.com",
        // Twitter / X
        "twitter.com", "x.com", "t.co",
        // Facebook
        "facebook.com", "fb.watch", "fb.com",
        // Reddit
        "reddit.com", "redd.it", "v.redd.it",
        // Other
        "vimeo.com", "dailymotion.com", "twitch.tv",
        "bilibili.com", "b23.tv",
        "soundcloud.com", "bandcamp.com",
        "streamable.com",
    )

    /**
     * Extract the first URL from arbitrary text.
     * YouTube sometimes shares "Check out this video: https://youtu.be/xxx" — this
     * pulls just the URL portion.
     */
    fun extract(text: String?): String? {
        if (text.isNullOrBlank()) return null
        return URL_REGEX.find(text.trim())?.value?.trim()
    }

    /** Returns true if [text] starts with http(s):// and contains a supported host. */
    fun isSupportedVideoUrl(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val lower = text.lowercase()
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) return false
        return SUPPORTED_HOSTS.any { host -> lower.contains(host) }
    }

    /**
     * One-call helper: extract URL from text and validate it's a supported video URL.
     * Returns the clean URL or null if nothing usable was found.
     */
    fun extractAndValidate(text: String?): String? {
        val url = extract(text) ?: return null
        return if (isSupportedVideoUrl(url)) url else null
    }
}
