package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedJobDao {
    @Query("SELECT * FROM saved_jobs ORDER BY savedAtTimestamp DESC")
    fun getAllSavedJobs(): Flow<List<SavedJobEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_jobs WHERE jobId = :jobId)")
    fun isJobSaved(jobId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_jobs WHERE jobId = :jobId)")
    suspend fun isJobSavedOnce(jobId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveJob(job: SavedJobEntity)

    @Query("DELETE FROM saved_jobs WHERE jobId = :jobId")
    suspend fun removeSavedJob(jobId: String)

    @Query("DELETE FROM saved_jobs")
    suspend fun clearAll()
}
