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

class ImportsApi(private val provider: SupabaseClientProvider = SupabaseClientProvider) {
    private val base = provider.baseUrl
    private val key = provider.anonKey

    @Serializable
    data class ImportRow(
        val id: Long? = null,
        val user_id: String? = null,
        val filename: String? = null,
        val source: String? = null,
        val rows_count: Int? = null,
        val status: String? = null
    )

    suspend fun createImport(filename: String?, source: String?, rowsCount: Int, status: String = "pending"): ImportRow {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/imports"
        val uid = token?.let { runCatching { AuthApi(provider).getUser(it).id }.getOrNull() }
        val payload = ImportRow(user_id = uid, filename = filename, source = source, rows_count = rowsCount, status = status)
        return AuthRefreshHelper.withRefresh {
            val resp = provider.client.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                setBody(payload)
            }
            runCatching { resp.body<List<ImportRow>>().firstOrNull() }
                .getOrElse { runCatching { resp.body<ImportRow>() }.getOrNull() }
                ?: ImportRow()
        }
    }
}
