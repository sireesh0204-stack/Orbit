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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.portfolio.jobpilot.ui.theme.SatinGold
import com.portfolio.jobpilot.ui.theme.Surface100
import com.portfolio.jobpilot.ui.theme.Surface200
import com.portfolio.jobpilot.ui.theme.SurfaceBorder
import com.portfolio.jobpilot.ui.theme.TextMuted
import com.portfolio.jobpilot.ui.theme.TextPrimary
import com.portfolio.jobpilot.ui.theme.TextSecondary
import com.portfolio.jobpilot.ui.theme.WarmAmber

private val CompactCardShape = RoundedCornerShape(14.dp)
private val MatchPillShape = RoundedCornerShape(50)

@Immutable
data class JobCardUiModel(
    val id: String,
    val title: String,
    val company: String,
    val locationMode: String = "",
    val compensationRange: String? = null,
    val matchPercentage: Int,
    val postedAgo: String = "",
    val assigneeTag: String = "Sireesh",
    val description: String? = null,
    val skillsRequired: List<String> = emptyList(),
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

    // 120Hz spring physics (evaluated in draw/graphics phase)
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "CompactJobCardScale"
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
            .clip(CompactCardShape)
            .clickable(
                interactionSource = cardInteraction,
                indication = null,
                role = Role.Button,
                onClick = { onCardClick(job.id) }
            ),
        shape = CompactCardShape,
        color = Surface100,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: Title (up to 2 lines) + Match % Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = job.title,
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                // Match % Badge
                Box(
                    modifier = Modifier
                        .background(color = matchAccent.copy(alpha = 0.12f), shape = MatchPillShape)
                        .border(width = 1.dp, color = matchAccent.copy(alpha = 0.35f), shape = MatchPillShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${job.matchPercentage}%",
                        style = TextStyle(
                            color = matchAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }

            // Row 2: Company • Location • Posted Time (Single compact line)
            val metadataLine = remember(job.company, job.locationMode, job.postedAgo) {
                buildString {
                    append(job.company)
                    if (job.locationMode.isNotBlank()) {
                        append("  •  ")
                        append(job.locationMode)
                    }
                    if (job.postedAgo.isNotBlank()) {
                        append("  •  ")
                        append(job.postedAgo)
                    }
                }
            }
            Text(
                text = metadataLine,
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Row 3: Salary (Shown ONLY when it exists, hidden when empty/null)
            if (!job.compensationRange.isNullOrBlank()) {
                Text(
                    text = job.compensationRange,
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFeatureSettings = "tnum"
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Row 4: Single Action Row: Star & Apply
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star Button
                IconButton(
                    onClick = { onSaveToggle(job.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (job.isSaved) Icons.Rounded.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Save role",
                        tint = if (job.isSaved) SatinGold else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Apply Button
                Button(
                    onClick = { onQuickDispatch(job.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Surface200,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = "Apply",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}
