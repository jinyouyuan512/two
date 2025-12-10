package com.example.healthapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class ImportedRecord(
    val date: String?,
    val steps: Int?,
    val sleepHours: Double?,
    val heartRate: Int?,
    val weightKg: Double? = null,
    val waterMl: Int? = null,
    val mood: String? = null,
    val moodNote: String? = null,
    val moodScore: Int? = null
)

class DataImportViewModel : ViewModel() {
    var imported by mutableStateOf(listOf<ImportedRecord>())
        private set

    enum class ImportType { Steps, HeartRate, Sleep, Weight, Water, Mood }
    var type by mutableStateOf(ImportType.Steps)
        private set

    fun updateImported(records: List<ImportedRecord>) {
        imported = records
    }

    fun clear() {
        imported = emptyList()
    }

    fun setImportType(t: ImportType) {
        type = t
    }
}
