package com.example.healthapp.data.remote

import com.example.healthapp.data.SessionManager
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class PostgrestApi(private val provider: SupabaseClientProvider = SupabaseClientProvider) {
    private val base = provider.baseUrl
    private val key = provider.anonKey

    @Serializable
    data class HeartRateRow(val at: String, val bpm: Int)

    @Serializable
    data class StepsRow(val at: String, val count: Int)

    @Serializable
    data class WeightRow(val at: String, val kg: Double)

    @Serializable
    data class WaterIntakeRow(val at: String, val ml: Int)

    @Serializable
    data class SleepSessionRow(val at: String, val hours: Double)

    @Serializable
    data class MoodLogRow(val at: String, val mood: String, val note: String? = null, val score: Int? = null)

    suspend fun heartRates(limit: Int = 20): List<HeartRateRow> {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/heart_rates?select=at,bpm&order=at.desc&limit=$limit"
        return AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
    }

    suspend fun steps(limit: Int = 7): List<StepsRow> {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/steps?select=at,count&order=at.desc&limit=$limit"
        return AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
    }

    suspend fun weights(limit: Int = 14): List<WeightRow> {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/weights?select=at,kg&order=at.desc&limit=$limit"
        return AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
    }

    suspend fun waterIntake(limit: Int = 14): List<WaterIntakeRow> {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/water_intake?select=at,ml&order=at.desc&limit=$limit"
        return AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
    }

    suspend fun sleepSessions(limit: Int = 14): List<SleepSessionRow> {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/sleep_sessions?select=at,hours&order=at.desc&limit=$limit"
        return AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
    }

    suspend fun moodLogs(limit: Int = 14): List<MoodLogRow> {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/mood_logs?select=at,mood,note,score&order=at.desc&limit=$limit"
        return AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
    }

    @Serializable
    data class StepsInsert(val user_id: String, val at: String, val count: Int)

    @Serializable
    data class HeartRateInsert(val user_id: String, val at: String, val bpm: Int)

    @Serializable
    data class SleepSessionInsert(val user_id: String, val at: String, val hours: Double)

    @Serializable
    data class WeightInsert(val user_id: String, val at: String, val kg: Double)

    @Serializable
    data class WaterIntakeInsert(val user_id: String, val at: String, val ml: Int)

    @Serializable
    data class MoodLogInsert(val user_id: String, val at: String, val mood: String, val note: String? = null, val score: Int? = null)

    // 冥想练习相关数据类
    @Serializable
    data class MeditationSessionRow(val at: String, val duration_seconds: Int, val completed: Boolean)

    @Serializable
    data class MeditationSessionInsert(val user_id: String, val at: String, val duration_seconds: Int, val completed: Boolean)

    // 压力评估相关数据类
    @Serializable
    data class StressAssessmentRow(val at: String, val level: Int, val note: String? = null)

    @Serializable
    data class StressAssessmentInsert(val user_id: String, val at: String, val level: Int, val note: String? = null)

    // 每日新闻相关数据类
    @Serializable
    data class DailyNewsRow(val id: String, val news_date: String, val published_at: String? = null, val title: String, val summary: String? = null, val content: String? = null, val url: String? = null, val source: String? = null, val created_at: String)

    @Serializable
    data class DailyNewsInsert(val news_date: String, val title: String, val summary: String? = null, val content: String? = null, val url: String? = null, val source: String? = null)

    private suspend fun currentUserId(): String? {
        val token = SessionManager.accessToken ?: return null
        return AuthApi(provider).getUser(token).id
    }

    // 每日新闻API
    suspend fun dailyNews(limit: Int = 20): List<DailyNewsRow> {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/daily_news?select=id,news_date,published_at,title,summary,content,url,source,created_at&order=published_at.desc&order=created_at.desc&limit=$limit"
        return AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
    }

    suspend fun insertDailyNews(rows: List<DailyNewsInsert>): Int {
        if (rows.isEmpty()) return 0
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/daily_news"
        return AuthRefreshHelper.withRefresh {
            provider.client.post(url) {
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(rows)
            }.body<List<DailyNewsRow>>().size
        }
    }

    suspend fun purgeDuplicateDailyNews(): Int {
        val token = SessionManager.accessToken
        val limit = 1000
        var offset = 0
        val all = mutableListOf<DailyNewsRow>()
        while (true) {
            val url = "$base/rest/v1/daily_news?select=id,news_date,published_at,title,summary,url,source,created_at&order=created_at.asc&limit=$limit&offset=$offset"
            val page = AuthRefreshHelper.withRefresh {
                provider.client.get(url) {
                    headers {
                        append("apikey", key)
                        if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }.body<List<DailyNewsRow>>()
            }
            if (page.isEmpty()) break
            all.addAll(page)
            if (page.size < limit) break
            offset += limit
        }

        val groups = mutableMapOf<String, MutableList<DailyNewsRow>>()
        for (row in all) {
            val keyPart = if (!row.url.isNullOrBlank()) "url:" + row.url!!.trim() else "ts:" + row.news_date + "|" + row.title.trim() + "|" + ((row.source ?: "").trim())
            groups.getOrPut(keyPart) { mutableListOf() }.add(row)
        }
        val toDelete = mutableListOf<String>()
        for ((_, list) in groups) {
            if (list.size <= 1) continue
            list.sortBy { it.created_at }
            for (i in 0 until list.size - 1) {
                toDelete.add(list[i].id)
            }
        }
        if (toDelete.isEmpty()) return 0
        var deleted = 0
        AuthRefreshHelper.withRefresh {
            for (id in toDelete) {
                val delUrl = "$base/rest/v1/daily_news?id=eq.$id"
                provider.client.delete(delUrl) {
                    headers {
                        append("apikey", key)
                        if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                        append("Prefer", "return=minimal")
                    }
                }
                deleted++
            }
        }
        return deleted
    }

    // 冥想练习相关API接口
    suspend fun meditationSessions(limit: Int = 14): List<MeditationSessionRow> {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/meditation_sessions?select=at,duration_seconds,completed&order=at.desc&limit=$limit"
        return AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
    }

    suspend fun insertMeditationSessions(rows: List<MeditationSessionInsert>): Int {
        if (rows.isEmpty()) return 0
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/meditation_sessions"
        return AuthRefreshHelper.withRefresh {
            provider.client.post(url) {
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(rows)
            }.body<List<MeditationSessionRow>>().size
        }
    }

    suspend fun insertMeditationSessionForCurrentUser(at: String, duration_seconds: Int, completed: Boolean): Boolean {
        val uid = currentUserId() ?: return false
        val inserted = insertMeditationSessions(listOf(MeditationSessionInsert(user_id = uid, at = at, duration_seconds = duration_seconds, completed = completed)))
        return inserted > 0
    }

    // 压力评估相关API接口
    suspend fun stressAssessments(limit: Int = 14): List<StressAssessmentRow> {
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/stress_assessments?select=at,level,note&order=at.desc&limit=$limit"
        return AuthRefreshHelper.withRefresh {
            provider.client.get(url) {
                headers {
                    append("apikey", key)
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
            }.body()
        }
    }

    suspend fun insertStressAssessments(rows: List<StressAssessmentInsert>): Int {
        if (rows.isEmpty()) return 0
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/stress_assessments"
        return AuthRefreshHelper.withRefresh {
            provider.client.post(url) {
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(rows)
            }.body<List<StressAssessmentRow>>().size
        }
    }

    suspend fun insertStressAssessmentForCurrentUser(at: String, level: Int, note: String? = null): Boolean {
        val uid = currentUserId() ?: return false
        val inserted = insertStressAssessments(listOf(StressAssessmentInsert(user_id = uid, at = at, level = level, note = note)))
        return inserted > 0
    }

    suspend fun insertSteps(rows: List<StepsInsert>): Int {
        if (rows.isEmpty()) return 0
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/steps"
        return AuthRefreshHelper.withRefresh {
            provider.client.post(url) {
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(rows)
            }.body<List<StepsRow>>().size
        }
    }

    suspend fun insertHeartRates(rows: List<HeartRateInsert>): Int {
        if (rows.isEmpty()) return 0
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/heart_rates"
        return AuthRefreshHelper.withRefresh {
            provider.client.post(url) {
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(rows)
            }.body<List<HeartRateRow>>().size
        }
    }

    suspend fun insertSleepSessions(rows: List<SleepSessionInsert>): Int {
        if (rows.isEmpty()) return 0
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/sleep_sessions"
        return AuthRefreshHelper.withRefresh {
            provider.client.post(url) {
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(rows)
            }.body<List<SleepSessionInsert>>().size
        }
    }

    suspend fun insertWeights(rows: List<WeightInsert>): Int {
        if (rows.isEmpty()) return 0
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/weights"
        return AuthRefreshHelper.withRefresh {
            provider.client.post(url) {
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(rows)
            }.body<List<WeightRow>>().size
        }
    }

    suspend fun insertWaterIntake(rows: List<WaterIntakeInsert>): Int {
        if (rows.isEmpty()) return 0
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/water_intake"
        return AuthRefreshHelper.withRefresh {
            provider.client.post(url) {
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(rows)
            }.body<List<WaterIntakeRow>>().size
        }
    }

    suspend fun insertMoodLogs(rows: List<MoodLogInsert>): Int {
        if (rows.isEmpty()) return 0
        val token = SessionManager.accessToken
        val url = "$base/rest/v1/mood_logs"
        return AuthRefreshHelper.withRefresh {
            provider.client.post(url) {
                headers {
                    append("apikey", key)
                    append("Prefer", "return=representation")
                    if (!token.isNullOrBlank()) append(HttpHeaders.Authorization, "Bearer $token")
                }
                contentType(ContentType.Application.Json)
                setBody(rows)
            }.body<List<MoodLogRow>>().size
        }
    }

    suspend fun insertMoodLogForCurrentUser(at: String, mood: String, note: String? = null, score: Int? = null): Boolean {
        val uid = currentUserId() ?: return false
        val inserted = insertMoodLogs(listOf(MoodLogInsert(user_id = uid, at = at, mood = mood, note = note, score = score)))
        return inserted > 0
    }
}
