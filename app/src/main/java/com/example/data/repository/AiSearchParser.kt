package com.example.data.repository

import com.example.data.source.JobSearchFilters
import com.example.data.source.JobSortOption

data class ParsedAiQuery(
    val originalQuery: String,
    val extractedLocation: String? = null,
    val extractedMinSalary: Long? = null,
    val extractedJobTitle: String? = null,
    val extractedSkills: List<String> = emptyList(),
    val extractedEmploymentType: String? = null,
    val extractedSector: String? = null,
    val extractedShift: String? = null,
    val confidence: Float = 0.92f
) {
    fun toJobSearchFilters(): JobSearchFilters {
        return JobSearchFilters(
            query = extractedJobTitle ?: "",
            location = extractedLocation,
            sector = extractedSector,
            employmentType = extractedEmploymentType,
            minSalary = extractedMinSalary,
            requiredSkills = extractedSkills,
            shift = extractedShift,
            sortBy = JobSortOption.BEST_MATCH
        )
    }
}

interface AiSearchQueryParser {
    suspend fun parseNaturalQuery(query: String): ParsedAiQuery
}

/**
 * Intelligent natural-language parser for Phase 2.
 * Converts natural intent queries like:
 * "Dhaka te 20k+ salary er junior electrical job chai"
 * "Maintenance engineer job, no night shift"
 * "Government engineer in Dhaka"
 */
class LocalAiSearchParser : AiSearchQueryParser {
    override suspend fun parseNaturalQuery(query: String): ParsedAiQuery {
        val lower = query.lowercase().trim()
        var location: String? = null
        var salary: Long? = null
        var title: String? = null
        val skills = mutableListOf<String>()
        var empType: String? = null
        var sector: String? = null
        var shift: String? = null

        // 1. Detect locations (Dhaka, Chittagong/Chattogram, Gazipur, Sylhet, etc.)
        when {
            lower.contains("dhaka") -> location = "Dhaka"
            lower.contains("gazipur") -> location = "Gazipur"
            lower.contains("chittagong") || lower.contains("chattogram") -> location = "Chattogram"
            lower.contains("sylhet") -> location = "Sylhet"
            lower.contains("rajshahi") -> location = "Rajshahi"
        }

        // 2. Detect salary patterns like "20k", "25k+", "20000", "25000 tk", "30k salary"
        val kMatch = Regex("""(\d+)\s*k""").find(lower)
        if (kMatch != null) {
            val num = kMatch.groupValues[1].toLongOrNull()
            if (num != null) salary = num * 1000
        } else {
            val numMatch = Regex("""(\d{5})""").find(lower)
            if (numMatch != null) {
                salary = numMatch.groupValues[1].toLongOrNull()
            }
        }

        // 3. Detect job types
        when {
            lower.contains("remote") -> empType = "Remote"
            lower.contains("intern") || lower.contains("internship") -> empType = "Internship"
            lower.contains("part time") -> empType = "Part Time"
            lower.contains("full time") -> empType = "Full Time"
        }

        // 4. Detect sectors
        when {
            lower.contains("govt") || lower.contains("government") -> sector = "Government"
            lower.contains("private") -> sector = "Private"
        }

        // 5. Detect shift preference (e.g. "no night shift", "day shift")
        when {
            lower.contains("no night shift") || lower.contains("night shift chara") || lower.contains("day shift") -> {
                shift = "Day Shift"
            }
            lower.contains("night shift") -> {
                shift = "Night Shift"
            }
        }

        // 6. Detect skills
        if (lower.contains("plc")) skills.add("PLC")
        if (lower.contains("autocad")) skills.add("AutoCAD")
        if (lower.contains("maintenance")) skills.add("Maintenance")
        if (lower.contains("wiring")) skills.add("Wiring")
        if (lower.contains("substation")) skills.add("Substation")

        // 7. Extract candidate title
        var cleanedTitle = lower
            .replace("dhaka te", "")
            .replace("dhaka", "")
            .replace("gazipur", "")
            .replace("no night shift", "")
            .replace("night shift", "")
            .replace("day shift", "")
            .replace("salary er", "")
            .replace("salary", "")
            .replace("job chai", "")
            .replace("job", "")
            .replace("chai", "")
            .replace("dorkar", "")
            .replace(",", " ")
            .replace(Regex("""\d+k\+?"""), "")
            .replace(Regex("""\d{5}\+?"""), "")
            .trim()

        if (cleanedTitle.length > 2) {
            title = cleanedTitle
        }

        return ParsedAiQuery(
            originalQuery = query,
            extractedLocation = location,
            extractedMinSalary = salary,
            extractedJobTitle = title,
            extractedSkills = skills,
            extractedEmploymentType = empType,
            extractedSector = sector,
            extractedShift = shift,
            confidence = 0.94f
        )
    }
}
