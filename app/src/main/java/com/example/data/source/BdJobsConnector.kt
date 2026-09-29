package com.example.data.source

import com.example.data.model.Job
import com.example.data.model.JobSourceConnector
import com.example.data.model.JobSourceType
import com.example.data.model.SourceConnectionStatus
import com.example.data.model.SourceHealth

class BdJobsConnector : JobSourceConnector {
    override val sourceId: String = "bdjobs_connector"
    override val sourceName: String = "BDJobs Partner Ingestion Network"
    override val sourceType: JobSourceType = JobSourceType.PARTNER_API
    override val connectionStatus: SourceConnectionStatus = SourceConnectionStatus.NOT_CONFIGURED
    override val isConfigured: Boolean = false
    override val isEnabled: Boolean = false
    override val refreshIntervalMinutes: Int = 180 // 3 hours
    override val requiredConfigurationNotice: String =
        "Requires OAuth2 partner credentials and API endpoint token from BDJobs Developer Gateway. Unauthorized scraping is strictly disabled."

    override suspend fun fetchJobs(page: Int, pageSize: Int): Result<List<Job>> {
        return Result.failure(UnsupportedOperationException(requiredConfigurationNotice))
    }

    override suspend fun searchJobs(query: String, filters: JobSearchFilters, page: Int, pageSize: Int): Result<List<Job>> {
        return Result.failure(UnsupportedOperationException(requiredConfigurationNotice))
    }

    override suspend fun getJobDetails(jobId: String): Result<Job> {
        return Result.failure(UnsupportedOperationException(requiredConfigurationNotice))
    }

    override suspend fun healthCheck(): SourceHealth {
        return SourceHealth(
            status = SourceConnectionStatus.NOT_CONFIGURED,
            message = "Unconfigured: $requiredConfigurationNotice"
        )
    }
}
