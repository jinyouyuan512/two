package com.example.healthapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.healthapp.data.repository.AuthRepository
import com.example.healthapp.data.repository.AuthTokens
import com.example.healthapp.data.repository.ProfilesRepository
import com.example.healthapp.data.SessionManager
import com.example.healthapp.data.TokenStore
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
        private set

    var isRestoring by mutableStateOf(true)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        errorMessage = null
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "请输入账号和密码"
            return
        }
        isLoading = true
        viewModelScope.launch {
            runCatching { repo.login(username, password) }
                .onSuccess { tokens: AuthTokens? ->
                    val access = tokens?.accessToken
                    if (!access.isNullOrBlank()) {
                        SessionManager.accessToken = access
                        SessionManager.refreshToken = tokens?.refreshToken
                        SessionManager.expiresAt = tokens?.expiresIn?.let { System.currentTimeMillis() + it * 1000 }
                        runCatching {
                            repo.getUserId(access)
                        }.onSuccess { uid ->
                            if (!uid.isNullOrBlank()) {
                                runCatching { ProfilesRepository().ensureProfile(uid) }
                            }
                        }
                        onSuccess()
                        isLoggedIn = true
                        isRestoring = false
                    } else {
                        errorMessage = "登录失败"
                    }
                }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    fun register(username: String, password: String, confirm: String, displayName: String, onSuccess: () -> Unit) {
        errorMessage = null
        if (username.isBlank() || password.isBlank() || confirm.isBlank() || displayName.isBlank()) {
            errorMessage = "请完整填写注册信息（包括昵称）"
            return
        }
        if (password != confirm) {
            errorMessage = "两次输入的密码不一致"
            return
        }
        isLoading = true
        viewModelScope.launch {
            runCatching { repo.register(username, password, displayName) }
                .onSuccess { tokens: AuthTokens? ->
                    val access = tokens?.accessToken
                    if (!access.isNullOrBlank()) {
                        SessionManager.accessToken = access
                        SessionManager.refreshToken = tokens?.refreshToken
                        SessionManager.expiresAt = tokens?.expiresIn?.let { System.currentTimeMillis() + it * 1000 }
                        runCatching {
                            repo.getUserId(access)
                        }.onSuccess { uid ->
                            if (!uid.isNullOrBlank()) {
                                runCatching { ProfilesRepository().ensureProfile(uid, displayName.ifBlank { null }) }
                            }
                        }
                        isLoggedIn = true
                        onSuccess()
                        isRestoring = false
                    } else {
                        errorMessage = "注册成功，请前往邮箱验证后登录"
                    }
                }
                .onFailure { errorMessage = it.message }
            isLoading = false
        }
    }

    fun register(username: String, password: String, confirm: String, onSuccess: () -> Unit) {
        register(username, password, confirm, "", onSuccess)
    }

    fun restoreSession(context: Context, onRestored: () -> Unit = {}) {
        viewModelScope.launch {
            isRestoring = true
            val (access, refresh, expiresAt) = TokenStore.load(context)
            SessionManager.accessToken = access
            SessionManager.refreshToken = refresh
            SessionManager.expiresAt = expiresAt
            val now = System.currentTimeMillis()
            val expired = expiresAt != null && expiresAt <= now
            if (expired && !refresh.isNullOrBlank()) {
                runCatching { repo.refresh(refresh) }
                    .onSuccess { tokens: AuthTokens? ->
                        val newAccess = tokens?.accessToken
                        if (!newAccess.isNullOrBlank()) {
                            SessionManager.accessToken = newAccess
                            SessionManager.refreshToken = tokens?.refreshToken
                            SessionManager.expiresAt = tokens?.expiresIn?.let { System.currentTimeMillis() + it * 1000 }
                            TokenStore.save(context, SessionManager.accessToken, SessionManager.refreshToken, SessionManager.expiresAt)
                        }
                    }
            }
            isLoggedIn = !SessionManager.accessToken.isNullOrBlank()
            onRestored()
            isRestoring = false
        }
    }

    fun persistSession(context: Context) {
        viewModelScope.launch {
            TokenStore.save(context, SessionManager.accessToken, SessionManager.refreshToken, SessionManager.expiresAt)
        }
    }

    fun logout(context: Context, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val token = SessionManager.accessToken
            runCatching {
                if (!token.isNullOrBlank()) {
                    com.example.healthapp.data.remote.AuthApi().logout(token)
                }
            }
            SessionManager.accessToken = null
            SessionManager.refreshToken = null
            SessionManager.expiresAt = null
            TokenStore.clear(context)
            isLoggedIn = false
            isRestoring = false
            onSuccess()
        }
    }
}
