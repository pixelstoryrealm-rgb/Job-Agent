package com.example.data.source

import com.example.data.model.Job
import com.example.data.model.JobSourceConnector
import com.example.data.model.JobSourceType
import com.example.data.model.SourceConnectionStatus
import com.example.data.model.SourceHealth

class GovernmentJobsConnector : JobSourceConnector {
    override val sourceId: String = "govt_jobs_connector"
    override val sourceName: String = "Government Recruitment Portals (BPSC/BPDB/DESCO)"
    override val sourceType: JobSourceType = JobSourceType.GOVERNMENT
    override val connectionStatus: SourceConnectionStatus = SourceConnectionStatus.NOT_CONFIGURED
    override val isConfigured: Boolean = false
    override val isEnabled: Boolean = false
    override val refreshIntervalMinutes: Int = 360 // 6 hours
    override val requiredConfigurationNotice: String =
        "Requires authorized API key or public e-recruitment RSS feed endpoint from Bangladesh Public Service Commission or Ministry recruiting portals."

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
