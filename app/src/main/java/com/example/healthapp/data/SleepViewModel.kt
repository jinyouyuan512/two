package com.example.healthapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * 睡眠数据视图模型
 * 负责管理睡眠数据的展示和交互
 */
class SleepViewModel(private val sleepRepository: SleepRepository) : ViewModel() {
    
    /**
     * 当前睡眠数据
     */
    val sleepData = sleepRepository.sleepData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = sleepRepository.sleepData.value
        )
    
    /**
     * 周睡眠数据
     */
    val weeklySleepData = sleepRepository.weeklySleepData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = sleepRepository.weeklySleepData.value
        )
    
    /**
     * 获取睡眠分数描述
     */
    fun getSleepScoreDescription(score: Int): String {
        return sleepRepository.getSleepScoreDescription(score)
    }
    
    /**
     * 开始睡眠监测
     */
    fun startSleepMonitoring() {
        sleepRepository.startSleepMonitoring()
    }
    
    /**
     * 停止睡眠监测
     */
    fun stopSleepMonitoring() {
        sleepRepository.stopSleepMonitoring()
    }
    
    /**
     * 更新当前睡眠阶段
     */
    fun updateCurrentSleepStage(stage: String, heartRate: Int = 0) {
        sleepRepository.updateCurrentSleepStage(stage, heartRate)
    }
    
    /**
     * 更新睡眠数据
     */
    fun updateSleepData(newData: com.example.healthapp.model.SleepData) {
        sleepRepository.updateSleepData(newData)
    }
    
    // Factory for creating SleepViewModel with a parameter
    class Factory(private val sleepRepository: SleepRepository) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SleepViewModel(sleepRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    
    companion object {
        fun factory(sleepRepository: SleepRepository): Factory {
            return Factory(sleepRepository)
        }
    }
}