package com.ytdlp.downloader.ui

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ytdlp.downloader.DownloadItem
import com.ytdlp.downloader.MainViewModel
import com.ytdlp.downloader.ui.theme.FacebookBlue
import com.ytdlp.downloader.ui.theme.InstaPink
import com.ytdlp.downloader.ui.theme.LgOnPrimaryContainer
import com.ytdlp.downloader.ui.theme.LgOnSurfaceVariant
import com.ytdlp.downloader.ui.theme.LgOutline
import com.ytdlp.downloader.ui.theme.LgOutlineVariant
import com.ytdlp.downloader.ui.theme.LgPrimary
import com.ytdlp.downloader.ui.theme.LgPrimaryContainer
import com.ytdlp.downloader.ui.theme.LgSecondary
import com.ytdlp.downloader.ui.theme.LgSecondaryContainer
import com.ytdlp.downloader.ui.theme.LgSurfaceBright
import com.ytdlp.downloader.ui.theme.LgSurfaceContainer
import com.ytdlp.downloader.ui.theme.LgSurfaceContainerHigh
import com.ytdlp.downloader.ui.theme.LgSurfaceContainerHighest
import com.ytdlp.downloader.ui.theme.LgSurfaceContainerLow
import com.ytdlp.downloader.ui.theme.LgSurfaceVariant
import com.ytdlp.downloader.ui.theme.TikTokWhite
import com.ytdlp.downloader.ui.theme.YoutubeRed
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    isBubbleEnabled: Boolean,
    onToggleBubble: () -> Unit,
    onViewAllDownloads: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val downloadDir = remember {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .apply { mkdirs() }.absolutePath
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // Hero text
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Ready to Download",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Paste a URL below or let Legend detect links automatically.",
                style = MaterialTheme.typography.bodyLarge,
                color = LgOnSurfaceVariant
            )
        }

        // Clipboard detection banner
        AnimatedVisibility(
            visible = state.showQuickDownload && state.clipboardUrl != null,
            enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(250)),
            exit  = shrinkVertically(tween(200)) + fadeOut(tween(150))
        ) {
            DetectionBanner(
                url       = state.clipboardUrl ?: "",
                onUse     = { viewModel.useClipboardUrl() },
                onDismiss = { viewModel.dismissClipboardBanner() }
            )
        }

        // URL input
        UrlInputSection(
            url        = state.url,
            enabled    = !state.isWorking,
            onChange   = viewModel::setUrl,
            onDownload = { viewModel.download(downloadDir) }
        )

        // Auto-detection toggle
        AutoDetectionRow(isBubbleEnabled = isBubbleEnabled, onToggle = onToggleBubble)

        // Progress / status
        AnimatedVisibility(
            visible = state.isWorking,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            DownloadProgressBanner(status = state.status)
        }

        // Error
        AnimatedVisibility(
            visible = state.error != null,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            state.error?.let { ErrorBannerHome(it) }
        }

        // Video title result
        AnimatedVisibility(
            visible = state.videoTitle != null && !state.isWorking,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            state.videoTitle?.let { VideoTitleCard(it) }
        }

        // Quick access grid
        QuickAccessGrid(onPlatformClick = { host ->
            viewModel.setUrl("https://$host/")
        })

        // Recent downloads
        if (state.downloads.isNotEmpty()) {
            RecentDownloadsSection(
                downloads      = state.downloads.take(3),
                context        = context,
                onViewAll      = onViewAllDownloads,
                onRemove       = { viewModel.removeDownload(it) }
            )
        }

        // Footer
        FooterSection()

        Spacer(Modifier.height(100.dp))
    }
}

// ── URL Input ─────────────────────────────────────────────────────────────────

@Composable
private fun UrlInputSection(
    url: String, enabled: Boolean,
    onChange: (String) -> Unit, onDownload: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LgSurfaceContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Link, null,
            tint     = LgOutline,
            modifier = Modifier.padding(start = 14.dp).size(20.dp)
        )
        OutlinedTextField(
            value           = url,
            onValueChange   = onChange,
            placeholder     = {
                Text(
                    "https://youtube.com/watch?v=…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LgOutlineVariant
                )
            },
            singleLine      = true,
            enabled         = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedContainerColor   = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor             = LgPrimary,
                focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
            ),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier  = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LgPrimaryContainer)
                .clickable(enabled = enabled && url.isNotBlank()) { onDownload() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Download, "Download", tint = LgOnPrimaryContainer, modifier = Modifier.size(20.dp))
        }
    }
}

// ── Auto-detection toggle ─────────────────────────────────────────────────────

@Composable
private fun AutoDetectionRow(isBubbleEnabled: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LgSurfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("✨", fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "AUTO-DETECTION",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                if (isBubbleEnabled) "Service active in background" else "Tap to enable background detection",
                style = MaterialTheme.typography.labelSmall,
                color = LgOutline
            )
        }
        Switch(
            checked         = isBubbleEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor    = MaterialTheme.colorScheme.onSurface,
                checkedTrackColor    = LgSecondaryContainer,
                uncheckedThumbColor  = LgOutline,
                uncheckedTrackColor  = LgSurfaceVariant,
                uncheckedBorderColor = LgOutlineVariant
            )
        )
    }
}

