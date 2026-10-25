package com.portfolio.jobpilot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto
import com.portfolio.jobpilot.ui.theme.*

@Composable
fun MyResumesScreen(
    resumes: List<TailoredResumeMetadataDto>,
    onDownloadDocx: (TailoredResumeMetadataDto) -> Unit,
    onDownloadPdf: (TailoredResumeMetadataDto) -> Unit,
    onUploadToDrive: (TailoredResumeMetadataDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Surface000)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "DRAFTER–REVIEWER ARCHIVE",
                    style = JobPilotTypography.labelSmall.copy(color = NeonCyan)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "My Tailored Resumes (${resumes.size})",
                    style = JobPilotTypography.headlineMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Strict single-column ATS layouts verified against raw text parsing traps.",
                    style = JobPilotTypography.bodyMedium
                )
            }
        }

        if (resumes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(Surface100)
                        .border(1.dp, SurfaceBorder, cardShape)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tailored resumes yet. Tap 'TAILOR' on any job card in your feed to dispatch the Drafter–Reviewer engine.",
                        style = JobPilotTypography.bodyMedium.copy(color = TextMuted)
                    )
                }
            }
        }

        items(resumes) { resume ->
            val scoreColor = if (resume.atsKeywordCoverageScore >= 90) NeonEmerald else NeonAmber

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(Surface100)
                    .border(1.dp, SurfaceBorder, cardShape)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(scoreColor.copy(alpha = 0.15f))
                                .border(1.dp, scoreColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ATS SCORE: ${resume.atsKeywordCoverageScore}%",
                                style = JobPilotTypography.labelSmall.copy(
                                    color = scoreColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = resume.createdAt,
                            style = JobPilotTypography.labelSmall.copy(color = TextMuted)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = resume.position,
                        style = JobPilotTypography.titleMedium
                    )
                    Text(
                        text = "${resume.company} • Expiry: ${resume.expiryDate}",
                        style = JobPilotTypography.bodyMedium.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action cluster (Thumb-friendly >= 48dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Download DOCX
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface200)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { onDownloadDocx(resume) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DOCX",
                                    style = JobPilotTypography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Download PDF
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface200)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { onDownloadPdf(resume) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PDF",
                                    style = JobPilotTypography.labelSmall.copy(color = NeonEmerald, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Upload to Drive
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SatinGold.copy(alpha = 0.15f))
                                .border(1.dp, SatinGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable { onUploadToDrive(resume) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CloudUpload,
                                    contentDescription = null,
                                    tint = SatinGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DRIVE SYNC",
                                    style = JobPilotTypography.labelSmall.copy(color = SatinGold, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

