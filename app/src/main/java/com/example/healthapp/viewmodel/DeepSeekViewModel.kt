package com.example.healthapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.call.body
import org.json.JSONObject
import com.example.healthapp.BuildConfig

class DeepSeekViewModel : ViewModel() {
    private val client = HttpClient(Android)
    private val baseUrl = BuildConfig.LLM_BASE_URL
    private val apiKey = BuildConfig.LLM_API_KEY
    private val model = BuildConfig.AI_MODEL

    data class Totals(val calories: Int, val fiberG: Int, val carbsG: Int, val fatG: Int, val proteinG: Int)

    fun generateNutritionAdvice(
        totals: com.example.healthapp.viewmodel.NutritionViewModel.NutritionTotals,
        onDone: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val url = if (baseUrl.isNotBlank()) baseUrl else "https://api.deepseek.com/v1/chat/completions"
                val prompt = "请基于用户今天的营养摄入（卡路里=${totals.calories}，蛋白=${totals.proteinG}g，碳水=${totals.carbsG}g，脂肪=${totals.fatG}g，纤维=${totals.fiberG}g）给出3条简明的饮食建议。"
                val payload = JSONObject()
                payload.put("model", if (model.isNotBlank()) model else "deepseek-chat")
                val messages = org.json.JSONArray()
                messages.put(JSONObject(mapOf("role" to "system", "content" to "你是一位营养助手，以简短可执行建议回复")))
                messages.put(JSONObject(mapOf("role" to "user", "content" to prompt)))
                payload.put("messages", messages)
                payload.put("temperature", 0.2)
                payload.put("stream", false)
                payload.put("max_tokens", 512)
                val resp: String = client.post(url) {
                    contentType(ContentType.Application.Json)
                    if (apiKey.isNotBlank()) {
                        header("Authorization", "Bearer $apiKey")
                    }
                    setBody(payload.toString())
                }.body()
                val json = JSONObject(resp)
                val choices = json.optJSONArray("choices")
                val text = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
                    ?: choices?.optJSONObject(0)?.optString("text")
                    ?: json.optJSONObject("error")?.optString("message")
                onDone(text ?: "生成失败，请检查网络或密钥配置")
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }
}
