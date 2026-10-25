package com.portfolio.jobpilot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.portfolio.jobpilot.data.remote.dto.PlatformUrlDto

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val jobId: String,
    val title: String,
    val company: String,
    val location: String,
    val primaryPlatform: String,
    val applyUrl: String,
    val sourceUrlsJson: String = "[]",
    val postedDate: String,
    val expiryDate: String,
    val isEstimatedExpiry: Boolean = true,
    val description: String = "",
    val matchPercentage: Int,
    val whyItMatches: String,
    val isFavorite: Boolean = false,
    val cachedAtEpochMs: Long = System.currentTimeMillis()
) {
    fun getSourceUrls(): List<PlatformUrlDto> {
        return try {
            val type = object : TypeToken<List<PlatformUrlDto>>() {}.type
            Gson().fromJson(sourceUrlsJson, type) ?: listOf(
                PlatformUrlDto(platform = primaryPlatform, url = applyUrl)
            )
        } catch (e: Exception) {
            listOf(PlatformUrlDto(platform = primaryPlatform, url = applyUrl))
        }
    }
}
