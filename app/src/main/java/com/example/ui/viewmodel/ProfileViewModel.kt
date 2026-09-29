package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.AiBackendService
import com.example.data.engine.ProfileAiAnalysisResult
import com.example.data.model.Education
import com.example.data.model.EmploymentType
import com.example.data.model.Experience
import com.example.data.model.JobPreference
import com.example.data.model.Skill
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val aiBackendService: AiBackendService
) : ViewModel() {

    val isFirebaseActive: Boolean get() = userRepository.isFirebaseActive

    val userProfile: StateFlow<UserProfile> = userRepository.userProfile
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserProfile(
                userId = "default",
                fullName = "Asif",
                title = "Junior Electrical Engineer",
                email = "asif.engineer@example.com",
                phone = "+880 1712-345678",
                location = "Dhaka, Bangladesh",
                bio = ""
            )
        )

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    // AI Profile Assistant States
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiAnalysisResult = MutableStateFlow<ProfileAiAnalysisResult?>(null)
    val aiAnalysisResult: StateFlow<ProfileAiAnalysisResult?> = _aiAnalysisResult.asStateFlow()

    private val _aiAdviceDialogTitle = MutableStateFlow<String?>(null)
    val aiAdviceDialogTitle: StateFlow<String?> = _aiAdviceDialogTitle.asStateFlow()

    private val _aiAdviceDialogItems = MutableStateFlow<List<String>>(emptyList())
    val aiAdviceDialogItems: StateFlow<List<String>> = _aiAdviceDialogItems.asStateFlow()

    fun updatePersonalDetails(name: String, title: String, phone: String, location: String, bio: String) {
        viewModelScope.launch {
            userRepository.updatePersonalInfo(name, title, phone, location, bio)
            _message.emit("Profile details updated successfully")
        }
    }

    fun addEducation(degree: String, institution: String, field: String, year: String, grade: String) {
        viewModelScope.launch {
            val edu = Education(
                id = UUID.randomUUID().toString(),
                degree = degree,
                institution = institution,
                fieldOfStudy = field,
                passingYear = year,
                grade = grade
            )
            userRepository.addEducation(edu)
            _message.emit("Education added")
        }
    }

    fun removeEducation(id: String) {
        viewModelScope.launch {
            userRepository.removeEducation(id)
            _message.emit("Education removed")
        }
    }

    fun addSkill(name: String, level: String) {
        viewModelScope.launch {
            val skill = Skill(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                level = level,
                years = 1
            )
            userRepository.addSkill(skill)
            _message.emit("Skill added")
        }
    }

    fun removeSkill(id: String) {
        viewModelScope.launch {
            userRepository.removeSkill(id)
            _message.emit("Skill removed")
        }
    }

    fun addExperience(title: String, company: String, location: String, start: String, end: String, isCurrent: Boolean, desc: String) {
        viewModelScope.launch {
            val exp = Experience(
                id = UUID.randomUUID().toString(),
                title = title,
                company = company,
                location = location,
                startDate = start,
                endDate = if (isCurrent) "Present" else end,
                isCurrent = isCurrent,
                description = desc
            )
            userRepository.addExperience(exp)
            _message.emit("Experience added")
        }
    }

    fun removeExperience(id: String) {
        viewModelScope.launch {
            userRepository.removeExperience(id)
            _message.emit("Experience removed")
        }
    }

    fun updateJobPreferences(locations: List<String>, minSalary: Long, maxSalary: Long, jobTypes: List<EmploymentType>, shift: String) {
        viewModelScope.launch {
            val updated = userProfile.value.preference.copy(
                preferredLocations = locations,
                expectedSalaryMin = minSalary,
                expectedSalaryMax = maxSalary,
                preferredJobTypes = jobTypes,
                preferredShift = shift
            )
            userRepository.updatePreferences(updated)
            _message.emit("Job preferences updated")
        }
    }

    fun uploadOrReplaceCV(fileName: String, fileSize: String, fileType: String, uriString: String? = null) {
        viewModelScope.launch {
            userRepository.uploadOrReplaceCV(fileName, fileSize, fileType, uriString)
            _message.emit("CV uploaded. Processing document...")
        }
    }

    fun deleteCV() {
        viewModelScope.launch {
            userRepository.deleteCV()
            _message.emit("CV deleted")
        }
    }

    // AI PROFILE ASSISTANCE ACTIONS (Section 21)

    fun triggerAnalyzeProfile() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = aiBackendService.analyzeProfile(userProfile.value)
            res.onSuccess {
                _aiAnalysisResult.value = it
            }.onFailure {
                _message.emit("AI analysis failed: ${it.localizedMessage}")
            }
            _isAiLoading.value = false
        }
    }

    fun triggerBestJobTypes() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = aiBackendService.getBestJobTypes(userProfile.value)
            res.onSuccess {
                _aiAdviceDialogTitle.value = "Best Matched Roles for You"
                _aiAdviceDialogItems.value = it
            }
            _isAiLoading.value = false
        }
    }

    fun triggerMissingSkills() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = aiBackendService.getMissingSkillsAdvice(userProfile.value)
            res.onSuccess {
                _aiAdviceDialogTitle.value = "Recommended Skills to Learn"
                _aiAdviceDialogItems.value = it
            }
            _isAiLoading.value = false
        }
    }

    fun triggerImproveSearch() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = aiBackendService.getJobSearchImprovements(userProfile.value)
            res.onSuccess {
                _aiAdviceDialogTitle.value = "Job Search Optimization Tips"
                _aiAdviceDialogItems.value = it
            }
            _isAiLoading.value = false
        }
    }

    fun triggerExplainCv() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = aiBackendService.explainCv(userProfile.value)
            res.onSuccess {
                _aiAdviceDialogTitle.value = "CV Keyword & Strength Breakdown"
                _aiAdviceDialogItems.value = listOf(it)
            }.onFailure {
                _message.emit(it.localizedMessage ?: "No CV available to explain")
            }
            _isAiLoading.value = false
        }
    }

    fun dismissAiAnalysisResult() {
        _aiAnalysisResult.value = null
    }

    fun dismissAiAdviceDialog() {
        _aiAdviceDialogTitle.value = null
        _aiAdviceDialogItems.value = emptyList()
    }
}
