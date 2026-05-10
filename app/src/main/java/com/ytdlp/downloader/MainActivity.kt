package com.ytdlp.downloader

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
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

    private fun downloadDir(): String = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
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

    /**
     * Routes incoming intents to the right handler.
     *
     * Supported entry points:
     *  1. ACTION_OPEN_URL — bubble tapped from another app
     *  2. ACTION_SEND with text/plain — Share Sheet (YouTube, TikTok, Insta, etc.)
     *  3. ACTION_VIEW with http(s) URI — user tapped a video link in a browser/email
     *  4. Default launcher — no action needed
     */
    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            ClipboardService.ACTION_OPEN_URL -> {
                val url = intent.getStringExtra(ClipboardService.EXTRA_URL)
                applyIncomingUrl(url, fromShare = false)
            }
            Intent.ACTION_SEND -> {
                if (intent.type != "text/plain") {
                    Toast.makeText(this, "Only text links are supported", Toast.LENGTH_SHORT).show()
                    return
                }
                // Try EXTRA_TEXT first (typical), then EXTRA_SUBJECT as a fallback
                val raw = intent.getStringExtra(Intent.EXTRA_TEXT)
                    ?: intent.getStringExtra(Intent.EXTRA_SUBJECT)
                applyIncomingUrl(raw, fromShare = true)
            }
            Intent.ACTION_VIEW -> {
                // User tapped a video URL in a browser, email, etc.
                val raw = intent.dataString
                applyIncomingUrl(raw, fromShare = true)
            }
        }
    }

    /**
     * Validate, clean, and apply an incoming URL.
     * Auto-triggers fetchInfo() so the user sees the title immediately
     * without needing to tap Info first.
     */
    private fun applyIncomingUrl(rawText: String?, fromShare: Boolean) {
        if (rawText.isNullOrBlank()) {
            if (fromShare) Toast.makeText(this, "No link found in shared content", Toast.LENGTH_SHORT).show()
            return
        }

        val url = UrlExtractor.extract(rawText)
        if (url == null) {
            if (fromShare) Toast.makeText(this, "No URL found in shared text", Toast.LENGTH_SHORT).show()
            return
        }

        if (!UrlExtractor.isSupportedVideoUrl(url)) {
            if (fromShare) {
                Toast.makeText(
                    this,
                    "Unsupported site. Try YouTube, TikTok, Instagram, or another supported platform.",
                    Toast.LENGTH_LONG
                ).show()
            }
            // Still fill the field so the user can manually try
            viewModel.setUrl(url)
            return
        }

        // Valid supported URL — set it, surface the banner, and auto-fetch the title
        viewModel.setUrl(url)
        viewModel.onClipboardUrlDetected(url)
        viewModel.fetchInfo()

        if (fromShare) {
            Toast.makeText(this, "Link received — fetching info…", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkClipboardForUrl() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            ?: return
        val url = UrlExtractor.extractAndValidate(text) ?: return
        viewModel.onClipboardUrlDetected(url)
    }

    companion object {
        /** Kept for backwards compatibility. Real list lives in [UrlExtractor]. */
        fun looksLikeVideoUrl(text: String): Boolean = UrlExtractor.isSupportedVideoUrl(text)
    }
}
