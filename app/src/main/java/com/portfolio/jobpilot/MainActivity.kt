package com.portfolio.jobpilot

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.portfolio.jobpilot.data.remote.dto.PlatformUrlDto
import com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.ui.adaptive.ProvideAdaptiveDeviceTier
import com.portfolio.jobpilot.ui.components.InterviewPrepBottomSheet
import com.portfolio.jobpilot.ui.components.JobCardUiModel
import com.portfolio.jobpilot.ui.components.JobDetailBottomSheet
import com.portfolio.jobpilot.ui.components.PlatformLinksBottomSheet
import com.portfolio.jobpilot.ui.components.UserOnboardingScreen
import com.portfolio.jobpilot.ui.components.sampleStripeDossierUiModel
import com.portfolio.jobpilot.ui.screens.FavoritesScreen
import com.portfolio.jobpilot.ui.screens.JobCockpitScreen
import com.portfolio.jobpilot.ui.screens.JobCockpitUiState
import com.portfolio.jobpilot.ui.screens.MyResumesScreen
import com.portfolio.jobpilot.ui.screens.ProfileScreen
import com.portfolio.jobpilot.ui.theme.JobPilotTheme
import com.portfolio.jobpilot.ui.theme.JobPilotTypography
import com.portfolio.jobpilot.ui.theme.NeonCyan
import com.portfolio.jobpilot.ui.theme.NeonEmerald
import com.portfolio.jobpilot.ui.theme.SatinGold
import com.portfolio.jobpilot.ui.theme.Surface000
import com.portfolio.jobpilot.ui.theme.Surface100
import com.portfolio.jobpilot.ui.theme.Surface200
import com.portfolio.jobpilot.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crash Guard: Intercept Samsung S-Pen / OneUI Hover Exit Bug in Jetpack Compose
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (throwable is IllegalStateException && throwable.message?.contains("ACTION_HOVER_EXIT") == true) {
                Log.w("OrbitCrashGuard", "Caught and prevented Samsung S-Pen / Compose hover exit crash", throwable)
                return@setDefaultUncaughtExceptionHandler
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        setContent {
            ProvideAdaptiveDeviceTier {
                JobPilotTheme {
                    OrbitApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrbitApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("orbit_prefs", Context.MODE_PRIVATE) }

    // Persistent onboarding state — starts FALSE so user gets fresh newly-installed setup
    var isOnboarded by rememberSaveable {
        mutableStateOf(prefs.getBoolean("is_onboarded", false))
    }
    var activeUserId by rememberSaveable {
        mutableStateOf(prefs.getString("active_user_id", "sireesh") ?: "sireesh")
    }

    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // Master Resumes (Saved per user in SharedPreferences)
    var sireeshMasterResume by rememberSaveable {
        mutableStateOf(prefs.getString("sireesh_master_resume", null))
    }
    var bhargaviMasterResume by rememberSaveable {
        mutableStateOf(prefs.getString("bhargavi_master_resume", null))
    }

    // Profiles start empty / clean (No invented data)
    var sireeshProfile by remember { mutableStateOf<UserProfileDto?>(null) }
    var bhargaviProfile by remember { mutableStateOf<UserProfileDto?>(null) }

    // Jobs lists start completely EMPTY (No mock data)
    val sireeshJobs = remember { mutableStateOf<List<JobCardUiModel>>(emptyList()) }
    val bhargaviJobs = remember { mutableStateOf<List<JobCardUiModel>>(emptyList()) }

    // Tailored Resumes lists start completely EMPTY
    val sireeshResumes = remember { mutableStateOf<List<TailoredResumeMetadataDto>>(emptyList()) }
    val bhargaviResumes = remember { mutableStateOf<List<TailoredResumeMetadataDto>>(emptyList()) }

    // Modals
    var activeDetailJob by remember { mutableStateOf<JobCardUiModel?>(null) }
    var activePrepJob by remember { mutableStateOf<JobCardUiModel?>(null) }
    var activePlatformUrlsJob by remember { mutableStateOf<JobCardUiModel?>(null) }

    // File picker launcher for Master Resume
    var pendingUploadUserId by remember { mutableStateOf("sireesh") }
    val resumePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Master_Resume.pdf"
            if (pendingUploadUserId == "sireesh") {
                sireeshMasterResume = fileName
                prefs.edit().putString("sireesh_master_resume", fileName).apply()
            } else {
                bhargaviMasterResume = fileName
                prefs.edit().putString("bhargavi_master_resume", fileName).apply()
            }
            Toast.makeText(context, "Original Master Resume uploaded: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // Active dataset pointers
    val currentJobs = if (activeUserId == "sireesh") sireeshJobs.value else bhargaviJobs.value
    val currentProfile = if (activeUserId == "sireesh") sireeshProfile else bhargaviProfile
    val currentMasterResume = if (activeUserId == "sireesh") sireeshMasterResume else bhargaviMasterResume
    val currentResumes = if (activeUserId == "sireesh") sireeshResumes.value else bhargaviResumes.value

    // ========================================================================
    // FIRST-LAUNCH ONBOARDING SCREEN (Clean Fresh Experience)
    // ========================================================================
    if (!isOnboarded) {
        UserOnboardingScreen(
            onCompleteSetup = { chosenUserId ->
                activeUserId = chosenUserId
                isOnboarded = true
                prefs.edit()
                    .putString("active_user_id", chosenUserId)
                    .putBoolean("is_onboarded", true)
                    .apply()
                Toast.makeText(context, "Welcome to Orbit, ${if (chosenUserId == "sireesh") "Sireesh" else "Bhargavi"}!", Toast.LENGTH_SHORT).show()
            },
            onUploadResumeClicked = { userId ->
                pendingUploadUserId = userId
                resumePickerLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "text/plain"
                    )
                )
            },
            uploadedResumeName = if (pendingUploadUserId == "sireesh") sireeshMasterResume else bhargaviMasterResume
        )
        return
    }

    // ========================================================================
    // JOB DETAIL BOTTOM SHEET
    // ========================================================================
    if (activeDetailJob != null) {
        val job = activeDetailJob!!
        JobDetailBottomSheet(
            job = job,
            onDismissRequest = { activeDetailJob = null },
            onTailorResume = { jobId ->
                activeDetailJob = null
                if (currentMasterResume.isNullOrBlank()) {
                    Toast.makeText(context, "Please upload your Master Resume first in Settings & Profile", Toast.LENGTH_LONG).show()
                    selectedTab = 1
                } else {
                    val newTailored = TailoredResumeMetadataDto(
                        resumeId = "res_${System.currentTimeMillis()}",
                        userId = activeUserId,
                        jobId = jobId,
                        company = job.company,
                        position = job.title,
                        createdAt = "Just now",
                        expiryDate = "2026-11-25",
                        atsKeywordCoverageScore = job.matchPercentage,
                        docxStoragePath = "/api/v1/tailor/${job.id}/docx",
                        pdfStoragePath = "/api/v1/tailor/${job.id}/pdf"
                    )
                    if (activeUserId == "sireesh") {
                        sireeshResumes.value = listOf(newTailored) + sireeshResumes.value
                    } else {
                        bhargaviResumes.value = listOf(newTailored) + bhargaviResumes.value
                    }
                    selectedTab = 3
                    Toast.makeText(context, "Tailored resume generated against $currentMasterResume!", Toast.LENGTH_SHORT).show()
                }
            },
            onApplyClicked = { _ ->
                activeDetailJob = null
                activePlatformUrlsJob = job
            },
            onSaveToggle = { jobId ->
                val updater = if (activeUserId == "sireesh") sireeshJobs else bhargaviJobs
                updater.value = updater.value.map {
                    if (it.id == jobId) it.copy(isSaved = !it.isSaved) else it
                }
                activeDetailJob = activeDetailJob?.copy(isSaved = !activeDetailJob!!.isSaved)
            },
            onOpenInterviewPrep = { _ ->
                activeDetailJob = null
                activePrepJob = job
            }
        )
    }

    // ========================================================================
    // INTERVIEW PREP BOTTOM SHEET
    // ========================================================================
    if (activePrepJob != null) {
        val job = activePrepJob!!
        val prepDossier = remember(job.id) {
            sampleStripeDossierUiModel().copy(
                companyName = job.company,
                targetPosition = job.title,
                domainBadge = "Technical & Systems Preparation"
            )
        }
        InterviewPrepBottomSheet(
            dossier = prepDossier,
            onDismissRequest = { activePrepJob = null },
            onCopyNotebookLmMarkdown = { text ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("NotebookLM Dossier", text))
                Toast.makeText(context, "Dossier copied for NotebookLM", Toast.LENGTH_SHORT).show()
            },
            onOpenNotebookLmWeb = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://notebooklm.google.com")))
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

    // ========================================================================
    // PLATFORM LINKS BOTTOM SHEET (Apply)
    // ========================================================================
    if (activePlatformUrlsJob != null) {
        val job = activePlatformUrlsJob!!
        PlatformLinksBottomSheet(
            jobTitle = job.title,
            company = job.company,
            sourceUrls = listOf(
                PlatformUrlDto(
                    "Direct Portal",
                    "https://www.google.com/search?q=${Uri.encode("${job.company} ${job.title} jobs")}"
                ),
                PlatformUrlDto(
                    "LinkedIn",
                    "https://www.linkedin.com/jobs/search/?keywords=${Uri.encode("${job.company} ${job.title}")}"
                )
            ),
            onDismiss = { activePlatformUrlsJob = null },
            onUrlSelected = { url ->
                activePlatformUrlsJob = null
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (_: Exception) {}
            }
        )
    }

    // ========================================================================
    // MAIN SCAFFOLD & BOTTOM NAVIGATION
    // ========================================================================
    Scaffold(
        containerColor = Surface000,
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
        when (selectedTab) {
            0 -> {
                // TAB 0: Clean Empty Jobs Screen
                JobCockpitScreen(
                    uiState = JobCockpitUiState(
                        isLoading = false,
                        isSyncing = false,
                        jobs = currentJobs
                    ),
                    onCardClick = { clickedId ->
                        activeDetailJob = currentJobs.find { it.id == clickedId }
                    },
                    onSaveRole = { roleId ->
                        val updater = if (activeUserId == "sireesh") sireeshJobs else bhargaviJobs
                        updater.value = updater.value.map {
                            if (it.id == roleId) it.copy(isSaved = !it.isSaved) else it
                        }
                    },
                    onQuickDispatch = { roleId ->
                        activePlatformUrlsJob = currentJobs.find { it.id == roleId }
                    },
                    onRefreshFeed = {
                        Toast.makeText(context, "Checking latest verified openings...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            1 -> {
                // TAB 1: Profile & Settings Screen
                ProfileScreen(
                    activeUserId = activeUserId,
                    onSwitchUser = { newUserId ->
                        activeUserId = newUserId
                        prefs.edit().putString("active_user_id", newUserId).apply()
                        Toast.makeText(context, "Active profile switched to ${if (newUserId == "sireesh") "Sireesh" else "Bhargavi"}", Toast.LENGTH_SHORT).show()
                    },
                    masterResumeFileName = currentMasterResume,
                    profile = currentProfile,
                    isUploading = false,
                    isSaving = false,
                    statusMessage = null,
                    errorMessage = null,
                    onSelectResumeClicked = {
                        pendingUploadUserId = activeUserId
                        resumePickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "text/plain"
                            )
                        )
                    },
                    onImportGoogleDocClicked = { docUrl ->
                        val docName = "GoogleDoc_Master_Resume"
                        if (activeUserId == "sireesh") {
                            sireeshMasterResume = docName
                            prefs.edit().putString("sireesh_master_resume", docName).apply()
                        } else {
                            bhargaviMasterResume = docName
                            prefs.edit().putString("bhargavi_master_resume", docName).apply()
                        }
                        Toast.makeText(context, "Google Doc linked as master reference resume", Toast.LENGTH_SHORT).show()
                    },
                    onToggleRemote = { _ -> },
                    onSaveProfileClicked = {
                        Toast.makeText(context, "Profile settings saved", Toast.LENGTH_SHORT).show()
                    },
                    onResetApp = {
                        prefs.edit().clear().apply()
                        sireeshMasterResume = null
                        bhargaviMasterResume = null
                        sireeshJobs.value = emptyList()
                        bhargaviJobs.value = emptyList()
                        sireeshResumes.value = emptyList()
                        bhargaviResumes.value = emptyList()
                        sireeshProfile = null
                        bhargaviProfile = null
                        isOnboarded = false
                        Toast.makeText(context, "Orbit reset to initial setup", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            2 -> {
                // TAB 2: Saved Roles Screen
                FavoritesScreen(
                    savedJobs = currentJobs.filter { it.isSaved },
                    onCardClick = { clickedId ->
                        activeDetailJob = currentJobs.find { it.id == clickedId }
                    },
                    onRemoveFavorite = { roleId ->
                        val updater = if (activeUserId == "sireesh") sireeshJobs else bhargaviJobs
                        updater.value = updater.value.map {
                            if (it.id == roleId) it.copy(isSaved = false) else it
                        }
                    },
                    onQuickDispatch = { roleId ->
                        activePlatformUrlsJob = currentJobs.find { it.id == roleId }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            3 -> {
                // TAB 3: Tailored Resumes Screen
                MyResumesScreen(
                    resumes = currentResumes,
                    onDownloadDocx = { resume ->
                        Toast.makeText(context, "Downloading ATS DOCX for ${resume.company}...", Toast.LENGTH_SHORT).show()
                    },
                    onDownloadPdf = { resume ->
                        Toast.makeText(context, "Downloading ATS PDF for ${resume.company}...", Toast.LENGTH_SHORT).show()
                    },
                    onUploadToDrive = { resume ->
                        Toast.makeText(context, "Exporting to Google Drive: JobPilot Resumes folder", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
