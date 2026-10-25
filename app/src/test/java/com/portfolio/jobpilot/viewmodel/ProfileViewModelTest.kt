package com.portfolio.jobpilot.viewmodel

import com.portfolio.jobpilot.data.local.dao.JobDao
import com.portfolio.jobpilot.data.local.entity.JobEntity
import com.portfolio.jobpilot.data.remote.JobApiService
import com.portfolio.jobpilot.data.remote.dto.JobPostingDto
import com.portfolio.jobpilot.data.remote.dto.RankedJobDto
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.data.remote.dto.UserProfilePatchDto
import com.portfolio.jobpilot.data.repository.JobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProfileViewModel
    private lateinit var fakeApi: FakeJobApiService
    private lateinit var repository: JobRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeApi = FakeJobApiService()
        repository = JobRepository(fakeApi, FakeJobDao())
        viewModel = ProfileViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile fetches user profile and updates state`() = runTest {
        fakeApi.profileToReturn = UserProfileDto(
            userId = "user_456",
            targetRoles = listOf("Senior Android Engineer"),
            totalYearsExperience = 8.0
        )

        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.profile)
        assertEquals("user_456", state.profile?.userId)
        assertEquals(8.0, state.profile?.totalYearsExperience ?: 0.0, 0.01)
    }

    @Test
    fun `uploadResume parses document and updates state with parsed profile`() = runTest {
        fakeApi.profileToReturn = UserProfileDto(
            userId = "user_456",
            targetRoles = listOf("Staff Backend Engineer"),
            totalYearsExperience = 9.0
        )

        viewModel.uploadResume("resume.pdf", "dummy_bytes".toByteArray())
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isUploading)
        assertNotNull(state.profile)
        assertEquals("Staff Backend Engineer", state.profile?.targetRoles?.firstOrNull())
        assertEquals("Resume successfully structured!", state.statusMessage)
    }

    @Test
    fun `uploadGoogleDoc imports from URL and updates state`() = runTest {
        fakeApi.profileToReturn = UserProfileDto(
            userId = "user_456",
            targetRoles = listOf("Staff Distributed Engineer"),
            totalYearsExperience = 8.5
        )

        viewModel.uploadGoogleDoc("https://docs.google.com/document/d/12345/edit")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isUploading)
        assertNotNull(state.profile)
        assertEquals("Staff Distributed Engineer", state.profile?.targetRoles?.firstOrNull())
    }

    @Test
    fun `updateTargetRoles and updateRemotePreference modify state in-memory`() = runTest {
        fakeApi.profileToReturn = UserProfileDto(
            userId = "user_456",
            targetRoles = listOf("Dev"),
            isRemotePreferred = false
        )
        viewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateTargetRoles(listOf("Lead Engineer", "Architect"))
        viewModel.updateRemotePreference(true)

        val updated = viewModel.uiState.value.profile
        assertEquals(listOf("Lead Engineer", "Architect"), updated?.targetRoles)
        assertTrue(updated?.isRemotePreferred == true)
    }

    private class FakeJobDao : JobDao {
        override fun getAllJobs(): Flow<List<JobEntity>> = flowOf(emptyList())
        override fun getFavoriteJobs(): Flow<List<JobEntity>> = flowOf(emptyList())
        override suspend fun getJobById(jobId: String): JobEntity? = null
        override suspend fun insertJobs(jobs: List<JobEntity>) {}
        override suspend fun updateFavoriteStatus(jobId: String, isFavorite: Boolean) {}
        override suspend fun purgeExpiredNonFavorites(todayIsoDate: String): Int = 0
        override suspend fun purgeAllExpiredJobs(todayIsoDate: String): Int = 0
        override suspend fun clearAll() {}
    }

    private class FakeJobApiService : JobApiService {
        var profileToReturn: UserProfileDto = UserProfileDto(userId = "default")

        override suspend fun healthCheck(): Response<Map<String, String>> =
            Response.success(emptyMap())

        override suspend fun getProfile(): Response<UserProfileDto> =
            Response.success(profileToReturn)

        override suspend fun patchProfile(patch: UserProfilePatchDto): Response<UserProfileDto> =
            Response.success(profileToReturn)

        override suspend fun uploadResume(file: MultipartBody.Part): Response<UserProfileDto> =
            Response.success(profileToReturn)

        override suspend fun uploadGoogleDoc(payload: Map<String, String>): Response<UserProfileDto> =
            Response.success(profileToReturn)

        override suspend fun listJobs(limit: Int): Response<List<JobPostingDto>> =
            Response.success(emptyList())

        override suspend fun getRankedJobs(timeframe: String, minMatch: Int): Response<List<RankedJobDto>> =
            Response.success(emptyList())

        override suspend fun tailorResume(jobId: String): Response<com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto> =
            Response.success(
                com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto(
                    resumeId = "res_1",
                    userId = "u1",
                    jobId = jobId,
                    company = "Stripe",
                    position = "Staff Eng",
                    createdAt = "2026-10-24",
                    expiryDate = "2026-11-24",
                    atsKeywordCoverageScore = 95
                )
            )

        override suspend fun listTailoredResumes(): Response<List<com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto>> =
            Response.success(emptyList())

        override suspend fun generateInterviewDossier(jobId: String): Response<com.portfolio.jobpilot.data.remote.dto.InterviewPrepDossierDto> =
            Response.success(createFakeDossier(jobId))

        override suspend fun getInterviewDossier(jobId: String): Response<com.portfolio.jobpilot.data.remote.dto.InterviewPrepDossierDto> =
            Response.success(createFakeDossier(jobId))

        override suspend fun exportNotebookLmMarkdown(jobId: String): Response<okhttp3.ResponseBody> =
            Response.success(
                "# FAKE DOSSIER".toResponseBody("text/markdown".toMediaTypeOrNull())
            )

        private fun createFakeDossier(jobId: String) = com.portfolio.jobpilot.data.remote.dto.InterviewPrepDossierDto(
            id = "prep_1",
            jobId = jobId,
            candidateUserId = "u1",
            company = "Stripe",
            position = "Staff Eng",
            createdAt = "2026-10-25",
            companyIntel = com.portfolio.jobpilot.data.remote.dto.CompanyIntelligenceDto("Stripe", "Fintech", "Staff Eng"),
            systemDesign = emptyList(),
            technicalConcepts = emptyList(),
            behavioralStarStories = emptyList(),
            reverseInterviewQuestions = emptyList(),
            notebookLmDossier = com.portfolio.jobpilot.data.remote.dto.NotebookLmDossierDto("Stripe Prep", "# Markdown", "prompt")
        )
    }
}

