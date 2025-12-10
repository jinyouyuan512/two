package com.example.healthapp.data.repository

import com.example.healthapp.data.remote.DailyTipsApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyTipsRepository(private val api: DailyTipsApi = DailyTipsApi()) {
    suspend fun getTodayTip(): String? {
        val rows = api.getFirstTip()
        return rows.firstOrNull()?.content
    }
}
