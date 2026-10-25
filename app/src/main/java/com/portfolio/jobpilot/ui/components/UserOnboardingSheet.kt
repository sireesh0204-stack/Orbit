package com.portfolio.jobpilot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.portfolio.jobpilot.ui.theme.Surface000
import com.portfolio.jobpilot.ui.theme.Surface100
import com.portfolio.jobpilot.ui.theme.Surface200
import com.portfolio.jobpilot.ui.theme.SurfaceBorder
import com.portfolio.jobpilot.ui.theme.TextMuted
import com.portfolio.jobpilot.ui.theme.TextPrimary
import com.portfolio.jobpilot.ui.theme.TextSecondary

private val ProfileCardShape = RoundedCornerShape(16.dp)

@Composable
fun UserOnboardingScreen(
    onCompleteSetup: (userId: String) -> Unit,
    onUploadResumeClicked: (userId: String) -> Unit,
    uploadedResumeName: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedUserId by remember { mutableStateOf("sireesh") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Surface000)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Orbit",
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.8).sp
                    )
                )
                Text(
                    text = "Welcome! Select who is using this device to start fresh.",
                    style = TextStyle(
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // User 1: Sireesh
            ProfileSelectionCard(
                name = "Sireesh",
                email = "sireesh0204@gmail.com",
                roleTitle = "Staff Backend & Systems",
                isSelected = selectedUserId == "sireesh",
                onSelect = { selectedUserId = "sireesh" }
            )

            // User 2: Bhargavi
            ProfileSelectionCard(
                name = "Bhargavi",
                email = "bhargaviyerroju@outlook.com",
                roleTitle = "Software Engineer / Tech",
                isSelected = selectedUserId == "bhargavi",
                onSelect = { selectedUserId = "bhargavi" }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Master Resume Upload Callout
            val isResumeAttached = !uploadedResumeName.isNullOrBlank()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ProfileCardShape)
                    .clickable { onUploadResumeClicked(selectedUserId) },
                shape = ProfileCardShape,
                color = Surface100,
                border = BorderStroke(1.dp, if (isResumeAttached) CalmEmerald.copy(alpha = 0.5f) else SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isResumeAttached) CalmEmerald.copy(alpha = 0.15f) else ExecutiveIndigo.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isResumeAttached) Icons.Filled.Description else Icons.Filled.CloudUpload,
                            contentDescription = null,
                            tint = if (isResumeAttached) CalmEmerald else ExecutiveIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isResumeAttached) uploadedResumeName!! else "Upload Original Master Resume",
                            style = TextStyle(
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = if (isResumeAttached) "Verified reference for this profile" else "PDF or DOCX — reference for tailoring (optional now)",
                            style = TextStyle(
                                color = if (isResumeAttached) CalmEmerald else TextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }

                    if (isResumeAttached) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = CalmEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Continue CTA Button
            Button(
                onClick = { onCompleteSetup(selectedUserId) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExecutiveIndigo,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = "Start Using Orbit",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ProfileSelectionCard(
    name: String,
    email: String,
    roleTitle: String,
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(if (isSelected) ExecutiveIndigo else Surface200, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = TextPrimary,
                            fontSize = 16.sp,
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
                        text = roleTitle,
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
                    contentDescription = "Selected",
                    tint = CalmEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
