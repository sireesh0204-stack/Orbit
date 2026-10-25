package com.portfolio.jobpilot.data.repository

import com.portfolio.jobpilot.data.local.dao.JobDao
import com.portfolio.jobpilot.data.local.entity.JobEntity
import com.portfolio.jobpilot.data.remote.JobApiService
import com.portfolio.jobpilot.data.remote.dto.RankedJobDto
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.data.remote.dto.UserProfilePatchDto
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class JobRepository(
    private val apiService: JobApiService,
    private val jobDao: JobDao
) {

    fun getJobsStream(): Flow<List<JobEntity>> = jobDao.getAllJobs()

    fun getFavoritesStream(): Flow<List<JobEntity>> = jobDao.getFavoriteJobs()

    suspend fun refreshJobs(timeframe: String = "7d", minMatch: Int = 90): Result<List<JobEntity>> {
        return try {
            val response = apiService.getRankedJobs(timeframe = timeframe, minMatch = minMatch)
            if (response.isSuccessful) {
                val dtoList = response.body().orEmpty()
                val entities = dtoList.map { it.toEntity() }
                jobDao.insertJobs(entities)
                Result.success(entities)
            } else {
                Result.failure(Exception("API Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(jobId: String, currentFavorite: Boolean): Result<Boolean> {
        val nextState = !currentFavorite
        return try {
            jobDao.updateFavoriteStatus(jobId, nextState)
            Result.success(nextState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun purgeExpiredJobs(todayIsoDate: String): Result<Int> {
        return try {
            val purged = jobDao.purgeAllExpiredJobs(todayIsoDate)
            Result.success(purged)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun tailorResume(jobId: String): Result<com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto> {
        return try {
            val response = apiService.tailorResume(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Tailoring Failed ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchTailoredHistory(): Result<List<com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto>> {
        return try {
            val response = apiService.listTailoredResumes()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception("History Failed ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProfile(): Result<UserProfileDto> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun patchProfile(patch: UserProfilePatchDto): Result<UserProfileDto> {
        return try {
            val response = apiService.patchProfile(patch)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadResume(fileName: String, fileBytes: ByteArray): Result<UserProfileDto> {
        return try {
            val lower = fileName.lowercase()
            val mimeType = when {
                lower.endsWith(".pdf") -> "application/pdf"
                lower.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                lower.endsWith(".txt") -> "text/plain"
                lower.endsWith(".png") -> "image/png"
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
                else -> "application/octet-stream"
            }
            val requestBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

            val response = apiService.uploadResume(part)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Upload Failed ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadGoogleDoc(url: String): Result<UserProfileDto> {
        return try {
            val response = apiService.uploadGoogleDoc(mapOf("url" to url))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Google Docs Import Failed ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateInterviewDossier(jobId: String): Result<com.portfolio.jobpilot.data.remote.dto.InterviewPrepDossierDto> {
        return try {
            val response = apiService.generateInterviewDossier(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Interview Dossier Generation Failed ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInterviewDossier(jobId: String): Result<com.portfolio.jobpilot.data.remote.dto.InterviewPrepDossierDto> {
        return try {
            val response = apiService.getInterviewDossier(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Get Interview Dossier Failed ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportNotebookLmMarkdown(jobId: String): Result<String> {
        return try {
            val response = apiService.exportNotebookLmMarkdown(jobId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.string())
            } else {
                Result.failure(Exception("NotebookLM Export Failed ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun RankedJobDto.toEntity(): JobEntity {
        val urlsJson = com.google.gson.Gson().toJson(
            if (this.sourceUrls.isNotEmpty()) this.sourceUrls
            else listOf(com.portfolio.jobpilot.data.remote.dto.PlatformUrlDto(this.primaryPlatform, this.applyUrl))
        )
        return JobEntity(
            jobId = this.jobId,
            title = this.title,
            company = this.company,
            location = this.location,
            primaryPlatform = this.primaryPlatform,
            applyUrl = this.applyUrl,
            sourceUrlsJson = urlsJson,
            postedDate = this.postedDate,
            expiryDate = this.expiryDate,
            isEstimatedExpiry = this.isEstimatedExpiry,
            description = this.description,
            matchPercentage = this.matchPercentage,
            whyItMatches = this.whyItMatches,
            isFavorite = this.isFavorite
        )
    }
}
