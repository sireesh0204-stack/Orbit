package com.portfolio.jobpilot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.portfolio.jobpilot.data.local.entity.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    @Query("SELECT * FROM jobs ORDER BY postedDate DESC, matchPercentage DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE isFavorite = 1 ORDER BY postedDate DESC")
    fun getFavoriteJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE jobId = :jobId LIMIT 1")
    suspend fun getJobById(jobId: String): JobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Query("UPDATE jobs SET isFavorite = :isFavorite WHERE jobId = :jobId")
    suspend fun updateFavoriteStatus(jobId: String, isFavorite: Boolean)

    @Query("DELETE FROM jobs WHERE expiryDate < :todayIsoDate AND isFavorite = 0")
    suspend fun purgeExpiredNonFavorites(todayIsoDate: String): Int

    @Query("DELETE FROM jobs WHERE expiryDate < :todayIsoDate")
    suspend fun purgeAllExpiredJobs(todayIsoDate: String): Int

    @Query("DELETE FROM jobs")
    suspend fun clearAll()
}

