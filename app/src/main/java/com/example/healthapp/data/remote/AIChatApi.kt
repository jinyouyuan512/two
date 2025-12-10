package com.example.healthapp.data.remote

import com.example.healthapp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.http.*
import org.json.JSONObject
import kotlinx.serialization.Serializable

class AIChatApi(private val provider: SupabaseClientProvider = SupabaseClientProvider) {
    private val base = provider.baseUrl
    private val key = provider.anonKey
    private val difyBaseUrl = BuildConfig.DIFY_BASE_URL
    private val difyApiKey = BuildConfig.DIFY_API_KEY

    private val difyClient: HttpClient = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 100000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 100000
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }
            )
        }
    }

    @Serializable
    data class ChatMessage(val role: String, val content: String)

    @Serializable
    data class ChatRequest(
        val messages: List<ChatMessage>,
        val context: Map<String, String>,
        val model: String? = null,
        val override_base_url: String? = null,
        val override_api_key: String? = null
    )

    // Dify请求数据类（适配Dify API的正确格式）
    @Serializable
    data class DifyChatRequest(
        val inputs: Map<String, String>,
        val query: String,
        val response_mode: String = "streaming",
        val conversation_id: String? = null,
        val user: String = "healthapp_user"
    )

    data class ChatResult(val answer: String, val conversationId: String?)

    suspend fun chat(
        messages: List<ChatMessage>,
        context: Map<String, String>,
        conversationId: String?,
        onToken: ((String) -> Unit)? = null
    ): ChatResult {
        // 检查是否配置了Dify API密钥
        if (difyApiKey.isNullOrBlank()) {
            return ChatResult("Dify API密钥未配置，请检查设置", null)
        }

        // 检查是否有用户查询
        val userQuery = messages.lastOrNull { it.role == "user" }?.content
        if (userQuery.isNullOrBlank()) {
            return ChatResult("无效的查询内容", null)
        }

        // 使用正确的Dify API端点
        val difyUrl = "$difyBaseUrl/v1/chat-messages"
        return try {
            val difyRequest = DifyChatRequest(
                inputs = context,
                query = userQuery,
                response_mode = "streaming",
                conversation_id = conversationId,
                user = context["user_id"] ?: "healthapp_user"
            )

            var fullAnswer = ""
            var finalConversationId: String? = conversationId

            difyClient.preparePost(difyUrl) {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $difyApiKey")
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(difyRequest)
            }.execute { response ->
                val channel: ByteReadChannel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    if (line.startsWith("data: ")) {
                        val jsonStr = line.substring(6)
                        if (jsonStr != "[DONE]") {
                            try {
                                val json = JSONObject(jsonStr)
                                val event = json.optString("event")
                                if (event == "message" || event == "agent_message") {
                                    val answer = json.optString("answer")
                                    fullAnswer += answer
                                    onToken?.invoke(answer)
                                    
                                    if (json.has("conversation_id")) {
                                        finalConversationId = json.optString("conversation_id")
                                    }
                                } else if (event == "error") {
                                    val errorMsg = json.optString("message")
                                    return@execute ChatResult("Dify API错误: $errorMsg", null)
                                }
                            } catch (e: Exception) {
                                // ignore parse error for single line
                            }
                        }
                    }
                }
                ChatResult(fullAnswer, finalConversationId)
            }
        } catch (e: Exception) {
            // 处理所有异常，返回详细错误信息
            return ChatResult("调用Dify API失败：${e.message}", null)
        }
    }
}
