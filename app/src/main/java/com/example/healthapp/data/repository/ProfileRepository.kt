package com.example.healthapp.data.repository

import com.example.healthapp.data.SessionManager
import com.example.healthapp.data.remote.AuthApi
import com.example.healthapp.data.remote.ProfilesApi
import com.example.healthapp.model.UserProfile

class ProfileRepository(
    private val authApi: AuthApi = AuthApi(),
    private val profilesApi: ProfilesApi = ProfilesApi()
) {
    suspend fun getProfile(): UserProfile? {
        val access = SessionManager.accessToken ?: return null
        val uid = runCatching { authApi.getUser(access).id }.getOrNull() ?: return null
        val row = runCatching { profilesApi.getProfile(uid) }.getOrNull()
        return row?.let {
            UserProfile(
                name = it.display_name ?: "用户",
                id = it.id,
                avatar = it.avatar_url ?: "",
                consecutiveDays = 0,
                exerciseCount = 0,
                healthPoints = 0
            )
        }
    }

    suspend fun updateProfile(profile: UserProfile) {
        val access = SessionManager.accessToken ?: return
        val uid = runCatching { authApi.getUser(access).id }.getOrNull() ?: return
        runCatching { profilesApi.upsertProfile(uid, profile.name, profile.avatar) }
    }
}

