package com.example.healthapp.data.remote

import android.content.Context
import android.provider.Settings
import android.util.Base64
import com.example.healthapp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.json

object BaiduAsrApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(json) }
    }

    @Serializable
    private data class TokenResp(
        @SerialName("access_token") val accessToken: String? = null,
        @SerialName("expires_in") val expiresIn: Long? = null
    )

    @Serializable
    private data class AsrResp(
        @SerialName("err_no") val errNo: Int? = null,
        @SerialName("err_msg") val errMsg: String? = null,
        @SerialName("result") val result: List<String>? = null
    )

    @Serializable
    data class AsrResult(val text: String?, val errNo: Int?, val errMsg: String?)

    @Volatile private var cachedToken: String? = null
    @Volatile private var tokenExpiryMs: Long = 0L

    private suspend fun getAccessToken(): String? {
        val now = System.currentTimeMillis()
        if (!cachedToken.isNullOrEmpty() && now < tokenExpiryMs) return cachedToken
        val ak = BuildConfig.BAIDU_API_KEY
        val sk = BuildConfig.BAIDU_SECRET_KEY
        if (ak.isNullOrEmpty() || sk.isNullOrEmpty()) return null
        val resp: TokenResp = client.get {
            url("https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials&client_id=$ak&client_secret=$sk")
        }.body()
        val token = resp.accessToken
        if (!token.isNullOrEmpty()) {
            cachedToken = token
            tokenExpiryMs = now + ((resp.expiresIn ?: 3600L) - 60L) * 1000L
        }
        return cachedToken
    }

    suspend fun recognizePcm16k(context: Context, pcmBytes: ByteArray, lang: String = "zh-CN"): String? {
        val token = getAccessToken() ?: return null
        val cuid = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "yk-health-device"
        val base64 = Base64.encodeToString(pcmBytes, Base64.NO_WRAP)
        val devPid = when (lang) {
            "en-US" -> 1737
            else -> 1537
        }
        val payload = """
            {
              "format":"pcm",
              "rate":16000,
              "dev_pid":$devPid,
              "channel":1,
              "token":"$token",
              "cuid":"$cuid",
              "len":${pcmBytes.size},
              "speech":"$base64"
            }
        """.trimIndent()
        val resp: AsrResp = client.post {
            url("https://vop.baidu.com/server_api")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()
        if (resp.errNo == 0) {
            val text = resp.result?.firstOrNull()?.trim()
            if (!text.isNullOrEmpty()) return text
        }
        return null
    }

    suspend fun recognizePcm16kDetailed(context: Context, pcmBytes: ByteArray, lang: String = "zh-CN"): AsrResult {
        val token = getAccessToken() ?: return AsrResult(null, -1, "token null")
        val cuid = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "yk-health-device"
        val base64 = Base64.encodeToString(pcmBytes, Base64.NO_WRAP)
        val devPid = when (lang) { "en-US" -> 1737; else -> 1537 }
        val payload = """
            {
              "format":"pcm",
              "rate":16000,
              "dev_pid":$devPid,
              "channel":1,
              "token":"$token",
              "cuid":"$cuid",
              "len":${pcmBytes.size},
              "speech":"$base64"
            }
        """.trimIndent()
        val resp: AsrResp = client.post {
            url("https://vop.baidu.com/server_api")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()
        val text = resp.result?.firstOrNull()?.trim()
        return AsrResult(text, resp.errNo, resp.errMsg)
    }
}
