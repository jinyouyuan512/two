package com.example.healthapp.data.remote

import com.example.healthapp.data.SessionManager
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class ProfilesApi(private val provider: SupabaseClientProvider = SupabaseClientProvider) {
    private val base = provider.baseUrl
    private val key = provider.anonKey

    @Serializable
    data class ProfileRow(
        val id: String,
        val display_name: String? = null,
        val avatar_url: String? = null
    )

    suspend fun upsertProfile(userId: String, displayName: String? = null, avatarUrl: String? = null): ProfileRow {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/profiles?on_conflict=id"
        val payload = ProfileRow(id = userId, display_name = displayName, avatar_url = avatarUrl)
        // Supabase returns an array when using return=representation
        val result: List<ProfileRow> = provider.client.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("apikey", key)
                append("Prefer", "return=representation,resolution=merge-duplicates")
                if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(payload)
        }.body()
        // Return the first element of the array
        return result.first()
    }

    suspend fun getProfile(userId: String): ProfileRow? {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/profiles?id=eq.$userId&select=id,display_name,avatar_url"
        val list: List<ProfileRow> = AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
        return list.firstOrNull()
    }
}
