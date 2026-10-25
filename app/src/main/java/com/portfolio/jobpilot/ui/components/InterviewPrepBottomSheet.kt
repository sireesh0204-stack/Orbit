package com.portfolio.jobpilot.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ============================================================================
// EXECUTIVE BENTO COCKPIT DESIGN TOKENS
// ============================================================================

private val Surface000 = Color(0xFF090D16)
private val Surface100 = Color(0xFF0F172A)
private val Surface200 = Color(0xFF1E293B)
private val SurfaceBorder = Color(0xFF334155)

private val ExecutiveIndigo = Color(0xFF6366F1)
private val CalmEmerald = Color(0xFF10B981)
private val WarmAmber = Color(0xFFF59E0B)
private val SignalCrimson = Color(0xFFEF4444)

private val Slate50 = Color(0xFFF8FAFC)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate400 = Color(0xFF94A3B8)

private val SheetTopShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private val BentoSquircleShape = RoundedCornerShape(16.dp)
private val BentoInnerShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50)

// ============================================================================
// UI DATA MODELS (@Immutable FOR 120Hz SKIPPABILITY)
// ============================================================================

enum class PrepDossierCategory(
    val label: String,
    val icon: ImageVector
) {
    SYSTEM_DESIGN("System Design", Icons.Rounded.AccountTree),
    TECH_CONCEPTS("Tech Concepts", Icons.Rounded.Memory),
    STAR_STORIES("STAR Stories", Icons.Rounded.Verified),
    REVERSE_QS("Reverse Qs", Icons.Rounded.Psychology),
    PODCAST_DOSSIER("Podcast Dossier", Icons.Rounded.GraphicEq)
}

@Immutable
data class SystemDesignComponentNode(
    val name: String,
    val role: String,
    val slaMetric: String
)

@Immutable
data class SystemDesignBlueprintUiModel(
    val id: String,
    val title: String,
    val architectureStyle: String,
    val targetThroughput: String,
    val problemStatement: String,
    val keyComponents: List<SystemDesignComponentNode>,
    val capAndLatencyTradeoffs: List<String>,
    val candidateTalkingPoints: List<String>
)

@Immutable
data class TechConceptCardUiModel(
    val id: String,
    val conceptTitle: String,
    val frequencyTag: String,
    val questionPrompt: String,
    val elevatorPitch30s: String,
    val deepDivePoints: List<String>,
    val trapsToAvoid: List<String>
)

@Immutable
data class StarStoryUiModel(
    val id: String,
    val headline: String,
    val competencyTag: String,
    val verificationBadge: String,
    val impactMetric: String,
    val situation: String,
    val task: String,
    val action: String,
    val result: String
)

@Immutable
data class ReverseQuestionUiModel(
    val id: String,
    val questionText: String,
    val strategicIntent: String,
    val targetPersona: String,
    val followUpAngle: String
)

@Immutable
data class PodcastDossierUiModel(
    val promptTitle: String,
    val estimatedReadMinutes: Int,
    val audioPromptInstructions: String,
    val rawNotebookLmMarkdown: String,
    val notebookSteps: List<String> = listOf(
        "Tap '1-Tap Copy NotebookLM Dossier' or 'Export .md' to stage the grounded interview packet.",
        "Launch NotebookLM Web, create a fresh study notebook, and paste the markdown source.",
        "Open Audio Overview -> Customize and paste the Audio Podcast Prompt below for a two-host technical mock debrief."
    )
)

@Immutable
data class InterviewPrepDossierUiModel(
    val jobId: String,
    val companyName: String,
    val targetPosition: String,
    val domainBadge: String,
    val readinessScorePercent: Int,
    val systemDesigns: List<SystemDesignBlueprintUiModel>,
    val techConcepts: List<TechConceptCardUiModel>,
    val starStories: List<StarStoryUiModel>,
    val reverseQuestions: List<ReverseQuestionUiModel>,
    val podcastDossier: PodcastDossierUiModel
)

