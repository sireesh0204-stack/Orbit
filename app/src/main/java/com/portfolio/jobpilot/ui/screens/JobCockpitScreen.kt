package com.portfolio.jobpilot.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.jobpilot.ui.components.CockpitQuickFilter
import com.portfolio.jobpilot.ui.components.CockpitUserTab
import com.portfolio.jobpilot.ui.components.DualUserFilterBar
import com.portfolio.jobpilot.ui.components.DualUserFilterState
import com.portfolio.jobpilot.ui.components.JobCard
import com.portfolio.jobpilot.ui.components.JobCardSkeleton
import com.portfolio.jobpilot.ui.components.JobCardUiModel
import com.portfolio.jobpilot.ui.components.SwipeToStageWrapper
import com.portfolio.jobpilot.ui.theme.CalmEmerald
import com.portfolio.jobpilot.ui.theme.ExecutiveIndigo
import com.portfolio.jobpilot.ui.theme.Surface000
import com.portfolio.jobpilot.ui.theme.Surface100
import com.portfolio.jobpilot.ui.theme.SurfaceBorder
import com.portfolio.jobpilot.ui.theme.TextMuted
import com.portfolio.jobpilot.ui.theme.TextPrimary
import com.portfolio.jobpilot.ui.theme.TextSecondary
import com.portfolio.jobpilot.ui.theme.WarmAmber

private val BentoCardShape = RoundedCornerShape(20.dp)
private val BentoControlShape = RoundedCornerShape(12.dp)
private val TelemetryPillShape = RoundedCornerShape(10.dp)

@Immutable
data class CockpitTelemetryState(
    val syncStatusLabel: String = "LIVE FEED",
    val lastSyncedAgo: String = "2m ago",
    val isSyncing: Boolean = false,
    val queuedCount: Int = 20,
    val highMatchCount: Int = 9,
    val latencyMs: Int = 18
)

