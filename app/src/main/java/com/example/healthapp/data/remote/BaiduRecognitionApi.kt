package com.example.healthapp.data.remote

import com.example.healthapp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class BaiduRecognitionApi {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                coerceInputValues = true
            })
        }
    }

    private val apiKey = BuildConfig.BAIDU_API_KEY
    private val secretKey = BuildConfig.BAIDU_SECRET_KEY
    
    private var accessToken: String? = null
    private var tokenExpiresAt: Long = 0

    @Serializable
    data class TokenResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("expires_in") val expiresIn: Long
    )

    @Serializable
    data class BaiduFoodResult(
        val name: String,
        val probability: String,
        val calorie: String? = null,
        @SerialName("has_calorie") val hasCalorie: Boolean = false
    )

    @Serializable
    data class BaiduRecognizeResponse(
        @SerialName("log_id") val logId: Long = 0,
        @SerialName("result_num") val resultNum: Int = 0,
        val result: List<BaiduFoodResult> = emptyList(),
        @SerialName("error_code") val errorCode: Int? = null,
        @SerialName("error_msg") val errorMsg: String? = null
    )

    private suspend fun getAccessToken(): String {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return accessToken!!
        }

        val url = "https://aip.baidubce.com/oauth/2.0/token"
        val response: TokenResponse = client.submitForm(
            url = url,
            formParameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("client_id", apiKey)
                append("client_secret", secretKey)
            }
        ).body()

        accessToken = response.accessToken
        // Expire a bit earlier than actual expiration to be safe (e.g., 1 minute early)
        tokenExpiresAt = System.currentTimeMillis() + (response.expiresIn * 1000) - 60000
        return response.accessToken
    }

    suspend fun recognizeFood(imageBase64: String): List<BaiduFoodResult> {
        if (apiKey.isBlank() || secretKey.isBlank()) {
            throw IllegalStateException("Baidu API Key or Secret Key is not configured")
        }

        val token = getAccessToken()
        val url = "https://aip.baidubce.com/rest/2.0/image-classify/v2/dish?access_token=$token"
        
        // Form-urlencoded body is standard for Baidu AI
        val responseStr = client.post(url) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("image=${java.net.URLEncoder.encode(imageBase64, "UTF-8")}&top_num=5")
        }.bodyAsText()

        val response = Json { ignoreUnknownKeys = true }.decodeFromString<BaiduRecognizeResponse>(responseStr)
        
        if (response.errorCode != null) {
            throw IllegalStateException("Error ${response.errorCode}: ${response.errorMsg}")
        }
        
        return response.result
    }
}
