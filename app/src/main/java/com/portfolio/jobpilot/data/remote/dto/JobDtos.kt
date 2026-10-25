package com.portfolio.jobpilot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlatformUrlDto(
    @SerializedName("platform") val platform: String,
    @SerializedName("url") val url: String
)

data class JobPostingDto(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("title") val title: String,
    @SerializedName("company") val company: String,
    @SerializedName("location") val location: String,
    @SerializedName("primary_platform") val primaryPlatform: String,
    @SerializedName("apply_url") val applyUrl: String,
    @SerializedName("source_urls") val sourceUrls: List<PlatformUrlDto> = emptyList(),
    @SerializedName("posted_date") val postedDate: String,
    @SerializedName("expiry_date") val expiryDate: String,
    @SerializedName("is_estimated_expiry") val isEstimatedExpiry: Boolean = true,
    @SerializedName("description") val description: String = "",
    @SerializedName("skills_required") val skillsRequired: List<String> = emptyList(),
    @SerializedName("is_favorite") val isFavorite: Boolean = false
)

data class RankedJobDto(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("title") val title: String,
    @SerializedName("company") val company: String,
    @SerializedName("location") val location: String,
    @SerializedName("primary_platform") val primaryPlatform: String,
    @SerializedName("apply_url") val applyUrl: String,
    @SerializedName("source_urls") val sourceUrls: List<PlatformUrlDto> = emptyList(),
    @SerializedName("posted_date") val postedDate: String,
    @SerializedName("expiry_date") val expiryDate: String,
    @SerializedName("is_estimated_expiry") val isEstimatedExpiry: Boolean = true,
    @SerializedName("description") val description: String = "",
    @SerializedName("skills_required") val skillsRequired: List<String> = emptyList(),
    @SerializedName("match_percentage") val matchPercentage: Int,
    @SerializedName("why_it_matches") val whyItMatches: String,
    @SerializedName("is_favorite") val isFavorite: Boolean = false
)

data class TailoredResumeMetadataDto(
    @SerializedName("resume_id") val resumeId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("job_id") val jobId: String,
    @SerializedName("company") val company: String,
    @SerializedName("position") val position: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("expiry_date") val expiryDate: String,
    @SerializedName("ats_keyword_coverage_score") val atsKeywordCoverageScore: Int,
    @SerializedName("docx_storage_path") val docxStoragePath: String? = null,
    @SerializedName("pdf_storage_path") val pdfStoragePath: String? = null,
    @SerializedName("google_drive_file_id") val googleDriveFileId: String? = null
)
