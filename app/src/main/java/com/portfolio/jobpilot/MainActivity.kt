package com.portfolio.jobpilot

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.portfolio.jobpilot.data.local.entity.JobEntity
import com.portfolio.jobpilot.data.remote.dto.PlatformUrlDto
import com.portfolio.jobpilot.data.remote.dto.SkillsGroupDto
import com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.ui.adaptive.ProvideAdaptiveDeviceTier
import com.portfolio.jobpilot.ui.components.CockpitUserTab
import com.portfolio.jobpilot.ui.components.DualUserFilterBar
import com.portfolio.jobpilot.ui.components.DualUserFilterState
import com.portfolio.jobpilot.ui.components.JobCard
import com.portfolio.jobpilot.ui.components.JobCardSkeleton
import com.portfolio.jobpilot.ui.components.JobCardUiModel
import com.portfolio.jobpilot.ui.components.SwipeToStageWrapper
import com.portfolio.jobpilot.ui.components.PassiveTelemetryHud
import com.portfolio.jobpilot.ui.components.PlatformLinksBottomSheet
import com.portfolio.jobpilot.ui.components.InterviewPrepBottomSheet
import com.portfolio.jobpilot.ui.components.sampleStripeDossierUiModel
import com.portfolio.jobpilot.ui.screens.CockpitTelemetryState
import com.portfolio.jobpilot.ui.screens.JobCockpitScreen
import com.portfolio.jobpilot.ui.screens.JobCockpitUiState
import com.portfolio.jobpilot.ui.screens.MyResumesScreen
import com.portfolio.jobpilot.ui.screens.ProfileScreen
import com.portfolio.jobpilot.ui.theme.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideAdaptiveDeviceTier {
                JobPilotTheme {
                    JobPilotApp()
                }
            }
        }
    }
}

