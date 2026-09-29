package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Application
import com.example.data.model.Job
import com.example.data.repository.ApplicationRepository
import com.example.data.repository.JobRepository
import com.example.data.repository.LocalAiSearchParser
import com.example.data.repository.ParsedAiQuery
import com.example.data.source.JobSearchFilters
import com.example.data.source.JobSortOption
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class JobActionEffect {
    data class ShowToast(val message: String) : JobActionEffect()
    data class NavigateToDetails(val jobId: String) : JobActionEffect()
    data class OpenQuickActions(val job: Job) : JobActionEffect()
}

class JobViewModel(
    private val jobRepository: JobRepository,
    private val applicationRepository: ApplicationRepository
) : ViewModel() {

    private val aiSearchParser: LocalAiSearchParser = LocalAiSearchParser()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _allJobs = MutableStateFlow<List<Job>>(emptyList())
    val allJobs: StateFlow<List<Job>> = _allJobs.asStateFlow()

    // AI Match swipe cards stack (index tracked)
    private val _aiCardIndex = MutableStateFlow(0)
    val aiCardIndex: StateFlow<Int> = _aiCardIndex.asStateFlow()

    // Hidden job IDs (e.g. swiped left or hidden)
    private val _hiddenJobIds = MutableStateFlow<Set<String>>(emptySet())
    val hiddenJobIds: StateFlow<Set<String>> = _hiddenJobIds.asStateFlow()

    // Search query & filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilters = MutableStateFlow(JobSearchFilters())
    val activeFilters: StateFlow<JobSearchFilters> = _activeFilters.asStateFlow()

    private val _parsedAiQuery = MutableStateFlow<ParsedAiQuery?>(null)
    val parsedAiQuery: StateFlow<ParsedAiQuery?> = _parsedAiQuery.asStateFlow()

    // Filtered jobs list
    private val _filteredJobs = MutableStateFlow<List<Job>>(emptyList())
    val filteredJobs: StateFlow<List<Job>> = _filteredJobs.asStateFlow()

    // Quick Actions selected job
    private val _selectedJobForQuickActions = MutableStateFlow<Job?>(null)
    val selectedJobForQuickActions: StateFlow<Job?> = _selectedJobForQuickActions.asStateFlow()

    // Saved Jobs set
    val savedJobIds: StateFlow<List<String>> = jobRepository.getSavedJobIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Effects (toasts, navigation)
    private val _actionEffects = MutableSharedFlow<JobActionEffect>()
    val actionEffects: SharedFlow<JobActionEffect> = _actionEffects.asSharedFlow()

    init {
        loadJobs()
    }

    fun loadJobs() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = jobRepository.getAllJobs()
            result.onSuccess { list ->
                _allJobs.value = list
                applyCurrentFilters(list)
            }.onFailure {
                _actionEffects.emit(JobActionEffect.ShowToast("Failed to load jobs: ${it.localizedMessage}"))
            }
            _isLoading.value = false
        }
    }

    fun refreshJobs() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = jobRepository.getAllJobs()
            result.onSuccess { list ->
                _allJobs.value = list
                applyCurrentFilters(list)
                _aiCardIndex.value = 0 // Reset card deck on pull down
                _actionEffects.emit(JobActionEffect.ShowToast("Jobs updated"))
            }
            _isRefreshing.value = false
        }
    }

    // SIGNATURE GESTURE HANDLERS FOR AI MATCH CARDS:

    // 1. Swipe Right -> Save Job
    fun onSwipeRightSave(job: Job) {
        viewModelScope.launch {
            jobRepository.saveJob(job.id)
            advanceCard()
            _actionEffects.emit(JobActionEffect.ShowToast("Saved: ${job.title}"))
        }
    }

    // 2. Swipe Left -> Skip / Hide Job
    fun onSwipeLeftSkip(job: Job) {
        viewModelScope.launch {
            _hiddenJobIds.value = _hiddenJobIds.value + job.id
            advanceCard()
            _actionEffects.emit(JobActionEffect.ShowToast("Skipped: ${job.title}"))
        }
    }

    // 3. Swipe Up -> Open Job Details
    fun onSwipeUpOpenDetails(job: Job) {
        viewModelScope.launch {
            _actionEffects.emit(JobActionEffect.NavigateToDetails(job.id))
        }
    }

    // 4. Long Press -> Open Quick Actions
    fun onCardLongPress(job: Job) {
        _selectedJobForQuickActions.value = job
    }

    fun closeQuickActions() {
        _selectedJobForQuickActions.value = null
    }

    private fun advanceCard() {
        val nonHidden = getActiveAiMatchJobs()
        if (_aiCardIndex.value < nonHidden.size - 1) {
            _aiCardIndex.value = _aiCardIndex.value + 1
        } else {
            // Loop back or stay at end
            _aiCardIndex.value = 0
        }
    }

    fun getActiveAiMatchJobs(): List<Job> {
        val hidden = _hiddenJobIds.value
        return _allJobs.value.filterNot { hidden.contains(it.id) }
            .sortedByDescending { it.match.percentage }
    }

    fun toggleSaveJob(jobId: String) {
        viewModelScope.launch {
            val isSaved = savedJobIds.value.contains(jobId)
            if (isSaved) {
                jobRepository.removeSavedJob(jobId)
                _actionEffects.emit(JobActionEffect.ShowToast("Job removed from saved"))
            } else {
                jobRepository.saveJob(jobId)
                _actionEffects.emit(JobActionEffect.ShowToast("Job saved successfully!"))
            }
        }
    }

    fun applyToJob(job: Job, notes: String = "") {
        viewModelScope.launch {
            val res = applicationRepository.applyToJob(job, notes)
            res.onSuccess {
                _actionEffects.emit(JobActionEffect.ShowToast("Applied to ${job.title} at ${job.company.name}!"))
            }.onFailure {
                _actionEffects.emit(JobActionEffect.ShowToast(it.message ?: "Application failed"))
            }
        }
    }

    fun recordApplicationStarted(job: Job) {
        viewModelScope.launch {
            val res = applicationRepository.recordApplicationStarted(job)
            res.onSuccess {
                _actionEffects.emit(JobActionEffect.ShowToast("Application tracked as started for ${job.title}"))
            }
        }
    }

    fun hideJob(jobId: String) {
        _hiddenJobIds.value = _hiddenJobIds.value + jobId
        advanceCard()
        viewModelScope.launch {
            _actionEffects.emit(JobActionEffect.ShowToast("Job hidden from suggestions"))
        }
    }

    fun reportJob(jobId: String) {
        viewModelScope.launch {
            _actionEffects.emit(JobActionEffect.ShowToast("Thank you. Job reported for review."))
        }
    }

    // SEARCH & FILTERING

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.contains("salary", ignoreCase = true) ||
                query.contains("chai", ignoreCase = true) ||
                query.contains("dorkar", ignoreCase = true) ||
                query.contains("te", ignoreCase = true) ||
                query.contains("er", ignoreCase = true) ||
                Regex("""\d+k""").containsMatchIn(query)
            ) {
                // Natural Language AI query detected!
                val parsed = aiSearchParser.parseNaturalQuery(query)
                _parsedAiQuery.value = parsed
                _activeFilters.value = parsed.toJobSearchFilters()
            } else {
                _parsedAiQuery.value = null
                _activeFilters.value = _activeFilters.value.copy(query = query)
            }
            executeSearch()
        }
    }

    fun updateSectorFilter(sector: String?) {
        _activeFilters.value = _activeFilters.value.copy(sector = sector)
        executeSearch()
    }

    fun updateEmploymentTypeFilter(empType: String?) {
        _activeFilters.value = _activeFilters.value.copy(employmentType = empType)
        executeSearch()
    }

    fun updateLocationFilter(location: String?) {
        _activeFilters.value = _activeFilters.value.copy(location = location)
        executeSearch()
    }

    fun updateSortOption(sortOption: JobSortOption) {
        _activeFilters.value = _activeFilters.value.copy(sortBy = sortOption)
        executeSearch()
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _parsedAiQuery.value = null
        _activeFilters.value = JobSearchFilters()
        executeSearch()
    }

    private fun executeSearch() {
        viewModelScope.launch {
            _isLoading.value = true
            val res = jobRepository.searchJobs(_activeFilters.value.query, _activeFilters.value)
            res.onSuccess {
                _filteredJobs.value = it
            }
            _isLoading.value = false
        }
    }

    private fun applyCurrentFilters(list: List<Job>) {
        _filteredJobs.value = list
    }
}
