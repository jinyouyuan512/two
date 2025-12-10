package com.example.healthapp.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import com.example.healthapp.data.SessionManager

class DailyTipsApi(private val provider: SupabaseClientProvider = SupabaseClientProvider) {
    private val base = provider.baseUrl
    private val key = provider.anonKey

    @Serializable
    data class DailyTipRow(
        val id: Long? = null,
        val content: String,
        val tip_date: String,
        val created_at: String? = null
    )

    suspend fun getTipForDate(date: String): List<DailyTipRow> {
        val url = "$base/rest/v1/daily_tips?tip_date=eq.$date&select=*"
        return provider.client.get(url) {
            headers {
                append("apikey", key)
                val token = SessionManager.accessToken
                if (!token.isNullOrBlank()) {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }.body()
    }

    suspend fun getFirstTip(): List<DailyTipRow> {
        val url = "$base/rest/v1/daily_tips?select=*&order=id.asc&limit=1"
        return provider.client.get(url) {
            headers {
                append("apikey", key)
                val token = SessionManager.accessToken
                if (!token.isNullOrBlank()) {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }.body()
    }
}
