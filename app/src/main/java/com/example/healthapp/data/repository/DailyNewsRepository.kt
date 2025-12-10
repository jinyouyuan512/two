package com.example.healthapp.data.repository

import com.example.healthapp.data.remote.PostgrestApi

class DailyNewsRepository(private val api: PostgrestApi = PostgrestApi()) {
    suspend fun list(limit: Int = 20) = api.dailyNews(limit)
    suspend fun insert(news: List<PostgrestApi.DailyNewsInsert>) = api.insertDailyNews(news)
}
