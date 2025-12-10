package com.example.healthapp.data.remote

import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.client.plugins.ClientRequestException
import kotlinx.serialization.Serializable

class AuthApi(private val clientProvider: SupabaseClientProvider = SupabaseClientProvider) {
    private val base = clientProvider.baseUrl
    private val key = clientProvider.anonKey

    @Serializable
    data class LoginRequest(val email: String, val password: String)

    @Serializable
    data class SignupRequest(
        val email: String, 
        val password: String,
        val data: Map<String, String>? = null
    )

    @Serializable
    data class TokenResponse(
        val access_token: String?,
        val refresh_token: String? = null,
        val token_type: String? = null,
        val expires_in: Long? = null,
        val user: UserResponse? = null
    )

    @Serializable
    data class RefreshRequest(val refresh_token: String)

    @Serializable
    data class UserMetadata(
        val display_name: String? = null,
        val full_name: String? = null,
        val name: String? = null
    )

    @Serializable
    data class UserResponse(
        val id: String? = null,
        val user_metadata: UserMetadata? = null
    )

    suspend fun login(email: String, password: String): TokenResponse {
        val url = "$base/auth/v1/token?grant_type=password"
        return clientProvider.client.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                append("apikey", key)
            }
            setBody(LoginRequest(email, password))
        }.let { resp ->
            parseTokenResponse(resp)
        }
    }

    suspend fun signup(email: String, password: String, displayName: String? = null): TokenResponse? {
        val url = "$base/auth/v1/signup"
        return try {
            val resp: HttpResponse = clientProvider.client.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", key)
                }
                val dataMap = if (!displayName.isNullOrBlank()) mapOf("display_name" to displayName) else null
                setBody(SignupRequest(email, password, dataMap))
            }
            runCatching { parseTokenResponse(resp) }.getOrNull()
        } catch (e: ClientRequestException) {
            val text = e.response.bodyAsText()
            val msg = mapError(text)
            throw IllegalStateException(msg)
        }
    }

    suspend fun refresh(refreshToken: String): TokenResponse {
        val url = "$base/auth/v1/token?grant_type=refresh_token"
        return clientProvider.client.post(url) {
            contentType(ContentType.Application.Json)
            headers { append("apikey", key) }
            setBody(RefreshRequest(refreshToken))
        }.let { resp ->
            parseTokenResponse(resp)
        }
    }

    suspend fun getUser(accessToken: String): UserResponse {
        val url = "$base/auth/v1/user"
        return clientProvider.client.get(url) {
            headers {
                append("apikey", key)
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }.body()
    }

    suspend fun logout(accessToken: String) {
        val url = "$base/auth/v1/logout"
        clientProvider.client.post(url) {
            headers {
                append("apikey", key)
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }
    }

    private suspend fun parseTokenResponse(resp: HttpResponse): TokenResponse {
        return try {
            resp.body()
        } catch (e: ClientRequestException) {
            val text = e.response.bodyAsText()
            val msg = mapError(text)
            throw IllegalStateException(msg)
        }
    }

    private fun mapError(bodyText: String): String {
        val lower = bodyText.lowercase()
        return when {
            lower.contains("invalid grant") || lower.contains("invalid login") -> "账号或密码不正确"
            lower.contains("email") && lower.contains("exists") -> "邮箱已存在"
            lower.contains("rate limit") || lower.contains("only request this once") -> "请求过于频繁，请稍后再试"
            else -> bodyText
        }
    }

    companion object {
        @JvmStatic
        fun mapErrorForTest(bodyText: String): String {
            val lower = bodyText.lowercase()
            return when {
                lower.contains("invalid grant") || lower.contains("invalid login") -> "账号或密码不正确"
                lower.contains("email") && lower.contains("exists") -> "邮箱已存在"
                lower.contains("rate limit") || lower.contains("only request this once") -> "请求过于频繁，请稍后再试"
                else -> bodyText
            }
        }
    }
}
