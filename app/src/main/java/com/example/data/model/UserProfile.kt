package com.example.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val avatarUrl: String? = null,
    val token: String? = null
)

data class Education(
    val id: String,
    val degree: String,
    val institution: String,
    val fieldOfStudy: String,
    val passingYear: String,
    val grade: String
)

data class Experience(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val isCurrent: Boolean = false,
    val description: String = ""
)

data class Skill(
    val id: String,
    val name: String,
    val level: String = "Intermediate", // Beginner, Intermediate, Expert
    val years: Int = 1
)

data class Certification(
    val id: String,
    val title: String,
    val organization: String,
    val issueDate: String,
    val credentialId: String? = null
)

data class JobPreference(
    val preferredTitles: List<String> = emptyList(),
    val preferredLocations: List<String> = listOf("Dhaka", "Chattogram"),
    val expectedSalaryMin: Long = 25000,
    val expectedSalaryMax: Long = 35000,
    val preferredJobTypes: List<EmploymentType> = listOf(EmploymentType.FULL_TIME),
    val preferredSectors: List<JobSector> = listOf(JobSector.PRIVATE),
    val preferredShift: String = "Day Shift", // "Day Shift", "Flexible", "No Night Shift"
    val openToRelocation: Boolean = false
)

data class CV(
    val id: String,
    val fileName: String,
    val fileSizeFormatted: String,
    val uploadDate: String,
    val fileType: String = "PDF", // PDF, DOCX
    val uriString: String? = null,
    val status: CvProcessingStatus = CvProcessingStatus.PROCESSED,
    val summary: String? = null,
    val extractedSkills: List<String> = emptyList()
)

data class UserProfile(
    val userId: String,
    val fullName: String,
    val title: String, // e.g. "Junior Electrical & Maintenance Engineer"
    val email: String,
    val phone: String,
    val location: String,
    val bio: String,
    val educations: List<Education> = emptyList(),
    val experiences: List<Experience> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val certifications: List<Certification> = emptyList(),
    val preference: JobPreference = JobPreference(),
    val cv: CV? = null
) {
    val completionPercentage: Int
        get() {
            var score = 0
            if (fullName.isNotBlank() && email.isNotBlank()) score += 20
            if (phone.isNotBlank() && location.isNotBlank()) score += 10
            if (educations.isNotEmpty()) score += 20
            if (skills.isNotEmpty()) score += 20
            if (experiences.isNotEmpty()) score += 15
            if (cv != null) score += 15
            return score.coerceIn(0, 100)
        }
}
