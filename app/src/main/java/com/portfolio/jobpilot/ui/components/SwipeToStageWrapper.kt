package com.portfolio.jobpilot.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.jobpilot.ui.theme.CalmEmerald
import com.portfolio.jobpilot.ui.theme.SignalCrimson
import com.portfolio.jobpilot.ui.theme.Surface000
import com.portfolio.jobpilot.ui.theme.SurfaceBorder
import com.portfolio.jobpilot.ui.theme.TextPrimary
import kotlin.math.abs
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(20.dp)

// ============================================================================
// STATELESS 120Hz SWIPE-TO-STAGE GESTURE WRAPPER
// ============================================================================

@Composable
fun SwipeToStageWrapper(
    onSaveRole: () -> Unit,
    onDismissRole: () -> Unit,
    modifier: Modifier = Modifier,
    swipeThresholdFraction: Float = 0.28f,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val offsetX = remember { Animatable(0f) }
    var containerWidthPx by remember { mutableFloatStateOf(1f) }
    var thresholdCrossed by remember { mutableStateOf(false) }

    val maxDragResistanceFactor = 0.65f
    val minVelocityPx = with(density) { 1100.dp.toPx() }

    val draggableState = rememberDraggableState { delta ->
        scope.launch {
            val rawTarget = offsetX.value + (delta * maxDragResistanceFactor)
            val maxBound = containerWidthPx * 0.55f
            val clampedTarget = rawTarget.coerceIn(-maxBound, maxBound)
            offsetX.snapTo(clampedTarget)

            val thresholdPx = containerWidthPx * swipeThresholdFraction
            val isNowOverThreshold = abs(clampedTarget) >= thresholdPx

            if (isNowOverThreshold && !thresholdCrossed) {
                thresholdCrossed = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } else if (!isNowOverThreshold && thresholdCrossed) {
                thresholdCrossed = false
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                if (size.width > 0) containerWidthPx = size.width.toFloat()
            }
            .clip(CardShape)
    ) {
        // Background Reveal Layer (drawn and transformed purely via graphicsLayer / drawWithCache)
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val corner = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                    onDrawBehind {
                        val currentOffset = offsetX.value
                        val thresholdPx = containerWidthPx * swipeThresholdFraction
                        val progress = (abs(currentOffset) / thresholdPx).coerceIn(0f, 1f)

                        val bgColor = when {
                            currentOffset > 0f -> lerp(
                                start = Surface000,
                                stop = CalmEmerald.copy(alpha = 0.88f),
                                fraction = progress
                            )
                            currentOffset < 0f -> lerp(
                                start = SurfaceBorder,
                                stop = SignalCrimson.copy(alpha = 0.88f),
                                fraction = progress
                            )
                            else -> Color.Transparent
                        }

                        drawRoundRect(color = bgColor, cornerRadius = corner)
                    }
                }
                .padding(horizontal = 24.dp)
        ) {
            // Right Swipe Reveal: Save / Stage Action (Left-Aligned)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        val currentOffset = offsetX.value
                        val thresholdPx = containerWidthPx * swipeThresholdFraction
                        val progress = (currentOffset / thresholdPx).coerceIn(0f, 1f)
                        alpha = progress
                        val scale = 0.75f + (0.25f * progress)
                        scaleX = scale
                        scaleY = scale
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = "Save Role",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "SAVE ROLE",
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            // Left Swipe Reveal: Archive / Dismiss Action (Right-Aligned)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .graphicsLayer {
                        val currentOffset = offsetX.value
                        val thresholdPx = containerWidthPx * swipeThresholdFraction
                        val progress = (-currentOffset / thresholdPx).coerceIn(0f, 1f)
                        alpha = progress
                        val scale = 0.75f + (0.25f * progress)
                        scaleX = scale
                        scaleY = scale
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ARCHIVE",
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Icon(
                    imageVector = Icons.Rounded.Archive,
                    contentDescription = "Archive Role",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Foreground Content Slot (JobCard)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = offsetX.value
                }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        val currentOffset = offsetX.value
                        val thresholdPx = containerWidthPx * swipeThresholdFraction
                        val triggeredByDistance = abs(currentOffset) >= thresholdPx
                        val triggeredByVelocity = abs(velocity) >= minVelocityPx &&
                            (velocity * currentOffset > 0f)

                        if (triggeredByDistance || triggeredByVelocity) {
                            if (currentOffset > 0f) {
                                onSaveRole()
                            } else if (currentOffset < 0f) {
                                onDismissRole()
                            }
                        }

                        thresholdCrossed = false

                        // 120Hz VRR-tuned spring snap-back
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                initialVelocity = velocity * 0.35f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }
                )
        ) {
            content()
        }
    }
}

