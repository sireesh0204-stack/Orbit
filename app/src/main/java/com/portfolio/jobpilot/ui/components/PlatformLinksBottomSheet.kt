package com.portfolio.jobpilot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.portfolio.jobpilot.data.remote.dto.PlatformUrlDto
import com.portfolio.jobpilot.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformLinksBottomSheet(
    jobTitle: String,
    company: String,
    sourceUrls: List<PlatformUrlDto>,
    onDismiss: () -> Unit,
    onUrlSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface100,
        contentColor = TextPrimary,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "VERIFIED APPLICATION PLATFORMS",
                style = JobPilotTypography.labelSmall.copy(color = NeonCyan)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = jobTitle,
                style = JobPilotTypography.titleMedium
            )
            Text(
                text = company,
                style = JobPilotTypography.bodyMedium.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (sourceUrls.isEmpty()) {
                Text(
                    text = "No direct links available.",
                    style = JobPilotTypography.bodyMedium.copy(color = TextMuted)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sourceUrls.forEach { item ->
                        val platformColor = when (item.platform.lowercase()) {
                            "linkedin" -> NeonCyan
                            "naukri" -> NeonEmerald
                            "indeed" -> SatinGold
                            else -> TextPrimary
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface200)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .clickable { onUrlSelected(item.url) }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Apply on ${item.platform}",
                                    style = JobPilotTypography.titleMedium.copy(
                                        color = platformColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open",
                                    tint = platformColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

