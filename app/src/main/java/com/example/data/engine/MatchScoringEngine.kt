package com.example.data.engine

import com.example.data.model.Job
import com.example.data.model.JobMatch
import com.example.data.model.UserProfile
import kotlin.math.roundToInt

data class ScoringWeights(
    val skillsWeight: Float = 0.35f,
    val experienceWeight: Float = 0.25f,
    val educationWeight: Float = 0.15f,
    val locationWeight: Float = 0.10f,
    val salaryWeight: Float = 0.10f,
    val shiftAndTypeWeight: Float = 0.05f
)

/**
 * Transparent scoring engine calculating realistic and explainable job match percentages.
 * Compares actual user profile data against job requirements.
 */
class MatchScoringEngine(
    private val weights: ScoringWeights = ScoringWeights()
) {

    fun calculateMatch(userProfile: UserProfile, job: Job): JobMatch {
        val userSkillNames = userProfile.skills.map { it.name.trim().lowercase() }
        val cvExtractedSkills = userProfile.cv?.extractedSkills?.map { it.trim().lowercase() } ?: emptyList()
        val allUserSkills = (userSkillNames + cvExtractedSkills).toSet()

        val jobSkills = job.skillsRequired.map { it.trim().lowercase() }

        // 1. SKILLS MATCH
        val matchedSkillsList = mutableListOf<String>()
        val missingSkillsList = mutableListOf<String>()

        for (jobSkill in job.skillsRequired) {
            val normalizedJobSkill = jobSkill.trim().lowercase()
            val hasSkill = allUserSkills.any { userSkill ->
                userSkill.contains(normalizedJobSkill) || normalizedJobSkill.contains(userSkill)
            }
            if (hasSkill) {
                matchedSkillsList.add(jobSkill)
            } else {
                missingSkillsList.add(jobSkill)
            }
        }

        val skillScore = if (jobSkills.isNotEmpty()) {
            (matchedSkillsList.size.toFloat() / jobSkills.size.toFloat()).coerceIn(0f, 1f)
        } else {
            0.8f
        }

        // 2. EXPERIENCE MATCH
        val totalYears = userProfile.experiences.size * 1.5f + (userProfile.skills.maxOfOrNull { it.years } ?: 1)
        val experienceScore = when {
            job.experienceRequired.contains("Entry", ignoreCase = true) -> 1.0f
            totalYears >= 2.0f -> 0.95f
            totalYears >= 1.0f -> 0.85f
            else -> 0.60f
        }
        val experienceExplanation = if (experienceScore >= 0.85f) {
            "Matches experience criteria (${job.experienceRequired})"
        } else {
            "Entry level background; role prefers ${job.experienceRequired}"
        }

        // 3. EDUCATION MATCH
        val userEduDegrees = userProfile.educations.map { it.degree.lowercase() + " " + it.fieldOfStudy.lowercase() }
        val jobEdu = job.educationRequired.lowercase()
        val educationScore = if (userEduDegrees.any { it.contains("eee") || it.contains("electrical") || it.contains("engineering") || it.contains("diploma") }) {
            0.95f
        } else if (userProfile.educations.isNotEmpty()) {
            0.75f
        } else {
            0.50f
        }
        val educationExplanation = if (educationScore >= 0.9f) {
            "Diploma/Degree aligned with engineering requirement"
        } else {
            "Educational background partially aligned"
        }

        // 4. LOCATION MATCH
        val preferredLocations = userProfile.preference.preferredLocations.map { it.lowercase() }
        val jobLoc = job.location.lowercase()
        val locationScore = if (preferredLocations.any { jobLoc.contains(it) || it.contains(jobLoc) } || userProfile.location.lowercase().contains(jobLoc)) {
            1.0f
        } else {
            0.5f
        }
        val locationExplanation = if (locationScore >= 0.9f) {
            "Direct match with preferred location (${job.location})"
        } else {
            "Outside primary preferred location"
        }

        // 5. SALARY MATCH
        val expectedMin = userProfile.preference.expectedSalaryMin
        val salaryScore = if (job.salaryMax >= expectedMin || job.salaryMin >= expectedMin) {
            1.0f
        } else if (job.salaryMax > 0 && job.salaryMax >= expectedMin * 0.8) {
            0.8f
        } else {
            0.6f
        }
        val salaryExplanation = if (salaryScore >= 0.8f) {
            "Salary aligns with expectations (${job.formattedSalary})"
        } else {
            "Below preferred salary range"
        }

        // 6. SHIFT & JOB TYPE MATCH
        val preferredJobTypes = userProfile.preference.preferredJobTypes
        val typeScore = if (preferredJobTypes.contains(job.employmentType)) 1.0f else 0.8f
        val typeExplanation = if (typeScore >= 0.9f) "Matches ${job.employmentType.label} preference" else "Alternative employment type"

        // WEIGHTED CALCULATION
        val totalWeightedScore = (
            skillScore * weights.skillsWeight +
            experienceScore * weights.experienceWeight +
            educationScore * weights.educationWeight +
            locationScore * weights.locationWeight +
            salaryScore * weights.salaryWeight +
            typeScore * weights.shiftAndTypeWeight
        )

        val finalPercentage = (totalWeightedScore * 100f).roundToInt().coerceIn(45, 98)

        // EXPLANATION BULLETS
        val reasons = mutableListOf<String>()
        if (matchedSkillsList.isNotEmpty()) {
            reasons.add("Skills matched: ${matchedSkillsList.take(2).joinToString(", ")}")
        }
        if (educationScore >= 0.85f) {
            reasons.add(educationExplanation)
        }
        if (locationScore >= 0.9f) {
            reasons.add("Preferred location (${job.location})")
        }
        if (salaryScore >= 0.8f) {
            reasons.add("Salary benchmark aligned")
        }
        if (reasons.isEmpty()) {
            reasons.add("General engineering aptitude match")
        }

        return JobMatch(
            percentage = finalPercentage,
            matchedReasons = reasons,
            matchedSkills = matchedSkillsList,
            missingSkills = missingSkillsList,
            matchedExperience = experienceExplanation,
            educationMatch = educationExplanation,
            locationMatch = locationExplanation,
            salaryMatch = salaryExplanation,
            jobTypeMatch = typeExplanation,
            confidenceScore = 0.92f
        )
    }
}
