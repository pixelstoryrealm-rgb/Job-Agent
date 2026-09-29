package com.example.data.model

data class Company(
    val id: String = "",
    val name: String = "",
    val logoUrl: String? = null,
    val website: String? = null,
    val industry: String = "Engineering & Technology",
    val location: String = "Dhaka, Bangladesh",
    val description: String = "",
    val verified: Boolean = true
) {
    val companyId: String get() = id
    val verifiedStatus: Boolean get() = verified
}

enum class EmploymentType(val label: String) {
    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    CONTRACT("Contract"),
    INTERNSHIP("Internship"),
    REMOTE("Remote")
}

enum class JobSector(val label: String) {
    PRIVATE("Private"),
    GOVERNMENT("Government"),
    MULTINATIONAL("Multinational"),
    STARTUP("Startup"),
    NGO("NGO")
}

data class JobMatch(
    val percentage: Int, // 0 - 100
    val matchedReasons: List<String> = emptyList(),
    val matchedSkills: List<String> = emptyList(),
    val missingSkills: List<String> = emptyList(),
    val matchedExperience: String = "Matches experience criteria",
    val educationMatch: String = "Relevant qualification",
    val locationMatch: String = "Preferred location",
    val salaryMatch: String = "Salary matches expectations",
    val jobTypeMatch: String = "Matches job type preference",
    val confidenceScore: Float = 0.95f
)

data class Job(
    val id: String,
    val title: String,
    val company: Company,
    val location: String,
    val salaryMin: Long,
    val salaryMax: Long,
    val currency: String = "৳",
    val employmentType: EmploymentType = EmploymentType.FULL_TIME,
    val sector: JobSector = JobSector.PRIVATE,
    val experienceRequired: String, // e.g. "1-2 Years", "Entry Level"
    val educationRequired: String, // e.g. "Diploma in EEE", "B.Sc in CSE"
    val skillsRequired: List<String>,
    val description: String,
    val responsibilities: List<String>,
    val requirements: List<String>,
    val benefits: List<String>,
    val postedDate: String,
    val deadline: String? = null, // null if deadline not specified
    val deadlineTimestamp: Long? = null, // for automatic expiration tracking
    val applicationUrl: String? = null,
    val match: JobMatch = JobMatch(85),
    val shift: String = "Day Shift", // "Day Shift", "Night Shift", "Flexible"
    val active: Boolean = true,
    val isDemo: Boolean = false, // true for controlled demo roles, false for live ingested
    val sourceName: String = "AI Job Network",
    val sourceJobId: String = id,
    val sourceUpdatedAt: Long = System.currentTimeMillis(),
    val firstSeenAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis(),
    val isNew: Boolean = false,
    val isFeatured: Boolean = false,
    val isRecentlyAdded: Boolean = false,
    val isHighMatch: Boolean = false,
    val isNearUser: Boolean = false,
    val possibleDuplicate: Boolean = false,
    val duplicateOfJobId: String? = null,
    val viewsCount: Int = 120,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val jobId: String get() = id
    val source: String get() = sourceName
    val officialApplyUrl: String? get() = applicationUrl

    val formattedSalary: String
        get() = if (salaryMin > 0 && salaryMax > 0) {
            "$currency${formatNumber(salaryMin)} – ${formatNumber(salaryMax)}"
        } else if (salaryMin > 0) {
            "$currency${formatNumber(salaryMin)}+"
        } else {
            "Negotiable"
        }

    val formattedDeadline: String
        get() = deadline ?: "Deadline not specified"

    val isExpired: Boolean
        get() = deadlineTimestamp != null && deadlineTimestamp < System.currentTimeMillis()

    private fun formatNumber(num: Long): String {
        return "%,d".format(num)
    }

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "jobId" to id,
            "title" to title,
            "companyId" to company.id,
            "companyName" to company.name,
            "location" to location,
            "salaryMin" to salaryMin,
            "salaryMax" to salaryMax,
            "currency" to currency,
            "experience" to experienceRequired,
            "education" to educationRequired,
            "skills" to skillsRequired,
            "jobType" to employmentType.label,
            "shift" to shift,
            "description" to description,
            "responsibilities" to responsibilities,
            "requirements" to requirements,
            "benefits" to benefits,
            "source" to sourceName,
            "sourceJobId" to sourceJobId,
            "officialApplyUrl" to applicationUrl,
            "postedAt" to postedDate,
            "deadline" to deadline,
            "deadlineTimestamp" to deadlineTimestamp,
            "active" to active,
            "isDemo" to isDemo,
            "isNew" to isNew,
            "possibleDuplicate" to possibleDuplicate,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}
