package com.portfolio.jobpilot.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.ui.theme.*

private val ProfileCardShape = RoundedCornerShape(16.dp)

@Composable
fun ProfileScreen(
    activeUserId: String,
    onSwitchUser: (String) -> Unit,
    masterResumeFileName: String,
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
    var showGoogleDocDialog by remember { mutableStateOf(false) }
    var googleDocUrlInput by remember { mutableStateOf("") }

    if (showGoogleDocDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleDocDialog = false },
            containerColor = Surface100,
            title = {
                Text(
                    text = "Import from Google Docs",
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste any Google Docs or Drive share link. Orbit will directly extract the text as your ground truth resume.",
                        style = TextStyle(color = TextSecondary, fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = googleDocUrlInput,
                        onValueChange = { googleDocUrlInput = it },
                        label = { Text("Google Docs URL", color = TextSecondary) },
                        placeholder = { Text("https://docs.google.com/document/d/...", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ExecutiveIndigo,
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
                    Text("Import", color = ExecutiveIndigo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleDocDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Surface000)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Minimal Header: Settings & Profile
        item {
            Text(
                text = "Settings & Profile",
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.6).sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Switch active user or update reference resume",
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            )
        }

        // Section 1: Active User Switcher
        item {
            Text(
                text = "ACTIVE PROFILE",
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Sireesh
                UserSwitchCard(
                    name = "Sireesh",
                    email = "sireesh0204@gmail.com",
                    role = "Staff Backend & Systems",
                    isSelected = activeUserId == "sireesh",
                    onSelect = { onSwitchUser("sireesh") }
                )

                // Bhargavi
                UserSwitchCard(
                    name = "Bhargavi",
                    email = "bhargaviyerroju@outlook.com",
                    role = "Software Engineer / Tech",
                    isSelected = activeUserId == "bhargavi",
                    onSelect = { onSwitchUser("bhargavi") }
                )
            }
        }

        // Section 2: Original Master Reference Resume
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "MASTER REFERENCE RESUME",
                style = TextStyle(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ProfileCardShape,
                color = Surface100,
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(CalmEmerald.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                tint = CalmEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = masterResumeFileName,
                                style = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Verified Original • Reference for all tailoring",
                                style = TextStyle(
                                    color = CalmEmerald,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Text(
                        text = "All resumes tailored for job descriptions take this original resume as the strict reference with zero hallucination. If you change your resume, update it here.",
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Replace File
                        Button(
                            onClick = onSelectResumeClicked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Surface200,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Replace Resume",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Google Docs
                        Button(
                            onClick = { showGoogleDocDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Surface200,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Google Docs",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (isUploading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = ExecutiveIndigo,
                            trackColor = Surface200
                        )
                    }

                    if (!statusMessage.isNullOrBlank()) {
                        Text(
                            text = statusMessage,
                            style = TextStyle(color = CalmEmerald, fontSize = 12.sp)
                        )
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            style = TextStyle(color = Color(0xFFFF5252), fontSize = 12.sp)
                        )
                    }
                }
            }
        }

        // Section 3: Grounded Profile Details
        if (profile != null) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "GROUNDED CANDIDATE DATA",
                    style = TextStyle(
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ProfileCardShape,
                    color = Surface100,
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target Roles",
                                style = TextStyle(color = TextSecondary, fontSize = 13.sp)
                            )
                            Text(
                                text = profile.targetRoles.joinToString(", "),
                                style = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Experience",
                                style = TextStyle(color = TextSecondary, fontSize = 13.sp)
                            )
                            Text(
                                text = "${profile.totalYearsExperience} Years",
                                style = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Eligible Locations",
                                style = TextStyle(color = TextSecondary, fontSize = 13.sp)
                            )
                            Text(
                                text = profile.preferredCities.joinToString(", "),
                                style = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        // Remote Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Remote Role Preference",
                                style = TextStyle(color = TextSecondary, fontSize = 13.sp)
                            )
                            Switch(
                                checked = profile.isRemotePreferred,
                                onCheckedChange = onToggleRemote,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CalmEmerald,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Surface200
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Verified Technical Skills",
                            style = TextStyle(
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            profile.skills.technical.forEach { skill ->
                                Box(
                                    modifier = Modifier
                                        .background(Surface200, RoundedCornerShape(6.dp))
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
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun UserSwitchCard(
    name: String,
    email: String,
    role: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) ExecutiveIndigo else SurfaceBorder
    val backgroundColor = if (isSelected) Surface200 else Surface100

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ProfileCardShape)
            .clickable(onClick = onSelect),
        shape = ProfileCardShape,
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(if (isSelected) ExecutiveIndigo else Surface200, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = email,
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = role,
                        style = TextStyle(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Active",
                    tint = CalmEmerald,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
