package com.example.data.source

import com.example.data.model.Job
import com.example.data.model.JobSourceConnector
import com.example.data.model.JobSourceType
import com.example.data.model.SourceConnectionStatus
import com.example.data.model.SourceHealth

class CompanyCareerConnector : JobSourceConnector {
    override val sourceId: String = "company_career_connector"
    override val sourceName: String = "Direct Corporate Career Portals"
    override val sourceType: JobSourceType = JobSourceType.COMPANY_CAREER
    override val connectionStatus: SourceConnectionStatus = SourceConnectionStatus.NOT_CONFIGURED
    override val isConfigured: Boolean = false
    override val isEnabled: Boolean = false
    override val refreshIntervalMinutes: Int = 720 // 12 hours
    override val requiredConfigurationNotice: String =
        "Requires verified corporate ATS webhooks (Greenhouse/Workday/BambooHR) or public career feed authorizations."

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
