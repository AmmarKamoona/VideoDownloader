package com.ytdlp.downloader.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ytdlp.downloader.MainViewModel
import com.ytdlp.downloader.ui.theme.Dark600
import com.ytdlp.downloader.ui.theme.Success
import com.ytdlp.downloader.ui.theme.Violet400
import com.ytdlp.downloader.ui.theme.Violet500
import com.ytdlp.downloader.ui.theme.Violet600
import com.ytdlp.downloader.ui.theme.Violet700
import java.io.File

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(
    viewModel: MainViewModel,
    isBubbleEnabled: Boolean = false,
    onToggleBubble: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current

    val downloadDir = remember {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .apply { mkdirs() }
            .absolutePath
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero header ───────────────────────────────────────────────────
            AppHeader(isBubbleEnabled = isBubbleEnabled, onToggleBubble = onToggleBubble)

            // ── Content ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Clipboard / share banner
                AnimatedVisibility(
                    visible = state.showQuickDownload && state.clipboardUrl != null,
                    enter = expandVertically(spring(stiffness = Spring.StiffnessMedium)) +
                            fadeIn(tween(200)),
                    exit  = shrinkVertically(tween(180)) + fadeOut(tween(150))
                ) {
                    ClipboardBanner(
                        url       = state.clipboardUrl ?: "",
                        onUse     = { viewModel.useClipboardUrl() },
                        onDismiss = { viewModel.dismissClipboardBanner() }
                    )
                }

                // URL input
                UrlInputCard(
                    url       = state.url,
                    enabled   = !state.isWorking,
                    onChange  = viewModel::setUrl
                )

                // Action buttons
                ActionRow(
                    isWorking = state.isWorking,
                    hasUrl    = state.url.isNotBlank(),
                    onInfo    = { viewModel.fetchInfo() },
                    onDownload = { viewModel.download(downloadDir) }
                )

                // Progress
                AnimatedVisibility(
                    visible = state.isWorking,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    ProgressCard(status = state.status)
                }

                // Video title preview
                AnimatedVisibility(
                    visible = state.videoTitle != null,
                    enter   = fadeIn(tween(300)) + slideInVertically { it / 2 },
                    exit    = fadeOut(tween(200))
                ) {
                    state.videoTitle?.let { TitleCard(title = it) }
                }

                // Error
                AnimatedVisibility(
                    visible = state.error != null,
                    enter   = fadeIn(tween(250)) + expandVertically(),
                    exit    = fadeOut(tween(200)) + shrinkVertically()
                ) {
                    state.error?.let { ErrorCard(message = it) }
                }

                // Success / file card
                AnimatedVisibility(
                    visible = state.lastFile != null,
                    enter   = fadeIn(tween(350)) + slideInVertically { it / 2 },
                    exit    = fadeOut(tween(200))
                ) {
                    state.lastFile?.let {
                        SuccessCard(
                            filePath = it,
                            context  = context
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Footer
                Footer()

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppHeader(isBubbleEnabled: Boolean, onToggleBubble: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )
            .padding(top = 48.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
    ) {
        Column {
            // Logo pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(Violet600, Violet400))
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "LEGEND",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    letterSpacing = androidx.compose.ui.unit.TextUnit(
                        2f, androidx.compose.ui.unit.TextUnitType.Sp
                    )
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Video Downloader",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Paste any video link and download instantly",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            // Floating bubble toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isBubbleEnabled) Violet600.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⬇", style = MaterialTheme.typography.bodyMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Floating Bubble",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (isBubbleEnabled)
                            "Shows a bubble when you copy a video link"
                        else
                            "Tap to enable — detects links in other apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked         = isBubbleEnabled,
                    onCheckedChange = { onToggleBubble() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Clipboard banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ClipboardBanner(url: String, onUse: () -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ContentPaste,
                contentDescription = null,
                tint   = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Video link detected",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                url,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        TextButton(
            onClick = onUse,
            colors  = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Use", style = MaterialTheme.typography.labelLarge)
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint     = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// URL input
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UrlInputCard(url: String, enabled: Boolean, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "VIDEO URL",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value         = url,
            onValueChange = onChange,
            placeholder   = {
                Text(
                    "https://youtube.com/watch?v=…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            },
            singleLine    = true,
            enabled       = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape         = RoundedCornerShape(14.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                cursorColor          = MaterialTheme.colorScheme.primary,
                focusedTextColor     = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor   = MaterialTheme.colorScheme.onSurface,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActionRow(
    isWorking: Boolean,
    hasUrl: Boolean,
    onInfo: () -> Unit,
    onDownload: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Info button
        Button(
            onClick  = onInfo,
            enabled  = !isWorking && hasUrl,
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                contentColor           = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Info", style = MaterialTheme.typography.labelLarge)
        }

        // Download button — gradient fill
        Box(
            modifier = Modifier
                .weight(2f)
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (!isWorking && hasUrl)
                        Brush.linearGradient(listOf(Violet600, Violet500))
                    else
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                ),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick  = onDownload,
                enabled  = !isWorking && hasUrl,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = Color.Transparent,
                    contentColor           = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                modifier  = Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = isWorking,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                    },
                    label = "download_label"
                ) { working ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (working) "Working…" else "Download",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Progress card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProgressCard(status: String) {
    SurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pulsing dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    "Downloading",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            LinearProgressIndicator(
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color          = MaterialTheme.colorScheme.primary,
                trackColor     = MaterialTheme.colorScheme.outline,
                strokeCap      = StrokeCap.Round
            )
            if (status.isNotBlank()) {
                Text(
                    status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Title preview card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TitleCard(title: String) {
    SurfaceCard {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "VIDEO TITLE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .border(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Success / file card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SuccessCard(filePath: String, context: Context) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                Success.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Success.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint     = Success,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        "SAVED TO DOWNLOADS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Success
                    )
                    Text(
                        File(filePath).name,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FileActionButton(
                    icon    = Icons.Default.PlayArrow,
                    label   = "Play",
                    onClick = { playFile(context, filePath) },
                    modifier = Modifier.weight(1f)
                )
                FileActionButton(
                    icon    = Icons.Default.FolderOpen,
                    label   = "Folder",
                    onClick = { openDownloadsFolder(context) },
                    modifier = Modifier.weight(1f)
                )
                FileActionButton(
                    icon    = Icons.Default.Share,
                    label   = "Share",
                    onClick = { shareFile(context, filePath) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FileActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick  = onClick,
        shape    = RoundedCornerShape(10.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor   = MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        modifier  = modifier.height(44.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Footer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Footer() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Created by AK",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        Text(
            "ammarkamoona.2012@gmail.com",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared surface card wrapper
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SurfaceCard(content: @Composable () -> Unit) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.padding(16.dp)) {
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Intent helpers (unchanged logic)
// ─────────────────────────────────────────────────────────────────────────────

private fun playFile(context: Context, filePath: String) {
    val file = File(filePath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "video/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Play with…"))
}

private fun openDownloadsFolder(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(
            Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload"),
            "vnd.android.document/directory"
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val fallback = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        try { context.startActivity(fallback) } catch (e2: Exception) { /* ignore */ }
    }
}

private fun shareFile(context: Context, filePath: String) {
    val file = File(filePath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "video/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share video"))
}
