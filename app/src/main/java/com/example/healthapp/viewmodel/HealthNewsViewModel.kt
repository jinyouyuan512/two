package com.example.healthapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.remote.PostgrestApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthNewsViewModel(private val api: PostgrestApi = PostgrestApi()) : ViewModel() {
    private val _news = MutableStateFlow<List<PostgrestApi.DailyNewsRow>>(emptyList())
    val news: StateFlow<List<PostgrestApi.DailyNewsRow>> = _news.asStateFlow()

    fun load(limit: Int = 10) {
        viewModelScope.launch {
            runCatching { api.dailyNews(limit) }
                .onSuccess { _news.value = it }
                .onFailure { /* ignore for now */ }
        }
    }

    fun purgeDuplicates(onDone: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = runCatching { api.purgeDuplicateDailyNews() }.getOrNull() ?: 0
            load(50)
            onDone?.invoke(count)
        }
    }
}
