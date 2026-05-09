package com.ytdlp.downloader

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
                    DownloaderScreen(
                        viewModel         = viewModel,
                        isBubbleEnabled   = Settings.canDrawOverlays(this),
                        onToggleBubble    = { toggleBubble() }
                    )
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
        // Bubble tapped in another app → URL passed directly
        if (intent?.action == ClipboardService.ACTION_OPEN_URL) {
            val url = intent.getStringExtra(ClipboardService.EXTRA_URL)?.trim()
            if (!url.isNullOrBlank()) {
                viewModel.setUrl(url)
                viewModel.onClipboardUrlDetected(url)
            }
            return
        }
        // Shared from another app via ACTION_SEND
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
            if (!sharedText.isNullOrBlank()) {
                viewModel.setUrl(sharedText)
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

    private fun toggleBubble() {
        if (Settings.canDrawOverlays(this)) {
            // Already granted — stop the service (toggle off)
            stopService(Intent(this, ClipboardService::class.java))
        } else {
            // Need permission — open the permission screen
            startActivity(Intent(this, OverlayPermissionActivity::class.java))
        }
    }

    companion object {
        val VIDEO_HOSTS = listOf(
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
