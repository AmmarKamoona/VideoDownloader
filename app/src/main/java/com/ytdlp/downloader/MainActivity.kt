package com.ytdlp.downloader

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ytdlp.downloader.ui.DownloaderScreen
import com.ytdlp.downloader.ui.theme.YtDownloaderTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)

        setContent {
            YtDownloaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DownloaderScreen(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkClipboardForUrl()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
            if (!sharedText.isNullOrBlank()) {
                viewModel.setUrl(sharedText)
                // If it looks like a video URL, also trigger the quick-download banner
                if (looksLikeVideoUrl(sharedText)) {
                    viewModel.onClipboardUrlDetected(sharedText)
                }
            }
        }
    }

    private fun checkClipboardForUrl() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            ?.trim()
            ?: return

        if (looksLikeVideoUrl(text)) {
            viewModel.onClipboardUrlDetected(text)
        }
    }

    companion object {
        private val VIDEO_HOSTS = listOf(
            "youtube.com", "youtu.be",
            "twitter.com", "x.com",
            "instagram.com",
            "tiktok.com",
            "facebook.com", "fb.watch",
            "vimeo.com",
            "dailymotion.com",
            "twitch.tv",
            "reddit.com",
            "bilibili.com"
        )

        fun looksLikeVideoUrl(text: String): Boolean {
            if (!text.startsWith("http://") && !text.startsWith("https://")) return false
            return VIDEO_HOSTS.any { host -> text.contains(host) }
        }
    }
}
