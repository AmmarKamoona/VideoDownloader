package com.ytdlp.downloader.ui

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ytdlp.downloader.ui.theme.Gold
import com.ytdlp.downloader.ui.theme.GoldDim
import com.ytdlp.downloader.ui.theme.LgOnPrimaryContainer
import com.ytdlp.downloader.ui.theme.LgOnSurfaceVariant
import com.ytdlp.downloader.ui.theme.LgOutline
import com.ytdlp.downloader.ui.theme.LgOutlineVariant
import com.ytdlp.downloader.ui.theme.LgPrimary
import com.ytdlp.downloader.ui.theme.LgPrimaryContainer
import com.ytdlp.downloader.ui.theme.LgSecondary
import com.ytdlp.downloader.ui.theme.LgSecondaryContainer
import com.ytdlp.downloader.ui.theme.LgSurfaceContainer
import com.ytdlp.downloader.ui.theme.LgSurfaceContainerHigh
import com.ytdlp.downloader.ui.theme.LgSurfaceContainerLow
import com.ytdlp.downloader.ui.theme.LgSurfaceVariant

@Composable
fun SettingsScreen(
    isBubbleEnabled: Boolean,
    onToggleBubble: () -> Unit
) {
    val context = LocalContext.current
    val downloadsPath = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        .absolutePath

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // Smart detection card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LgSurfaceContainerHigh)
                .border(1.dp, LgOutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LgPrimaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text("✨", fontSize = 18.sp) }
                Column {
                    Text("Smart Link Detection", style = MaterialTheme.typography.titleSmall, color = LgPrimary)
                    Text(
                        "Legend monitors your clipboard to instantly provide download options when a video URL is detected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LgOnSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("📋" to "Copy URL", "⬇" to "Auto-Detect").forEach { (icon, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LgSurfaceContainer)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(icon, fontSize = 20.sp)
                            Text(label, style = MaterialTheme.typography.labelSmall, color = LgOnSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Permissions section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("✓", color = LgSecondary, fontSize = 16.sp)
                Text("Required Permissions", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            }

            PermissionRow(
                icon    = Icons.Default.Layers,
                title   = "Draw over other apps",
                subtitle = "Enables the quick-download overlay",
                enabled = isBubbleEnabled,
                onToggle = onToggleBubble
            )

            PermissionRow(
                icon    = Icons.Default.NotificationsActive,
                title   = "Notification access",
                subtitle = "Tap to manage in system settings",
                enabled = true,
                onToggle = {
                    val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try { context.startActivity(intent) } catch (e: Exception) { /* ignore */ }
                }
            )
        }

        // Preferences section
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚙", color = LgPrimary, fontSize = 16.sp)
                Text("Preferences", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LgSurfaceContainer)
            ) {
                // Download location — tap to open in file manager
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDownloadsFolder(context) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, null, tint = LgOnSurfaceVariant, modifier = Modifier.size(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Download location", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(downloadsPath, style = MaterialTheme.typography.labelSmall, color = LgPrimary, maxLines = 1)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = LgOnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Creator / donate section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LgSurfaceContainerLow)
                .border(1.dp, LgOutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "CREATED WITH PASSION",
                style = MaterialTheme.typography.labelSmall,
                color = LgOutline,
                letterSpacing = 1.sp
            )
            Text(
                "Built by AK",
                style = MaterialTheme.typography.titleSmall.copy(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Gold, GoldDim))
                )
            )
            Text(
                "ammarkamoona.2012@gmail.com",
                style = MaterialTheme.typography.labelSmall,
                color = Gold.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LgPrimaryContainer)
                    .clickable {
                        // PayPal donate form keyed to the email address.
                        // Works for any verified PayPal account — no paypal.me handle required.
                        val donateUrl = "https://www.paypal.com/donate?" +
                            "business=ammarkamoona.2012%40gmail.com" +
                            "&item_name=Legend+Video+Downloader" +
                            "&currency_code=USD"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(donateUrl))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("♥", color = LgOnPrimaryContainer, fontSize = 16.sp)
                    Text("Donate via PayPal", style = MaterialTheme.typography.labelLarge, color = LgOnPrimaryContainer)
                }
            }
            Text(
                "Support the creator to keep this project alive",
                style = MaterialTheme.typography.labelSmall,
                color = LgOutline
            )
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LgSurfaceContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LgSurfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = LgPrimary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = LgOnSurfaceVariant)
        }
        // Visual toggle indicator
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (enabled) LgPrimaryContainer else LgSurfaceVariant)
                .clickable { onToggle() }
                .padding(4.dp),
            contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (enabled) LgOnPrimaryContainer else LgOutline)
            )
        }
    }
}


/**
 * Open the public Downloads folder in the user's file manager.
 * Tries the Documents content URI first, falls back to the system
 * Downloads UI if no file manager handles the URI.
 */
private fun openDownloadsFolder(context: android.content.Context) {
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
        try { context.startActivity(fallback) }
        catch (e2: Exception) { /* no compatible app */ }
    }
}
