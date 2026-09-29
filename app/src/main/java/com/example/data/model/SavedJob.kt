package com.example.data.model

data class SavedJob(
    val jobId: String,
    val savedAtTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
