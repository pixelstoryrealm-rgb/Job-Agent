package com.example.data.repository

import com.example.data.engine.MatchScoringEngine
import com.example.data.firebase.FirebaseManager
import com.example.data.local.SavedJobDao
import com.example.data.local.SavedJobEntity
import com.example.data.model.Job
import com.example.data.model.JobSourceConnector
import com.example.data.model.SourceHealth
import com.example.data.source.JobSearchFilters
import com.example.data.source.JobSourceRegistry
import com.example.data.source.OperatingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

interface JobRepository {
    suspend fun getAllJobs(): Result<List<Job>>
    suspend fun fetchJobsPaged(page: Int = 1, pageSize: Int = 20): Result<List<Job>>
    suspend fun getJobById(jobId: String): Result<Job>
    suspend fun searchJobs(query: String, filters: JobSearchFilters = JobSearchFilters()): Result<List<Job>>
    suspend fun searchJobsPaged(query: String, filters: JobSearchFilters, page: Int = 1, pageSize: Int = 20): Result<List<Job>>
    fun getSavedJobIds(): Flow<List<String>>
    fun isJobSaved(jobId: String): Flow<Boolean>
    suspend fun saveJob(jobId: String)
    suspend fun removeSavedJob(jobId: String)
    suspend fun getSavedJobs(): Result<List<Job>>
    fun getOperatingMode(): OperatingMode
    fun setOperatingMode(mode: OperatingMode)
    suspend fun getConnectorHealthReports(): List<Pair<JobSourceConnector, SourceHealth>>
}

class JobRepositoryImpl(
    private val jobSourceRegistry: JobSourceRegistry,
    private val savedJobDao: SavedJobDao,
    private val firebaseManager: FirebaseManager,
    private val userRepository: UserRepository,
    private val scoringEngine: MatchScoringEngine = MatchScoringEngine()
) : JobRepository {

    override fun getOperatingMode(): OperatingMode = jobSourceRegistry.activeMode

    override fun setOperatingMode(mode: OperatingMode) {
        jobSourceRegistry.setOperatingMode(mode)
    }

    override suspend fun getConnectorHealthReports(): List<Pair<JobSourceConnector, SourceHealth>> {
        return jobSourceRegistry.getConnectorHealthReports()
    }

    override suspend fun getAllJobs(): Result<List<Job>> {
        return fetchJobsPaged(page = 1, pageSize = 50)
    }

    override suspend fun fetchJobsPaged(page: Int, pageSize: Int): Result<List<Job>> {
        val rawResult = jobSourceRegistry.fetchAggregatedJobs(page, pageSize)
        return if (rawResult.isSuccess) {
            val jobs = rawResult.getOrNull() ?: emptyList()
            val profile = userRepository.userProfile.firstOrNull()
            val scoredJobs = if (profile != null) {
                jobs.map { job ->
                    val match = scoringEngine.calculateMatch(profile, job)
                    job.copy(match = match, isHighMatch = match.percentage >= 85)
                }
            } else jobs
            Result.success(scoredJobs)
        } else {
            rawResult
        }
    }

    override suspend fun getJobById(jobId: String): Result<Job> {
        val rawResult = jobSourceRegistry.getJobDetails(jobId)
        return if (rawResult.isSuccess) {
            val job = rawResult.getOrNull()!!
            val profile = userRepository.userProfile.firstOrNull()
            val scoredJob = if (profile != null) {
                val match = scoringEngine.calculateMatch(profile, job)
                job.copy(match = match, isHighMatch = match.percentage >= 85)
            } else job
            Result.success(scoredJob)
        } else {
            rawResult
        }
    }

    override suspend fun searchJobs(query: String, filters: JobSearchFilters): Result<List<Job>> {
        return searchJobsPaged(query, filters, page = 1, pageSize = 50)
    }

    override suspend fun searchJobsPaged(query: String, filters: JobSearchFilters, page: Int, pageSize: Int): Result<List<Job>> {
        val rawResult = jobSourceRegistry.searchAggregatedJobs(query, filters, page, pageSize)
        return if (rawResult.isSuccess) {
            val jobs = rawResult.getOrNull() ?: emptyList()
            val profile = userRepository.userProfile.firstOrNull()
            val scoredJobs = if (profile != null) {
                jobs.map { job ->
                    val match = scoringEngine.calculateMatch(profile, job)
                    job.copy(match = match, isHighMatch = match.percentage >= 85)
                }
            } else jobs
            Result.success(scoredJobs)
        } else {
            rawResult
        }
    }

    override fun getSavedJobIds(): Flow<List<String>> {
        return savedJobDao.getAllSavedJobs().map { entities ->
            entities.map { it.jobId }
        }
    }

    override fun isJobSaved(jobId: String): Flow<Boolean> {
        return savedJobDao.isJobSaved(jobId)
    }

    override suspend fun saveJob(jobId: String) {
        savedJobDao.saveJob(SavedJobEntity(jobId = jobId))

        val firestore = firebaseManager.firestore
        val auth = firebaseManager.auth
        val uid = auth?.currentUser?.uid
        if (firestore != null && uid != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("savedJobs").document(jobId)
                    .set(
                        mapOf(
                            "jobId" to jobId,
                            "savedAt" to System.currentTimeMillis()
                        )
                    ).await()
            } catch (_: Exception) {}
        }
    }

    override suspend fun removeSavedJob(jobId: String) {
        savedJobDao.removeSavedJob(jobId)

        val firestore = firebaseManager.firestore
        val auth = firebaseManager.auth
        val uid = auth?.currentUser?.uid
        if (firestore != null && uid != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("savedJobs").document(jobId)
                    .delete().await()
            } catch (_: Exception) {}
        }
    }

    override suspend fun getSavedJobs(): Result<List<Job>> {
        val allJobsRes = getAllJobs()
        return if (allJobsRes.isSuccess) {
            val allJobs = allJobsRes.getOrNull() ?: emptyList()
            val jobs = allJobs.filter { job ->
                savedJobDao.isJobSavedOnce(job.id)
            }
            Result.success(jobs)
        } else {
            allJobsRes
        }
    }
}
