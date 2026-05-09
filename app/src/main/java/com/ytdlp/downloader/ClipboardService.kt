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
 * Foreground service that shows a floating bubble when a video URL is copied.
 *
 * KEY DESIGN DECISION — why we use OnPrimaryClipChangedListener instead of polling:
 * Android 10+ (API 29+) blocks background apps from reading clipboard content.
 * clipboardManager.primaryClip returns null from a background service.
 * However, OnPrimaryClipChangedListener IS fired by the system even for background
 * services — we just can't read the content immediately. We post a short delay
 * (300ms) to give the foreground app time to finish writing, then read on the
 * main thread while the service is still technically "active" from the callback.
 */
class ClipboardService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var clipboardManager: ClipboardManager
    private val handler = Handler(Looper.getMainLooper())

    private var overlayView: View? = null
    private var lastShownUrl: String = ""

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        // Delay slightly so the clipboard write is fully committed
        handler.postDelayed({ checkClipboard() }, 300)
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        windowManager    = getSystemService(WINDOW_SERVICE) as WindowManager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        startForeground(NOTIF_ID, buildNotification())
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        handler.removeCallbacksAndMessages(null)
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Clipboard check ───────────────────────────────────────────────────────

    private fun checkClipboard() {
        // On Android 10+ this may still return null if we're fully background.
        // The listener fires when the user explicitly copies — at that moment
        // the system grants a brief window to read. The 300ms delay helps.
        val text = try {
            clipboardManager.primaryClip
                ?.getItemAt(0)
                ?.coerceToText(this)
                ?.toString()
                ?.trim()
        } catch (e: Exception) {
            null
        } ?: return

        if (text.isBlank()) return
        if (!MainActivity.looksLikeVideoUrl(text)) return
        if (text == lastShownUrl) return  // already showed for this URL

        showOverlay(text)
    }

    // ── Overlay ───────────────────────────────────────────────────────────────

    private fun showOverlay(url: String) {
        if (!Settings.canDrawOverlays(this)) return
        removeOverlay()

        lastShownUrl = url

        val view = LayoutInflater.from(this).inflate(R.layout.overlay_bubble, null)

        val domain = Uri.parse(url).host?.removePrefix("www.") ?: "video"
        view.findViewById<TextView>(R.id.bubble_label).text = "↓  $domain"

        val params = buildLayoutParams()

        // ── Touch: distinguish tap vs drag ────────────────────────────────────
        var downX = 0f; var downY = 0f
        var initParamX = 0; var initParamY = 0
        var isDragging = false
        val DRAG_THRESHOLD = 10f

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY
                    initParamX = params.x; initParamY = params.y
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (!isDragging && (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD)) {
                        isDragging = true
                    }
                    if (isDragging) {
                        params.x = initParamX + dx.toInt()
                        params.y = initParamY + dy.toInt()
                        try { windowManager.updateViewLayout(view, params) } catch (e: Exception) { }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // It was a tap — open the app
                        openApp(url)
                    }
                    true
                }
                else -> false
            }
        }

        // Close button
        view.findViewById<ImageButton>(R.id.bubble_close).setOnClickListener {
            removeOverlay()
        }

        try {
            windowManager.addView(view, params)
            overlayView = view
        } catch (e: Exception) {
            return
        }

        // Auto-dismiss after 10 s
        handler.postDelayed({ removeOverlay() }, AUTO_DISMISS_MS)
    }

    private fun openApp(url: String) {
        val launch = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_URL
            putExtra(EXTRA_URL, url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(launch)
        removeOverlay()
    }

    private fun removeOverlay() {
        handler.removeCallbacksAndMessages(AUTO_DISMISS_TOKEN)
        overlayView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) { }
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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
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
                ).apply {
                    description = "Watches clipboard for video links"
                    setShowBadge(false)
                }
            )
        }

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, ClipboardService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Legend Video Downloader")
            .setContentText("Watching for video links…")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .build()
    }

    companion object {
        const val ACTION_STOP     = "com.ytdlp.downloader.STOP_SERVICE"
        const val ACTION_OPEN_URL = "com.ytdlp.downloader.OPEN_URL"
        const val EXTRA_URL       = "url"
        private const val NOTIF_ID         = 1001
        private const val AUTO_DISMISS_MS  = 10_000L
        private val AUTO_DISMISS_TOKEN     = Any()
    }
}
