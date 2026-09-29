package com.example.data.model

import com.example.data.source.JobSearchFilters

enum class JobSourceType(val displayName: String) {
    LOCAL_DEMO("Verified Demo Dataset"),
    GOVERNMENT("Government Gazette & Portals"),
    COMPANY_CAREER("Direct Company Portals"),
    PARTNER_API("Authorized Partner APIs"),
    REMOTE_AGGREGATOR("Cloud Aggregation Feed")
}

enum class SourceConnectionStatus(val label: String) {
    CONNECTED("Connected"),
    NOT_CONFIGURED("Not Configured"),
    RATE_LIMITED("Rate Limited"),
    TEMPORARILY_UNAVAILABLE("Temporarily Unavailable"),
    ERROR("Error")
}

data class SourceHealth(
    val status: SourceConnectionStatus,
    val message: String,
    val latencyMs: Long = 0,
    val lastCheckTimestamp: Long = System.currentTimeMillis()
)

interface JobSourceConnector {
    val sourceId: String
    val sourceName: String
    val sourceType: JobSourceType
    val connectionStatus: SourceConnectionStatus
    val isConfigured: Boolean
    val isEnabled: Boolean
    val refreshIntervalMinutes: Int
    val requiredConfigurationNotice: String?

    suspend fun fetchJobs(page: Int = 1, pageSize: Int = 20): Result<List<Job>>
    suspend fun searchJobs(query: String, filters: JobSearchFilters, page: Int = 1, pageSize: Int = 20): Result<List<Job>>
    suspend fun getJobDetails(jobId: String): Result<Job>
    suspend fun healthCheck(): SourceHealth
}
