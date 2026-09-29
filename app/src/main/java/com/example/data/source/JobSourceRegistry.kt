package com.example.data.source

import com.example.data.engine.JobDeduplicationEngine
import com.example.data.engine.JobFreshnessManager
import com.example.data.model.Job
import com.example.data.model.JobSourceConnector
import com.example.data.model.SourceHealth

enum class OperatingMode {
    DEMO,
    PRODUCTION
}

class JobSourceRegistry(
    private val deduplicationEngine: JobDeduplicationEngine = JobDeduplicationEngine(),
    private val freshnessManager: JobFreshnessManager = JobFreshnessManager()
) {
    private val connectors = mutableListOf<JobSourceConnector>()

    var activeMode: OperatingMode = OperatingMode.DEMO
        private set

    init {
        // Register default connectors
        connectors.add(LocalDemoJobConnector())
        connectors.add(GovernmentJobsConnector())
        connectors.add(CompanyCareerConnector())
        connectors.add(BdJobsConnector())
        connectors.add(RemoteAuthorizedJobsConnector())
    }

    fun setOperatingMode(mode: OperatingMode) {
        activeMode = mode
    }

    fun getAllConnectors(): List<JobSourceConnector> = connectors

    suspend fun getConnectorHealthReports(): List<Pair<JobSourceConnector, SourceHealth>> {
        return connectors.map { connector ->
            Pair(connector, connector.healthCheck())
        }
    }

    fun getActiveConnectors(): List<JobSourceConnector> {
        return when (activeMode) {
            OperatingMode.DEMO -> connectors.filter { it.isConfigured }
            OperatingMode.PRODUCTION -> connectors.filter { !it.sourceId.contains("demo") && it.isConfigured }
        }.ifEmpty {
            // Fallback to configured demo connector if no live connectors yet active
            connectors.filter { it.isConfigured }
        }
    }

    suspend fun fetchAggregatedJobs(page: Int = 1, pageSize: Int = 20): Result<List<Job>> {
        val active = getActiveConnectors()
        val allResults = mutableListOf<Job>()

        for (connector in active) {
            val res = connector.fetchJobs(page, pageSize)
            res.getOrNull()?.let { allResults.addAll(it) }
        }

        // Apply deduplication & freshness management
        val deduplicated = deduplicationEngine.deduplicate(allResults)
        val finalized = deduplicated.map { freshnessManager.processFreshness(it) }

        return Result.success(finalized)
    }

    suspend fun searchAggregatedJobs(query: String, filters: JobSearchFilters, page: Int = 1, pageSize: Int = 20): Result<List<Job>> {
        val active = getActiveConnectors()
        val allResults = mutableListOf<Job>()

        for (connector in active) {
            val res = connector.searchJobs(query, filters, page, pageSize)
            res.getOrNull()?.let { allResults.addAll(it) }
        }

        val deduplicated = deduplicationEngine.deduplicate(allResults)
        val finalized = deduplicated.map { freshnessManager.processFreshness(it) }

        return Result.success(finalized)
    }

    suspend fun getJobDetails(jobId: String): Result<Job> {
        val active = getActiveConnectors()
        for (connector in active) {
            val res = connector.getJobDetails(jobId)
            if (res.isSuccess) {
                val job = res.getOrNull()!!
                return Result.success(freshnessManager.processFreshness(job))
            }
        }
        return Result.failure(NoSuchElementException("Job with ID $jobId not found across active connectors"))
    }
}
