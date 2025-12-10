package com.example.healthapp.data.remote

import com.example.healthapp.BuildConfig
import io.ktor.client.request.post
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class RpaApi(private val provider: SupabaseClientProvider = SupabaseClientProvider) {
    private val webhook = BuildConfig.RPA_WEBHOOK_URL
    private val token = BuildConfig.RPA_TOKEN

    suspend fun trigger(workflow: String, payload: Any): Boolean {
        if (webhook.isBlank()) return false
        val resp = provider.client.post(webhook) {
            contentType(ContentType.Application.Json)
            headers {
                if (token.isNotBlank()) append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(mapOf("workflow" to workflow, "data" to payload))
        }
        return resp.status.value in 200..299
    }
}

