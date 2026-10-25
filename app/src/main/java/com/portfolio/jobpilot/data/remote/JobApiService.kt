package com.portfolio.jobpilot.data.remote

import com.portfolio.jobpilot.data.remote.dto.InterviewPrepDossierDto
import com.portfolio.jobpilot.data.remote.dto.JobPostingDto
import com.portfolio.jobpilot.data.remote.dto.RankedJobDto
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.data.remote.dto.UserProfilePatchDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface JobApiService {

    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>

    @GET("api/v1/profile")
    suspend fun getProfile(): Response<UserProfileDto>

    @PATCH("api/v1/profile")
    suspend fun patchProfile(@Body patch: UserProfilePatchDto): Response<UserProfileDto>

    @Multipart
    @POST("api/v1/profile/upload-resume")
    suspend fun uploadResume(@Part file: MultipartBody.Part): Response<UserProfileDto>

    @POST("api/v1/profile/upload-google-doc")
    suspend fun uploadGoogleDoc(@Body payload: Map<String, String>): Response<UserProfileDto>

    @GET("api/v1/jobs")
    suspend fun listJobs(@Query("limit") limit: Int = 10): Response<List<JobPostingDto>>

    @GET("api/v1/jobs/rank")
    suspend fun getRankedJobs(
        @Query("timeframe") timeframe: String = "7d",
        @Query("min_match") minMatch: Int = 90
    ): Response<List<RankedJobDto>>

    @POST("api/v1/tailor")
    suspend fun tailorResume(@Query("job_id") jobId: String): Response<com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto>

    @GET("api/v1/tailor/history")
    suspend fun listTailoredResumes(): Response<List<com.portfolio.jobpilot.data.remote.dto.TailoredResumeMetadataDto>>

    @POST("api/v1/interview/{job_id}/generate")
    suspend fun generateInterviewDossier(@Path("job_id") jobId: String): Response<InterviewPrepDossierDto>

    @GET("api/v1/interview/{job_id}")
    suspend fun getInterviewDossier(@Path("job_id") jobId: String): Response<InterviewPrepDossierDto>

    @GET("api/v1/interview/{job_id}/notebooklm")
    suspend fun exportNotebookLmMarkdown(@Path("job_id") jobId: String): Response<ResponseBody>
}
