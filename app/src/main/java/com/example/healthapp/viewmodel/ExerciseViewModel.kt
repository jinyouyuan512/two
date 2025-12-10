package com.example.healthapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.MockData
import com.example.healthapp.model.ExercisePlan
import com.example.healthapp.model.ExerciseSessionRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ExerciseViewModel : ViewModel() {
    private val _plans = MutableStateFlow<List<ExercisePlan>>(emptyList())
    val plans: StateFlow<List<ExercisePlan>> = _plans.asStateFlow()

    data class RunningSession(
        val plan: ExercisePlan,
        val startMillis: Long,
        val elapsedSeconds: Int,
        val heartRates: List<Int>,
        val steps: Int,
        val isPaused: Boolean
    )

    private val _session = MutableStateFlow<RunningSession?>(null)
    val session: StateFlow<RunningSession?> = _session.asStateFlow()

    private val _records = MutableStateFlow<List<ExerciseSessionRecord>>(emptyList())
    val records: StateFlow<List<ExerciseSessionRecord>> = _records.asStateFlow()

    private var ticker: Job? = null

    fun init() {
        if (_plans.value.isEmpty()) _plans.value = MockData.getExercisePlans()
    }

    fun addPlan(name: String, durationMinutes: Int, calories: Int) {
        _plans.value = _plans.value + ExercisePlan(name, durationMinutes, calories, "开始")
    }

    fun removePlan(name: String) {
        _plans.value = _plans.value.filterNot { it.name == name }
    }

    fun start(plan: ExercisePlan) {
        _session.value = RunningSession(plan, System.currentTimeMillis(), 0, emptyList(), 0, false)
        startTicker()
    }

    fun pause() {
        val s = _session.value ?: return
        _session.value = s.copy(isPaused = true)
        stopTicker()
    }

    fun resume() {
        val s = _session.value ?: return
        _session.value = s.copy(isPaused = false)
        startTicker()
    }

    fun stop(context: Context) {
        val s = _session.value ?: return
        stopTicker()
        val avgHr = if (s.heartRates.isEmpty()) 0 else s.heartRates.sum() / s.heartRates.size
        val durationMin = (s.elapsedSeconds / 60)
        val record = ExerciseSessionRecord(
            planName = s.plan.name,
            startTime = s.startMillis.toString(),
            endTime = System.currentTimeMillis().toString(),
            durationMinutes = durationMin,
            caloriesBurned = s.plan.calories,
            averageHeartRate = avgHr,
            steps = s.steps
        )
        _records.value = _records.value + record
        saveRecords(context)
        _session.value = null
    }

    private fun startTicker() {
        stopTicker()
        ticker = viewModelScope.launch {
            while (true) {
                delay(1000)
                val s = _session.value ?: break
                if (s.isPaused) continue
                val nextElapsed = s.elapsedSeconds + 1
                val nextHr = (120..160).random()
                val nextSteps = s.steps + (1..3).random()
                _session.value = s.copy(elapsedSeconds = nextElapsed, heartRates = s.heartRates + nextHr, steps = nextSteps)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    fun loadRecords(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECORDS, null)
        if (!raw.isNullOrBlank()) {
            runCatching { parseRecords(raw) }.onSuccess { _records.value = it }
        }
    }

    fun saveRecords(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = toJson(_records.value)
        prefs.edit().putString(KEY_RECORDS, json).apply()
    }

    companion object {
        private const val PREFS = "exercise_records_prefs"
        private const val KEY_RECORDS = "records_json"
    }

    private fun parseRecords(text: String): List<ExerciseSessionRecord> {
        val arr = JSONArray(text)
        val list = mutableListOf<ExerciseSessionRecord>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            list += ExerciseSessionRecord(
                planName = o.optString("planName"),
                startTime = o.optString("startTime"),
                endTime = o.optString("endTime"),
                durationMinutes = o.optInt("durationMinutes"),
                caloriesBurned = o.optInt("caloriesBurned"),
                averageHeartRate = o.optInt("averageHeartRate"),
                steps = o.optInt("steps")
            )
        }
        return list
    }

    private fun toJson(records: List<ExerciseSessionRecord>): String {
        val arr = JSONArray()
        records.forEach { r ->
            val o = JSONObject()
            o.put("planName", r.planName)
            o.put("startTime", r.startTime)
            o.put("endTime", r.endTime)
            o.put("durationMinutes", r.durationMinutes)
            o.put("caloriesBurned", r.caloriesBurned)
            o.put("averageHeartRate", r.averageHeartRate)
            o.put("steps", r.steps)
            arr.put(o)
        }
        return arr.toString()
    }
}
