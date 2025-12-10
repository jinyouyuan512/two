package com.example.healthapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.SessionManager
import com.example.healthapp.data.repository.AuthRepository
import com.example.healthapp.data.repository.ProfilesRepository
import com.example.healthapp.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val profilesRepo: ProfilesRepository = ProfilesRepository(),
    private val profileRepo: ProfileRepository = ProfileRepository()
) : ViewModel() {
    var userId by mutableStateOf<String?>(null)
        private set
    var displayName by mutableStateOf<String?>(null)
        private set
    var displayNameDraft by mutableStateOf("")
        private set
    var consecutiveDays by mutableStateOf<Int?>(null)
        private set
    var exerciseCount by mutableStateOf<Int?>(null)
        private set
    var healthPoints by mutableStateOf<Int?>(null)
        private set

    fun load() {
        val token = SessionManager.accessToken
        if (token.isNullOrBlank()) return
        viewModelScope.launch {
            runCatching { authRepo.getUser(token) }
                .onSuccess { user ->
                    userId = user.id
                    val authName = user.user_metadata?.display_name 
                        ?: user.user_metadata?.full_name 
                        ?: user.user_metadata?.name
                    
                    if (!user.id.isNullOrBlank()) {
                        runCatching { profilesRepo.getProfile(user.id) }
                            .onSuccess { row ->
                                // Prefer Auth metadata name, fallback to Profiles table name
                                val name = authName?.takeIf { it.isNotBlank() } ?: row?.display_name
                                displayName = name
                                displayNameDraft = name ?: ""
                            }
                        runCatching { profileRepo.getProfile() }
                            .onSuccess { p ->
                                if (p != null) {
                                    consecutiveDays = p.consecutiveDays
                                    exerciseCount = p.exerciseCount
                                    healthPoints = p.healthPoints
                                }
                            }
                    }
                }
        }
    }

    fun updateDisplayNameDraft(name: String) {
        displayNameDraft = name
    }

    fun updateDisplayName() {
        val uid = userId ?: return
        val name = displayNameDraft.ifBlank { null }
        viewModelScope.launch {
            runCatching { profilesRepo.ensureProfile(uid, name) }
                .onSuccess {
                    displayName = displayNameDraft
                }
        }
    }
}
