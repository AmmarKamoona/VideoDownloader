package com.ytdlp.downloader.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ytdlp.downloader.DownloadItem
import com.ytdlp.downloader.MainViewModel
import com.ytdlp.downloader.ui.theme.LgOnPrimaryContainer
import com.ytdlp.downloader.ui.theme.LgOnSurfaceVariant
import com.ytdlp.downloader.ui.theme.LgOutline
import com.ytdlp.downloader.ui.theme.LgOutlineVariant
import com.ytdlp.downloader.ui.theme.LgPrimary
import com.ytdlp.downloader.ui.theme.LgPrimaryContainer
import com.ytdlp.downloader.ui.theme.LgSecondary
import com.ytdlp.downloader.ui.theme.LgSurfaceBright
import com.ytdlp.downloader.ui.theme.LgSurfaceContainer
import com.ytdlp.downloader.ui.theme.LgSurfaceContainerHigh
import com.ytdlp.downloader.ui.theme.LgSurfaceContainerHighest
import com.ytdlp.downloader.ui.theme.LgSurfaceContainerLow
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun DownloadsScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0=In Progress, 1=Completed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // Tab bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LgSurfaceContainerLow)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("IN PROGRESS", "COMPLETED").forEachIndexed { index, label ->
                val active = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) LgPrimaryContainer else androidx.compose.ui.graphics.Color.Transparent)
                        .then(
                            Modifier.padding(vertical = 10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) LgOnPrimaryContainer else LgOnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // Active download (if any)
        if (selectedTab == 0) {
            if (state.isWorking) {
                ActiveDownloadCard(title = state.videoTitle ?: "Downloading…", status = state.status)
            } else if (state.downloads.isEmpty()) {
                EmptyState("No active downloads")
            } else {
                EmptyState("No active downloads")
            }
        } else {
            // Completed
            if (state.downloads.isEmpty()) {
                EmptyState("No completed downloads yet")
            } else {
                Text(
                    "COMPLETED DOWNLOADS",
                    style = MaterialTheme.typography.labelLarge,
                    color = LgOnSurfaceVariant,
                    letterSpacing = 1.sp
                )
                state.downloads.forEach { item ->
                    CompletedItemCard(item = item, context = context, onRemove = { viewModel.removeDownload(item.id) })
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun ActiveDownloadCard(title: String, status: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LgSurfaceContainerHigh)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LgSurfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) { Text("⟳", color = LgPrimary, fontSize = 24.sp) }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style    = MaterialTheme.typography.titleSmall,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (status.isNotBlank()) {
                    Text(status, style = MaterialTheme.typography.labelSmall, color = LgOnSurfaceVariant)
                }
            }
        }
        ShimmerProgressBar(progress = null)
    }
}

@Composable
private fun CompletedItemCard(item: DownloadItem, context: Context, onRemove: () -> Unit) {
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
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LgSurfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) { Text("▶", color = LgPrimary, fontSize = 20.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style    = MaterialTheme.typography.bodyLarge,
                color    = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.sizeLabel.isNotBlank()) {
                Text(item.sizeLabel, style = MaterialTheme.typography.labelSmall, color = LgOnSurfaceVariant)
            }
        }
        Row {
            IconButton(onClick = { shareFile(context, item.filePath) }, modifier = Modifier.size(36.dp)) {
                Text("↗", color = LgOnSurfaceVariant, fontSize = 16.sp)
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Text("🗑", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LgSurfaceContainerLow),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📥", fontSize = 32.sp)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = LgOutline)
        }
    }
}

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
