package com.example.healthapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.repository.DailyTipsRepository
import kotlinx.coroutines.launch

class DailyTipsViewModel(
    private val repository: DailyTipsRepository = DailyTipsRepository()
) : ViewModel() {
    var dailyTip by mutableStateOf<String?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            runCatching { repository.getTodayTip() }
                .onSuccess { tip ->
                    dailyTip = tip
                }
                .onFailure {
                    // Fail silently or log
                    it.printStackTrace()
                }
        }
    }
}
