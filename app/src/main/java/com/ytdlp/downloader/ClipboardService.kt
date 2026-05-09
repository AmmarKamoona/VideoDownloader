package com.ytdlp.downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.NotificationCompat

/**
 * Foreground service that:
 *  1. Polls the clipboard every second for video URLs.
 *  2. When a video URL is detected, draws a floating overlay bubble.
 *  3. Tapping the bubble opens MainActivity with the URL pre-filled.
 *  4. The bubble auto-dismisses after 8 seconds or when tapped.
 */
class ClipboardService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var clipboardManager: ClipboardManager
    private val handler = Handler(Looper.getMainLooper())

    private var overlayView: View? = null
    private var lastDetectedUrl: String = ""
    private var lastShownUrl: String = ""

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        windowManager    = getSystemService(WINDOW_SERVICE) as WindowManager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        startForeground(NOTIF_ID, buildNotification())
        handler.post(pollRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Clipboard polling ─────────────────────────────────────────────────────

    private val pollRunnable = object : Runnable {
        override fun run() {
            checkClipboard()
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    private fun checkClipboard() {
        val text = clipboardManager.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            ?.trim()
            ?: return

        if (MainActivity.looksLikeVideoUrl(text) && text != lastShownUrl) {
            lastDetectedUrl = text
            showOverlay(text)
        }
    }

    // ── Overlay ───────────────────────────────────────────────────────────────

    private fun showOverlay(url: String) {
        if (!Settings.canDrawOverlays(this)) return
        removeOverlay() // remove any existing bubble first

        lastShownUrl = url

        val view = LayoutInflater.from(this)
            .inflate(R.layout.overlay_bubble, null)

        // Label — show the domain only
        val domain = Uri.parse(url).host?.removePrefix("www.") ?: "video"
        view.findViewById<TextView>(R.id.bubble_label).text = "↓ $domain"

        // Tap to open app
        view.setOnClickListener {
            val launch = Intent(this, MainActivity::class.java).apply {
                action = ACTION_OPEN_URL
                putExtra(EXTRA_URL, url)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(launch)
            removeOverlay()
        }

        // Drag support
        val params = buildLayoutParams()
        var initialX = 0; var initialY = 0
        var touchX = 0f;  var touchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    touchX = event.rawX; touchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - touchX).toInt()
                    params.y = initialY + (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }

        // Close button
        view.findViewById<ImageButton>(R.id.bubble_close).setOnClickListener {
            removeOverlay()
        }

        windowManager.addView(view, params)
        overlayView = view

        // Auto-dismiss after 8 s
        handler.postDelayed({ removeOverlay() }, AUTO_DISMISS_MS)
    }

    private fun removeOverlay() {
        overlayView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) { /* already removed */ }
            overlayView = null
        }
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 200
        }
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private fun buildNotification(): Notification {
        val channelId = "clipboard_watcher"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Video link detector",
                    NotificationManager.IMPORTANCE_MIN
                ).apply { description = "Watches clipboard for video links" }
            )
        }

        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, ClipboardService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Legend Video Downloader")
            .setContentText("Watching for video links…")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .build()
    }

    companion object {
        const val ACTION_STOP     = "com.ytdlp.downloader.STOP_SERVICE"
        const val ACTION_OPEN_URL = "com.ytdlp.downloader.OPEN_URL"
        const val EXTRA_URL       = "url"
        private const val NOTIF_ID          = 1001
        private const val POLL_INTERVAL_MS  = 1000L
        private const val AUTO_DISMISS_MS   = 8000L
    }
}
