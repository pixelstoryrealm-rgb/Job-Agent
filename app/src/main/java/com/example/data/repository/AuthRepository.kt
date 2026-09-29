package com.example.data.repository

import com.example.data.firebase.FirebaseManager
import com.example.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

enum class AuthStateStatus {
    UNAUTHENTICATED,
    AUTHENTICATING,
    AUTHENTICATED,
    ERROR
}

data class AuthState(
    val status: AuthStateStatus,
    val user: User? = null,
    val errorMessage: String? = null,
    val isFirebaseBacked: Boolean = false
)

interface AuthRepository {
    val authState: Flow<AuthState>
    val currentUser: User?
    suspend fun login(email: String, pass: String): Result<User>
    suspend fun register(name: String, email: String, phone: String, pass: String): Result<User>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun logout()
    suspend fun signInWithGoogle(idToken: String): Result<User>
}

class AuthRepositoryImpl(
    private val firebaseManager: FirebaseManager,
    private val userRepository: UserRepository
) : AuthRepository {

    private val auth: FirebaseAuth? get() = firebaseManager.auth

    private val _authState = MutableStateFlow(
        AuthState(
            status = AuthStateStatus.AUTHENTICATED,
            user = User(
                id = "user_asif_01",
                name = "Asif",
                email = "asif.engineer@example.com",
                phone = "+880 1712-345678"
            ),
            isFirebaseBacked = firebaseManager.isFirebaseConfigured
        )
    )

    override val authState: Flow<AuthState> = _authState.asStateFlow()
    override val currentUser: User? get() = _authState.value.user

    init {
        // Observe Firebase Auth state if configured
        auth?.addAuthStateListener { fbAuth ->
            val fbUser = fbAuth.currentUser
            if (fbUser != null) {
                val user = mapFirebaseUser(fbUser)
                _authState.value = AuthState(
                    status = AuthStateStatus.AUTHENTICATED,
                    user = user,
                    isFirebaseBacked = true
                )
            } else {
                _authState.value = AuthState(
                    status = AuthStateStatus.UNAUTHENTICATED,
                    isFirebaseBacked = true
                )
            }
        }
    }

    override suspend fun login(email: String, pass: String): Result<User> {
        _authState.value = _authState.value.copy(status = AuthStateStatus.AUTHENTICATING)

        val fbAuth = auth
        return if (fbAuth != null) {
            try {
                val authResult = fbAuth.signInWithEmailAndPassword(email, pass).await()
                val fbUser = authResult.user ?: throw IllegalStateException("Firebase user was null after sign in")
                val user = mapFirebaseUser(fbUser)
                _authState.value = AuthState(AuthStateStatus.AUTHENTICATED, user, isFirebaseBacked = true)
                Result.success(user)
            } catch (e: Exception) {
                _authState.value = AuthState(AuthStateStatus.ERROR, errorMessage = e.localizedMessage, isFirebaseBacked = true)
                Result.failure(e)
            }
        } else {
            // Local fallback when Firebase is awaiting google-services.json
            val localRes = userRepository.login(email, pass)
            localRes.onSuccess { user ->
                _authState.value = AuthState(AuthStateStatus.AUTHENTICATED, user, isFirebaseBacked = false)
            }.onFailure {
                _authState.value = AuthState(AuthStateStatus.ERROR, errorMessage = it.localizedMessage, isFirebaseBacked = false)
            }
            localRes
        }
    }

    override suspend fun register(name: String, email: String, phone: String, pass: String): Result<User> {
        _authState.value = _authState.value.copy(status = AuthStateStatus.AUTHENTICATING)

        val fbAuth = auth
        return if (fbAuth != null) {
            try {
                val authResult = fbAuth.createUserWithEmailAndPassword(email, pass).await()
                val fbUser = authResult.user ?: throw IllegalStateException("Firebase user was null after registration")
                val user = User(id = fbUser.uid, name = name, email = email, phone = phone)
                userRepository.updatePersonalInfo(name, "Junior Engineer", phone, "Dhaka", "")
                _authState.value = AuthState(AuthStateStatus.AUTHENTICATED, user, isFirebaseBacked = true)
                Result.success(user)
            } catch (e: Exception) {
                _authState.value = AuthState(AuthStateStatus.ERROR, errorMessage = e.localizedMessage, isFirebaseBacked = true)
                Result.failure(e)
            }
        } else {
            val localRes = userRepository.register(name, email, phone, pass)
            localRes.onSuccess { user ->
                _authState.value = AuthState(AuthStateStatus.AUTHENTICATED, user, isFirebaseBacked = false)
            }.onFailure {
                _authState.value = AuthState(AuthStateStatus.ERROR, errorMessage = it.localizedMessage, isFirebaseBacked = false)
            }
            localRes
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        val fbAuth = auth
        return if (fbAuth != null) {
            try {
                fbAuth.sendPasswordResetEmail(email).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // Local mode acknowledges reset
            Result.success(Unit)
        }
    }

    override suspend fun logout() {
        auth?.signOut()
        userRepository.logout()
        _authState.value = AuthState(
            status = AuthStateStatus.UNAUTHENTICATED,
            isFirebaseBacked = firebaseManager.isFirebaseConfigured
        )
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        // Prepares Google Credential Auth endpoint
        return Result.failure(UnsupportedOperationException("Google Sign-In requires SHA-1 fingerprint registered in Firebase Console."))
    }

    private fun mapFirebaseUser(fbUser: FirebaseUser): User {
        return User(
            id = fbUser.uid,
            name = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "User",
            email = fbUser.email ?: "",
            phone = fbUser.phoneNumber ?: "",
            avatarUrl = fbUser.photoUrl?.toString()
        )
    }
}