// ── Detection banner ──────────────────────────────────────────────────────────

@Composable
fun DetectionBanner(url: String, onUse: () -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LgSurfaceBright)
            .border(1.dp, LgPrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "VIDEO LINK DETECTED",
                style = MaterialTheme.typography.labelLarge,
                color = LgSecondary,
                letterSpacing = 0.8.sp
            )
            Text(
                url,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onUse,
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = LgPrimaryContainer,
                        contentColor   = LgOnPrimaryContainer
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Download Now", style = MaterialTheme.typography.labelLarge)
                }
                TextButton(
                    onClick = onDismiss,
                    shape   = RoundedCornerShape(8.dp),
                    border  = androidx.compose.foundation.BorderStroke(1.dp, LgPrimary.copy(alpha = 0.3f)),
                    colors  = ButtonDefaults.textButtonColors(contentColor = LgPrimary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Dismiss", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, "Close", tint = LgOutlineVariant, modifier = Modifier.size(16.dp))
        }
    }
}

// ── Progress banner ───────────────────────────────────────────────────────────

@Composable
private fun DownloadProgressBanner(status: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LgSurfaceContainer)
            .border(1.dp, LgPrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("⟳", color = LgPrimary, fontSize = 16.sp)
            Text(
                if (status.isNotBlank()) status else "Downloading…",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        ShimmerProgressBar(progress = null)
    }
}

// ── Error banner ──────────────────────────────────────────────────────────────

@Composable
private fun ErrorBannerHome(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("✕", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

// ── Video title card ──────────────────────────────────────────────────────────

@Composable
private fun VideoTitleCard(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LgSurfaceContainerHigh)
            .border(1.dp, LgOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LgPrimaryContainer.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) { Text("▶", color = LgPrimary, fontSize = 16.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text("VIDEO FOUND", style = MaterialTheme.typography.labelSmall, color = LgPrimary, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(2.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ── Quick access grid ─────────────────────────────────────────────────────────

@Composable
private fun QuickAccessGrid(onPlatformClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "QUICK ACCESS",
            style = MaterialTheme.typography.labelLarge,
            color = LgOutline,
            letterSpacing = 1.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Triple("▶", YoutubeRed, "YouTube") to "youtube.com",
                Triple("📷", InstaPink, "Insta") to "instagram.com",
                Triple("♪", TikTokWhite, "TikTok") to "tiktok.com",
                Triple("f", FacebookBlue, "FB") to "facebook.com"
            ).forEach { (info, host) ->
                val (icon, color, label) = info
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LgSurfaceContainerHigh)
                        .clickable { onPlatformClick(host) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(icon, color = color, fontSize = 22.sp)
                        Text(label, style = MaterialTheme.typography.labelSmall, color = LgOnSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ── Recent downloads ──────────────────────────────────────────────────────────

@Composable
private fun RecentDownloadsSection(
    downloads: List<DownloadItem>,
    context: Context,
    onViewAll: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "RECENT DOWNLOADS",
                style = MaterialTheme.typography.labelLarge,
                color = LgOutline,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onViewAll) {
                Text("View All", style = MaterialTheme.typography.labelLarge, color = LgPrimary)
            }
        }
        downloads.forEach { item ->
            CompletedDownloadRow(item = item, context = context, onRemove = { onRemove(item.id) })
        }
    }
}

@Composable
fun CompletedDownloadRow(item: DownloadItem, context: Context, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LgSurfaceContainer)
            .border(1.dp, LgOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail placeholder
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LgSurfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Text("▶", color = LgPrimary, fontSize = 18.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.sizeLabel.isNotBlank()) {
                Text(item.sizeLabel, style = MaterialTheme.typography.labelSmall, color = LgOutline)
            }
            Spacer(Modifier.height(6.dp))
            // Completed progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(LgSurfaceBright)
            ) {
                Box(Modifier.fillMaxWidth().height(3.dp).background(LgSecondary))
            }
        }
        Row {
            IconButton(onClick = { shareFile(context, item.filePath) }, modifier = Modifier.size(36.dp)) {
                Text("↗", color = LgOnSurfaceVariant, fontSize = 16.sp)
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Text("✕", color = LgOnSurfaceVariant, fontSize = 14.sp)
            }
        }
    }
}

// ── Footer ────────────────────────────────────────────────────────────────────

@Composable
private fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LgOutlineVariant.copy(alpha = 0.3f))
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "CREATED WITH PASSION",
            style = MaterialTheme.typography.labelSmall,
            color = LgOutline,
            letterSpacing = 1.sp
        )
        Text(
            "Built by AK",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "ammarkamoona.2012@gmail.com",
            style = MaterialTheme.typography.labelSmall,
            color = LgPrimary
        )
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

private fun shareFile(context: Context, filePath: String) {
    val file = File(filePath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Share video"
        )
    )
}
