package com.portfolio.jobpilot.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portfolio.jobpilot.ui.theme.Surface000
import com.portfolio.jobpilot.ui.theme.Surface100
import com.portfolio.jobpilot.ui.theme.Surface200
import com.portfolio.jobpilot.ui.theme.SurfaceBorder

private val CardShape = RoundedCornerShape(20.dp)
private val BentoCellShape = RoundedCornerShape(12.dp)

// ============================================================================
// STATELESS 120Hz ZERO-REFLOW SHIMMER SKELETON
// ============================================================================

@Composable
fun JobCardSkeleton(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "JobCardShimmerTransition")
    val shimmerProgress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerSweepProgress"
    )

    // Lambda provider prevents recomposition; read occurs solely inside drawWithCache
    val progressProvider = remember { { shimmerProgress } }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = Surface100,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Tier: Executive Header + Match Pill Geometry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Title Placeholder (matches 22.sp line height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.74f)
                            .height(20.dp)
                            .bentoShimmer(progressProvider, cornerRadius = 6.dp)
                    )
                    // Company + Timestamp Placeholder (matches 18.sp line height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.48f)
                            .height(16.dp)
                            .bentoShimmer(progressProvider, cornerRadius = 5.dp)
                    )
                }

                // Tabular Match Badge Placeholder
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(26.dp)
                        .bentoShimmer(progressProvider, cornerRadius = 50.dp)
                )
            }

            // Middle Tier: Dual Bento Sub-Cells
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBentoMetricCell(
                    progressProvider = progressProvider,
                    modifier = Modifier.weight(1f)
                )
                SkeletonBentoMetricCell(
                    progressProvider = progressProvider,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottom Tier: Cockpit Status + 48dp Ergonomic Thumb Targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dual-User Assignee Tag Placeholder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Surface200)
                    )
                    Box(
                        modifier = Modifier
                            .width(76.dp)
                            .height(14.dp)
                            .bentoShimmer(progressProvider, cornerRadius = 4.dp)
                    )
                }

                // Right-Aligned 48dp Action Cluster Placeholders
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 48dp Bookmark Target Skeleton
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, SurfaceBorder.copy(alpha = 0.5f), BentoCellShape)
                            .bentoShimmer(progressProvider, cornerRadius = 12.dp)
                    )
                    // 48dp Dispatch Target Skeleton
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .height(48.dp)
                            .bentoShimmer(progressProvider, cornerRadius = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonBentoMetricCell(
    progressProvider: () -> Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Surface000, BentoCellShape)
            .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), BentoCellShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(10.dp)
                .bentoShimmer(progressProvider, cornerRadius = 3.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(15.dp)
                .bentoShimmer(progressProvider, cornerRadius = 4.dp)
        )
    }
}

/**
 * Draw-phase-only shimmer modifier. Skips Composition and Layout phases entirely.
 */
private fun Modifier.bentoShimmer(
    progressProvider: () -> Float,
    cornerRadius: Dp
): Modifier = this.drawWithCache {
    val radiusPx = cornerRadius.toPx()
    val corner = CornerRadius(radiusPx, radiusPx)
    val sweepWidth = size.width * 1.4f

    onDrawBehind {
        val progress = progressProvider()
        val startX = progress * size.width
        val brush = Brush.linearGradient(
            colors = listOf(
                Surface000,
                Surface200,
                Surface000
            ),
            start = Offset(startX, 0f),
            end = Offset(startX + sweepWidth, size.height)
        )
        drawRoundRect(
            brush = brush,
            cornerRadius = corner
        )
    }
}

