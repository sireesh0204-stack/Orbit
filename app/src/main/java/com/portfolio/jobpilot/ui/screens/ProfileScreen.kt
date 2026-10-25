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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.ui.theme.*

@Composable
fun ProfileScreen(
    profile: UserProfileDto?,
    isUploading: Boolean,
    isSaving: Boolean,
    statusMessage: String?,
    errorMessage: String?,
    onSelectResumeClicked: () -> Unit,
    onImportGoogleDocClicked: (String) -> Unit,
    onToggleRemote: (Boolean) -> Unit,
    onSaveProfileClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    var showGoogleDocDialog by remember { mutableStateOf(false) }
    var googleDocUrlInput by remember { mutableStateOf("") }

    if (showGoogleDocDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleDocDialog = false },
            containerColor = Surface100,
            title = {
                Text(
                    text = "IMPORT FROM GOOGLE DOCS / DRIVE",
                    style = JobPilotTypography.titleMedium.copy(color = NeonCyan)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste any Google Docs or Google Drive share link. We'll automatically export and extract the text with zero hallucination.",
                        style = JobPilotTypography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = googleDocUrlInput,
                        onValueChange = { googleDocUrlInput = it },
                        label = { Text("Google Docs URL", color = TextSecondary) },
                        placeholder = { Text("https://docs.google.com/document/d/...", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (googleDocUrlInput.isNotBlank()) {
                            showGoogleDocDialog = false
                            onImportGoogleDocClicked(googleDocUrlInput.trim())
                            googleDocUrlInput = ""
                        }
                    }
                ) {
                    Text("IMPORT", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleDocDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Surface000)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upload Resume Action Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(Surface100)
                    .border(1.dp, SurfaceBorder, cardShape)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "RESUME INGESTION ENGINE",
                        style = JobPilotTypography.labelSmall.copy(color = NeonCyan)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Upload master resume (PDF, DOCX, TXT, PNG, or JPEG). AI extracts verified history, skills, and dates with zero hallucination.",
                        style = JobPilotTypography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Token Efficiency Tip Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Surface200)
                            .border(1.dp, SatinGold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "💡",
                                style = JobPilotTypography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Token Efficiency Tip: Uploading in .TXT, .DOCX, or Google Docs uses direct text-stream extraction, significantly reducing AI token consumption and latency compared to OCR-heavy PDF or image formats.",
                                style = JobPilotTypography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = SatinGold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    if (isUploading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = NeonCyan,
                            trackColor = Surface200
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMessage ?: "Extracting text & parsing...",
                            style = JobPilotTypography.labelSmall.copy(color = NeonCyan)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { onSelectResumeClicked() }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CloudUpload,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SELECT RESUME (PDF, DOCX, TXT, PNG, JPG)",
                                    style = JobPilotTypography.labelSmall.copy(
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Google Docs Import Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface200)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .clickable { showGoogleDocDialog = true }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📄 IMPORT FROM GOOGLE DOCS / DRIVE",
                                    style = JobPilotTypography.labelSmall.copy(
                                        color = SatinGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = JobPilotTypography.labelSmall.copy(color = NeonCrimson)
                        )
                    }
                }
            }
        }

        // Parsed Profile Fields Overview
        if (profile != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(Surface100)
                        .border(1.dp, SurfaceBorder, cardShape)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "VERIFIED CANDIDATE PROFILE",
                            style = JobPilotTypography.labelSmall.copy(color = NeonEmerald)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Target Roles: ${profile.targetRoles.joinToString(", ")}",
                            style = JobPilotTypography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total Experience: ${profile.totalYearsExperience} Years (Tabular)",
                            style = JobPilotTypography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Preferred Cities: ${profile.preferredCities.joinToString(", ")}",
                            style = JobPilotTypography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Remote Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Remote Role Preference",
                                style = JobPilotTypography.bodyMedium.copy(color = TextPrimary)
                            )
                            Switch(
                                checked = profile.isRemotePreferred,
                                onCheckedChange = onToggleRemote,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Surface000,
                                    checkedTrackColor = NeonEmerald,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Surface200
                                )
                            )
                        }
                    }
                }
            }

            // Technical Skills Tags
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(Surface100)
                        .border(1.dp, SurfaceBorder, cardShape)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "EXTRACTED TECHNICAL SKILLS",
                            style = JobPilotTypography.labelSmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            profile.skills.technical.take(6).forEach { skill ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Surface200)
                                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = skill,
                                        style = JobPilotTypography.labelSmall.copy(color = NeonCyan)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Work History Cards
            item {
                Text(
                    text = "WORK HISTORY",
                    style = JobPilotTypography.labelSmall.copy(color = TextSecondary)
                )
            }

            items(profile.workHistory) { work ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface100)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "${work.title} • ${work.company}",
                            style = JobPilotTypography.titleMedium.copy(fontSize = 15.sp)
                        )
                        Text(
                            text = "${work.startDate} – ${work.endDate}",
                            style = JobPilotTypography.labelSmall.copy(color = TextMuted)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        work.bulletPoints.forEach { bp ->
                            Text(
                                text = "• $bp",
                                style = JobPilotTypography.bodyMedium.copy(fontSize = 12.sp)
                            )
                        }
                    }
                }
            }

            // Bottom Save Changes Button (Thumb zone >= 48x48dp)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeonEmerald)
                        .clickable(enabled = !isSaving) { onSaveProfileClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = null,
                            tint = Surface000,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSaving) "SAVING PROFILE..." else "SAVE / PATCH PROFILE",
                            style = JobPilotTypography.titleMedium.copy(
                                color = Surface000,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

