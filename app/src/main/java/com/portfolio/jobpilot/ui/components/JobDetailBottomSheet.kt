package com.portfolio.jobpilot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.jobpilot.ui.theme.CalmEmerald
import com.portfolio.jobpilot.ui.theme.ExecutiveIndigo
import com.portfolio.jobpilot.ui.theme.SatinGold
import com.portfolio.jobpilot.ui.theme.Surface000
import com.portfolio.jobpilot.ui.theme.Surface100
import com.portfolio.jobpilot.ui.theme.Surface200
import com.portfolio.jobpilot.ui.theme.SurfaceBorder
import com.portfolio.jobpilot.ui.theme.TextMuted
import com.portfolio.jobpilot.ui.theme.TextPrimary
import com.portfolio.jobpilot.ui.theme.TextSecondary
import com.portfolio.jobpilot.ui.theme.WarmAmber

private val SheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private val BentoBlockShape = RoundedCornerShape(12.dp)
private val SkillPillShape = RoundedCornerShape(6.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobDetailBottomSheet(
    job: JobCardUiModel,
    onDismissRequest: () -> Unit,
    onTailorResume: (String) -> Unit,
    onApplyClicked: (String) -> Unit,
    onSaveToggle: (String) -> Unit,
    onOpenInterviewPrep: (String) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val scrollState = rememberScrollState()

    val matchAccent = when {
        job.matchPercentage >= 85 -> CalmEmerald
        job.matchPercentage >= 70 -> ExecutiveIndigo
        else -> WarmAmber
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Surface000,
        dragHandle = null,
        shape = SheetShape,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SurfaceBorder)
                    .align(Alignment.CenterHorizontally)
            )

            // Top Bar: Dismiss + Star action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Surface100, CircleShape)
                        .border(1.dp, SurfaceBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onSaveToggle(job.id) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Surface100, CircleShape)
                        .border(1.dp, SurfaceBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (job.isSaved) Icons.Rounded.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Save role",
                        tint = if (job.isSaved) SatinGold else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Job Title + Match % Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = job.title,
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp,
                        letterSpacing = (-0.3).sp
                    ),
                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .background(color = matchAccent.copy(alpha = 0.12f), shape = RoundedCornerShape(50))
                        .border(width = 1.dp, color = matchAccent.copy(alpha = 0.35f), shape = RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${job.matchPercentage}% MATCH",
                        style = TextStyle(
                            color = matchAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }

            // Company, Location & Posted Date
            Text(
                text = "${job.company}  •  ${job.locationMode}  •  ${job.postedAgo}",
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            // Compensation (shown only when available)
            if (!job.compensationRange.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(BentoBlockShape)
                        .background(Surface100)
                        .border(1.dp, SurfaceBorder, BentoBlockShape)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ESTIMATED COMPENSATION",
                            style = TextStyle(
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = job.compensationRange,
                            style = TextStyle(
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                }
            }

            // PRIMARY ACTION: TAILOR RESUME (Moved from card into detail screen)
            Button(
                onClick = { onTailorResume(job.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExecutiveIndigo,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tailor Resume for this Role",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // SECONDARY ACTIONS: Apply & Interview Prep
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Apply Button
                Button(
                    onClick = { onApplyClicked(job.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Surface200,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(
                        text = "Apply",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextSecondary
                    )
                }

                // Interview Prep Button
                Button(
                    onClick = { onOpenInterviewPrep(job.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Surface100,
                        contentColor = TextPrimary
                    ),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CalmEmerald
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Interview Prep",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Description / Role Scope (Ground truth only, never invented)
            if (!job.description.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ABOUT THE ROLE",
                        style = TextStyle(
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Text(
                        text = job.description,
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            // Required Skills (if available)
            if (job.skillsRequired.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "TARGET SKILLS",
                        style = TextStyle(
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        )
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        job.skillsRequired.forEach { skill ->
                            Box(
                                modifier = Modifier
                                    .clip(SkillPillShape)
                                    .background(Surface100)
                                    .border(1.dp, SurfaceBorder, SkillPillShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = skill,
                                    style = TextStyle(
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
