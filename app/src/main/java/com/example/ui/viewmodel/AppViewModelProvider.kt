package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.engine.AiBackendService
import com.example.data.engine.MatchScoringEngine
import com.example.data.engine.ProductionAiBackendClient
import com.example.data.firebase.FirebaseManager
import com.example.data.local.AppDatabase
import com.example.data.repository.ApplicationRepository
import com.example.data.repository.ApplicationRepositoryImpl
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.JobRepository
import com.example.data.repository.JobRepositoryImpl
import com.example.data.repository.NotificationRepository
import com.example.data.repository.NotificationRepositoryImpl
import com.example.data.repository.UserRepository
import com.example.data.repository.UserRepositoryImpl
import com.example.data.source.JobSourceRegistry

class AppContainer(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val firebaseManager = FirebaseManager(context)
    val scoringEngine = MatchScoringEngine()
    val aiBackendService: AiBackendService = ProductionAiBackendClient()
    val jobSourceRegistry = JobSourceRegistry()

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userProfileDao(), firebaseManager)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(firebaseManager, userRepository)
    }

    val jobRepository: JobRepository by lazy {
        JobRepositoryImpl(
            jobSourceRegistry = jobSourceRegistry,
            savedJobDao = database.savedJobDao(),
            firebaseManager = firebaseManager,
            userRepository = userRepository,
            scoringEngine = scoringEngine
        )
    }

    val applicationRepository: ApplicationRepository by lazy {
        ApplicationRepositoryImpl(database.applicationDao(), firebaseManager)
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(database.notificationDao())
    }
}

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(container.authRepository) as T
            }
            modelClass.isAssignableFrom(JobViewModel::class.java) -> {
                JobViewModel(container.jobRepository, container.applicationRepository) as T
            }
            modelClass.isAssignableFrom(ApplicationViewModel::class.java) -> {
                ApplicationViewModel(container.applicationRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(container.userRepository, container.aiBackendService) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(container.notificationRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
