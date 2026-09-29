package com.example.data.engine

import com.example.data.model.Job

/**
 * Stable deduplication engine.
 * Identifies duplicate vacancies originating across multiple job feeds.
 * If confidence is certain (canonical URL or identical normalized company+title+location+salary), merges metadata.
 * If uncertain, marks possibleDuplicate = true without deleting valid positions.
 */
class JobDeduplicationEngine {

    fun deduplicate(incomingJobs: List<Job>, existingJobs: List<Job> = emptyList()): List<Job> {
        val seen = mutableListOf<Job>()
        val allToProcess = existingJobs + incomingJobs

        for (candidate in allToProcess) {
            val exactMatch = seen.find { existing ->
                isExactDuplicate(existing, candidate)
            }

            if (exactMatch != null) {
                // Merge sources/views without duplicating item
                val updated = exactMatch.copy(
                    viewsCount = maxOf(exactMatch.viewsCount, candidate.viewsCount),
                    lastSeenAt = System.currentTimeMillis()
                )
                val idx = seen.indexOf(exactMatch)
                seen[idx] = updated
            } else {
                val potentialMatch = seen.find { existing ->
                    isPotentialDuplicate(existing, candidate)
                }

                if (potentialMatch != null) {
                    // Mark as possible duplicate for user transparency without deleting
                    seen.add(
                        candidate.copy(
                            possibleDuplicate = true,
                            duplicateOfJobId = potentialMatch.id
                        )
                    )
                } else {
                    seen.add(candidate)
                }
            }
        }

        return seen
    }

    private fun isExactDuplicate(a: Job, b: Job): Boolean {
        if (a.id == b.id) return true
        if (a.sourceName == b.sourceName && a.sourceJobId == b.sourceJobId && a.sourceJobId.isNotBlank()) return true
        if (!a.applicationUrl.isNullOrBlank() && !b.applicationUrl.isNullOrBlank() && a.applicationUrl == b.applicationUrl) return true

        val companyA = normalizeString(a.company.name)
        val companyB = normalizeString(b.company.name)
        val titleA = normalizeString(a.title)
        val titleB = normalizeString(b.title)
        val locA = normalizeString(a.location)
        val locB = normalizeString(b.location)

        val sameCompany = companyA == companyB
        val sameTitle = titleA == titleB
        val sameLoc = locA.contains(locB) || locB.contains(locA)
        val sameSalary = a.salaryMin == b.salaryMin && a.salaryMax == b.salaryMax

        return sameCompany && sameTitle && sameLoc && sameSalary && a.salaryMin > 0
    }

    private fun isPotentialDuplicate(a: Job, b: Job): Boolean {
        val companyA = normalizeString(a.company.name)
        val companyB = normalizeString(b.company.name)
        val titleA = normalizeString(a.title)
        val titleB = normalizeString(b.title)

        return (companyA == companyB && titleA == titleB && a.id != b.id)
    }

    private fun normalizeString(input: String): String {
        return input.lowercase()
            .replace("ltd.", "")
            .replace("ltd", "")
            .replace("plc", "")
            .replace("limited", "")
            .replace("industries", "")
            .replace(Regex("""[^a-z0-9]"""), "")
            .trim()
    }
}
