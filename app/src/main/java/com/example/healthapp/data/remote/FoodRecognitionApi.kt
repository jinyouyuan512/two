package com.example.healthapp.data.remote

import com.example.healthapp.BuildConfig
import com.example.healthapp.data.SessionManager
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.json.JSONArray
import org.json.JSONObject

class FoodRecognitionApi(private val provider: SupabaseClientProvider = SupabaseClientProvider) {
    private val base = provider.baseUrl
    private val key = provider.anonKey

    data class RecognizeResponse(
        val foods: List<String> = emptyList(),
        val calories: Int? = null,
        val fiberG: Int? = null,
        val carbsG: Int? = null,
        val fatG: Int? = null,
        val proteinG: Int? = null
    )

    suspend fun recognize(imageBase64: String): RecognizeResponse? {
        val token = SessionManager.accessToken
        val functionPath = "food-recognize"
        val url = "$base/functions/v1/$functionPath"
        return runCatching {
            val text = provider.client.post(url) {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                setBody(mapOf("image_base64" to imageBase64))
            }.bodyAsText()
            parseResponse(text)
        }.getOrNull()
    }

    private fun parseResponse(text: String): RecognizeResponse {
        val obj = JSONObject(text)
        val foods = mutableListOf<String>()
        if (obj.has("foods")) {
            val arr = obj.opt("foods")
            when (arr) {
                is JSONArray -> {
                    for (i in 0 until arr.length()) {
                        foods += arr.optString(i)
                    }
                }
                is String -> foods += arr
            }
        }
        return RecognizeResponse(
            foods = foods,
            calories = obj.optInt("calories").takeIf { obj.has("calories") },
            fiberG = obj.optInt("fiberG").takeIf { obj.has("fiberG") },
            carbsG = obj.optInt("carbsG").takeIf { obj.has("carbsG") },
            fatG = obj.optInt("fatG").takeIf { obj.has("fatG") },
            proteinG = obj.optInt("proteinG").takeIf { obj.has("proteinG") }
        )
    }
}
