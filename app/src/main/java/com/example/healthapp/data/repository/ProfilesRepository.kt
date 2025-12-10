package com.example.healthapp.data.repository

import com.example.healthapp.data.remote.ProfilesApi

class ProfilesRepository(private val api: ProfilesApi = ProfilesApi()) {
    suspend fun ensureProfile(userId: String, displayName: String? = null): Boolean {
        // Fetch existing profile first to avoid overwriting data with nulls
        val existing = runCatching { api.getProfile(userId) }.getOrNull()
        
        // Determine the name to save: use provided name, or fallback to existing name
        val nameToSave = displayName ?: existing?.display_name
        
        // Determine the avatar to save: preserve existing avatar
        val avatarToSave = existing?.avatar_url
        
        val row = api.upsertProfile(userId, nameToSave, avatarToSave)
        return row.id.isNotBlank()
    }

    suspend fun getProfile(userId: String): ProfilesApi.ProfileRow? {
        return api.getProfile(userId)
    }
}
