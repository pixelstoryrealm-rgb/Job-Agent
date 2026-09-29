package com.example.data.source

import com.example.data.model.Job
import com.example.data.model.JobSourceConnector
import com.example.data.model.JobSourceType
import com.example.data.model.SourceConnectionStatus
import com.example.data.model.SourceHealth

class RemoteAuthorizedJobsConnector : JobSourceConnector {
    override val sourceId: String = "remote_authorized_connector"
    override val sourceName: String = "Production Aggregation Cloud (Asif AI Backend)"
    override val sourceType: JobSourceType = JobSourceType.REMOTE_AGGREGATOR
    override val connectionStatus: SourceConnectionStatus = SourceConnectionStatus.NOT_CONFIGURED
    override val isConfigured: Boolean = false
    override val isEnabled: Boolean = false
    override val refreshIntervalMinutes: Int = 60 // 1 hour
    override val requiredConfigurationNotice: String =
        "Requires deployed production backend endpoint (https://api.yourjob.asif.app/v1) with verified API authorization."

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
