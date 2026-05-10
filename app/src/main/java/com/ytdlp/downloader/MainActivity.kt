package com.ytdlp.downloader

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ytdlp.downloader.ui.DownloaderScreen
import com.ytdlp.downloader.ui.theme.YtDownloaderTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    /**
     * Reactive Compose state for the bubble toggle.
     * Plain Kotlin booleans don't trigger recomposition — this one does.
     */
    private var bubbleEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)
        refreshBubbleState()

        setContent {
            YtDownloaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DownloaderScreen(
                        viewModel       = viewModel,
                        isBubbleEnabled = bubbleEnabled,
                        onToggleBubble  = { toggleBubble() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // User may have granted permission in Settings or stopped the service
        // from the notification — re-sync the toggle state every time.
        refreshBubbleState()
        // If permission was just granted, auto-start the service so the switch
        // visually matches the user's intent without a second tap.
        if (Settings.canDrawOverlays(this) && !isServiceRunning()) {
            startBubbleService()
            refreshBubbleState()
        }
        // Re-scan the Downloads folder so previously-downloaded files appear
        // in the Downloads tab even after the app process was killed.
        viewModel.refreshDownloads(downloadDir())
        checkClipboardForUrl()
    }

    private fun downloadDir(): String = android.os.Environment
        .getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        .apply { mkdirs() }
        .absolutePath

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    // ── Toggle logic ──────────────────────────────────────────────────────────

    private fun toggleBubble() {
        if (!Settings.canDrawOverlays(this)) {
            // Need permission — open the system "Draw over other apps" page.
            // onResume will start the service automatically when the user returns.
            startActivity(Intent(this, OverlayPermissionActivity::class.java))
            return
        }
        // Permission granted — flip the service on or off
        if (isServiceRunning()) {
            stopService(Intent(this, ClipboardService::class.java))
        } else {
            startBubbleService()
        }
        refreshBubbleState()
    }

    private fun startBubbleService() {
        val serviceIntent = Intent(this, ClipboardService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun refreshBubbleState() {
        bubbleEnabled = Settings.canDrawOverlays(this) && isServiceRunning()
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == ClipboardService::class.java.name }
    }

    // ── Intent handling ───────────────────────────────────────────────────────

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == ClipboardService.ACTION_OPEN_URL) {
            val url = intent.getStringExtra(ClipboardService.EXTRA_URL)?.trim()
            if (!url.isNullOrBlank()) {
                viewModel.setUrl(url)
                viewModel.onClipboardUrlDetected(url)
            }
            return
        }
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