@Immutable
data class JobCockpitUiState(
    val isLoading: Boolean = false,
    val telemetry: CockpitTelemetryState = CockpitTelemetryState(),
    val filterState: DualUserFilterState = DualUserFilterState(),
    val jobs: List<JobCardUiModel> = emptyList()
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JobCockpitScreen(
    uiState: JobCockpitUiState,
    onTabSelected: (CockpitUserTab) -> Unit,
    onQuickFilterToggle: (CockpitQuickFilter) -> Unit,
    onCardClick: (String) -> Unit,
    onSaveRole: (String) -> Unit,
    onArchiveRole: (String) -> Unit,
    onQuickDispatch: (String) -> Unit,
    onTailorResume: (String) -> Unit,
    onRefreshFeed: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Surface000)
            .statusBarsPadding()
    ) {
        // 1. Sticky Top: Passive Telemetry HUD
        PassiveTelemetryHud(
            telemetry = uiState.telemetry,
            activeUserTab = uiState.filterState.selectedTab,
            onTailorResume = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val targetJobId = uiState.jobs.firstOrNull()?.id ?: "MASTER_PROFILE"
                onTailorResume(targetJobId)
            },
            onRefreshFeed = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onRefreshFeed()
            }
        )

        // 2. Sticky Sub-Header: Dual-User Filter Bar
        DualUserFilterBar(
            state = uiState.filterState,
            onTabSelected = onTabSelected,
            onQuickFilterToggle = onQuickFilterToggle
        )

        // Zero-reflow scroll elevation hairline; reads scroll state only inside graphicsLayer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .graphicsLayer {
                    val isScrolled = listState.firstVisibleItemIndex > 0 ||
                        listState.firstVisibleItemScrollOffset > 0
                    alpha = if (isScrolled) 1f else 0.28f
                }
                .background(SurfaceBorder)
        )

        // 3. 120Hz VRR Feed Body
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 14.dp,
                end = 16.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                uiState.isLoading -> {
                    items(
                        count = 4,
                        key = { index -> "job_skeleton_$index" },
                        contentType = { "job_skeleton" }
                    ) {
                        JobCardSkeleton()
                    }
                }

                uiState.jobs.isEmpty() -> {
                    item(
                        key = "bento_empty_state",
                        contentType = "empty_state"
                    ) {
                        BentoEmptyStateCard(
                            selectedTab = uiState.filterState.selectedTab,
                            hasActiveFilters = uiState.filterState.activeQuickFilters.isNotEmpty(),
                            onRefreshFeed = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onRefreshFeed()
                            },
                            modifier = Modifier.padding(top = 20.dp)
                        )
                    }
                }

                else -> {
                    items(
                        items = uiState.jobs,
                        key = { job -> job.id },
                        contentType = { "job_bento_card" }
                    ) { job ->
                        // Remember item-scoped callbacks to prevent lambda reallocation at 120fps
                        val onSave = remember(job.id, onSaveRole) {
                            { onSaveRole(job.id) }
                        }
                        val onArchive = remember(job.id, onArchiveRole) {
                            { onArchiveRole(job.id) }
                        }

                        SwipeToStageWrapper(
                            onSaveRole = onSave,
                            onDismissRole = onArchive,
                            modifier = Modifier.animateItemPlacement(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        ) {
                            JobCard(
                                job = job,
                                onCardClick = onCardClick,
                                onSaveToggle = onSaveRole,
                                onQuickDispatch = onQuickDispatch,
                                onTailorResume = onTailorResume
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// STICKY PASSIVE TELEMETRY HUD
// ============================================================================

@Composable
private fun PassiveTelemetryHud(
    telemetry: CockpitTelemetryState,
    activeUserTab: CockpitUserTab,
    onTailorResume: () -> Unit,
    onRefreshFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HudTelemetryTransition")

    // Continuous pulse & sync spin read strictly inside graphicsLayer / drawWithCache
    val livePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LivePulseAlpha"
    )

    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SyncIconRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface000)
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Brand Identity + Live Telemetry Badges
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Draw-phase pulsing status beacon
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .drawWithCache {
                            val beaconColor = if (telemetry.isSyncing) WarmAmber else CalmEmerald
                            onDrawBehind {
                                drawCircle(
                                    color = beaconColor.copy(alpha = livePulseAlpha * 0.35f),
                                    radius = size.minDimension * 0.95f
                                )
                                drawCircle(
                                    color = beaconColor,
                                    radius = size.minDimension * 0.5f
                                )
                            }
                        }
                )

                Text(
                    text = "JOBPILOT COCKPIT",
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.9.sp
                    ),
                    maxLines = 1
                )

                Text(
                    text = "•  ${telemetry.lastSyncedAgo}",
                    style = TextStyle(
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFeatureSettings = "tnum"
                    ),
                    maxLines = 1
                )
            }

            // Tabular Telemetry Pill Strip
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TelemetryMetricPill(
                    label = "QUEUE",
                    value = "${telemetry.queuedCount}",
                    accentColor = ExecutiveIndigo
                )
                TelemetryMetricPill(
                    label = "PRIME",
                    value = "${telemetry.highMatchCount}",
                    accentColor = CalmEmerald
                )
                TelemetryMetricPill(
                    label = "PING",
                    value = "${telemetry.latencyMs}ms",
                    accentColor = TextSecondary
                )
            }
        }

        // Right: 48dp Ergonomic Cockpit Utility Controls (Tailor Resume + Live Sync)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 48dp AI Resume Tailor Trigger
            HudActionButton(
                onClick = onTailorResume,
                borderColor = ExecutiveIndigo.copy(alpha = 0.55f),
                containerColor = ExecutiveIndigo.copy(alpha = 0.14f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Tailor Resume for ${activeUserTab.label}",
                        tint = ExecutiveIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Tailor",
                        style = TextStyle(
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // 48dp x 48dp Sync Trigger
            HudActionButton(
                onClick = onRefreshFeed,
                borderColor = SurfaceBorder,
                containerColor = Surface100
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = "Sync Live Jobs",
                    tint = if (telemetry.isSyncing) WarmAmber else TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            rotationZ = if (telemetry.isSyncing) syncRotation else 0f
                        }
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetricPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Surface100, TelemetryPillShape)
            .border(1.dp, SurfaceBorder.copy(alpha = 0.7f), TelemetryPillShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        )
        Text(
            text = value,
            style = TextStyle(
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            )
        )
    }
}

@Composable
private fun HudActionButton(
    onClick: () -> Unit,
    borderColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "HudButtonScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(BentoControlShape)
            .background(containerColor)
            .border(1.dp, borderColor, BentoControlShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ============================================================================
// BENTO EMPTY STATE CARD (WITH 48DP SYNC CTA)
// ============================================================================

@Composable
private fun BentoEmptyStateCard(
    selectedTab: CockpitUserTab,
    hasActiveFilters: Boolean,
    onRefreshFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonInteraction = remember { MutableInteractionSource() }
    val isPressed by buttonInteraction.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "EmptyStateButtonScale"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BentoCardShape,
        color = Surface100,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Radar Icon Housing
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface000)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Radar,
                    contentDescription = null,
                    tint = ExecutiveIndigo,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = if (hasActiveFilters) {
                    "No Roles Match Active Filters"
                } else {
                    "Zero Staged Roles for ${selectedTab.label}"
                },
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = if (hasActiveFilters) {
                    "Relax one of your quick-filter chips above or pull live telemetry from the upstream job index."
                } else {
                    "All matching positions have been triaged. Trigger a live cockpit sync to ingest fresh roles."
                },
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Normal
                ),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 48dp Thumb-Zone Primary Sync Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(BentoControlShape)
                    .background(ExecutiveIndigo)
                    .clickable(
                        interactionSource = buttonInteraction,
                        indication = null,
                        role = Role.Button,
                        onClick = onRefreshFeed
                    )
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pull to Sync Live Jobs",
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    )
                )
            }
        }
    }
}
