package com.portfolio.jobpilot

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.portfolio.jobpilot.data.remote.dto.SkillsGroupDto
import com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.data.remote.dto.WorkExperienceDto
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
        setContent {
            ProvideAdaptiveDeviceTier {
                JobPilotTheme {
                    OrbitApp()
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun OrbitApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("orbit_prefs", Context.MODE_PRIVATE) }

    // Persistent active user state (default: "sireesh", onboarding checked via pref)
    var activeUserId by rememberSaveable {
        mutableStateOf(prefs.getString("active_user_id", "sireesh") ?: "sireesh")
    }
    var isOnboarded by rememberSaveable {
        mutableStateOf(prefs.getBoolean("is_onboarded", true))
    }

    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // Master Resume filenames per user
    var sireeshMasterResume by rememberSaveable {
        mutableStateOf("Sireesh_Staff_Backend_Systems_Resume.pdf")
    }
    var bhargaviMasterResume by rememberSaveable {
        mutableStateOf("Bhargavi_Software_Engineer_Resume.pdf")
    }

    // Grounded Profiles
    val sireeshProfile = remember {
        UserProfileDto(
            userId = "sireesh",
            targetRoles = listOf("Staff Backend Engineer", "Distributed Systems Lead"),
            totalYearsExperience = 7.5,
            preferredCities = listOf("Hyderabad (Hybrid/Office)", "Remote (India)"),
            isRemotePreferred = true,
            skills = SkillsGroupDto(
                technical = listOf("Go", "Kafka", "Distributed Systems", "GCP", "Python", "Kubernetes", "PostgreSQL", "Kotlin"),
                soft = listOf("System Architecture", "Cross-functional Leadership"),
                toolsAndPlatforms = listOf("Docker", "Linux", "Terraform", "Git")
            ),
            workHistory = listOf(
                WorkExperienceDto(
                    title = "Staff Backend Engineer",
                    company = "High-Scale FinTech",
                    startDate = "2022",
                    endDate = "Present",
                    bulletPoints = listOf(
                        "Architected distributed transaction ledger handling 100K+ RPS with sub-15ms p99 latency in Go.",
                        "Engineered zero-data-loss Kafka event pipeline with strict ordering and idempotency guarantees."
                    )
                )
            )
        )
    }

    val bhargaviProfile = remember {
        UserProfileDto(
            userId = "bhargavi",
            targetRoles = listOf("Software Engineer", "Full Stack Developer"),
            totalYearsExperience = 3.5,
            preferredCities = listOf("Hyderabad", "Remote (India)"),
            isRemotePreferred = true,
            skills = SkillsGroupDto(
                technical = listOf("React", "TypeScript", "Python", "REST APIs", "Java", "Spring Boot", "SQL", "AWS"),
                soft = listOf("Product Thinking", "Agile Collaboration"),
                toolsAndPlatforms = listOf("Git", "Docker", "Node.js", "Jira")
            ),
            workHistory = listOf(
                WorkExperienceDto(
                    title = "Software Engineer",
                    company = "Enterprise Cloud Platforms",
                    startDate = "2023",
                    endDate = "Present",
                    bulletPoints = listOf(
                        "Built customer-facing React & TypeScript micro-frontends serving 500K+ monthly active users.",
                        "Designed robust RESTful microservices in Spring Boot and Python with automated CI/CD."
                    )
                )
            )
        )
    }

    // Distinct Jobs per user (Strictly Isolated)
    val sireeshJobs = remember {
        mutableStateOf(
            listOf(
                JobCardUiModel(
                    id = "s_job_1",
                    title = "Staff Backend Engineer — Cloud & Distributed Systems",
                    company = "Microsoft",
                    locationMode = "Hyderabad (Hybrid)",
                    compensationRange = "₹50L – ₹70L",
                    matchPercentage = 96,
                    postedAgo = "5m ago",
                    assigneeTag = "Sireesh",
                    description = "Lead architecture for next-gen Azure distributed messaging and high-scale event backbones in Go and modern systems languages.",
                    skillsRequired = listOf("Go", "Kafka", "Distributed Systems", "Cloud Platforms"),
                    whyItMatches = "1:1 match with your 7.5+ years building distributed event backbones in Go and Kafka. Hyderabad campus office access matches your location policy.",
                    expiryDaysRemaining = 21,
                    isSaved = true
                ),
                JobCardUiModel(
                    id = "s_job_2",
                    title = "Staff Backend Engineer — Core Payments Engine",
                    company = "Stripe",
                    locationMode = "Remote (India)",
                    compensationRange = "₹45L – ₹65L",
                    matchPercentage = 94,
                    postedAgo = "18m ago",
                    assigneeTag = "Sireesh",
                    description = "Build high-throughput idempotent financial ledger pipelines handling multi-billion dollar payment streams with 99.999% availability.",
                    skillsRequired = listOf("Distributed Systems", "Go", "Kafka", "PostgreSQL"),
                    whyItMatches = "100% Remote flexibility. Core ledger requirements match your high-scale transaction processing history.",
                    expiryDaysRemaining = 14,
                    isSaved = false
                ),
                JobCardUiModel(
                    id = "s_job_3",
                    title = "Senior Software Engineer — Platform Infrastructure",
                    company = "Uber",
                    locationMode = "Hyderabad (Office)",
                    compensationRange = "₹48L – ₹62L",
                    matchPercentage = 92,
                    postedAgo = "35m ago",
                    assigneeTag = "Sireesh",
                    description = "Scale global dispatch and real-time routing mesh across multi-region hybrid Kubernetes clusters.",
                    skillsRequired = listOf("Kubernetes", "Go", "Distributed Systems", "gRPC"),
                    whyItMatches = "Hyderabad physical office match. Direct alignment with container orchestration and low-latency microservices.",
                    expiryDaysRemaining = 18,
                    isSaved = false
                ),
                JobCardUiModel(
                    id = "s_job_4",
                    title = "Senior Software Engineer — Distributed Cloud",
                    company = "Google",
                    locationMode = "Hyderabad (Hybrid)",
                    compensationRange = "₹55L – ₹75L",
                    matchPercentage = 91,
                    postedAgo = "1h ago",
                    assigneeTag = "Sireesh",
                    description = "Design planetary-scale storage and compute orchestration services with zero-downtime rolling upgrades.",
                    skillsRequired = listOf("Distributed Systems", "Go", "GCP", "Linux"),
                    whyItMatches = "Hyderabad campus hybrid. GCP and distributed systems experience maps 1:1.",
                    expiryDaysRemaining = 25,
                    isSaved = false
                ),
                JobCardUiModel(
                    id = "s_job_5",
                    title = "Staff Infrastructure Engineer",
                    company = "Atlassian",
                    locationMode = "Remote (India)",
                    compensationRange = "₹52L – ₹68L",
                    matchPercentage = 89,
                    postedAgo = "2h ago",
                    assigneeTag = "Sireesh",
                    description = "Modernize developer productivity infrastructure and distributed event pipelines across Jira and Confluence backends.",
                    skillsRequired = listOf("Kafka", "Kubernetes", "Kotlin", "Go"),
                    whyItMatches = "100% Remote flexibility. Matches high-scale event streaming background.",
                    expiryDaysRemaining = 20,
                    isSaved = false
                )
            )
        )
    }

    val bhargaviJobs = remember {
        mutableStateOf(
            listOf(
                JobCardUiModel(
                    id = "b_job_1",
                    title = "Software Engineer — Core Platform & APIs",
                    company = "Salesforce",
                    locationMode = "Hyderabad (Hybrid)",
                    compensationRange = "₹22L – ₹32L",
                    matchPercentage = 95,
                    postedAgo = "8m ago",
                    assigneeTag = "Bhargavi",
                    description = "Develop customer 360 platform extensions, reusable UI modules, and resilient backend REST microservices.",
                    skillsRequired = listOf("React", "TypeScript", "Java", "REST APIs"),
                    whyItMatches = "Matches your 3.5 years of React, TypeScript, and microservice integration. Hyderabad location fully eligible.",
                    expiryDaysRemaining = 21,
                    isSaved = true
                ),
                JobCardUiModel(
                    id = "b_job_2",
                    title = "Full Stack Software Engineer",
                    company = "ServiceNow",
                    locationMode = "Hyderabad (Office)",
                    compensationRange = "₹20L – ₹28L",
                    matchPercentage = 93,
                    postedAgo = "25m ago",
                    assigneeTag = "Bhargavi",
                    description = "Engineer enterprise workflow portals and real-time dashboard components using modern web frameworks and cloud APIs.",
                    skillsRequired = listOf("React", "Python", "SQL", "REST APIs"),
                    whyItMatches = "Direct match for React dashboard experience and Python backend integration.",
                    expiryDaysRemaining = 16,
                    isSaved = false
                ),
                JobCardUiModel(
                    id = "b_job_3",
                    title = "Software Engineer II — Platform Experience",
                    company = "Intuit",
                    locationMode = "Remote (India)",
                    compensationRange = "₹24L – ₹34L",
                    matchPercentage = 92,
                    postedAgo = "45m ago",
                    assigneeTag = "Bhargavi",
                    description = "Build intuitive financial workflow applications for millions of small businesses with accessibility and performance.",
                    skillsRequired = listOf("TypeScript", "React", "Node.js", "AWS"),
                    whyItMatches = "100% Remote eligibility. Direct alignment with your component architecture and API integration background.",
                    expiryDaysRemaining = 20,
                    isSaved = false
                ),
                JobCardUiModel(
                    id = "b_job_4",
                    title = "Software Engineer — Payments UX & Integrations",
                    company = "PayPal",
                    locationMode = "Remote (India)",
                    compensationRange = "₹22L – ₹30L",
                    matchPercentage = 90,
                    postedAgo = "1h ago",
                    assigneeTag = "Bhargavi",
                    description = "Collaborate with international teams to deliver secure, responsive payment checkout interfaces.",
                    skillsRequired = listOf("React", "TypeScript", "REST APIs", "SQL"),
                    whyItMatches = "100% Remote. Solid fit for modern frontend and clean API contracts.",
                    expiryDaysRemaining = 14,
                    isSaved = false
                ),
                JobCardUiModel(
                    id = "b_job_5",
                    title = "Applications Developer",
                    company = "Oracle",
                    locationMode = "Hyderabad (Hybrid)",
                    compensationRange = "₹18L – ₹26L",
                    matchPercentage = 88,
                    postedAgo = "2h ago",
                    assigneeTag = "Bhargavi",
                    description = "Implement next-generation enterprise cloud application features and relational database schemas.",
                    skillsRequired = listOf("Java", "SQL", "REST APIs", "React"),
                    whyItMatches = "Hyderabad location match. Solid grounding in full stack enterprise architectures.",
                    expiryDaysRemaining = 28,
                    isSaved = false
                )
            )
        )
    }

    // Resumes per user (Tailored strictly against their own master resume)
    val sireeshResumes = remember {
        mutableStateOf(
            listOf(
                TailoredResumeMetadataDto(
                    resumeId = "res_s_1",
                    userId = "sireesh",
                    jobId = "s_job_2",
                    company = "Stripe",
                    position = "Staff Backend Engineer — Core Payments Engine",
                    createdAt = "Today, 14:30",
                    expiryDate = "2026-11-24",
                    atsKeywordCoverageScore = 96,
                    docxStoragePath = "/api/v1/tailor/res_s_1/docx",
                    pdfStoragePath = "/api/v1/tailor/res_s_1/pdf"
                ),
                TailoredResumeMetadataDto(
                    resumeId = "res_s_2",
                    userId = "sireesh",
                    jobId = "s_job_1",
                    company = "Microsoft",
                    position = "Staff Backend Engineer — Cloud & Distributed Systems",
                    createdAt = "Yesterday",
                    expiryDate = "2026-11-23",
                    atsKeywordCoverageScore = 94,
                    docxStoragePath = "/api/v1/tailor/res_s_2/docx",
                    pdfStoragePath = "/api/v1/tailor/res_s_2/pdf"
                )
            )
        )
    }

    val bhargaviResumes = remember {
        mutableStateOf(
            listOf(
                TailoredResumeMetadataDto(
                    resumeId = "res_b_1",
                    userId = "bhargavi",
                    jobId = "b_job_1",
                    company = "Salesforce",
                    position = "Software Engineer — Core Platform & APIs",
                    createdAt = "Today, 11:15",
                    expiryDate = "2026-11-24",
                    atsKeywordCoverageScore = 95,
                    docxStoragePath = "/api/v1/tailor/res_b_1/docx",
                    pdfStoragePath = "/api/v1/tailor/res_b_1/pdf"
                )
            )
        )
    }

    // Modal state
    var activeDetailJob by remember { mutableStateOf<JobCardUiModel?>(null) }
    var activePrepJob by remember { mutableStateOf<JobCardUiModel?>(null) }
    var activePlatformUrlsJob by remember { mutableStateOf<JobCardUiModel?>(null) }

    // File picker launcher for Master Resume
    val resumePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Master_Resume_Updated.pdf"
            if (activeUserId == "sireesh") {
                sireeshMasterResume = fileName
            } else {
                bhargaviMasterResume = fileName
            }
            Toast.makeText(context, "Original Master Resume updated: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // Active dataset pointers
    val currentJobs = if (activeUserId == "sireesh") sireeshJobs.value else bhargaviJobs.value
    val currentProfile = if (activeUserId == "sireesh") sireeshProfile else bhargaviProfile
    val currentMasterResume = if (activeUserId == "sireesh") sireeshMasterResume else bhargaviMasterResume
    val currentResumes = if (activeUserId == "sireesh") sireeshResumes.value else bhargaviResumes.value

    // ========================================================================
    // FIRST-LAUNCH ONBOARDING SCREEN
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
            },
            onUploadResumeClicked = {
                resumePickerLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "text/plain"
                    )
                )
            }
        )
        return
    }

    // ========================================================================
    // JOB DETAIL BOTTOM SHEET (With prominent Tailor Resume CTA)
    // ========================================================================
    if (activeDetailJob != null) {
        val job = activeDetailJob!!
        JobDetailBottomSheet(
            job = job,
            onDismissRequest = { activeDetailJob = null },
            onTailorResume = { jobId ->
                activeDetailJob = null
                // Tailor using the active user's master resume
                val newTailored = TailoredResumeMetadataDto(
                    resumeId = "res_${System.currentTimeMillis()}",
                    userId = activeUserId,
                    jobId = jobId,
                    company = job.company,
                    position = job.title,
                    createdAt = "Just now",
                    expiryDate = "2026-11-24",
                    atsKeywordCoverageScore = job.matchPercentage,
                    docxStoragePath = "/api/v1/tailor/${job.id}/docx",
                    pdfStoragePath = "/api/v1/tailor/${job.id}/pdf"
                )
                if (activeUserId == "sireesh") {
                    sireeshResumes.value = listOf(newTailored) + sireeshResumes.value
                } else {
                    bhargaviResumes.value = listOf(newTailored) + bhargaviResumes.value
                }
                selectedTab = 3 // Switch to Resumes tab
                Toast.makeText(context, "Tailored resume generated for ${job.company}!", Toast.LENGTH_SHORT).show()
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
                domainBadge = if (job.company.contains("Microsoft", ignoreCase = true) || job.company.contains("Google", ignoreCase = true)) {
                    "Enterprise Cloud & Distributed Systems"
                } else {
                    "High-Throughput Web & Payments Infrastructure"
                }
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
                com.portfolio.jobpilot.data.remote.dto.PlatformUrlDto(
                    "Direct Portal",
                    "https://www.google.com/search?q=${Uri.encode("${job.company} ${job.title} jobs")}"
                ),
                com.portfolio.jobpilot.data.remote.dto.PlatformUrlDto(
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
                // TAB 0: Clean Jobs Screen
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
                        resumePickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "text/plain"
                            )
                        )
                    },
                    onImportGoogleDocClicked = { docUrl ->
                        if (activeUserId == "sireesh") {
                            sireeshMasterResume = "Sireesh_GoogleDoc_Master_Resume"
                        } else {
                            bhargaviMasterResume = "Bhargavi_GoogleDoc_Master_Resume"
                        }
                        Toast.makeText(context, "Google Doc imported as master reference resume", Toast.LENGTH_SHORT).show()
                    },
                    onToggleRemote = { _ -> },
                    onSaveProfileClicked = {
                        Toast.makeText(context, "Profile settings saved", Toast.LENGTH_SHORT).show()
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
