package com.ytdlp.downloader.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ytdlp.downloader.MainViewModel
import com.ytdlp.downloader.ui.theme.GreenSpot
import com.ytdlp.downloader.ui.theme.Neon
import com.ytdlp.downloader.ui.theme.NeonBright
import com.ytdlp.downloader.ui.theme.NeonDim
import com.ytdlp.downloader.ui.theme.NeonGlow
import com.ytdlp.downloader.ui.theme.SpotBlack
import com.ytdlp.downloader.ui.theme.SpotDark1
import com.ytdlp.downloader.ui.theme.SpotDark2
import com.ytdlp.downloader.ui.theme.SpotDark3
import com.ytdlp.downloader.ui.theme.SpotDark4
import com.ytdlp.downloader.ui.theme.SpotMuted
import com.ytdlp.downloader.ui.theme.SpotSub
import com.ytdlp.downloader.ui.theme.SpotWhite
import java.io.File


// ─────────────────────────────────────────────────────────────────────────────
// Root Screen
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
            .apply { mkdirs() }.absolutePath
    }

    Scaffold(containerColor = SpotDark1) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Immersive hero ────────────────────────────────────────────────
            HeroSection(isBubbleEnabled = isBubbleEnabled, onToggleBubble = onToggleBubble)

            // ── Main content ──────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Clipboard detection banner
                AnimatedVisibility(
                    visible = state.showQuickDownload && state.clipboardUrl != null,
                    enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(tween(250)),
                    exit  = shrinkVertically(tween(200)) + fadeOut(tween(150))
                ) {
                    ClipboardBanner(
                        url       = state.clipboardUrl ?: "",
                        onUse     = { viewModel.useClipboardUrl() },
                        onDismiss = { viewModel.dismissClipboardBanner() }
                    )
                }

                // URL input
                UrlInput(url = state.url, enabled = !state.isWorking, onChange = viewModel::setUrl)

                // Primary download button
                DownloadButton(
                    isWorking  = state.isWorking,
                    hasUrl     = state.url.isNotBlank(),
                    onDownload = { viewModel.download(downloadDir) }
                )

                // Info button (secondary)
                InfoButton(
                    isWorking = state.isWorking,
                    hasUrl    = state.url.isNotBlank(),
                    onInfo    = { viewModel.fetchInfo() }
                )

                // Progress
                AnimatedVisibility(
                    visible = state.isWorking,
                    enter   = fadeIn(tween(300)) + expandVertically(),
                    exit    = fadeOut(tween(200)) + shrinkVertically()
                ) { ProgressSection(status = state.status) }

                // Video title
                AnimatedVisibility(
                    visible = state.videoTitle != null,
                    enter   = fadeIn(tween(350)) + slideInVertically { it / 3 },
                    exit    = fadeOut(tween(200))
                ) { state.videoTitle?.let { TitleTrackCard(title = it) } }

                // Error
                AnimatedVisibility(
                    visible = state.error != null,
                    enter   = fadeIn(tween(250)) + expandVertically(),
                    exit    = fadeOut(tween(200)) + shrinkVertically()
                ) { state.error?.let { ErrorBanner(message = it) } }

                // Success
                AnimatedVisibility(
                    visible = state.lastFile != null,
                    enter   = fadeIn(tween(400)) + slideInVertically { it / 3 },
                    exit    = fadeOut(tween(200))
                ) { state.lastFile?.let { DownloadedTrackCard(filePath = it, context = context) } }

                Spacer(Modifier.height(40.dp))
                FooterCredit()
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero Section  — Spotify "Now Playing" inspired
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(isBubbleEnabled: Boolean, onToggleBubble: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Deep background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SpotBlack)
        )

        // Radial glow — the "album art" atmosphere
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Neon.copy(alpha = 0.35f),
                            Neon.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(0.3f * 1080f, 0f),
                        radius = 700f
                    )
                )
        )

        // Secondary warm glow bottom-right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GreenSpot.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(1080f, 900f),
                        radius = 500f
                    )
                )
        )

        // Fade to scaffold at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, SpotDark1))
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Neon.copy(alpha = 0.18f))
                        .border(1.dp, Neon.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "LEGEND",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonBright,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Middle: big title
            Column {
                Text(
                    "Video",
                    style = MaterialTheme.typography.displayMedium,
                    color = SpotWhite
                )
                Text(
                    "Downloader",
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = Brush.linearGradient(listOf(NeonBright, GreenSpot))
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "YouTube · Instagram · TikTok · and more",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotSub
                )
            }

            // Bottom: bubble toggle row
            BubbleToggleRow(isBubbleEnabled = isBubbleEnabled, onToggle = onToggleBubble)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bubble toggle
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BubbleToggleRow(isBubbleEnabled: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SpotDark3.copy(alpha = 0.85f))
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (isBubbleEnabled) Neon.copy(alpha = 0.2f) else SpotDark4
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("⬇", fontSize = 15.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Floating Bubble",
                style = MaterialTheme.typography.titleSmall,
                color = SpotWhite
            )
            Text(
                if (isBubbleEnabled) "Active — watching for video links"
                else "Enable to detect links in other apps",
                style = MaterialTheme.typography.bodySmall,
                color = SpotSub
            )
        }
        Switch(
            checked         = isBubbleEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor       = SpotWhite,
                checkedTrackColor       = Neon,
                uncheckedThumbColor     = SpotSub,
                uncheckedTrackColor     = SpotDark4,
                uncheckedBorderColor    = SpotLine
            )
        )
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
            .background(
                Brush.linearGradient(
                    listOf(NeonDim.copy(alpha = 0.6f), SpotDark3)
                )
            )
            .border(1.dp, Neon.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Neon.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ContentPaste, null, tint = NeonBright, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Link detected", style = MaterialTheme.typography.titleSmall, color = SpotWhite)
            Text(url, style = MaterialTheme.typography.bodySmall, color = SpotSub, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        TextButton(onClick = onUse, colors = ButtonDefaults.textButtonColors(contentColor = NeonBright)) {
            Text("USE", style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, "Dismiss", tint = SpotMuted, modifier = Modifier.size(15.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// URL Input
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UrlInput(url: String, enabled: Boolean, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "PASTE LINK",
            style = MaterialTheme.typography.labelMedium,
            color = SpotSub,
            letterSpacing = 1.5.sp
        )
        OutlinedTextField(
            value           = url,
            onValueChange   = onChange,
            placeholder     = {
                Text(
                    "https://youtube.com/watch?v=…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpotMuted
                )
            },
            singleLine      = true,
            enabled         = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            shape           = RoundedCornerShape(16.dp),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Neon,
                unfocusedBorderColor    = SpotLine,
                focusedContainerColor   = SpotDark3,
                unfocusedContainerColor = SpotDark2,
                disabledContainerColor  = SpotDark2.copy(alpha = 0.5f),
                cursorColor             = Neon,
                focusedTextColor        = SpotWhite,
                unfocusedTextColor      = SpotWhite,
                disabledTextColor       = SpotMuted,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Download Button — large, glowing, Spotify-style CTA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DownloadButton(isWorking: Boolean, hasUrl: Boolean, onDownload: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "btn_scale"
    )

    val active = !isWorking && hasUrl

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        // Glow shadow behind button
        if (active) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .blur(20.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Neon.copy(alpha = 0.5f))
            )
        }

        Button(
            onClick           = onDownload,
            enabled           = active,
            shape             = RoundedCornerShape(50.dp),
            interactionSource = interactionSource,
            colors            = ButtonDefaults.buttonColors(
                containerColor         = if (active) Neon else SpotDark3,
                contentColor           = SpotWhite,
                disabledContainerColor = SpotDark3,
                disabledContentColor   = SpotMuted
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
            modifier  = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            AnimatedContent(
                targetState = isWorking,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "dl_btn"
            ) { working ->
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.Center
                ) {
                    if (working) {
                        // Animated dots
                        val inf = rememberInfiniteTransition(label = "dots")
                        val alpha by inf.animateFloat(
                            initialValue = 0.3f, targetValue = 1f,
                            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                            label = "dot_alpha"
                        )
                        Text("Downloading", style = MaterialTheme.typography.labelLarge, color = SpotWhite.copy(alpha = alpha))
                    } else {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Download", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Info Button — ghost style
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InfoButton(isWorking: Boolean, hasUrl: Boolean, onInfo: () -> Unit) {
    Button(
        onClick  = onInfo,
        enabled  = !isWorking && hasUrl,
        shape    = RoundedCornerShape(50.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = Color.Transparent,
            contentColor           = SpotSub,
            disabledContainerColor = Color.Transparent,
            disabledContentColor   = SpotMuted.copy(alpha = 0.4f)
        ),
        border   = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (!isWorking && hasUrl) SpotLine else SpotLine.copy(alpha = 0.3f)
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        modifier  = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("Get Video Info", style = MaterialTheme.typography.labelLarge)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Progress Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProgressSection(status: String) {
    val inf = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by inf.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing)),
        label = "shimmer_x"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SpotDark2)
            .border(1.dp, SpotLine, RoundedCornerShape(16.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pulsing neon dot
            val pulse = rememberInfiniteTransition(label = "pulse")
            val dotScale by pulse.animateFloat(
                initialValue = 0.8f, targetValue = 1.3f,
                animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                label = "dot"
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(Neon)
            )
            Text("Downloading…", style = MaterialTheme.typography.titleSmall, color = SpotWhite)
        }

        // Shimmer progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SpotDark4)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Neon.copy(alpha = 0.9f),
                                NeonBright,
                                Neon.copy(alpha = 0.9f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerX * 800f, 0f),
                            end   = Offset(shimmerX * 800f + 400f, 0f)
                        )
                    )
            )
        }

        if (status.isNotBlank()) {
            Text(status, style = MaterialTheme.typography.bodySmall, color = SpotSub)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Title Track Card — Spotify "track info" style
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TitleTrackCard(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SpotDark2)
            .border(1.dp, SpotLine, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Fake album art square
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(listOf(NeonDim, SpotDark4))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = NeonBright, modifier = Modifier.size(26.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("VIDEO FOUND", style = MaterialTheme.typography.labelMedium, color = Neon, letterSpacing = 1.sp)
            Spacer(Modifier.height(3.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = SpotWhite, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("✕", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Downloaded Track Card — Spotify "saved track" style
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DownloadedTrackCard(filePath: String, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SpotDark2)
            .border(1.dp, GreenSpot.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(GreenSpot.copy(alpha = 0.15f), Color.Transparent))
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenSpot.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Download, null, tint = GreenSpot, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("SAVED", style = MaterialTheme.typography.labelMedium, color = GreenSpot, letterSpacing = 1.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    File(filePath).name,
                    style    = MaterialTheme.typography.titleSmall,
                    color    = SpotWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Downloads folder", style = MaterialTheme.typography.bodySmall, color = SpotSub)
            }
        }

        // Divider
        Box(Modifier.fillMaxWidth().height(1.dp).background(SpotLine))

        // Action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrackActionChip(Icons.Default.PlayArrow, "Play",   { playFile(context, filePath) }, Modifier.weight(1f))
            TrackActionChip(Icons.Default.FolderOpen, "Folder", { openDownloadsFolder(context) }, Modifier.weight(1f))
            TrackActionChip(Icons.Default.Share,      "Share",  { shareFile(context, filePath) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TrackActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick   = onClick,
        shape     = RoundedCornerShape(50.dp),
        colors    = ButtonDefaults.buttonColors(
            containerColor = SpotDark3,
            contentColor   = SpotSub
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        modifier  = modifier.height(40.dp)
    ) {
        Icon(icon, label, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Footer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FooterCredit() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.width(24.dp).height(1.dp).background(SpotLine))
            Spacer(Modifier.width(10.dp))
            Text("AK", style = MaterialTheme.typography.labelSmall, color = SpotMuted, letterSpacing = 2.sp)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.width(24.dp).height(1.dp).background(SpotLine))
        }
        Text("ammarkamoona.2012@gmail.com", style = MaterialTheme.typography.labelSmall, color = SpotMuted.copy(alpha = 0.6f), textAlign = TextAlign.Center)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Intent helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun playFile(context: Context, filePath: String) {
    val file = File(filePath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Play with…"
        )
    )
}

private fun openDownloadsFolder(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(
            Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload"),
            "vnd.android.document/directory"
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try { context.startActivity(intent) }
    catch (e: Exception) {
        try { context.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) }
        catch (e2: Exception) { /* ignore */ }
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
