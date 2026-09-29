package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Application
import com.example.data.model.ApplicationStatus

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val companyName: String,
    val location: String,
    val salaryText: String,
    val appliedDate: String,
    val status: String, // String representation of ApplicationStatus
    val statusUpdateDate: String,
    val notes: String = "",
    val interviewDate: String? = null
) {
    fun toDomain(): Application {
        val appStatus = try {
            ApplicationStatus.valueOf(status)
        } catch (_: Exception) {
            ApplicationStatus.APPLIED
        }
        return Application(
            id = id,
            jobId = jobId,
            jobTitle = jobTitle,
            companyName = companyName,
            location = location,
            salaryText = salaryText,
            appliedDate = appliedDate,
            status = appStatus,
            statusUpdateDate = statusUpdateDate,
            notes = notes,
            interviewDate = interviewDate
        )
    }

    companion object {
        fun fromDomain(app: Application): ApplicationEntity {
            return ApplicationEntity(
                id = app.id,
                jobId = app.jobId,
                jobTitle = app.jobTitle,
                companyName = app.companyName,
                location = app.location,
                salaryText = app.salaryText,
                appliedDate = app.appliedDate,
                status = app.status.name,
                statusUpdateDate = app.statusUpdateDate,
                notes = app.notes,
                interviewDate = app.interviewDate
            )
        }
    }
}
