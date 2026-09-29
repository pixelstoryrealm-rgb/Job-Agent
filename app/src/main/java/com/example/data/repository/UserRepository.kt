package com.example.data.repository

import android.net.Uri
import com.example.data.firebase.FirebaseManager
import com.example.data.local.UserProfileDao
import com.example.data.model.CV
import com.example.data.model.Certification
import com.example.data.model.CvProcessingStatus
import com.example.data.model.Education
import com.example.data.model.EmploymentType
import com.example.data.model.Experience
import com.example.data.model.FirestoreUser
import com.example.data.model.JobPreference
import com.example.data.model.JobSector
import com.example.data.model.Skill
import com.example.data.model.User
import com.example.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface UserRepository {
    val currentUser: Flow<User?>
    val userProfile: Flow<UserProfile>
    val isFirebaseActive: Boolean
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun register(name: String, email: String, phone: String, pass: String): Result<User>
    suspend fun logout()
    suspend fun updatePersonalInfo(name: String, title: String, phone: String, location: String, bio: String)
    suspend fun addEducation(education: Education)
    suspend fun removeEducation(educationId: String)
    suspend fun addSkill(skill: Skill)
    suspend fun removeSkill(skillId: String)
    suspend fun addExperience(experience: Experience)
    suspend fun removeExperience(experienceId: String)
    suspend fun updatePreferences(preference: JobPreference)
    suspend fun uploadOrReplaceCV(fileName: String, fileSize: String, fileType: String, uriString: String? = null)
    suspend fun deleteCV()
    suspend fun syncProfileWithFirestore()
}

