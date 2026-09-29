package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Application
import com.example.data.model.ApplicationStatus
import com.example.data.model.ApplicationSummary
import com.example.data.repository.ApplicationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApplicationViewModel(
    private val applicationRepository: ApplicationRepository
) : ViewModel() {

    private val _selectedStatusFilter = MutableStateFlow<ApplicationStatus?>(null)
    val selectedStatusFilter: StateFlow<ApplicationStatus?> = _selectedStatusFilter.asStateFlow()

    val applications: StateFlow<List<Application>> = applicationRepository.getAllApplications()
        .combine(_selectedStatusFilter) { apps, filter ->
            if (filter == null) apps else apps.filter { it.status == filter }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<ApplicationSummary> = applicationRepository.getApplicationSummary()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ApplicationSummary(0, 0, 0, 0, 0)
        )

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    fun setStatusFilter(status: ApplicationStatus?) {
        _selectedStatusFilter.value = status
    }

    fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus) {
        viewModelScope.launch {
            applicationRepository.updateStatus(applicationId, newStatus)
            _toastEvent.emit("Status updated to ${newStatus.label}")
        }
    }

    fun withdrawApplication(applicationId: String) {
        viewModelScope.launch {
            applicationRepository.deleteApplication(applicationId)
            _toastEvent.emit("Application withdrawn")
        }
    }
}
