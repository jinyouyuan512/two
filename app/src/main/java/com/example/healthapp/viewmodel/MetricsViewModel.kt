package com.example.healthapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.repository.MetricsRepository
import com.example.healthapp.data.repository.AggregatesRepository
import com.example.healthapp.model.HeartRateData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class MetricsViewModel(
    private val repo: MetricsRepository = MetricsRepository()
) : ViewModel() {
    var heartRates by mutableStateOf<List<HeartRateData>>(emptyList())
        private set

    var steps by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set

    var weights by mutableStateOf<List<Pair<String, Double>>>(emptyList())
        private set

    var waterIntake by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set

    var sleepHours by mutableStateOf<List<Pair<String, Double>>>(emptyList())
        private set

    var moods by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set

    // 冥想练习相关状态
    var meditationSessions by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set

    val totalMeditationSeconds: Int
        get() = meditationSessions.sumOf { it.second }

    val totalMeditationMinutes: Double
        get() = (totalMeditationSeconds / 60.0).let { String.format("%.1f", it).toDouble() }

    // 压力评估相关状态
    var stressAssessments by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set

    val averageStressLevel: Float
        get() = if (stressAssessments.isEmpty()) 0f else stressAssessments.map { it.second.toFloat() }.average().toFloat()

    var stepsTotal by mutableStateOf<Int?>(null)
        private set

    var waterTotal by mutableStateOf<Int?>(null)
        private set

    var sleepAvg by mutableStateOf<Double?>(null)
        private set

    enum class TimeRange { Days7, Days14, Days30 }
    var timeRange by mutableStateOf(TimeRange.Days7)
        private set

    private fun rangeDays(): Int = when (timeRange) {
        TimeRange.Days7 -> 7
        TimeRange.Days14 -> 14
        TimeRange.Days30 -> 30
    }

    val heartRatesView: List<HeartRateData>
        get() = heartRates.takeLast(rangeDays())

    val stepsView: List<Pair<String, Int>>
        get() = steps.takeLast(rangeDays())

    val weightsView: List<Pair<String, Double>>
        get() = weights.takeLast(rangeDays())

    val waterIntakeView: List<Pair<String, Int>>
        get() = waterIntake.takeLast(rangeDays())

    val sleepHoursView: List<Pair<String, Double>>
        get() = sleepHours.takeLast(rangeDays())

    val currentHeartRate: Int?
        get() = heartRates.lastOrNull()?.value

    val stepsLatest: Int?
        get() = steps.lastOrNull()?.second

    val weightLatest: Double?
        get() = weights.lastOrNull()?.second

    val waterLatest: Int?
        get() = waterIntake.lastOrNull()?.second

    val sleepLatestHours: Double?
        get() = sleepHours.lastOrNull()?.second

    val moodLatest: String?
        get() = moods.lastOrNull()?.second

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val recentRecords: List<String>
        get() = buildList {
            heartRates.lastOrNull()?.let { add("心率 ${it.value} bpm · ${it.time}") }
            steps.lastOrNull()?.let { add("步数 ${it.second} · ${it.first}") }
            weights.lastOrNull()?.let { add("体重 ${String.format("%.1f", it.second)} kg · ${it.first}") }
            waterIntake.lastOrNull()?.let { add("饮水 ${it.second} ml · ${it.first}") }
            sleepHours.lastOrNull()?.let { add("睡眠 ${String.format("%.1f", it.second)} 小时 · ${it.first}") }
            moods.lastOrNull()?.let { add("情绪 ${it.second} · ${it.first}") }
        }

    fun load() {
        viewModelScope.launch {
            runCatching { repo.fetchHeartRates() }
                .onSuccess { heartRates = it }
                .onFailure { errorMessage = it.message }
            runCatching { repo.fetchSteps() }
                .onSuccess { steps = it }
                .onFailure { errorMessage = it.message }
            runCatching { repo.fetchWeights() }
                .onSuccess { weights = it }
                .onFailure { errorMessage = it.message }
            runCatching { repo.fetchWaterIntake() }
                .onSuccess { waterIntake = it }
                .onFailure { errorMessage = it.message }
            runCatching { repo.fetchSleepHours() }
                .onSuccess { sleepHours = it }
                .onFailure { errorMessage = it.message }
            runCatching { repo.fetchMoodLogs() }
                .onSuccess { moods = it }
                .onFailure { errorMessage = it.message }
            runCatching { repo.fetchMeditationSessions() }
                .onSuccess { meditationSessions = it }
                .onFailure { errorMessage = it.message }
            
            runCatching { repo.fetchStressAssessments() }
                .onSuccess { stressAssessments = it }
                .onFailure { errorMessage = it.message }

            runCatching { AggregatesRepository().fetch(7) }
                .onSuccess { agg ->
                    stepsTotal = agg.stepsTotal
                    waterTotal = agg.waterTotal
                    sleepAvg = agg.sleepAvg
                }
                .onFailure { /* ignore optional aggregates */ }
        }
    }

    fun updateTimeRange(range: TimeRange) {
        timeRange = range
        val days = when (range) {
            TimeRange.Days7 -> 7
            TimeRange.Days14 -> 14
            TimeRange.Days30 -> 30
        }
        viewModelScope.launch {
            runCatching { AggregatesRepository().fetch(days) }
                .onSuccess { agg ->
                    stepsTotal = agg.stepsTotal
                    waterTotal = agg.waterTotal
                    sleepAvg = agg.sleepAvg
                }
        }
    }

    fun addMood(mood: String, note: String? = null, score: Int? = null) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            runCatching { repo.addMood(now, mood, note, score) }
                .onSuccess {
                    runCatching { repo.fetchMoodLogs() }
                        .onSuccess { moods = it }
                }
        }
    }

    fun addMeditationSession(durationSeconds: Int, completed: Boolean) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            runCatching { repo.addMeditationSession(now, durationSeconds, completed) }
                .onSuccess {
                    runCatching { repo.fetchMeditationSessions() }
                        .onSuccess { meditationSessions = it }
                }
        }
    }

    fun addStressAssessment(level: Int, note: String? = null) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            runCatching { repo.addStressAssessment(now, level, note) }
                .onSuccess {
                    runCatching { repo.fetchStressAssessments() }
                        .onSuccess { stressAssessments = it }
                }
        }
    }
}
