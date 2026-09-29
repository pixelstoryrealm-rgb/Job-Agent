package com.example.data.source

import com.example.data.model.Company
import com.example.data.model.EmploymentType
import com.example.data.model.Job
import com.example.data.model.JobMatch
import com.example.data.model.JobSector
import com.example.data.model.JobSourceConnector
import com.example.data.model.JobSourceType
import com.example.data.model.SourceConnectionStatus
import com.example.data.model.SourceHealth
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class LocalDemoJobConnector : JobSourceConnector {
    override val sourceId: String = "local_demo_connector"
    override val sourceName: String = "Curated Demo Dataset (Bangladesh)"
    override val sourceType: JobSourceType = JobSourceType.LOCAL_DEMO
    override val connectionStatus: SourceConnectionStatus = SourceConnectionStatus.CONNECTED
    override val isConfigured: Boolean = true
    override val isEnabled: Boolean = true
    override val refreshIntervalMinutes: Int = 1440 // 24 hours
    override val requiredConfigurationNotice: String? = null

    private val now = System.currentTimeMillis()

    private val demoJobs = listOf(
        Job(
            id = "demo_job_1",
            title = "Junior Maintenance Engineer",
            company = Company(
                id = "comp_1",
                name = "ABC Engineering Ltd.",
                industry = "Industrial Manufacturing",
                location = "Tejgaon I/A, Dhaka",
                verified = true
            ),
            location = "Dhaka",
            salaryMin = 20000,
            salaryMax = 25000,
            currency = "৳",
            employmentType = EmploymentType.FULL_TIME,
            sector = JobSector.PRIVATE,
            experienceRequired = "1-2 Years",
            educationRequired = "Diploma in EEE",
            skillsRequired = listOf("Electrical Troubleshooting", "Preventive Maintenance", "Motor Controls", "PLC Basics"),
            description = "ABC Engineering Ltd. is seeking a disciplined Junior Maintenance Engineer to oversee daily factory machinery electrical servicing, prevent unplanned downtime, and maintain industrial panels.",
            responsibilities = listOf(
                "Perform scheduled electrical inspections and routine maintenance of machinery.",
                "Troubleshoot motor starter circuits, VFDs, and sensors on the production floor.",
                "Maintain accurate logs of breakdowns, spare parts usage, and repair actions.",
                "Ensure strict compliance with workplace electrical safety guidelines."
            ),
            requirements = listOf(
                "Diploma in Electrical or Electronics Engineering from a recognized polytechnic.",
                "Minimum 1 year hands-on experience in manufacturing plant maintenance.",
                "Strong grasp of single-phase and 3-phase power distribution circuits.",
                "Willingness to work in rotating factory shifts."
            ),
            benefits = listOf(
                "Subsidized lunch and tea allowance.",
                "Two festival bonuses annually.",
                "Health & accidental coverage.",
                "Annual increment based on performance."
            ),
            postedDate = "2 days ago",
            deadline = "25 Oct 2026",
            deadlineTimestamp = now + TimeUnit.DAYS.toMillis(27),
            applicationUrl = "https://careers.abcengineering-bd.com/apply/jme-04",
            match = JobMatch(
                percentage = 92,
                matchedReasons = listOf(
                    "Diploma in EEE",
                    "Relevant maintenance experience",
                    "Preferred location (Dhaka)",
                    "Salary aligns with preference"
                ),
                missingSkills = listOf("Advanced Siemens PLC"),
                confidenceScore = 0.94f
            ),
            isDemo = true,
            isFeatured = true,
            isHighMatch = true,
            isNearUser = true,
            isRecentlyAdded = true,
            firstSeenAt = now - TimeUnit.HOURS.toMillis(12)
        ),
        Job(
            id = "demo_job_2",
            title = "Junior Electrical Engineer",
            company = Company(
                id = "comp_2",
                name = "Meghna Power & Energy Ltd.",
                industry = "Power & Grid Distribution",
                location = "Motijheel, Dhaka",
                verified = true
            ),
            location = "Dhaka",
            salaryMin = 28000,
            salaryMax = 35000,
            currency = "৳",
            employmentType = EmploymentType.FULL_TIME,
            sector = JobSector.MULTINATIONAL,
            experienceRequired = "1-3 Years",
            educationRequired = "B.Sc / Diploma in EEE",
            skillsRequired = listOf("Power Distribution", "AutoCAD Electrical", "Switchgear", "Substation Ops"),
            description = "Join our grid substation support team managing 33/11kV electrical substations, load calculation, and safety switchgear installations across Greater Dhaka projects.",
            responsibilities = listOf(
                "Assist Senior Project Engineers in sub-station installation and commissioning.",
                "Review Single Line Diagrams (SLD) and cable scheduling.",
                "Conduct relay testing, transformer oil inspection, and earth resistance measurement."
            ),
            requirements = listOf(
                "Diploma or Bachelor's in Electrical and Electronics Engineering.",
                "Proficiency in AutoCAD for electrical wiring diagrams.",
                "Analytical mindset with high attention to safety protocols."
            ),
            benefits = listOf(
                "Provident Fund and Gratuity.",
                "Company transportation or conveyance allowance.",
                "Professional certification sponsorships."
            ),
            postedDate = "Yesterday",
            deadline = "20 Oct 2026",
            deadlineTimestamp = now + TimeUnit.DAYS.toMillis(22),
            applicationUrl = "https://meghnapower.com/careers/junior-ee",
            match = JobMatch(
                percentage = 88,
                matchedReasons = listOf(
                    "Degree matches engineering criteria",
                    "Electrical diagram reading expertise",
                    "Prime Dhaka location",
                    "Attractive salary growth"
                ),
                missingSkills = listOf("High-voltage relay calibration"),
                confidenceScore = 0.89f
            ),
            isDemo = true,
            isFeatured = true,
            isHighMatch = true,
            isNearUser = true,
            firstSeenAt = now - TimeUnit.HOURS.toMillis(24)
        ),
        Job(
            id = "demo_job_3",
            title = "Electrical Technician",
            company = Company(
                id = "comp_3",
                name = "Apex Industrial Tech",
                industry = "Textile & Machinery",
                location = "Mirpur, Dhaka",
                verified = true
            ),
            location = "Dhaka",
            salaryMin = 18000,
            salaryMax = 22000,
            currency = "৳",
            employmentType = EmploymentType.FULL_TIME,
            sector = JobSector.PRIVATE,
            experienceRequired = "Entry Level / 1 Year",
            educationRequired = "Diploma / Trade Certificate",
            skillsRequired = listOf("Wiring", "Circuit Breakers", "Multimeter Diagnostics", "Generator Servicing"),
            description = "Hands-on electrical technician needed for testing control panels, repairing heavy-duty circuit breakers, and assisting generator maintenance crews.",
            responsibilities = listOf(
                "Assemble electrical control boxes and relay mounts according to schematics.",
                "Test continuity and voltage levels with digital multimeters.",
                "Assist field technicians during diesel generator periodic maintenance."
            ),
            requirements = listOf(
                "Polytechnic diploma or vocational trade certificate in Electrical.",
                "Solid understanding of phase balance and earthing systems.",
                "Strong physical stamina and dedication."
            ),
            benefits = listOf(
                "Overtime allowance.",
                "Safety gear and medical allowance.",
                "Lunch subsidy."
            ),
            postedDate = "3 days ago",
            deadline = "10 Oct 2026",
            deadlineTimestamp = now + TimeUnit.DAYS.toMillis(12),
            applicationUrl = "https://apextechnologies.com.bd/jobs/elec-tech",
            match = JobMatch(
                percentage = 85,
                matchedReasons = listOf(
                    "Matches vocational electrical background",
                    "Entry-level accessible",
                    "Location nearby"
                ),
                confidenceScore = 0.86f
            ),
            isDemo = true,
            isFeatured = false,
            isRecentlyAdded = true,
            isNearUser = true,
            firstSeenAt = now - TimeUnit.HOURS.toMillis(36)
        ),
        Job(
            id = "demo_job_4",
            title = "Production & Electrical Engineer",
            company = Company(
                id = "comp_4",
                name = "Walton Hi-Tech Industries PLC",
                industry = "Electronics Manufacturing",
                location = "Chandra, Gazipur",
                verified = true
            ),
            location = "Gazipur",
            salaryMin = 32000,
            salaryMax = 42000,
            currency = "৳",
            employmentType = EmploymentType.FULL_TIME,
            sector = JobSector.MULTINATIONAL,
            experienceRequired = "2-3 Years",
            educationRequired = "B.Sc / Diploma in EEE / IPE",
            skillsRequired = listOf("Assembly Line Automation", "Quality Control", "Root Cause Analysis", "PLC"),
            description = "Drive operational excellence at Walton's modern electronics manufacturing plants. Lead automated production lines and implement preventive electrical fixes.",
            responsibilities = listOf(
                "Supervise automated SMT and appliance assembly conveyor lines.",
                "Diagnose sensor feedback loops and pneumatics errors.",
                "Minimize line stops and collaborate with quality assurance teams."
            ),
            requirements = listOf(
                "Diploma or Degree in EEE/IPE/Mechanical.",
                "2+ years experience in automated manufacturing lines.",
                "Leadership capability to manage plant operators."
            ),
            benefits = listOf(
                "Free company transport from Dhaka.",
                "Subsidized dorm accommodation.",
                "Profit sharing fund (WPPF) and festival bonus."
            ),
            postedDate = "4 days ago",
            deadline = "25 Oct 2026",
            deadlineTimestamp = now + TimeUnit.DAYS.toMillis(27),
            applicationUrl = "https://waltonbd.com/career/production-engineer",
            match = JobMatch(
                percentage = 81,
                matchedReasons = listOf(
                    "High salary benchmark (৳32,000+)",
                    "Strong brand and career trajectory",
                    "Provides transport from Dhaka"
                ),
                missingSkills = listOf("SMT Pick-and-Place machine calibration"),
                confidenceScore = 0.82f
            ),
            isDemo = true,
            isFeatured = true,
            isHighMatch = true,
            firstSeenAt = now - TimeUnit.DAYS.toMillis(3)
        ),
        Job(
            id = "demo_job_5",
            title = "Assistant Engineer (Substation Project)",
            company = Company(
                id = "comp_5",
                name = "Desh Power Infrastructure (Govt Partner)",
                industry = "Power Transmission",
                location = "Dhanmondi, Dhaka",
                verified = true
            ),
            location = "Dhaka",
            salaryMin = 26000,
            salaryMax = 32000,
            currency = "৳",
            employmentType = EmploymentType.CONTRACT,
            sector = JobSector.GOVERNMENT,
            experienceRequired = "1-2 Years",
            educationRequired = "Diploma in EEE",
            skillsRequired = listOf("Govt Tender Specs", "Substation Erection", "Earthing Grid", "Site Supervision"),
            description = "Govt electrification partner project seeking Assistant Engineer to supervise rural grid substation expansion under Dhaka electric division.",
            responsibilities = listOf(
                "Oversee civil and electrical contractor activities at sub-station sites.",
                "Verify equipment receipt according to BPDB / DESCO standard specifications.",
                "Prepare weekly milestone progress reports for government project directors."
            ),
            requirements = listOf(
                "Diploma in Electrical Engineering.",
                "Familiarity with national electrical grid standards.",
                "Proficiency in documentation and MS Excel."
            ),
            benefits = listOf(
                "Govt project allowance and mobile bill.",
                "Opportunity for direct absorption in govt contractor panels.",
                "Comprehensive site travel per diem."
            ),
            postedDate = "Just now",
            deadline = "30 Oct 2026",
            deadlineTimestamp = now + TimeUnit.DAYS.toMillis(32),
            applicationUrl = "https://deshpower.gov-partner.org/apply/sub-station",
            match = JobMatch(
                percentage = 94,
                matchedReasons = listOf(
                    "Govt partner project preference",
                    "Exact Diploma in EEE fit",
                    "Desired salary range (৳26k-৳32k)",
                    "Dhaka based head office"
                ),
                confidenceScore = 0.96f
            ),
            isDemo = true,
            isFeatured = true,
            isHighMatch = true,
            isRecentlyAdded = true,
            isNearUser = true,
            firstSeenAt = now - TimeUnit.HOURS.toMillis(2)
        ),
        Job(
            id = "demo_job_6",
            title = "Solar System Installation Specialist",
            company = Company(
                id = "comp_6",
                name = "Green Energy Solar Solutions",
                industry = "Renewable Energy",
                location = "Uttara, Dhaka",
                verified = true
            ),
            location = "Dhaka",
            salaryMin = 22000,
            salaryMax = 28000,
            currency = "৳",
            employmentType = EmploymentType.FULL_TIME,
            sector = JobSector.STARTUP,
            experienceRequired = "1 Year",
            educationRequired = "Diploma in EEE / Renewable",
            skillsRequired = listOf("Solar Inverter Setup", "Roof Mounting", "Net Metering", "Battery Bank"),
            description = "Rapidly expanding clean energy company looking for proactive solar technicians to install commercial rooftop solar setups and smart inverters.",
            responsibilities = listOf(
                "Design solar array connections and inverter wiring.",
                "Configure lithium-ion battery backup systems.",
                "Conduct grid-tie net-metering tests with utility inspectors."
            ),
            requirements = listOf(
                "Diploma in EEE or vocational renewable certificate.",
                "No fear of rooftop heights and field activities.",
                "Quick troubleshooting skills."
            ),
            benefits = listOf(
                "Field commission per completed megawatt installation.",
                "Mobile and fuel allowance.",
                "Health insurance."
            ),
            postedDate = "5 days ago",
            deadline = "12 Oct 2026",
            deadlineTimestamp = now + TimeUnit.DAYS.toMillis(14),
            applicationUrl = "https://greenenergy.bd/jobs/solar-tech",
            match = JobMatch(
                percentage = 84,
                matchedReasons = listOf(
                    "Clean energy sector growth",
                    "Matches diploma qualifications",
                    "Competitive incentive structure"
                ),
                confidenceScore = 0.85f
            ),
            isDemo = true,
            isRecentlyAdded = true,
            firstSeenAt = now - TimeUnit.DAYS.toMillis(4)
        )
    )

    override suspend fun fetchJobs(page: Int, pageSize: Int): Result<List<Job>> {
        delay(120)
        val start = (page - 1) * pageSize
        if (start >= demoJobs.size) return Result.success(emptyList())
        val paged = demoJobs.drop(start).take(pageSize)
        return Result.success(paged)
    }

    override suspend fun searchJobs(query: String, filters: JobSearchFilters, page: Int, pageSize: Int): Result<List<Job>> {
        delay(100)
        var filtered = demoJobs

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter { job ->
                job.title.lowercase().contains(q) ||
                job.company.name.lowercase().contains(q) ||
                job.location.lowercase().contains(q) ||
                job.skillsRequired.any { it.lowercase().contains(q) } ||
                job.educationRequired.lowercase().contains(q)
            }
        }

        if (!filters.location.isNullOrBlank()) {
            val loc = filters.location.lowercase()
            filtered = filtered.filter { it.location.lowercase().contains(loc) }
        }

        if (!filters.sector.isNullOrBlank()) {
            val sec = filters.sector.lowercase()
            filtered = filtered.filter { it.sector.name.lowercase().contains(sec) || it.sector.label.lowercase().contains(sec) }
        }

        if (!filters.employmentType.isNullOrBlank()) {
            val emp = filters.employmentType.lowercase()
            filtered = filtered.filter { it.employmentType.label.lowercase().contains(emp) }
        }

        if (filters.minSalary != null) {
            filtered = filtered.filter { it.salaryMax >= filters.minSalary }
        }

        if (filters.maxSalary != null) {
            filtered = filtered.filter { it.salaryMin <= filters.maxSalary }
        }

        if (!filters.shift.isNullOrBlank()) {
            val sh = filters.shift.lowercase()
            filtered = filtered.filter { it.shift.lowercase().contains(sh) }
        }

        if (filters.activeOnly) {
            filtered = filtered.filter { it.active && !it.isExpired }
        }

        if (filters.freshOnly) {
            filtered = filtered.filter { it.isNew }
        }

        // Sorting
        filtered = when (filters.sortBy) {
            JobSortOption.BEST_MATCH -> filtered.sortedByDescending { it.match.percentage }
            JobSortOption.NEWEST -> filtered.sortedByDescending { it.firstSeenAt }
            JobSortOption.SALARY_HIGH_TO_LOW -> filtered.sortedByDescending { it.salaryMax }
            JobSortOption.SALARY_LOW_TO_HIGH -> filtered.sortedBy { it.salaryMin }
            JobSortOption.RELEVANCE -> filtered.sortedByDescending { it.match.confidenceScore }
        }

        val start = (page - 1) * pageSize
        if (start >= filtered.size) return Result.success(emptyList())
        val paged = filtered.drop(start).take(pageSize)
        return Result.success(paged)
    }

    override suspend fun getJobDetails(jobId: String): Result<Job> {
        val job = demoJobs.find { it.id == jobId }
        return if (job != null) Result.success(job) else Result.failure(NoSuchElementException("Job $jobId not found"))
    }

    override suspend fun healthCheck(): SourceHealth {
        return SourceHealth(
            status = SourceConnectionStatus.CONNECTED,
            message = "Local demo connector active (6 engineering vacancies loaded)",
            latencyMs = 2
        )
    }
}
