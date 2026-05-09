package com.ytdlp.downloader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ytdlp.downloader.ui.theme.Violet400
import com.ytdlp.downloader.ui.theme.Violet600
import com.ytdlp.downloader.ui.theme.YtDownloaderTheme

/**
 * One-shot activity shown the first time the user enables the floating bubble.
 * Explains what the permission does, then sends them to the system settings page.
 * On return, starts the ClipboardService if permission was granted.
 */
class OverlayPermissionActivity : ComponentActivity() {

    private val overlayResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check again after returning from Settings
        if (Settings.canDrawOverlays(this)) {
            startClipboardService()
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Already granted — just start the service and leave
        if (Settings.canDrawOverlays(this)) {
            startClipboardService()
            finish()
            return
        }

        setContent {
            YtDownloaderTheme {
                PermissionScreen(
                    onAllow  = { requestOverlayPermission() },
                    onSkip   = { finish() }
                )
            }
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayResult.launch(intent)
    }

    private fun startClipboardService() {
        val intent = Intent(this, ClipboardService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@Composable
private fun PermissionScreen(onAllow: () -> Unit, onSkip: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Violet600, Violet400))),
                contentAlignment = Alignment.Center
            ) {
                Text("⬇", style = MaterialTheme.typography.displaySmall, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Enable Floating Bubble",
                style     = MaterialTheme.typography.headlineSmall,
                color     = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                "Legend Video Downloader can show a small floating button whenever you copy or view a video link in another app — like YouTube or Instagram.\n\nTap it to download instantly without switching apps.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                "This requires the \"Draw over other apps\" permission.",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = onAllow,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Allow & Enable Bubble", style = MaterialTheme.typography.labelLarge)
            }

            TextButton(onClick = onSkip) {
                Text(
                    "Not now",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