@Composable
fun JobPilotApp() {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Local profile state for demo & offline inspection
    var profileState by remember {
        mutableStateOf<UserProfileDto?>(
            UserProfileDto(
                userId = "pilot_user_1",
                targetRoles = listOf("Staff Backend Engineer", "Lead Distributed Systems"),
                totalYearsExperience = 7.5,
                preferredCities = listOf("Hyderabad", "Remote"),
                isRemotePreferred = true,
                skills = SkillsGroupDto(
                    technical = listOf("Kotlin", "Python", "Go", "Kafka", "Docker", "GCP"),
                    soft = listOf("Leadership", "System Design"),
                    toolsAndPlatforms = listOf("Linux", "Git", "Kubernetes")
                )
            )
        )
    }

    var isUploading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            statusMessage = "Reading resume document..."
            isUploading = true
            // File reading can be dispatched to ProfileViewModel / Coroutine
            isUploading = false
            statusMessage = "Resume parsed & structured successfully."
        }
    }

    var tailoredResumes by remember {
        mutableStateOf<List<TailoredResumeMetadataDto>>(
            listOf(
                TailoredResumeMetadataDto(
                    resumeId = "res_demo_1",
                    userId = "pilot_user_1",
                    jobId = "demo_job_1",
                    company = "Stripe",
                    position = "Staff Backend Engineer — Core Systems",
                    createdAt = "2026-10-24 23:30",
                    expiryDate = "2026-11-23",
                    atsKeywordCoverageScore = 96,
                    docxStoragePath = "/api/v1/tailor/res_demo_1/docx",
                    pdfStoragePath = "/api/v1/tailor/res_demo_1/pdf"
                )
            )
        )
    }
    var activeSheetJob by remember { mutableStateOf<JobEntity?>(null) }
    var activePrepJob by remember { mutableStateOf<JobCardUiModel?>(null) }
    var filterState by remember {
        mutableStateOf(
            DualUserFilterState(
                selectedTab = CockpitUserTab.ALL,
                allRolesCount = 20,
                sireeshCount = 12,
                bhargaviCount = 8
            )
        )
    }

    if (activePrepJob != null) {
        val job = activePrepJob!!
        val prepDossier = remember(job.id) {
            sampleStripeDossierUiModel().copy(
                companyName = job.company,
                targetPosition = job.title,
                domainBadge = if (job.company.contains("Microsoft", ignoreCase = true)) {
                    "Enterprise Cloud & Distributed Systems"
                } else {
                    "Fintech & Payments Infrastructure"
                }
            )
        }
        InterviewPrepBottomSheet(
            dossier = prepDossier,
            onDismissRequest = { activePrepJob = null },
            onCopyNotebookLmMarkdown = { text ->
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("NotebookLM Dossier", text)
                clipboard.setPrimaryClip(clip)
            },
            onOpenNotebookLmWeb = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://notebooklm.google.com"))
                    context.startActivity(intent)
                } catch (_: Exception) {}
            },
            onExportMarkdownFile = {
                try {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, prepDossier.podcastDossier.rawNotebookLmMarkdown)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export Dossier"))
                } catch (_: Exception) {}
            }
        )
    }

    if (activeSheetJob != null) {
        val job = activeSheetJob!!
        PlatformLinksBottomSheet(
            jobTitle = job.title,
            company = job.company,
            sourceUrls = job.getSourceUrls(),
            onDismiss = { activeSheetJob = null },
            onUrlSelected = { url ->
                activeSheetJob = null
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback
                }
            }
        )
    }

    Scaffold(
        containerColor = Surface000,
        topBar = {
            if (selectedTab != 0) {
                PassiveTelemetryHud(
                    activeUserEmail = "sireesh0204@gmail.com",
                    syncStatusText = "LIVE",
                    activeFilterText = "Hyderabad (All) + Remote • 7+ YoE"
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Surface100,
                contentColor = TextPrimary,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Jobs") },
                    label = { Text("Jobs", style = JobPilotTypography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        indicatorColor = Surface200
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile", style = JobPilotTypography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        indicatorColor = Surface200
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Saved") },
                    label = { Text("Saved", style = JobPilotTypography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SatinGold,
                        indicatorColor = Surface200
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.Description, contentDescription = "Resumes") },
                    label = { Text("Resumes", style = JobPilotTypography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonEmerald,
                        indicatorColor = Surface200
                    )
                )
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            val cockpitJobs = remember {
                listOf(
                    JobCardUiModel(
                        id = "demo_job_1",
                        title = "Staff Backend Engineer — Cloud & Distributed Systems",
                        company = "Microsoft",
                        locationMode = "Hyderabad (Hybrid / Office)",
                        compensationRange = "₹50L – ₹70L",
                        matchPercentage = 96,
                        postedAgo = "5m ago",
                        assigneeTag = "Sireesh",
                        whyItMatches = "Hyderabad campus accessible with hybrid office reach. Core stack matches your Go, Kafka, and distributed systems experience 1:1.",
                        expiryDaysRemaining = 21,
                        isSaved = true
                    ),
                    JobCardUiModel(
                        id = "demo_job_2",
                        title = "Staff Backend Engineer — Core Payments",
                        company = "Stripe",
                        locationMode = "Remote (India)",
                        compensationRange = "₹45L – ₹65L",
                        matchPercentage = 94,
                        postedAgo = "18m ago",
                        assigneeTag = "Sireesh",
                        whyItMatches = "100% Remote flexibility. Your 7+ years building high-throughput distributed event pipelines in Go & Kotlin maps 1:1 with Stripe's payments infrastructure.",
                        expiryDaysRemaining = 14,
                        isSaved = false
                    )
                )
            }

            JobCockpitScreen(
                uiState = JobCockpitUiState(
                    isLoading = false,
                    telemetry = CockpitTelemetryState(
                        syncStatusLabel = "LIVE FEED",
                        lastSyncedAgo = "5m ago",
                        isSyncing = false,
                        queuedCount = 20,
                        highMatchCount = 12,
                        latencyMs = 18
                    ),
                    filterState = filterState,
                    jobs = cockpitJobs
                ),
                onTabSelected = { tab ->
                    filterState = filterState.copy(selectedTab = tab)
                },
                onQuickFilterToggle = { filter ->
                    val updated = filterState.activeQuickFilters.toMutableSet()
                    if (updated.contains(filter)) updated.remove(filter) else updated.add(filter)
                    filterState = filterState.copy(activeQuickFilters = updated)
                },
                onCardClick = { clickedId ->
                    activePrepJob = cockpitJobs.find { it.id == clickedId }
                },
                onSaveRole = { _ -> /* toggle favorite */ },
                onArchiveRole = { _ -> /* archive role */ },
                onQuickDispatch = { _ -> /* open links sheet */ },
                onTailorResume = { _ -> /* tailor resume */ },
                onRefreshFeed = { /* trigger 5m ultra-fresh live sync */ },
                modifier = Modifier.padding(innerPadding)
            )
        } else if (selectedTab == 1) {
            ProfileScreen(
                profile = profileState,
                isUploading = isUploading,
                isSaving = isSaving,
                statusMessage = statusMessage,
                errorMessage = errorMessage,
                onSelectResumeClicked = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "text/plain",
                            "image/png",
                            "image/jpeg"
                        )
                    )
                },
                onImportGoogleDocClicked = { url ->
                    statusMessage = "Importing Google Doc..."
                    isUploading = true
                    // Simulate import completion in demo UI
                    isUploading = false
                    statusMessage = "Google Doc imported & parsed."
                },
                onToggleRemote = { isRemote ->
                    profileState = profileState?.copy(isRemotePreferred = isRemote)
                },
                onSaveProfileClicked = {
                    isSaving = true
                    // Saved confirmation
                    isSaving = false
                    statusMessage = "Profile updated."
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else if (selectedTab == 3) {
            MyResumesScreen(
                resumes = tailoredResumes,
                onDownloadDocx = { /* Trigger download */ },
                onDownloadPdf = { /* Trigger download */ },
                onUploadToDrive = { /* Trigger drive upload */ },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
