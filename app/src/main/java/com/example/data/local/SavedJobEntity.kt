package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_jobs")
data class SavedJobEntity(
    @PrimaryKey
    val jobId: String,
    val savedAtTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
