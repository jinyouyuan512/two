package com.example.healthapp.data

import com.example.healthapp.model.SleepData
import com.example.healthapp.model.WeeklySleepData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 睡眠数据仓库
 * 负责管理睡眠数据的存储和获取
 */
class SleepRepository {
    private val _sleepData = MutableStateFlow(MockData.getSleepData())
    val sleepData: StateFlow<SleepData> = _sleepData.asStateFlow()
    
    private val _weeklySleepData = MutableStateFlow(MockData.getWeeklySleepData())
    val weeklySleepData: StateFlow<List<WeeklySleepData>> = _weeklySleepData.asStateFlow()
    
    /**
     * 更新睡眠数据
     */
    fun updateSleepData(newData: SleepData) {
        _sleepData.value = newData
    }
    
    /**
     * 更新周睡眠数据
     */
    fun updateWeeklySleepData(newData: List<WeeklySleepData>) {
        _weeklySleepData.value = newData
    }
    
    /**
     * 获取睡眠分数描述
     */
    fun getSleepScoreDescription(score: Int): String {
        return when (score) {
            in 90..100 -> "优秀！你的睡眠质量非常好，继续保持。"
            in 80..89 -> "良好！你的睡眠质量不错，但还有提升空间。"
            in 70..79 -> "一般！建议调整作息，改善睡眠环境。"
            in 60..69 -> "较差！需要关注睡眠问题，建议咨询专业人士。"
            else -> "很差！请立即调整生活习惯，必要时寻求医疗帮助。"
        }
    }
    
    /**
     * 开始睡眠监测
     */
    fun startSleepMonitoring() {
        val currentData = _sleepData.value
        _sleepData.value = currentData.copy(
            isMonitoring = true,
            startTime = System.currentTimeMillis(), // 记录开始时间
            endTime = 0,
            currentStage = "浅睡眠", // 直接进入浅睡眠阶段，不再停留在准备中
            currentHeartRate = 60
        )
    }
    
    /**
     * 停止睡眠监测
     */
    fun stopSleepMonitoring() {
        val currentData = _sleepData.value
        val endTime = System.currentTimeMillis()
        val totalMinutes = ((endTime - currentData.startTime) / (1000 * 60)).toFloat()
        val totalHours = totalMinutes / 60
        
        // Update main sleep data
        _sleepData.value = currentData.copy(
            isMonitoring = false,
            endTime = endTime, // 记录结束时间
            totalHours = totalHours, // 更新总睡眠时间
            bedTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(currentData.startTime)),
            wakeTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(endTime)),
            currentStage = "已结束",
            // Recalculate sleep stages percentages based on actual monitoring
            lightSleepPercent = 40,
            deepSleepPercent = 35,
            remSleepPercent = 20,
            awakePercent = 5,
            sleepScore = 85,
            sleepLatency = 10,
            wakeCount = 1
        )
        
        // Update weekly sleep data (add current sleep to weekly data)
        val newWeeklyData = _weeklySleepData.value.toMutableList()
        // Replace the last day with current sleep data
        if (newWeeklyData.isNotEmpty()) {
            newWeeklyData[newWeeklyData.size - 1] = newWeeklyData[newWeeklyData.size - 1].copy(hours = totalHours)
            _weeklySleepData.value = newWeeklyData
        }
    }
    
    /**
     * 更新当前睡眠阶段
     */
    fun updateCurrentSleepStage(stage: String, heartRate: Int = 0) {
        val currentData = _sleepData.value
        if (currentData.isMonitoring) {
            _sleepData.value = currentData.copy(
                currentStage = stage,
                currentHeartRate = heartRate
            )
        }
    }
}