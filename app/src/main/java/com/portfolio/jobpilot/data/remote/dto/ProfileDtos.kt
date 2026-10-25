package com.portfolio.jobpilot.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WorkExperienceDto(
    @SerializedName("company") val company: String,
    @SerializedName("title") val title: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("bullet_points") val bulletPoints: List<String> = emptyList(),
    @SerializedName("technologies_used") val technologiesUsed: List<String> = emptyList()
)

data class EducationDto(
    @SerializedName("institution") val institution: String,
    @SerializedName("degree") val degree: String,
    @SerializedName("year") val year: String
)

data class SkillsGroupDto(
    @SerializedName("technical") val technical: List<String> = emptyList(),
    @SerializedName("soft") val soft: List<String> = emptyList(),
    @SerializedName("tools_and_platforms") val toolsAndPlatforms: List<String> = emptyList()
)

data class UserProfileDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("target_roles") val targetRoles: List<String> = emptyList(),
    @SerializedName("total_years_experience") val totalYearsExperience: Double = 0.0,
    @SerializedName("preferred_cities") val preferredCities: List<String> = emptyList(),
    @SerializedName("is_remote_preferred") val isRemotePreferred: Boolean = true,
    @SerializedName("skills") val skills: SkillsGroupDto = SkillsGroupDto(),
    @SerializedName("domains") val domains: List<String> = emptyList(),
    @SerializedName("work_history") val workHistory: List<WorkExperienceDto> = emptyList(),
    @SerializedName("education") val education: List<EducationDto> = emptyList()
)

data class UserProfilePatchDto(
    @SerializedName("target_roles") val targetRoles: List<String>? = null,
    @SerializedName("total_years_experience") val totalYearsExperience: Double? = null,
    @SerializedName("preferred_cities") val preferredCities: List<String>? = null,
    @SerializedName("is_remote_preferred") val isRemotePreferred: Boolean? = null,
    @SerializedName("skills") val skills: SkillsGroupDto? = null,
    @SerializedName("domains") val domains: List<String>? = null,
    @SerializedName("work_history") val workHistory: List<WorkExperienceDto>? = null,
    @SerializedName("education") val education: List<EducationDto>? = null
)

