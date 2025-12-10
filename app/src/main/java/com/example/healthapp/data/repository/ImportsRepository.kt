package com.example.healthapp.data.repository

import com.example.healthapp.data.remote.ImportsApi
import com.example.healthapp.data.remote.AuthApi
import com.example.healthapp.data.SessionManager
import com.example.healthapp.data.remote.PostgrestApi
import com.example.healthapp.viewmodel.ImportedRecord
import com.example.healthapp.viewmodel.DataImportViewModel.ImportType

class ImportsRepository(
    private val api: ImportsApi = ImportsApi(),
    private val postgrest: PostgrestApi = PostgrestApi()
) {
    suspend fun createJob(filename: String?, source: String?, rowsCount: Int): Boolean {
        val row = api.createImport(filename, source, rowsCount, status = "pending")
        return (row.id ?: 0L) > 0L
    }

    suspend fun saveRecords(records: List<ImportedRecord>): SaveResult {
        val access = SessionManager.accessToken
        val uid = if (!access.isNullOrBlank()) AuthApi().getUser(access).id else null
        if (uid.isNullOrBlank()) return SaveResult(0, 0, 0, 0, 0, 0)
        val steps = records.mapNotNull { r ->
            val at = r.date ?: return@mapNotNull null
            val count = r.steps ?: return@mapNotNull null
            PostgrestApi.StepsInsert(user_id = uid, at = at, count = count)
        }
        val hearts = records.mapNotNull { r ->
            val at = r.date ?: return@mapNotNull null
            val bpm = r.heartRate ?: return@mapNotNull null
            PostgrestApi.HeartRateInsert(user_id = uid, at = at, bpm = bpm)
        }
        val sleeps = records.mapNotNull { r ->
            val at = r.date ?: return@mapNotNull null
            val hours = r.sleepHours ?: return@mapNotNull null
            PostgrestApi.SleepSessionInsert(user_id = uid, at = at, hours = hours)
        }
        val weights = records.mapNotNull { r ->
            val at = r.date ?: return@mapNotNull null
            val kg = r.weightKg ?: return@mapNotNull null
            PostgrestApi.WeightInsert(user_id = uid, at = at, kg = kg)
        }
        val waters = records.mapNotNull { r ->
            val at = r.date ?: return@mapNotNull null
            val ml = r.waterMl ?: return@mapNotNull null
            PostgrestApi.WaterIntakeInsert(user_id = uid, at = at, ml = ml)
        }
        val moods = records.mapNotNull { r ->
            val at = r.date ?: return@mapNotNull null
            val mood = r.mood ?: return@mapNotNull null
            PostgrestApi.MoodLogInsert(user_id = uid, at = at, mood = mood, note = r.moodNote, score = r.moodScore)
        }

        var stepsInserted = 0
        var heartsInserted = 0
        var sleepsInserted = 0
        var weightsInserted = 0
        var watersInserted = 0
        var moodsInserted = 0

        runCatching { postgrest.insertSteps(steps) }.onSuccess { stepsInserted = it }
        runCatching { postgrest.insertHeartRates(hearts) }.onSuccess { heartsInserted = it }
        runCatching { postgrest.insertSleepSessions(sleeps) }.onSuccess { sleepsInserted = it }
        runCatching { postgrest.insertWeights(weights) }.onSuccess { weightsInserted = it }
        runCatching { postgrest.insertWaterIntake(waters) }.onSuccess { watersInserted = it }
        runCatching { postgrest.insertMoodLogs(moods) }.onSuccess { moodsInserted = it }

        return SaveResult(stepsInserted, heartsInserted, sleepsInserted, weightsInserted, watersInserted, moodsInserted)
    }

    data class SaveResult(val steps: Int, val hearts: Int, val sleeps: Int, val weights: Int, val waters: Int, val moods: Int)

    suspend fun saveByType(type: ImportType, records: List<ImportedRecord>): Int {
        val access = SessionManager.accessToken
        val uid = if (!access.isNullOrBlank()) AuthApi().getUser(access).id else null
        if (uid.isNullOrBlank()) return 0
        return when (type) {
            ImportType.Steps -> postgrest.insertSteps(records.mapNotNull { r -> r.date?.let { d -> r.steps?.let { PostgrestApi.StepsInsert(uid, d, it) } } })
            ImportType.HeartRate -> postgrest.insertHeartRates(records.mapNotNull { r -> r.date?.let { d -> r.heartRate?.let { PostgrestApi.HeartRateInsert(uid, d, it) } } })
            ImportType.Sleep -> postgrest.insertSleepSessions(records.mapNotNull { r -> r.date?.let { d -> r.sleepHours?.let { PostgrestApi.SleepSessionInsert(uid, d, it) } } })
            ImportType.Weight -> postgrest.insertWeights(records.mapNotNull { r -> r.date?.let { d -> r.weightKg?.let { PostgrestApi.WeightInsert(uid, d, it) } } })
            ImportType.Water -> postgrest.insertWaterIntake(records.mapNotNull { r -> r.date?.let { d -> r.waterMl?.let { PostgrestApi.WaterIntakeInsert(uid, d, it) } } })
            ImportType.Mood -> postgrest.insertMoodLogs(records.mapNotNull { r ->
                val d = r.date ?: return@mapNotNull null
                val m = r.mood ?: return@mapNotNull null
                PostgrestApi.MoodLogInsert(uid, d, m, r.moodNote, r.moodScore)
            })
        }
    }
}
