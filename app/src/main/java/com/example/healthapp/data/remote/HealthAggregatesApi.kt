package com.example.healthapp.data.remote

import com.example.healthapp.data.SessionManager
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class HealthAggregatesApi(private val provider: SupabaseClientProvider = SupabaseClientProvider) {
    private val base = provider.baseUrl
    private val key = provider.anonKey

    @Serializable
    data class AggRequest(val rangeDays: Int = 7)

    @Serializable
    data class AggResponse(val stepsTotal: Int, val waterTotal: Int, val sleepAvg: Double)

    suspend fun fetch(rangeDays: Int = 7): AggResponse {
        val token = SessionManager.accessToken
        val url = "$base/functions/v1/health-aggregates"
        return provider.client.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("apikey", key)
                if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(AggRequest(rangeDays))
        }.body()
    }
}

