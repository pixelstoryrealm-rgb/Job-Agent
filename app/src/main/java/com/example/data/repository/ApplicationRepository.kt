package com.example.data.repository

import com.example.data.firebase.FirebaseManager
import com.example.data.local.ApplicationDao
import com.example.data.local.ApplicationEntity
import com.example.data.model.Application
import com.example.data.model.ApplicationStatus
import com.example.data.model.ApplicationSummary
import com.example.data.model.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface ApplicationRepository {
    fun getAllApplications(): Flow<List<Application>>
    fun getApplicationsByStatus(status: ApplicationStatus): Flow<List<Application>>
    fun getApplicationSummary(): Flow<ApplicationSummary>
    suspend fun applyToJob(job: Job, notes: String = ""): Result<Application>
    suspend fun recordApplicationStarted(job: Job): Result<Application>
    suspend fun updateStatus(applicationId: String, newStatus: ApplicationStatus, notes: String? = null)
    suspend fun deleteApplication(applicationId: String)
    suspend fun hasApplied(jobId: String): Boolean
}

class ApplicationRepositoryImpl(
    private val applicationDao: ApplicationDao,
    private val firebaseManager: FirebaseManager
) : ApplicationRepository {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun getAllApplications(): Flow<List<Application>> {
        return applicationDao.getAllApplications().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getApplicationsByStatus(status: ApplicationStatus): Flow<List<Application>> {
        return applicationDao.getApplicationsByStatus(status.name).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getApplicationSummary(): Flow<ApplicationSummary> {
        return applicationDao.getAllApplications().map { list ->
            var appStarted = 0
            var applied = 0
            var shortlisted = 0
            var interview = 0
            var selected = 0
            var rejected = 0
            var withdrawn = 0

            for (entity in list) {
                when (entity.status) {
                    ApplicationStatus.APPLICATION_STARTED.name -> appStarted++
                    ApplicationStatus.APPLIED.name -> applied++
                    ApplicationStatus.SHORTLISTED.name -> shortlisted++
                    ApplicationStatus.INTERVIEW.name -> interview++
                    ApplicationStatus.SELECTED.name -> selected++
                    ApplicationStatus.REJECTED.name -> rejected++
                    ApplicationStatus.WITHDRAWN.name -> withdrawn++
                }
            }

            ApplicationSummary(
                applicationStartedCount = appStarted,
                appliedCount = applied,
                shortlistedCount = shortlisted,
                interviewCount = interview,
                selectedCount = selected,
                rejectedCount = rejected,
                withdrawnCount = withdrawn
            )
        }
    }

    override suspend fun applyToJob(job: Job, notes: String): Result<Application> {
        val existing = applicationDao.getApplicationByJobId(job.id)
        if (existing != null) {
            return Result.failure(IllegalStateException("Already tracking application for this position"))
        }

        val today = dateFormat.format(Date())
        val newApp = Application(
            id = UUID.randomUUID().toString(),
            jobId = job.id,
            jobTitle = job.title,
            companyName = job.company.name,
            location = job.location,
            salaryText = job.formattedSalary,
            appliedDate = today,
            status = ApplicationStatus.APPLIED,
            statusUpdateDate = today,
            notes = notes.ifBlank { "Application submitted with tailored AI profile" }
        )

        applicationDao.insertApplication(ApplicationEntity.fromDomain(newApp))
        syncApplicationToFirestore(newApp)

        return Result.success(newApp)
    }

    override suspend fun recordApplicationStarted(job: Job): Result<Application> {
        val existing = applicationDao.getApplicationByJobId(job.id)
        if (existing != null) {
            return Result.success(existing.toDomain())
        }

        val today = dateFormat.format(Date())
        val newApp = Application(
            id = UUID.randomUUID().toString(),
            jobId = job.id,
            jobTitle = job.title,
            companyName = job.company.name,
            location = job.location,
            salaryText = job.formattedSalary,
            appliedDate = today,
            status = ApplicationStatus.APPLIED,
            statusUpdateDate = today,
            notes = "Application started via official external link (${job.company.name})"
        )

        applicationDao.insertApplication(ApplicationEntity.fromDomain(newApp))
        syncApplicationToFirestore(newApp)

        return Result.success(newApp)
    }

    override suspend fun updateStatus(applicationId: String, newStatus: ApplicationStatus, notes: String?) {
        val today = dateFormat.format(Date())
        applicationDao.updateStatus(applicationId, newStatus.name, today)

        val firestore = firebaseManager.firestore
        val auth = firebaseManager.auth
        val uid = auth?.currentUser?.uid
        if (firestore != null && uid != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("applications").document(applicationId)
                    .update(
                        mapOf(
                            "status" to newStatus.name,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    ).await()
            } catch (_: Exception) {}
        }
    }

    override suspend fun deleteApplication(applicationId: String) {
        applicationDao.deleteApplication(applicationId)

        val firestore = firebaseManager.firestore
        val auth = firebaseManager.auth
        val uid = auth?.currentUser?.uid
        if (firestore != null && uid != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("applications").document(applicationId)
                    .delete().await()
            } catch (_: Exception) {}
        }
    }

    override suspend fun hasApplied(jobId: String): Boolean {
        return applicationDao.getApplicationByJobId(jobId) != null
    }

    private suspend fun syncApplicationToFirestore(app: Application) {
        val firestore = firebaseManager.firestore
        val auth = firebaseManager.auth
        val uid = auth?.currentUser?.uid
        if (firestore != null && uid != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("applications").document(app.id)
                    .set(
                        mapOf(
                            "applicationId" to app.id,
                            "jobId" to app.jobId,
                            "jobTitle" to app.jobTitle,
                            "company" to app.companyName,
                            "status" to app.status.name,
                            "appliedAt" to app.appliedDate,
                            "notes" to app.notes,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    ).await()
            } catch (_: Exception) {}
        }
    }
}
