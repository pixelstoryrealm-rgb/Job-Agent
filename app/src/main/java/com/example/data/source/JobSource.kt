package com.example.data.source

import com.example.data.model.Job

typealias JobSource = com.example.data.model.JobSourceConnector

data class JobSearchFilters(
    val query: String = "",
    val location: String? = null,
    val sector: String? = null, // Government, Private, etc.
    val employmentType: String? = null, // Full Time, Remote, Internship, etc.
    val minSalary: Long? = null,
    val maxSalary: Long? = null,
    val experience: String? = null,
    val education: String? = null,
    val requiredSkills: List<String> = emptyList(),
    val shift: String? = null,
    val sourceName: String? = null,
    val activeOnly: Boolean = true,
    val freshOnly: Boolean = false,
    val sortBy: JobSortOption = JobSortOption.BEST_MATCH
)

enum class JobSortOption(val displayName: String) {
    BEST_MATCH("Best Match"),
    NEWEST("Newest"),
    SALARY_HIGH_TO_LOW("Salary: High to Low"),
    SALARY_LOW_TO_HIGH("Salary: Low to High"),
    RELEVANCE("Relevance")
}
