package com.portfolio.jobpilot.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.portfolio.jobpilot.ui.components.JobCard
import com.portfolio.jobpilot.ui.components.JobCardSkeleton
import com.portfolio.jobpilot.ui.components.JobCardUiModel
import com.portfolio.jobpilot.ui.theme.Surface000
import com.portfolio.jobpilot.ui.theme.Surface100
import com.portfolio.jobpilot.ui.theme.SurfaceBorder
import com.portfolio.jobpilot.ui.theme.TextMuted
import com.portfolio.jobpilot.ui.theme.TextPrimary
import com.portfolio.jobpilot.ui.theme.TextSecondary

@Immutable
data class JobCockpitUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val jobs: List<JobCardUiModel> = emptyList()
)

@Composable
fun JobCockpitScreen(
    uiState: JobCockpitUiState,
    onCardClick: (String) -> Unit,
    onSaveRole: (String) -> Unit,
    onQuickDispatch: (String) -> Unit,
    onRefreshFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Surface000)
    ) {
        // ====================================================================
        // CLEAN MINIMAL HEADER: Just "Jobs" and a Refresh Icon
        // ====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jobs",
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.6).sp
                )
            )

            // Minimal Refresh Action Button
            IconButton(
                onClick = onRefreshFeed,
                modifier = Modifier
                    .size(38.dp)
                    .background(Surface100, CircleShape)
                    .border(1.dp, SurfaceBorder, CircleShape)
            ) {
                if (uiState.isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = "Refresh jobs",
                        tint = TextPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // ====================================================================
        // COMPACT JOBS LIST (4+ Cards Fit on Screen)
        // ====================================================================
        if (uiState.isLoading && uiState.jobs.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(5) {
                    JobCardSkeleton()
                }
            }
        } else if (uiState.jobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No active roles found",
                        style = TextStyle(
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Tap the refresh icon above to ingest latest postings.",
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.jobs,
                    key = { it.id }
                ) { job ->
                    JobCard(
                        job = job,
                        onCardClick = onCardClick,
                        onSaveToggle = onSaveRole,
                        onQuickDispatch = onQuickDispatch
                    )
                }
                
                // Bottom spacing for clean navigation bar clearance
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
