package com.example.data.model

data class FirestoreUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "Dhaka, Bangladesh",
    val profilePhotoUrl: String? = null,
    val bio: String = "",
    val educations: List<Education> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val experiences: List<Experience> = emptyList(),
    val certifications: List<Certification> = emptyList(),
    val trainings: List<String> = emptyList(),
    val preferredLocations: List<String> = listOf("Dhaka"),
    val expectedSalaryMin: Long = 20000,
    val expectedSalaryMax: Long = 35000,
    val preferredJobTypes: List<String> = listOf("Full Time"),
    val preferredIndustries: List<String> = listOf("Engineering", "Manufacturing", "Power"),
    val preferredRoles: List<String> = listOf("Junior Electrical Engineer", "Maintenance Engineer"),
    val preferredShift: String = "Day Shift", // "Day Shift", "Flexible", "No Night Shift"
    val profileCompletion: Int = 0,
    val cvMetadata: CVMetadata? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            userId = uid,
            fullName = name,
            title = preferredRoles.firstOrNull() ?: "Junior Engineer",
            email = email,
            phone = phone,
            location = location,
            bio = bio,
            educations = educations,
            experiences = experiences,
            skills = skills,
            certifications = certifications,
            preference = JobPreference(
                preferredTitles = preferredRoles,
                preferredLocations = preferredLocations,
                expectedSalaryMin = expectedSalaryMin,
                expectedSalaryMax = expectedSalaryMax,
                preferredJobTypes = preferredJobTypes.mapNotNull {
                    try { EmploymentType.valueOf(it.replace(" ", "_").uppercase()) } catch (_: Exception) { null }
                },
                preferredShift = preferredShift
            ),
            cv = cvMetadata?.toCV()
        )
    }

    companion object {
        fun fromUserProfile(profile: UserProfile): FirestoreUser {
            return FirestoreUser(
                uid = profile.userId,
                name = profile.fullName,
                email = profile.email,
                phone = profile.phone,
                location = profile.location,
                bio = profile.bio,
                educations = profile.educations,
                skills = profile.skills,
                experiences = profile.experiences,
                certifications = profile.certifications,
                preferredLocations = profile.preference.preferredLocations,
                expectedSalaryMin = profile.preference.expectedSalaryMin,
                expectedSalaryMax = profile.preference.expectedSalaryMax,
                preferredJobTypes = profile.preference.preferredJobTypes.map { it.label },
                preferredRoles = profile.preference.preferredTitles,
                preferredShift = profile.preference.preferredShift,
                profileCompletion = profile.completionPercentage,
                cvMetadata = profile.cv?.let { CVMetadata.fromCV(it, profile.userId) },
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}

enum class CvProcessingStatus {
    UPLOADING,
    UPLOADED,
    PROCESSING,
    PROCESSED,
    FAILED
}

data class CVMetadata(
    val id: String = "",
    val fileName: String = "",
    val storagePath: String = "",
    val uploadTime: Long = System.currentTimeMillis(),
    val uploadDateFormatted: String = "",
    val fileType: String = "PDF",
    val fileSizeFormatted: String = "",
    val processingStatus: CvProcessingStatus = CvProcessingStatus.UPLOADED,
    val extractedTextStatus: String = "COMPLETED", // PENDING, COMPLETED, FAILED
    val analysisStatus: String = "ANALYZED",
    val extractedSkills: List<String> = emptyList(),
    val extractedSummary: String = "",
    val suggestedRoles: List<String> = emptyList(),
    val errorMessage: String? = null
) {
    fun toCV(): CV {
        return CV(
            id = id,
            fileName = fileName,
            fileSizeFormatted = fileSizeFormatted,
            uploadDate = uploadDateFormatted,
            fileType = fileType,
            uriString = storagePath,
            status = processingStatus,
            summary = extractedSummary,
            extractedSkills = extractedSkills
        )
    }

    companion object {
        fun fromCV(cv: CV, uid: String): CVMetadata {
            return CVMetadata(
                id = cv.id,
                fileName = cv.fileName,
                storagePath = "users/$uid/cv/${cv.id}/${cv.fileName}",
                uploadTime = System.currentTimeMillis(),
                uploadDateFormatted = cv.uploadDate,
                fileType = cv.fileType,
                fileSizeFormatted = cv.fileSizeFormatted,
                processingStatus = cv.status,
                extractedSkills = cv.extractedSkills,
                extractedSummary = cv.summary ?: ""
            )
        }
    }
}
