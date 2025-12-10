package com.example.healthapp.data.repository

import com.example.healthapp.data.remote.AuthApi
import com.example.healthapp.data.remote.AuthApi.UserResponse
import kotlin.system.measureTimeMillis

data class AuthTokens(
    val accessToken: String?,
    val refreshToken: String?,
    val expiresIn: Long?
)

class AuthRepository(private val api: AuthApi = AuthApi()) {
    suspend fun login(email: String, password: String): AuthTokens? {
        val res = api.login(email, password)
        return AuthTokens(res.access_token, res.refresh_token, res.expires_in)
    }

    suspend fun register(email: String, password: String, displayName: String? = null): AuthTokens? {
        val res = api.signup(email, password, displayName)
        val tokens = res?.let { AuthTokens(it.access_token, it.refresh_token, it.expires_in) }
        return tokens ?: runCatching { login(email, password) }.getOrNull()
    }

    suspend fun refresh(refreshToken: String): AuthTokens? {
        val res = api.refresh(refreshToken)
        return AuthTokens(res.access_token, res.refresh_token, res.expires_in)
    }

    suspend fun getUserId(accessToken: String): String? {
        val u: UserResponse = api.getUser(accessToken)
        return u.id
    }

    suspend fun getUser(accessToken: String): UserResponse {
        return api.getUser(accessToken)
    }
}
