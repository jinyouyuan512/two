package com.example.healthapp.data.remote

import com.example.healthapp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class SparkWsApi(
    private val appIdProvider: () -> String = { BuildConfig.IFLYTEK_APP_ID },
    private val apiKeyProvider: () -> String = { BuildConfig.IFLYTEK_API_KEY },
    private val apiSecretProvider: () -> String = { BuildConfig.IFLYTEK_API_SECRET }
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient(Android) {
        install(WebSockets)
        install(ContentNegotiation) { json(json) }
    }

    @Serializable
    data class Msg(val role: String, val content: String)

    suspend fun chat(messages: List<Msg>, model: String = BuildConfig.IFLYTEK_MODEL_DOMAIN, onToken: ((String) -> Unit)? = null): String {
        val appId = appIdProvider()
        val apiKey = apiKeyProvider()
        val apiSecret = apiSecretProvider()
        require(appId.isNotBlank() && apiKey.isNotBlank() && apiSecret.isNotBlank())
        val host = BuildConfig.IFLYTEK_WS_HOST
        val path = BuildConfig.IFLYTEK_WS_PATH
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        val date = sdf.format(Date())
        val signStr = "host: $host\ndate: $date\nGET $path HTTP/1.1"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(apiSecret.toByteArray(), "HmacSHA256"))
        val signature = Base64.encodeToString(mac.doFinal(signStr.toByteArray()), Base64.NO_WRAP)
        val authorization = "api_key=\"$apiKey\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"$signature\""
        val auth64 = Base64.encodeToString(authorization.toByteArray(), Base64.NO_WRAP)
        val url = "wss://$host$path?authorization=${URLEncoder.encode(auth64, "UTF-8")}&date=${URLEncoder.encode(date, "UTF-8")}&host=$host"

        var result = StringBuilder()
        var errCode: Int? = null
        var errMessage: String? = null
        try {
            client.webSocket(urlString = url) {
                val payload = buildRequest(appId, model, messages)
                send(Frame.Text(payload))
                for (frame in incoming) {
                    val text = (frame as? Frame.Text)?.readText() ?: continue
                    val obj = json.parseToJsonElement(text).jsonObject
                    val header = obj["header"]?.jsonObject
                    val status = header?.get("status")?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                    val code = header?.get("code")?.jsonPrimitive?.contentOrNull?.toIntOrNull()
                    val msg = header?.get("message")?.jsonPrimitive?.contentOrNull
                    val payloadObj = obj["payload"]?.jsonObject
                    val choices = payloadObj?.get("choices")?.jsonObject
                    val texts = choices?.get("text")?.jsonArray
                    val first = texts?.firstOrNull()?.jsonObject
                    val content = first?.get("content")?.jsonPrimitive?.contentOrNull
                    if (!content.isNullOrBlank()) {
                        result.append(content)
                        onToken?.invoke(content)
                    }
                    if (code != null && code != 0) {
                        errCode = code
                        errMessage = msg
                        break
                    }
                    if (status == 2) break
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("星火WS连接失败: ${e.message}")
        }
        val out = result.toString()
        if (out.isBlank()) {
            throw IllegalStateException(errMessage ?: "星火WS调用失败${errCode?.let { "(code=$it)" } ?: ""}")
        }
        return out
    }

    private fun buildRequest(appId: String, model: String, messages: List<Msg>): String {
        val uid = UUID.randomUUID().toString()
        val msgArr = messages.joinToString(prefix = "[", postfix = "]") { "{\"role\":\"${it.role}\",\"content\":\"${escape(it.content)}\"}" }
        return "{" +
            "\"header\":{\"app_id\":\"$appId\",\"uid\":\"$uid\"}," +
            "\"parameter\":{\"chat\":{\"domain\":\"$model\"}}," +
            "\"payload\":{\"message\":{\"text\":$msgArr}}" +
            "}"
    }

    private fun escape(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
}