class UserRepositoryImpl(
    private val userProfileDao: UserProfileDao,
    private val firebaseManager: FirebaseManager
) : UserRepository {

    override val isFirebaseActive: Boolean get() = firebaseManager.isFirebaseConfigured

    private val defaultUser = User(
        id = "user_asif_01",
        name = "Asif",
        email = "asif.engineer@example.com",
        phone = "+880 1712-345678"
    )

    private val _currentUserState = MutableStateFlow<User?>(defaultUser)
    override val currentUser: Flow<User?> = _currentUserState.asStateFlow()

    private val _profileState = MutableStateFlow(
        UserProfile(
            userId = "user_asif_01",
            fullName = "Asif",
            title = "Junior Electrical & Maintenance Engineer",
            email = "asif.engineer@example.com",
            phone = "+880 1712-345678",
            location = "Dhaka, Bangladesh",
            bio = "Enthusiastic and results-driven Junior Electrical Engineer with hands-on expertise in industrial maintenance, motor control circuits, and PLC troubleshooting.",
            educations = listOf(
                Education(
                    id = "edu_1",
                    degree = "Diploma in Engineering",
                    institution = "Dhaka Polytechnic Institute",
                    fieldOfStudy = "Electrical Engineering (EEE)",
                    passingYear = "2024",
                    grade = "CGPA 3.75 / 4.00"
                )
            ),
            experiences = listOf(
                Experience(
                    id = "exp_1",
                    title = "Trainee Electrical Maintenance Engineer",
                    company = "Standard Power & Switchgear",
                    location = "Tejgaon, Dhaka",
                    startDate = "Jan 2025",
                    endDate = "Present",
                    isCurrent = true,
                    description = "Diagnosed electrical control panels, circuit breakers, and assisted senior engineers in factory substation safety checks."
                )
            ),
            skills = listOf(
                Skill("sk_1", "Preventive Maintenance", "Intermediate", 2),
                Skill("sk_2", "Motor Controls & VFD", "Intermediate", 1),
                Skill("sk_3", "AutoCAD Electrical", "Intermediate", 2),
                Skill("sk_4", "Single Line Diagrams (SLD)", "Expert", 2),
                Skill("sk_5", "PLC Basics (Siemens/Delta)", "Beginner", 1)
            ),
            certifications = listOf(
                Certification("cert_1", "Industrial Automation & PLC Basics", "BITAC Dhaka", "2024")
            ),
            preference = JobPreference(
                preferredTitles = listOf("Junior Maintenance Engineer", "Junior Electrical Engineer", "Assistant Engineer"),
                preferredLocations = listOf("Dhaka", "Gazipur"),
                expectedSalaryMin = 22000,
                expectedSalaryMax = 32000,
                preferredJobTypes = listOf(EmploymentType.FULL_TIME),
                preferredSectors = listOf(JobSector.PRIVATE, JobSector.GOVERNMENT),
                preferredShift = "Day Shift"
            ),
            cv = CV(
                id = "cv_asif_01",
                fileName = "Asif_Electrical_Engineer_CV.pdf",
                fileSizeFormatted = "480 KB",
                uploadDate = "18 Sep 2026",
                fileType = "PDF",
                status = CvProcessingStatus.PROCESSED,
                summary = "Diploma in Electrical Engineering with verified maintenance background",
                extractedSkills = listOf("Preventive Maintenance", "Motor Controls", "AutoCAD", "PLC")
            )
        )
    )

    override val userProfile: Flow<UserProfile> = _profileState.asStateFlow()

    override suspend fun login(email: String, pass: String): Result<User> {
        val user = User(
            id = "user_asif_01",
            name = if (email.contains("asif", ignoreCase = true)) "Asif" else email.substringBefore("@").replaceFirstChar { it.uppercase() },
            email = email,
            phone = "+880 1712-345678"
        )
        _currentUserState.value = user
        return Result.success(user)
    }

    override suspend fun register(name: String, email: String, phone: String, pass: String): Result<User> {
        val user = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            phone = phone
        )
        _currentUserState.value = user
        _profileState.value = _profileState.value.copy(
            userId = user.id,
            fullName = name,
            email = email,
            phone = phone
        )
        syncProfileWithFirestore()
        return Result.success(user)
    }

    override suspend fun logout() {
        _currentUserState.value = null
    }

    override suspend fun updatePersonalInfo(
        name: String,
        title: String,
        phone: String,
        location: String,
        bio: String
    ) {
        _profileState.value = _profileState.value.copy(
            fullName = name,
            title = title,
            phone = phone,
            location = location,
            bio = bio
        )
        syncProfileWithFirestore()
    }

    override suspend fun addEducation(education: Education) {
        val current = _profileState.value.educations.toMutableList()
        current.add(education)
        _profileState.value = _profileState.value.copy(educations = current)
        syncProfileWithFirestore()
    }

    override suspend fun removeEducation(educationId: String) {
        val filtered = _profileState.value.educations.filterNot { it.id == educationId }
        _profileState.value = _profileState.value.copy(educations = filtered)
        syncProfileWithFirestore()
    }

    override suspend fun addSkill(skill: Skill) {
        val current = _profileState.value.skills.toMutableList()
        if (current.none { it.name.equals(skill.name, ignoreCase = true) }) {
            current.add(skill)
            _profileState.value = _profileState.value.copy(skills = current)
            syncProfileWithFirestore()
        }
    }

    override suspend fun removeSkill(skillId: String) {
        val filtered = _profileState.value.skills.filterNot { it.id == skillId }
        _profileState.value = _profileState.value.copy(skills = filtered)
        syncProfileWithFirestore()
    }

    override suspend fun addExperience(experience: Experience) {
        val current = _profileState.value.experiences.toMutableList()
        current.add(experience)
        _profileState.value = _profileState.value.copy(experiences = current)
        syncProfileWithFirestore()
    }

    override suspend fun removeExperience(experienceId: String) {
        val filtered = _profileState.value.experiences.filterNot { it.id == experienceId }
        _profileState.value = _profileState.value.copy(experiences = filtered)
        syncProfileWithFirestore()
    }

    override suspend fun updatePreferences(preference: JobPreference) {
        _profileState.value = _profileState.value.copy(preference = preference)
        syncProfileWithFirestore()
    }

    override suspend fun uploadOrReplaceCV(
        fileName: String,
        fileSize: String,
        fileType: String,
        uriString: String?
    ) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val cvId = UUID.randomUUID().toString()

        // 1. Mark as UPLOADING
        val initialCv = CV(
            id = cvId,
            fileName = fileName,
            fileSizeFormatted = fileSize,
            uploadDate = dateFormat.format(Date()),
            fileType = fileType,
            uriString = uriString,
            status = CvProcessingStatus.UPLOADING
        )
        _profileState.value = _profileState.value.copy(cv = initialCv)

        // 2. Upload to Cloud Storage if available
        val storage = firebaseManager.storage
        val auth = firebaseManager.auth
        val uid = auth?.currentUser?.uid ?: _profileState.value.userId

        if (storage != null && uriString != null) {
            try {
                val cvRef = storage.reference.child("users/$uid/cv/$cvId/$fileName")
                cvRef.putFile(Uri.parse(uriString)).await()
            } catch (_: Exception) {}
        }

        // 3. Update status to PROCESSING -> PROCESSED
        CoroutineScope(Dispatchers.IO).launch {
            delay(500)
            _profileState.value = _profileState.value.copy(
                cv = initialCv.copy(status = CvProcessingStatus.PROCESSING)
            )
            delay(700)
            val extracted = listOf("Electrical Troubleshooting", "Motor Controls", "AutoCAD", "Preventive Maintenance")
            _profileState.value = _profileState.value.copy(
                cv = initialCv.copy(
                    status = CvProcessingStatus.PROCESSED,
                    summary = "Verified engineering document. Polytechnic credentials detected.",
                    extractedSkills = extracted
                )
            )
            syncProfileWithFirestore()
        }
    }

    override suspend fun deleteCV() {
        _profileState.value = _profileState.value.copy(cv = null)
        syncProfileWithFirestore()
    }

    override suspend fun syncProfileWithFirestore() {
        val firestore = firebaseManager.firestore
        val auth = firebaseManager.auth
        val uid = auth?.currentUser?.uid ?: _profileState.value.userId
        if (firestore != null) {
            try {
                val fUser = FirestoreUser.fromUserProfile(_profileState.value)
                firestore.collection("users").document(uid)
                    .set(fUser)
                    .await()
            } catch (_: Exception) {}
        }
    }
}
