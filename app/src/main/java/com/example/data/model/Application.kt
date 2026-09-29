package com.example.data.model

enum class ApplicationStatus(val label: String, val stepIndex: Int) {
    APPLICATION_STARTED("Application Started", 0),
    APPLIED("Applied", 1),
    SHORTLISTED("Shortlisted", 2),
    INTERVIEW("Interview", 3),
    SELECTED("Selected", 4),
    REJECTED("Rejected", -1),
    WITHDRAWN("Withdrawn", -2)
}

data class Application(
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val companyName: String,
    val location: String,
    val salaryText: String,
    val appliedDate: String,
    val status: ApplicationStatus = ApplicationStatus.APPLIED,
    val statusUpdateDate: String = appliedDate,
    val notes: String = "",
    val interviewDate: String? = null,
    val applyUrl: String? = null,
    val deadline: String? = null,
    val followUpReminderDate: String? = null
)

data class ApplicationSummary(
    val applicationStartedCount: Int = 0,
    val appliedCount: Int = 0,
    val shortlistedCount: Int = 0,
    val interviewCount: Int = 0,
    val selectedCount: Int = 0,
    val rejectedCount: Int = 0,
    val withdrawnCount: Int = 0
)
