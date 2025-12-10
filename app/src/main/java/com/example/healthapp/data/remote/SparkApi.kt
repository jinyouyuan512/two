package com.example.healthapp.data.remote

import com.example.healthapp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
 
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray

class SparkApi(
    private val client: HttpClient = SupabaseClientProvider.client,
    private val passwordProvider: () -> String = { BuildConfig.IFLYTEK_API_PASSWORD }
) {
    private val endpoint = BuildConfig.IFLYTEK_REST_ENDPOINT

    @Serializable
    data class SparkMessage(val role: String, val content: String)

    @Serializable
    data class SparkRequest(
        val model: String = BuildConfig.IFLYTEK_MODEL_DOMAIN,
        val messages: List<SparkMessage>,
        val stream: Boolean = false,
        val temperature: Double? = null,
        val top_k: Int? = null,
        val max_tokens: Int? = null
    )

    @Serializable
    data class SparkChoice(val message: SparkMessage? = null)

    @Serializable
    data class SparkResponse(
        val choices: List<SparkChoice>? = null,
        val error: JsonObject? = null
    )

    suspend fun chat(messages: List<SparkMessage>, modelOverride: String? = null): String {
        val password = passwordProvider()
        require(!password.isNullOrBlank()) { "IFLYTEK_API_PASSWORD not set" }
        val model = modelOverride?.takeIf { it.isNotBlank() } ?: BuildConfig.IFLYTEK_MODEL_DOMAIN
        val req = SparkRequest(model = model, messages = messages, stream = false)
        val text: String = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $password")
            setBody(req)
        }.bodyAsText()
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val obj = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull()
        val choices = obj?.get("choices")?.jsonArray
        val firstMsg = choices?.firstOrNull()?.jsonObject?.get("message")?.jsonObject
        val content = firstMsg?.get("content")?.jsonPrimitive?.contentOrNull
        if (!content.isNullOrBlank()) return content
        val err = obj?.get("error")?.jsonObject
        val msg = err?.get("message")?.jsonPrimitive?.contentOrNull
        error("spark error: ${msg ?: "unknown"}")
    }
}
