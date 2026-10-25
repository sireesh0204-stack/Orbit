package com.portfolio.jobpilot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.jobpilot.ui.theme.*

@Composable
fun PassiveTelemetryHud(
    activeUserEmail: String,
    syncStatusText: String,
    activeFilterText: String,
    modifier: Modifier = Modifier
) {
    // Top 20% Read-Only Zone: Glanceable system telemetry
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface000)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Whitelist User Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface100)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "PILOT: $activeUserEmail",
                    style = JobPilotTypography.labelSmall.copy(color = NeonCyan)
                )
            }

            // Sync pulse heartbeat
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(NeonEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = syncStatusText,
                    style = JobPilotTypography.labelSmall.copy(color = TextMuted)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Active Filter Telemetry
        Text(
            text = "FILTERS: $activeFilterText",
            style = JobPilotTypography.labelSmall.copy(
                letterSpacing = 0.5.sp,
                color = TextSecondary
            )
        )
    }
}

