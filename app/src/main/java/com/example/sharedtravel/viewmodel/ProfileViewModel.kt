package com.example.sharedtravel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharedtravel.data.model.User
import com.example.sharedtravel.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {
    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    // Observe the current user's profile
    val userProfile: StateFlow<User?> = auth.currentUser?.uid?.let { uid ->
        repository.getUserProfileFlow(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } ?: MutableStateFlow(null)

    /**
     * Updates or creates the user profile.
     */
    fun saveProfile(firstName: String, lastName: String, drivingSince: String) {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""
        
        viewModelScope.launch {
            _uiState.value = ProfileState.Loading
            try {
                // Keep existing rating/reviews if they exist, or use defaults
                val current = userProfile.value
                val newUser = User(
                    uid = uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    drivingSinceYear = drivingSince,
                    averageRating = current?.averageRating ?: 5.0,
                    totalReviews = current?.totalReviews ?: 0,
                    phone = current?.phone ?: "",
                    university = current?.university ?: ""
                )
                repository.saveUserProfile(newUser)
                _uiState.value = ProfileState.Success
            } catch (e: Exception) {
                _uiState.value = ProfileState.Error(e.localizedMessage ?: "Failed to save profile")
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun resetState() {
        _uiState.value = ProfileState.Idle
    }
}
