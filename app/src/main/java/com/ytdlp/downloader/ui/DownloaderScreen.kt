package com.ytdlp.downloader.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ytdlp.downloader.MainViewModel
import com.ytdlp.downloader.ui.theme.LgBackground
import com.ytdlp.downloader.ui.theme.LgOnPrimaryContainer
import com.ytdlp.downloader.ui.theme.LgOnSurfaceVariant
import com.ytdlp.downloader.ui.theme.LgPrimary
import com.ytdlp.downloader.ui.theme.LgPrimaryContainer
import com.ytdlp.downloader.ui.theme.LgSurfaceContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(
    viewModel: MainViewModel,
    isBubbleEnabled: Boolean = false,
    onToggleBubble: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = LgBackground,
        topBar = { LegendTopBar() },
        bottomBar = {
            LegendBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel          = viewModel,
                    isBubbleEnabled    = isBubbleEnabled,
                    onToggleBubble     = onToggleBubble,
                    onViewAllDownloads = { selectedTab = 1 }
                )
                1 -> DownloadsScreen(viewModel = viewModel)
                2 -> SettingsScreen(
                    isBubbleEnabled = isBubbleEnabled,
                    onToggleBubble  = onToggleBubble
                )
            }
        }
    }
}

// ── Top App Bar ───────────────────────────────────────────────────────────────

@Composable
private fun LegendTopBar() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LgBackground)
            .padding(horizontal = 16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("⚡", fontSize = 20.sp, color = LgPrimary)
            Text(
                "Legend",
                style = MaterialTheme.typography.headlineMedium,
                color = LgPrimary
            )
        }
        // Folder icon — opens the public Downloads folder in the file manager
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable { openDownloadsFolderTop(context) },
            contentAlignment = Alignment.Center
        ) {
            Text("📁", fontSize = 18.sp, color = LgOnSurfaceVariant)
        }
    }
}

private fun openDownloadsFolderTop(context: Context) {
    val docsUri = Uri.parse(
        "content://com.android.externalstorage.documents/document/primary%3ADownload"
    )
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(docsUri, "vnd.android.document/directory")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val fallback = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
    try { context.startActivity(viewIntent) }
    catch (e: Exception) {
        try { context.startActivity(fallback) } catch (e2: Exception) { /* ignore */ }
    }
}

// ── Bottom Navigation ─────────────────────────────────────────────────────────

@Composable
private fun LegendBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LgSurfaceContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            icon     = Icons.Default.Home,
            label    = "Home",
            selected = selectedTab == 0,
            onClick  = { onTabSelected(0) }
        )
        NavItem(
            icon     = Icons.Default.Download,
            label    = "Downloads",
            selected = selectedTab == 1,
            onClick  = { onTabSelected(1) }
        )
        NavItem(
            icon     = Icons.Default.Settings,
            label    = "Settings",
            selected = selectedTab == 2,
            onClick  = { onTabSelected(2) }
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(LgPrimaryContainer)
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, label, tint = LgOnPrimaryContainer, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = LgOnPrimaryContainer)
        }
    } else {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, label, tint = LgOnSurfaceVariant, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = LgOnSurfaceVariant)
        }
    }
}
