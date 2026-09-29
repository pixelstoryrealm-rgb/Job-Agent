package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthState
import com.example.data.repository.AuthStateStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AuthState(AuthStateStatus.AUTHENTICATED, authRepository.currentUser)
        )

    private val _resetPasswordSent = MutableStateFlow(false)
    val resetPasswordSent: StateFlow<Boolean> = _resetPasswordSent.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || !email.contains("@")) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }

        viewModelScope.launch {
            _errorMessage.value = null
            val result = authRepository.login(email.trim(), pass)
            result.onFailure {
                _errorMessage.value = it.localizedMessage ?: "Sign in failed"
            }
        }
    }

    fun register(name: String, email: String, phone: String, pass: String, confirmPass: String) {
        if (name.isBlank()) {
            _errorMessage.value = "Please enter your full name"
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }
        if (pass != confirmPass) {
            _errorMessage.value = "Passwords do not match"
            return
        }

        viewModelScope.launch {
            _errorMessage.value = null
            val result = authRepository.register(name.trim(), email.trim(), phone.trim(), pass)
            result.onFailure {
                _errorMessage.value = it.localizedMessage ?: "Registration failed"
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _errorMessage.value = "Please enter your registered email"
            return
        }
        viewModelScope.launch {
            _errorMessage.value = null
            val res = authRepository.sendPasswordReset(email.trim())
            res.onSuccess {
                _resetPasswordSent.value = true
            }.onFailure {
                _errorMessage.value = it.localizedMessage ?: "Failed to dispatch reset link"
            }
        }
    }

    fun resetError() {
        _errorMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
