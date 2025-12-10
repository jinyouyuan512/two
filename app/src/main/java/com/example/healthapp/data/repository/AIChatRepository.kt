package com.example.healthapp.data.repository

import com.example.healthapp.data.remote.AIChatApi
import com.example.healthapp.data.remote.SparkApi
import com.example.healthapp.data.remote.SparkWsApi
import com.example.healthapp.model.AIChatMessage
import com.example.healthapp.BuildConfig
import com.example.healthapp.data.SessionManager
import com.example.healthapp.data.repository.AuthRepository
import android.content.Context
import android.provider.Settings
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("UNUSED_PARAMETER")
class AIChatRepository(
    private val api: AIChatApi = AIChatApi(),
    private val sparkApi: SparkApi = SparkApi(),
    private val sparkWsApi: SparkWsApi = SparkWsApi(),
    private val metricsRepository: MetricsRepository = MetricsRepository()
) {
    data class Reply(val text: String, val source: String)

    private var conversationId: String? = null

    @Suppress("UNUSED_PARAMETER")
    suspend fun sendMessage(
        history: List<AIChatMessage>,
        input: String,
        onToken: ((String) -> Unit)? = null,
        force: Boolean = false,
        preferStreaming: Boolean = true,
        context: Context
    ): Reply {
        return withContext(Dispatchers.IO) {
            val ctx = buildContext(context)
            val msgs = mutableListOf<AIChatApi.ChatMessage>()
            val sys = AIChatApi.ChatMessage("system", buildSystemPrompt(ctx))
            msgs.add(sys)
            history.takeLast(10).forEach { m ->
                msgs.add(AIChatApi.ChatMessage(if (m.isUser) "user" else "assistant", m.message))
            }
            msgs.add(AIChatApi.ChatMessage("user", input))
            
            // 只专注于Dify API调用
            try {
                val result = api.chat(msgs, ctx, conversationId, onToken)
                if (!result.conversationId.isNullOrBlank()) {
                    conversationId = result.conversationId
                }
                return@withContext Reply(result.answer, "dify")
            } catch (e: Exception) {
                // 捕获并处理Dify API调用异常
                val errorMsg = "Dify API调用失败：${e.message}"
                return@withContext Reply(errorMsg, "dify_error")
            }
        }
    }

    private suspend fun buildContext(context: Context): Map<String, String> {
        return runCatching {
            val steps = metricsRepository.fetchSteps().takeLast(7)
            val heart = metricsRepository.fetchHeartRates().takeLast(5)
            val weights = metricsRepository.fetchWeights().takeLast(7)
            val waters = metricsRepository.fetchWaterIntake().takeLast(7)
            val sleeps = metricsRepository.fetchSleepHours().takeLast(7)
            val moods = metricsRepository.fetchMoodLogs().takeLast(1)
            val avgSteps = if (steps.isNotEmpty()) steps.map { it.second }.average().toInt() else 0
            val lastHeart = heart.lastOrNull()?.value ?: 0
            val sleepAvg = if (sleeps.isNotEmpty()) sleeps.map { it.second }.average() else 0.0
            val waterTotal = if (waters.isNotEmpty()) waters.sumOf { it.second } else 0
            val lastMood = moods.lastOrNull()?.second ?: ""
            val lastWeight = weights.lastOrNull()?.second ?: 0.0
            val avgWeight = if (weights.isNotEmpty()) weights.map { it.second }.average() else 0.0
            val weightDelta7d = (lastWeight - avgWeight)

            val ctx = mutableMapOf(
                "avg_steps_7d" to avgSteps.toString(),
                "last_heart_bpm" to lastHeart.toString(),
                "sleep_avg_7d" to String.format("%.2f", sleepAvg),
                "water_total_7d" to waterTotal.toString(),
                "mood_last" to lastMood,
                "weight_last" to String.format("%.2f", lastWeight),
                "weight_delta_7d" to String.format("%.2f", weightDelta7d)
            )

            val access = SessionManager.accessToken
            val uid = if (!access.isNullOrBlank()) runCatching { AuthRepository().getUserId(access) }.getOrNull() else null
            val deviceId = runCatching { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) }.getOrNull()
            val fallback = deviceId ?: UUID.randomUUID().toString()
            ctx["user_id"] = uid ?: fallback

            ctx
        }.getOrElse { emptyMap() }
    }

    private fun fallbackResponse(input: String, context: Map<String, String>): String {
        val steps = context["avg_steps_7d"]?.toIntOrNull() ?: 0
        val bpm = context["last_heart_bpm"]?.toIntOrNull() ?: 0
        return when {
            input.contains("运动") || input.contains("锻炼") -> {
                val add = if (steps < 6000) "本周步数偏低，建议每天增加20-30分钟快走。" else "保持当前运动量，并加入力量训练提升心肺耐力。"
                "建议进行30分钟中等强度运动，如快走或骑行。$add"
            }
            input.contains("饮食") || input.contains("食谱") -> {
                "推荐清淡均衡饮食：早餐燕麦水果，午餐鸡胸蔬菜沙拉，晚餐蒸鱼配糙米。"
            }
            input.contains("睡眠") || input.contains("作息") -> {
                "保持固定作息，睡前避免电子设备，营造安静、凉爽的睡眠环境。"
            }
            input.contains("心率") || input.contains("心脏") -> {
                if (bpm > 95) "当前静息心率偏高，建议减压与规律运动，并关注咖啡因摄入。" else "静息心率稳定，维持规律有氧运动与充足睡眠。"
            }
            else -> {
                "我可以根据你的数据提供运动、营养、睡眠等建议，请具体说明你的问题。"
            }
        }
    }

    private fun buildSystemPrompt(context: Map<String, String>): String {
        val avgSteps = context["avg_steps_7d"] ?: ""
        val lastHeart = context["last_heart_bpm"] ?: ""
        val sleepAvg = context["sleep_avg_7d"] ?: ""
        val waterTotal = context["water_total_7d"] ?: ""
        val moodLast = context["mood_last"] ?: ""
        val weightLast = context["weight_last"] ?: ""
        val weightDelta = context["weight_delta_7d"] ?: ""
        return "你是循证的中文健康助手，提供非医疗诊断的生活方式建议。输出结构包含：现状总结、目标设定、运动计划（频次/时长/心率或RPE）、饮食建议（宏量比例与示例菜单）、睡眠与作息、监测与观察点、风险与就医建议、本周任务清单。请结合用户近期数据：步数均值" +
            avgSteps + "，最近静息心率" + lastHeart + "，近7天平均睡眠" + sleepAvg + "小时，近7天饮水总量" + waterTotal + "ml，最近情绪" + moodLast + "，最近体重" + weightLast + "kg，体重相对均值变化" + weightDelta + "kg。遇到红旗症状时提示就医。"
    }

    fun testFallback(input: String, context: Map<String, String>): String {
        return fallbackResponse(input, context)
    }
}
