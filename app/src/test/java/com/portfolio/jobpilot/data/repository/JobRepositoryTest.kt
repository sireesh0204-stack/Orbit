package com.portfolio.jobpilot.data.repository

import com.portfolio.jobpilot.data.local.dao.JobDao
import com.portfolio.jobpilot.data.local.entity.JobEntity
import com.portfolio.jobpilot.data.remote.JobApiService
import com.portfolio.jobpilot.data.remote.dto.JobPostingDto
import com.portfolio.jobpilot.data.remote.dto.RankedJobDto
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.data.remote.dto.UserProfilePatchDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class JobRepositoryTest {

    private lateinit var repository: JobRepository
    private lateinit var fakeDao: FakeJobDao
    private lateinit var fakeApi: FakeJobApiService

    @Before
    fun setUp() {
        fakeDao = FakeJobDao()
        fakeApi = FakeJobApiService()
        repository = JobRepository(fakeApi, fakeDao)
    }

    @Test
    fun `refreshJobs fetches ranked jobs from API and saves them to DAO`() = runBlocking {
        fakeApi.rankedJobsToReturn = listOf(
            RankedJobDto(
                jobId = "job_1",
                title = "Senior Mobile Engineer",
                company = "Airbnb",
                location = "Remote",
                primaryPlatform = "LinkedIn",
                applyUrl = "https://linkedin.com/1",
                postedDate = "2026-10-24",
                expiryDate = "2026-11-24",
                matchPercentage = 95,
                whyItMatches = "Great match"
            )
        )

        val result = repository.refreshJobs()
        assertTrue(result.isSuccess)
        assertEquals(1, fakeDao.storedJobs.size)
        assertEquals("job_1", fakeDao.storedJobs[0].jobId)
        assertEquals(95, fakeDao.storedJobs[0].matchPercentage)
    }

    @Test
    fun `refreshJobs returns failure when API returns error`() = runBlocking {
        fakeApi.shouldFail = true
        val result = repository.refreshJobs()
        assertTrue(result.isFailure)
        assertEquals(0, fakeDao.storedJobs.size)
    }

    @Test
    fun `toggleFavorite toggles favorite status in DAO`() = runBlocking {
        val result = repository.toggleFavorite("job_123", currentFavorite = false)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        assertEquals(true, fakeDao.favoriteStatuses["job_123"])
    }

    @Test
    fun `purgeExpiredJobs calls DAO to purge expired records`() = runBlocking {
        val result = repository.purgeExpiredJobs("2026-10-24")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `uploadResume uploads file bytes and returns parsed profile`() = runBlocking {
        val result = repository.uploadResume("my_resume.docx", "sample_bytes".toByteArray())
        assertTrue(result.isSuccess)
        assertEquals("uid_1", result.getOrNull()?.userId)
    }

    private class FakeJobDao : JobDao {
        val storedJobs = mutableListOf<JobEntity>()
        val favoriteStatuses = mutableMapOf<String, Boolean>()

        override fun getAllJobs(): Flow<List<JobEntity>> = flowOf(storedJobs)
        override fun getFavoriteJobs(): Flow<List<JobEntity>> = flowOf(storedJobs.filter { it.isFavorite })
        override suspend fun getJobById(jobId: String): JobEntity? = storedJobs.find { it.jobId == jobId }
        override suspend fun insertJobs(jobs: List<JobEntity>) {
            storedJobs.clear()
            storedJobs.addAll(jobs)
        }
        override suspend fun updateFavoriteStatus(jobId: String, isFavorite: Boolean) {
            favoriteStatuses[jobId] = isFavorite
        }
        override suspend fun purgeExpiredNonFavorites(todayIsoDate: String): Int = 0
        override suspend fun purgeAllExpiredJobs(todayIsoDate: String): Int = 0
        override suspend fun clearAll() { storedJobs.clear() }
    }

    private class FakeJobApiService : JobApiService {
        var rankedJobsToReturn: List<RankedJobDto> = emptyList()
        var shouldFail: Boolean = false

        override suspend fun healthCheck(): Response<Map<String, String>> =
            Response.success(mapOf("status" to "ok"))

        override suspend fun getProfile(): Response<UserProfileDto> =
            Response.success(UserProfileDto(userId = "uid_1"))

        override suspend fun patchProfile(patch: UserProfilePatchDto): Response<UserProfileDto> =
            Response.success(UserProfileDto(userId = "uid_1"))

        override suspend fun uploadResume(file: okhttp3.MultipartBody.Part): Response<UserProfileDto> =
            Response.success(UserProfileDto(userId = "uid_1"))

        override suspend fun uploadGoogleDoc(payload: Map<String, String>): Response<UserProfileDto> =
            Response.success(UserProfileDto(userId = "uid_1"))

        override suspend fun listJobs(limit: Int): Response<List<JobPostingDto>> =
            Response.success(emptyList())

        override suspend fun getRankedJobs(timeframe: String, minMatch: Int): Response<List<RankedJobDto>> {
            return if (shouldFail) {
                Response.error(500, "Internal Server Error".toResponseBody("text/plain".toMediaTypeOrNull()))
            } else {
                Response.success(rankedJobsToReturn)
            }
        }

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