// ============================================================================
// STATELESS TOP-LEVEL BOTTOM SHEET COMPOSABLE
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewPrepBottomSheet(
    dossier: InterviewPrepDossierUiModel,
    onDismissRequest: () -> Unit,
    onCopyNotebookLmMarkdown: (String) -> Unit,
    onOpenNotebookLmWeb: () -> Unit,
    onExportMarkdownFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    var selectedCategory by rememberSaveable { mutableStateOf(PrepDossierCategory.SYSTEM_DESIGN) }
    var isDossierCopied by remember { mutableStateOf(false) }
    var isPodcastPromptCopied by remember { mutableStateOf(false) }

    // Default first tech concept expanded for immediate scanability
    val expandedConceptMap = remember(dossier.jobId) {
        mutableStateMapOf<String, Boolean>().apply {
            dossier.techConcepts.firstOrNull()?.let { put(it.id, true) }
        }
    }

    val listState: LazyListState = rememberLazyListState()

    LaunchedEffect(isDossierCopied) {
        if (isDossierCopied) {
            delay(2000L)
            isDossierCopied = false
        }
    }

    LaunchedEffect(isPodcastPromptCopied) {
        if (isPodcastPromptCopied) {
            delay(2000L)
            isPodcastPromptCopied = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetMaxWidth = Dp.Unspecified,
        shape = SheetTopShape,
        containerColor = Surface000,
        contentColor = Slate50,
        scrimColor = Color.Black.copy(alpha = 0.75f),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .background(Surface000)
                .navigationBarsPadding()
        ) {
            // 1. Pinned Executive Header
            PinnedDossierHeader(
                companyName = dossier.companyName,
                targetPosition = dossier.targetPosition,
                domainBadge = dossier.domainBadge,
                readinessScorePercent = dossier.readinessScorePercent,
                isCopied = isDossierCopied,
                onCopyDossier = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isDossierCopied = true
                    onCopyNotebookLmMarkdown(dossier.podcastDossier.rawNotebookLmMarkdown)
                },
                onOpenNotebookLm = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenNotebookLmWeb()
                },
                onDismiss = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDismissRequest()
                }
            )

            // 2. Sticky Segmented Category Navigation
            StickyCategoryNavRow(
                selectedCategory = selectedCategory,
                categoryCounts = remember(dossier) {
                    mapOf(
                        PrepDossierCategory.SYSTEM_DESIGN to dossier.systemDesigns.size,
                        PrepDossierCategory.TECH_CONCEPTS to dossier.techConcepts.size,
                        PrepDossierCategory.STAR_STORIES to dossier.starStories.size,
                        PrepDossierCategory.REVERSE_QS to dossier.reverseQuestions.size,
                        PrepDossierCategory.PODCAST_DOSSIER to 1
                    )
                },
                onSelectCategory = { category ->
                    if (category != selectedCategory) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedCategory = category
                    }
                }
            )

            // Zero-reflow scroll elevation hairline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .graphicsLayer {
                        val isScrolled = listState.firstVisibleItemIndex > 0 ||
                            listState.firstVisibleItemScrollOffset > 0
                        alpha = if (isScrolled) 1f else 0.35f
                    }
                    .background(SurfaceBorder)
            )

            // 3. 120Hz Content Area with Smooth Animated Transitions
            AnimatedContent(
                targetState = selectedCategory,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(180)) togetherWith
                        fadeOut(animationSpec = tween(140)))
                },
                label = "DossierCategoryContentTransition",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { category ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (category) {
                        PrepDossierCategory.SYSTEM_DESIGN -> {
                            items(
                                items = dossier.systemDesigns,
                                key = { "sys_design_${it.id}" },
                                contentType = { "system_design_bento" }
                            ) { blueprint ->
                                SystemDesignBentoCard(blueprint = blueprint)
                            }
                        }

                        PrepDossierCategory.TECH_CONCEPTS -> {
                            items(
                                items = dossier.techConcepts,
                                key = { "tech_concept_${it.id}" },
                                contentType = { "tech_concept_bento" }
                            ) { concept ->
                                val isExpanded = expandedConceptMap[concept.id] == true
                                TechConceptExpandableBentoCard(
                                    concept = concept,
                                    isExpanded = isExpanded,
                                    onToggleExpand = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        expandedConceptMap[concept.id] = !isExpanded
                                    }
                                )
                            }
                        }

                        PrepDossierCategory.STAR_STORIES -> {
                            items(
                                items = dossier.starStories,
                                key = { "star_story_${it.id}" },
                                contentType = { "star_story_bento" }
                            ) { story ->
                                StarStoryBentoCard(story = story)
                            }
                        }

                        PrepDossierCategory.REVERSE_QS -> {
                            itemsIndexed(
                                items = dossier.reverseQuestions,
                                key = { _, item -> "reverse_q_${item.id}" },
                                contentType = { _, _ -> "reverse_q_bento" }
                            ) { index, question ->
                                ReverseQuestionBentoCard(
                                    indexNumber = index + 1,
                                    question = question
                                )
                            }
                        }

                        PrepDossierCategory.PODCAST_DOSSIER -> {
                            item(
                                key = "podcast_dossier_card",
                                contentType = "podcast_dossier_bento"
                            ) {
                                PodcastDossierBentoSection(
                                    podcastDossier = dossier.podcastDossier,
                                    isPromptCopied = isPodcastPromptCopied,
                                    onCopyAudioPrompt = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isPodcastPromptCopied = true
                                        onCopyNotebookLmMarkdown(dossier.podcastDossier.audioPromptInstructions)
                                    },
                                    onCopyFullMarkdown = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isDossierCopied = true
                                        onCopyNotebookLmMarkdown(dossier.podcastDossier.rawNotebookLmMarkdown)
                                    },
                                    onExportMarkdownFile = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onExportMarkdownFile()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 1. PINNED HEADER (DRAG HANDLE + ROLE METADATA + 48DP NOTEBOOKLM ACTIONS)
// ============================================================================

@Composable
private fun PinnedDossierHeader(
    companyName: String,
    targetPosition: String,
    domainBadge: String,
    readinessScorePercent: Int,
    isCopied: Boolean,
    onCopyDossier: () -> Unit,
    onOpenNotebookLm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface000)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Custom G2 Drag Handle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .clip(PillShape)
                    .background(SurfaceBorder)
            )
        }

        // Company & Target Role Title + 48dp Close Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$companyName — $targetPosition",
                    style = TextStyle(
                        color = Slate50,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Domain Badge + Tabular Readiness Pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(ExecutiveIndigo.copy(alpha = 0.14f), PillShape)
                            .border(1.dp, ExecutiveIndigo.copy(alpha = 0.4f), PillShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = domainBadge,
                            style = TextStyle(
                                color = Slate200,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(CalmEmerald.copy(alpha = 0.14f), PillShape)
                            .border(1.dp, CalmEmerald.copy(alpha = 0.4f), PillShape)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$readinessScorePercent% READY",
                            style = TextStyle(
                                color = CalmEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                }
            }

            // 48dp x 48dp Ergonomic Close Target
            BentoPressableBox(
                onClick = onDismiss,
                containerColor = Surface100,
                borderColor = SurfaceBorder,
                shape = BentoInnerShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Interview Prep Dossier",
                    tint = Slate400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Action Row: 1-Tap Copy NotebookLM Dossier + Open NotebookLM Web
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val copyBgColor by animateColorAsState(
                targetValue = if (isCopied) CalmEmerald else ExecutiveIndigo,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "CopyDossierButtonBg"
            )

            // Primary 1-Tap Copy Button (48dp height)
            BentoPressableBox(
                onClick = onCopyDossier,
                containerColor = copyBgColor,
                borderColor = Color.Transparent,
                shape = BentoInnerShape,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        tint = Slate50,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCopied) "Dossier Copied to Clipboard" else "1-Tap Copy NotebookLM Dossier",
                        style = TextStyle(
                            color = Slate50,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.1).sp,
                            fontFeatureSettings = "tnum"
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Secondary Open NotebookLM Button (48dp height)
            BentoPressableBox(
                onClick = onOpenNotebookLm,
                containerColor = Surface100,
                borderColor = SurfaceBorder,
                shape = BentoInnerShape,
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Open NotebookLM",
                        style = TextStyle(
                            color = Slate50,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = "Open NotebookLM Web",
                        tint = ExecutiveIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 2. STICKY SEGMENTED CATEGORY NAVIGATION (48DP REACHABILITY)
// ============================================================================

@Composable
private fun StickyCategoryNavRow(
    selectedCategory: PrepDossierCategory,
    categoryCounts: Map<PrepDossierCategory, Int>,
    onSelectCategory: (PrepDossierCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface000)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PrepDossierCategory.entries.forEach { category ->
            val isSelected = category == selectedCategory
            val count = categoryCounts[category] ?: 0

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) ExecutiveIndigo else Surface100,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "CategoryTabBg"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) ExecutiveIndigo else SurfaceBorder,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "CategoryTabBorder"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected) Slate50 else Slate400,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "CategoryTabText"
            )

            BentoPressableBox(
                onClick = { onSelectCategory(category) },
                containerColor = containerColor,
                borderColor = borderColor,
                shape = BentoInnerShape,
                role = Role.Tab,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = if (isSelected) Slate50 else ExecutiveIndigo,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = category.label,
                        style = TextStyle(
                            color = contentColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) Surface000.copy(alpha = 0.28f) else Surface200,
                                shape = PillShape
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$count",
                            style = TextStyle(
                                color = contentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3A. SYSTEM DESIGN TAB BENTO CARD
// ============================================================================

@Composable
private fun SystemDesignBentoCard(
    blueprint: SystemDesignBlueprintUiModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BentoSquircleShape,
        color = Surface100,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header: Title + Architecture Style & Throughput Pills
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Layers,
                            contentDescription = null,
                            tint = ExecutiveIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = blueprint.architectureStyle.uppercase(),
                            style = TextStyle(
                                color = ExecutiveIndigo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.7.sp
                            )
                        )
                    }

                    // Tabular SLA / Throughput Badge
                    Row(
                        modifier = Modifier
                            .background(Surface200, PillShape)
                            .border(1.dp, SurfaceBorder, PillShape)
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            tint = CalmEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = blueprint.targetThroughput,
                            style = TextStyle(
                                color = Slate50,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                }

                Text(
                    text = blueprint.title,
                    style = TextStyle(
                        color = Slate50,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Problem Statement Sub-Cell
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface000, BentoInnerShape)
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.7f), BentoInnerShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "PROBLEM STATEMENT",
                    style = TextStyle(
                        color = Slate400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Text(
                    text = blueprint.problemStatement,
                    style = TextStyle(
                        color = Slate200,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            // Key Components Bento List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "KEY ARCHITECTURE COMPONENTS",
                    style = TextStyle(
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )

                blueprint.keyComponents.forEach { node ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface200.copy(alpha = 0.55f), BentoInnerShape)
                            .border(1.dp, SurfaceBorder, BentoInnerShape)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = node.name,
                                style = TextStyle(
                                    color = Slate50,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = node.role,
                                style = TextStyle(
                                    color = Slate400,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Surface000, PillShape)
                                .border(1.dp, SurfaceBorder, PillShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = node.slaMetric,
                                style = TextStyle(
                                    color = ExecutiveIndigo,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                )
                            )
                        }
                    }
                }
            }

            // CAP & Latency Trade-offs Callout (WarmAmber Accent)
            AccentCalloutBox(
                title = "CAP & LATENCY TRADE-OFFS",
                accentColor = WarmAmber,
                icon = Icons.Rounded.Bolt,
                bullets = blueprint.capAndLatencyTradeoffs
            )

            // Candidate Talking Points Callout (CalmEmerald Accent)
            AccentCalloutBox(
                title = "CANDIDATE TALKING POINTS",
                accentColor = CalmEmerald,
                icon = Icons.Rounded.CheckCircle,
                bullets = blueprint.candidateTalkingPoints
            )
        }
    }
}

// ============================================================================
// 3B. TECH CONCEPTS EXPANDABLE BENTO CARD
// ============================================================================

@Composable
private fun TechConceptExpandableBentoCard(
    concept: TechConceptCardUiModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ConceptChevronRotation"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BentoSquircleShape,
        color = Surface100,
        border = BorderStroke(
            width = 1.dp,
            color = if (isExpanded) ExecutiveIndigo.copy(alpha = 0.65f) else SurfaceBorder
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 48dp+ Touch Target Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
                    .clickable(
                        role = Role.Button,
                        onClick = onToggleExpand
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(ExecutiveIndigo.copy(alpha = 0.16f), PillShape)
                                .border(1.dp, ExecutiveIndigo.copy(alpha = 0.4f), PillShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = concept.frequencyTag,
                                style = TextStyle(
                                    color = ExecutiveIndigo,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    fontFeatureSettings = "tnum"
                                )
                            )
                        }
                        Text(
                            text = concept.conceptTitle,
                            style = TextStyle(
                                color = Slate400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = concept.questionPrompt,
                        style = TextStyle(
                            color = Slate50,
                            fontSize = 15.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(BentoInnerShape)
                        .background(Surface200)
                        .border(1.dp, SurfaceBorder, BentoInnerShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse Concept" else "Expand Concept",
                        tint = Slate50,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                rotationZ = chevronRotation
                            }
                    )
                }
            }

            // Always-Visible 30-Second Elevator Pitch
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .background(Surface000, BentoInnerShape)
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.7f), BentoInnerShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "30S ELEVATOR PITCH",
                    style = TextStyle(
                        color = CalmEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontFeatureSettings = "tnum"
                    )
                )
                Text(
                    text = concept.elevatorPitch30s,
                    style = TextStyle(
                        color = Slate200,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                )
            }

            // Expandable Deep-Dive & Traps Callout
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Deep-Dive Architectural Points
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface200.copy(alpha = 0.45f), BentoInnerShape)
                            .border(1.dp, SurfaceBorder, BentoInnerShape)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TECHNICAL DEEP-DIVE POINTS",
                            style = TextStyle(
                                color = Slate400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )
                        concept.deepDivePoints.forEach { point ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 7.dp)
                                        .size(5.dp)
                                        .background(ExecutiveIndigo, CircleShape)
                                )
                                Text(
                                    text = point,
                                    style = TextStyle(
                                        color = Slate50,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }

                    // Prominent ⚠️ Traps to Avoid Callout Block
                    AccentCalloutBox(
                        title = "⚠️ TRAPS TO AVOID",
                        accentColor = SignalCrimson,
                        icon = Icons.Rounded.WarningAmber,
                        bullets = concept.trapsToAvoid
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3C. STAR STORIES TAB BENTO CARD (WITH VERIFIED GROUNDING PILL)
// ============================================================================

@Composable
private fun StarStoryBentoCard(
    story: StarStoryUiModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BentoSquircleShape,
        color = Surface100,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Verification & Competency Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Grounded Verification Pill
                Row(
                    modifier = Modifier
                        .background(CalmEmerald.copy(alpha = 0.14f), PillShape)
                        .border(1.dp, CalmEmerald.copy(alpha = 0.45f), PillShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        tint = CalmEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = story.verificationBadge,
                        style = TextStyle(
                            color = CalmEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }

                // Tabular Impact Metric Pill
                Box(
                    modifier = Modifier
                        .background(ExecutiveIndigo.copy(alpha = 0.14f), PillShape)
                        .border(1.dp, ExecutiveIndigo.copy(alpha = 0.4f), PillShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = story.impactMetric,
                        style = TextStyle(
                            color = Slate50,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }

            // Story Headline & Competency Tag
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = story.competencyTag.uppercase(),
                    style = TextStyle(
                        color = Slate400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Text(
                    text = story.headline,
                    style = TextStyle(
                        color = Slate50,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // 4-Stage S.T.A.R. Grid Blocks
            StarStageBlock(
                stageBadge = "S",
                stageTitle = "SITUATION",
                accentColor = Slate400,
                bodyText = story.situation
            )
            StarStageBlock(
                stageBadge = "T",
                stageTitle = "TASK",
                accentColor = WarmAmber,
                bodyText = story.task
            )
            StarStageBlock(
                stageBadge = "A",
                stageTitle = "ACTION",
                accentColor = ExecutiveIndigo,
                bodyText = story.action
            )
            StarStageBlock(
                stageBadge = "R",
                stageTitle = "RESULT (QUANTIFIED)",
                accentColor = CalmEmerald,
                bodyText = story.result
            )
        }
    }
}

@Composable
private fun StarStageBlock(
    stageBadge: String,
    stageTitle: String,
    accentColor: Color,
    bodyText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface000, BentoInnerShape)
            .border(1.dp, SurfaceBorder.copy(alpha = 0.75f), BentoInnerShape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.16f))
                .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stageBadge,
                style = TextStyle(
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                )
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stageTitle,
                style = TextStyle(
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            )
            Text(
                text = bodyText,
                style = TextStyle(
                    color = Slate200,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontFeatureSettings = "tnum"
                )
            )
        }
    }
}

// ============================================================================
// 3D. REVERSE QUESTIONS TAB BENTO CARD
// ============================================================================

@Composable
private fun ReverseQuestionBentoCard(
    indexNumber: Int,
    question: ReverseQuestionUiModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BentoSquircleShape,
        color = Surface100,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tabular Question Number Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(ExecutiveIndigo, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (indexNumber < 10) "0$indexNumber" else "$indexNumber",
                            style = TextStyle(
                                color = Slate50,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                    Text(
                        text = "ASK: ${question.targetPersona.uppercase()}",
                        style = TextStyle(
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.7.sp
                        )
                    )
                }
            }

            // Question Copy
            Text(
                text = "\"${question.questionText}\"",
                style = TextStyle(
                    color = Slate50,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            // Strategic Intent Pill Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ExecutiveIndigo.copy(alpha = 0.1f), BentoInnerShape)
                    .border(1.dp, ExecutiveIndigo.copy(alpha = 0.35f), BentoInnerShape)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "STRATEGIC INTENT",
                    style = TextStyle(
                        color = ExecutiveIndigo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Text(
                    text = question.strategicIntent,
                    style = TextStyle(
                        color = Slate200,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
            }

            // Follow-Up Lever
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface000, BentoInnerShape)
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.7f), BentoInnerShape)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FOLLOW-UP:",
                    style = TextStyle(
                        color = CalmEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                )
                Text(
                    text = question.followUpAngle,
                    style = TextStyle(
                        color = Slate400,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

// ============================================================================
// 3E. PODCAST DOSSIER TAB (MARKDOWN PREVIEW + 3-STEP NOTEBOOKLM WORKFLOW)
// ============================================================================

@Composable
private fun PodcastDossierBentoSection(
    podcastDossier: PodcastDossierUiModel,
    isPromptCopied: Boolean,
    onCopyAudioPrompt: () -> Unit,
    onCopyFullMarkdown: () -> Unit,
    onExportMarkdownFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 3-Step NotebookLM Onboarding Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = BentoSquircleShape,
            color = Surface100,
            border = BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = ExecutiveIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "NOTEBOOKLM AUDIO BRIEFING WORKFLOW",
                            style = TextStyle(
                                color = Slate50,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.7.sp
                            )
                        )
                    }

                    Text(
                        text = "~${podcastDossier.estimatedReadMinutes}m READ",
                        style = TextStyle(
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }

                podcastDossier.notebookSteps.forEachIndexed { idx, stepText ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface000, BentoInnerShape)
                            .border(1.dp, SurfaceBorder.copy(alpha = 0.7f), BentoInnerShape)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(ExecutiveIndigo.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, ExecutiveIndigo, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = TextStyle(
                                    color = Slate50,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                )
                            )
                        }
                        Text(
                            text = stepText,
                            style = TextStyle(
                                color = Slate200,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Copy Audio Podcast Prompt CTA + Export .md File Button (48dp Targets)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val promptBtnColor by animateColorAsState(
                        targetValue = if (isPromptCopied) CalmEmerald else ExecutiveIndigo,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "CopyPodcastPromptColor"
                    )

                    BentoPressableBox(
                        onClick = onCopyAudioPrompt,
                        containerColor = promptBtnColor,
                        borderColor = Color.Transparent,
                        shape = BentoInnerShape,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isPromptCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                                contentDescription = null,
                                tint = Slate50,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPromptCopied) "Podcast Prompt Copied" else "Copy Audio Podcast Prompt",
                                style = TextStyle(
                                    color = Slate50,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    BentoPressableBox(
                        onClick = onExportMarkdownFile,
                        containerColor = Surface200,
                        borderColor = SurfaceBorder,
                        shape = BentoInnerShape,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FileDownload,
                                contentDescription = "Export Markdown File",
                                tint = Slate50,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = ".MD",
                                style = TextStyle(
                                    color = Slate50,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                )
                            )
                        }
                    }
                }
            }
        }

        // Markdown Preview Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = BentoSquircleShape,
            color = Surface100,
            border = BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GROUNDED MARKDOWN DOSSIER PREVIEW",
                        style = TextStyle(
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )

                    BentoPressableBox(
                        onClick = onCopyFullMarkdown,
                        containerColor = Surface200,
                        borderColor = SurfaceBorder,
                        shape = BentoInnerShape,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy Full Markdown",
                                tint = ExecutiveIndigo,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Copy Raw .MD",
                                style = TextStyle(
                                    color = Slate50,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface000, BentoInnerShape)
                        .border(1.dp, SurfaceBorder, BentoInnerShape)
                        .padding(14.dp)
                ) {
                    Text(
                        text = podcastDossier.rawNotebookLmMarkdown,
                        style = TextStyle(
                            color = Slate200,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            }
        }
    }
}

// ============================================================================
// SHARED ZERO-REFLOW BENTO PRIMITIVES
// ============================================================================

@Composable
private fun AccentCalloutBox(
    title: String,
    accentColor: Color,
    icon: ImageVector,
    bullets: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithCache {
                val corner = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                val barWidth = 4.dp.toPx()
                onDrawBehind {
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.1f),
                        cornerRadius = corner
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset.Zero,
                        size = Size(width = barWidth, height = size.height),
                        cornerRadius = corner
                    )
                }
            }
            .border(1.dp, accentColor.copy(alpha = 0.35f), BentoInnerShape)
            .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = TextStyle(
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp
                )
            )
        }

        bullets.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .size(5.dp)
                        .background(accentColor, CircleShape)
                )
                Text(
                    text = item,
                    style = TextStyle(
                        color = Slate50,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFeatureSettings = "tnum"
                    )
                )
            }
        }
    }
}

@Composable
private fun BentoPressableBox(
    onClick: () -> Unit,
    containerColor: Color,
    borderColor: Color,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    role: Role = Role.Button,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 120Hz VRR-safe spring; read exclusively inside graphicsLayer lambda
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "BentoPressableScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor, shape)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.dp, borderColor, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = role,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
