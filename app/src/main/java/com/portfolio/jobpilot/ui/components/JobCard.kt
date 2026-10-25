package com.portfolio.jobpilot.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.portfolio.jobpilot.ui.theme.Surface200
import com.portfolio.jobpilot.ui.theme.SurfaceBorder
import com.portfolio.jobpilot.ui.theme.TextMuted
import com.portfolio.jobpilot.ui.theme.TextPrimary
import com.portfolio.jobpilot.ui.theme.TextSecondary
import com.portfolio.jobpilot.ui.theme.WarmAmber

private val CardShape = RoundedCornerShape(20.dp)
private val BentoCellShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50)

@Immutable
data class JobCardUiModel(
    val id: String,
    val title: String,
    val company: String,
    val locationMode: String,
    val compensationRange: String,
    val matchPercentage: Int,
    val postedAgo: String,
    val assigneeTag: String,
    val whyItMatches: String? = null,
    val expiryDaysRemaining: Int = 14,
    val isSaved: Boolean = false
)

@Composable
fun JobCard(
    job: JobCardUiModel,
    onCardClick: (String) -> Unit,
    onSaveToggle: (String) -> Unit,
    onQuickDispatch: (String) -> Unit,
    onTailorResume: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardInteraction = remember { MutableInteractionSource() }
    val isPressed by cardInteraction.collectIsPressedAsState()

    // 120Hz VRR-safe spring; read exclusively inside graphicsLayer lambda to skip measure/layout phases
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.982f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "JobCardPressScale"
    )

    val matchAccent = remember(job.matchPercentage) {
        when {
            job.matchPercentage >= 85 -> CalmEmerald
            job.matchPercentage >= 70 -> ExecutiveIndigo
            else -> WarmAmber
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(CardShape)
            .clickable(
                interactionSource = cardInteraction,
                indication = null,
                role = Role.Button,
                onClick = { onCardClick(job.id) }
            ),
        shape = CardShape,
        color = Surface100,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Tier: Executive Header + Tabular Match Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = job.title,
                        style = TextStyle(
                            color = TextPrimary,
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.2).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${job.company}  •  ${job.postedAgo}",
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Tabular Percentage Badge (zero horizontal jitter on value updates)
                Box(
                    modifier = Modifier
                        .background(
                            color = matchAccent.copy(alpha = 0.12f),
                            shape = PillShape
                        )
                        .border(
                            width = 1.dp,
                            color = matchAccent.copy(alpha = 0.35f),
                            shape = PillShape
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${job.matchPercentage}% MATCH",
                        style = TextStyle(
                            color = matchAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }

            // Middle Tier: Dual Bento Sub-Cells (Recessed Deep Titanium Slate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BentoMetricCell(
                    label = "COMPENSATION",
                    value = job.compensationRange,
                    isTabular = true,
                    modifier = Modifier.weight(1f)
                )
                BentoMetricCell(
                    label = "WORK MODE",
                    value = job.locationMode,
                    isTabular = false,
                    modifier = Modifier.weight(1f)
                )
            }

            // Optional Rationale Block ("Why It Matches" zero-hallucination analysis)
            if (!job.whyItMatches.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(BentoCellShape)
                        .background(Surface200)
                        .border(1.dp, SurfaceBorder.copy(alpha = 0.5f), BentoCellShape)
                        .padding(10.dp)
                ) {
                    Text(
                        text = job.whyItMatches,
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            // Bottom Tier: Dual-User Cockpit Status + 48dp Thumb-Zone Action Cluster
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dual-User Cockpit Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ExecutiveIndigo, CircleShape)
                    )
                    Text(
                        text = job.assigneeTag,
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp
                        )
                    )
                }

                // Ergonomic Right-Aligned 48dp Thumb Targets for 20:9 / 19.5:9 Panels
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 48dp x 48dp Bookmark Target
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(BentoCellShape)
                            .background(Surface000)
                            .border(1.dp, SurfaceBorder, BentoCellShape)
                            .clickable(
                                role = Role.Checkbox,
                                onClick = { onSaveToggle(job.id) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (job.isSaved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (job.isSaved) "Remove Bookmark" else "Bookmark Role",
                            tint = if (job.isSaved) ExecutiveIndigo else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Optional 48dp Tailor Action Button
                    if (onTailorResume != null) {
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clip(BentoCellShape)
                                .background(Surface200)
                                .border(1.dp, SurfaceBorder, BentoCellShape)
                                .clickable(
                                    role = Role.Button,
                                    onClick = { onTailorResume(job.id) }
                                )
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = "Tailor Resume",
                                    tint = CalmEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tailor",
                                    style = TextStyle(
                                        color = CalmEmerald,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }

                    // 48dp Height Primary Cockpit Dispatch Target
                    Row(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(BentoCellShape)
                            .background(ExecutiveIndigo)
                            .clickable(
                                role = Role.Button,
                                onClick = { onQuickDispatch(job.id) }
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Apply",
                            style = TextStyle(
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Dispatch Role",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoMetricCell(
    label: String,
    value: String,
    isTabular: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Surface000, BentoCellShape)
            .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), BentoCellShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            maxLines = 1
        )
        Text(
            text = value,
            style = TextStyle(
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = if (isTabular) "tnum" else null
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
