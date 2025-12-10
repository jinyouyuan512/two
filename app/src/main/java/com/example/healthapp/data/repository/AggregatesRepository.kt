package com.example.healthapp.data.repository

import com.example.healthapp.data.remote.HealthAggregatesApi

class AggregatesRepository(private val api: HealthAggregatesApi = HealthAggregatesApi()) {
    data class Aggregates(val stepsTotal: Int, val waterTotal: Int, val sleepAvg: Double)
    suspend fun fetch(rangeDays: Int = 7): Aggregates {
        val r = api.fetch(rangeDays)
        return Aggregates(r.stepsTotal, r.waterTotal, r.sleepAvg)
    }
}

