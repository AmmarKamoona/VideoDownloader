package com.ytdlp.downloader.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ytdlp.downloader.ui.theme.LgPrimary
import com.ytdlp.downloader.ui.theme.LgPrimaryContainer
import com.ytdlp.downloader.ui.theme.LgSurfaceBright

/**
 * Animated shimmer progress bar.
 * [progress] null = indeterminate shimmer, 0f..1f = determinate fill.
 */
@Composable
fun ShimmerProgressBar(progress: Float?) {
    val inf = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by inf.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing)),
        label = "shimmer_x"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(LgSurfaceBright)
    ) {
        if (progress == null) {
            // Indeterminate shimmer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                LgPrimary.copy(alpha = 0.8f),
                                LgPrimaryContainer,
                                LgPrimary.copy(alpha = 0.8f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerX * 800f, 0f),
                            end   = Offset(shimmerX * 800f + 400f, 0f)
                        )
                    )
            )
        } else {
            // Determinate
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(LgPrimaryContainer)
            )
        }
    }
}
