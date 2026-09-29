package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY appliedDate DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE status = :status ORDER BY appliedDate DESC")
    fun getApplicationsByStatus(status: String): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE jobId = :jobId LIMIT 1")
    suspend fun getApplicationByJobId(jobId: String): ApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)

    @Update
    suspend fun updateApplication(application: ApplicationEntity)

    @Query("UPDATE applications SET status = :newStatus, statusUpdateDate = :updateDate WHERE id = :id")
    suspend fun updateStatus(id: String, newStatus: String, updateDate: String)

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteApplication(id: String)
}
