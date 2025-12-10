package com.example.healthapp.data.remote

import com.example.healthapp.data.SessionManager
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

object AuthRefreshHelper {
    suspend fun <T> withRefresh(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: ClientRequestException) {
            val status = e.response.status
            val refresh = SessionManager.refreshToken
            if (status == HttpStatusCode.Unauthorized && !refresh.isNullOrBlank()) {
                val api = AuthApi()
                val res = api.refresh(refresh)
                SessionManager.accessToken = res.access_token
                SessionManager.refreshToken = res.refresh_token
                SessionManager.expiresAt = res.expires_in?.let { System.currentTimeMillis() + it * 1000 }
                block()
            } else {
                throw e
            }
        }
    }
}

