package com.example.healthapp.data.repository

import com.example.healthapp.data.remote.PostgrestApi
import com.example.healthapp.model.HeartRateData
import kotlin.math.roundToInt

class MetricsRepository(private val api: PostgrestApi = PostgrestApi()) {
    suspend fun fetchHeartRates(): List<HeartRateData> {
        return api.heartRates().map { HeartRateData(it.at, it.bpm) }.reversed()
    }

    suspend fun fetchSteps(): List<Pair<String, Int>> {
        return api.steps().map { it.at to it.count }.reversed()
    }

    suspend fun fetchWeights(): List<Pair<String, Double>> {
        return api.weights().map { it.at to it.kg }.reversed()
    }

    suspend fun fetchWaterIntake(): List<Pair<String, Int>> {
        return api.waterIntake().map { it.at to it.ml }.reversed()
    }

    suspend fun fetchSleepHours(): List<Pair<String, Double>> {
        return api.sleepSessions().map { it.at to it.hours }.reversed()
    }

    suspend fun fetchMoodLogs(): List<Pair<String, String>> {
        return api.moodLogs().map { it.at to it.mood }.reversed()
    }

    // 冥想练习相关方法
    suspend fun fetchMeditationSessions(): List<Pair<String, Int>> {
        return api.meditationSessions().map { it.at to it.duration_seconds }.reversed()
    }

    suspend fun addMeditationSession(at: String, durationSeconds: Int, completed: Boolean): Boolean {
        return api.insertMeditationSessionForCurrentUser(at, durationSeconds, completed)
    }

    // 压力评估相关方法
    suspend fun fetchStressAssessments(): List<Pair<String, Int>> {
        return api.stressAssessments().map { it.at to it.level }.reversed()
    }

    suspend fun addStressAssessment(at: String, level: Int, note: String? = null): Boolean {
        return api.insertStressAssessmentForCurrentUser(at, level, note)
    }

    suspend fun addMood(at: String, mood: String, note: String? = null, score: Int? = null): Boolean {
        return api.insertMoodLogForCurrentUser(at, mood, note, score)
    }
}
