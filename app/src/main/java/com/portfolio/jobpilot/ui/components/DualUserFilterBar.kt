package com.portfolio.jobpilot.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.jobpilot.ui.theme.CalmEmerald
import com.portfolio.jobpilot.ui.theme.ExecutiveIndigo
import com.portfolio.jobpilot.ui.theme.Surface000
import com.portfolio.jobpilot.ui.theme.Surface100
import com.portfolio.jobpilot.ui.theme.SurfaceBorder
import com.portfolio.jobpilot.ui.theme.TextMuted
import com.portfolio.jobpilot.ui.theme.TextPrimary
import com.portfolio.jobpilot.ui.theme.TextSecondary

private val SegmentedContainerShape = RoundedCornerShape(16.dp)
private val SegmentedPillShape = RoundedCornerShape(12.dp)
private val QuickChipShape = RoundedCornerShape(12.dp)

enum class CockpitUserTab(val label: String) {
    ALL("All Roles"),
    SIREESH("Sireesh"),
    BHARGAVI("Bhargavi")
}

enum class CockpitQuickFilter(
    val label: String,
    val icon: ImageVector,
    val activeAccent: Color
) {
    HIGH_MATCH(
        label = ">= 90% Match Only",
        icon = Icons.Rounded.Bolt,
        activeAccent = CalmEmerald
    ),
    REMOTE_ONLY(
        label = "Remote Only",
        icon = Icons.Rounded.Public,
        activeAccent = ExecutiveIndigo
    ),
    EXPIRING_SOON(
        label = "Expiring Soon (<48h)",
        icon = Icons.Rounded.Schedule,
        activeAccent = ExecutiveIndigo
    )
}

@Immutable
data class DualUserFilterState(
    val selectedTab: CockpitUserTab = CockpitUserTab.ALL,
    val allRolesCount: Int = 20,
    val sireeshCount: Int = 12,
    val bhargaviCount: Int = 8,
    val activeQuickFilters: Set<CockpitQuickFilter> = emptySet()
) {
    fun countFor(tab: CockpitUserTab): Int = when (tab) {
        CockpitUserTab.ALL -> allRolesCount
        CockpitUserTab.SIREESH -> sireeshCount
        CockpitUserTab.BHARGAVI -> bhargaviCount
    }
}

@Composable
fun DualUserFilterBar(
    state: DualUserFilterState,
    onTabSelected: (CockpitUserTab) -> Unit,
    onQuickFilterToggle: (CockpitQuickFilter) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface000)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Primary Dual-User Segmented Selector
        PrimarySegmentedSelector(
            state = state,
            onTabSelected = { tab ->
                if (tab != state.selectedTab) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTabSelected(tab)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        )

        // 2. Secondary Quick-Filter Chips Row (Horizontally Scrollable, 48dp Reachability)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CockpitQuickFilter.entries.forEach { filter ->
                val isSelected = state.activeQuickFilters.contains(filter)
                QuickFilterChip(
                    filter = filter,
                    isSelected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onQuickFilterToggle(filter)
                    }
                )
            }
        }
    }
}

@Composable
private fun PrimarySegmentedSelector(
    state: DualUserFilterState,
    onTabSelected: (CockpitUserTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = CockpitUserTab.entries

    // 120Hz VRR spring indicator position; read exclusively inside drawWithCache
    val animatedTabIndex by animateFloatAsState(
        targetValue = state.selectedTab.ordinal.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "SegmentedPillSlide"
    )

    // Smooth accent transition between ExecutiveIndigo (All/Sireesh) and CalmEmerald (Bhargavi)
    val targetIndicatorAccent by animateFloatAsState(
        targetValue = if (state.selectedTab == CockpitUserTab.BHARGAVI) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "SegmentedPillAccent"
    )

    Row(
        modifier = modifier
            .clip(SegmentedContainerShape)
            .background(Surface100)
            .border(1.dp, SurfaceBorder, SegmentedContainerShape)
            .padding(4.dp)
            .drawWithCache {
                val tabCount = tabs.size
                val tabWidth = size.width / tabCount
                val cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())

                onDrawBehind {
                    val currentOffset = animatedTabIndex * tabWidth
                    val pillColor = lerp(
                        start = ExecutiveIndigo,
                        stop = CalmEmerald,
                        fraction = targetIndicatorAccent
                    )

                    drawRoundRect(
                        color = pillColor,
                        topLeft = Offset(x = currentOffset, y = 0f),
                        size = Size(width = tabWidth, height = size.height),
                        cornerRadius = cornerRadius
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val isSelected = state.selectedTab == tab
            val count = state.countFor(tab)

            val labelColor by animateColorAsState(
                targetValue = if (isSelected) TextPrimary else TextSecondary,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "SegmentLabelColor"
            )

            val countColor by animateColorAsState(
                targetValue = if (isSelected) TextPrimary.copy(alpha = 0.92f) else TextSecondary,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "SegmentCountColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(SegmentedPillShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Tab,
                        onClick = { onTabSelected(tab) }
                    )
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = tab.label,
                        style = TextStyle(
                            color = labelColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = (-0.1).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "($count)",
                        style = TextStyle(
                            color = countColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickFilterChip(
    filter: CockpitQuickFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Zero-reflow 120Hz press feedback read in draw/graphicsLayer phase
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ChipPressScale"
    )

    val containerColor = if (isSelected) {
        filter.activeAccent.copy(alpha = 0.16f)
    } else {
        Surface100
    }

    val borderColor = if (isSelected) {
        filter.activeAccent
    } else {
        SurfaceBorder
    }

    val textColor = if (isSelected) TextPrimary else TextSecondary
    val iconTint = if (isSelected) filter.activeAccent else TextMuted

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .defaultMinSize(minHeight = 48.dp)
            .clip(QuickChipShape)
            .background(containerColor)
            .border(1.dp, borderColor, QuickChipShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Checkbox,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = filter.icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = filter.label,
            style = TextStyle(
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontFeatureSettings = "tnum"
            ),
            maxLines = 1
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(filter.activeAccent, CircleShape)
            )
        }
    }
}

