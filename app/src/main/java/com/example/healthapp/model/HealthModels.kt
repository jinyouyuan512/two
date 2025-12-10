package com.example.healthapp.model

data class HealthMetric(
    val type: String,
    val value: String,
    val unit: String,
    val description: String,
    val color: String
)

data class HealthSuggestion(
    val id: Int,
    val title: String,
    val description: String,
    val icon: String
)

data class UserProfile(
    val name: String,
    val id: String,
    val avatar: String,
    val consecutiveDays: Int,
    val exerciseCount: Int,
    val healthPoints: Int
)

data class ExercisePlan(
    val name: String,
    val duration: Int,
    val calories: Int,
    val status: String
)

data class MealRecord(
    val mealType: String,
    val time: String,
    val foods: String,
    val calories: Int,
    val nutrients: Map<String, String>
)

data class HeartRateData(
    val time: String,
    val value: Int
)

data class SleepData(
    val totalHours: Float,
    val bedTime: String,
    val wakeTime: String,
    val sleepScore: Int,
    val lightSleepPercent: Int,
    val deepSleepPercent: Int,
    val remSleepPercent: Int,
    val awakePercent: Int,
    val sleepLatency: Int,
    val wakeCount: Int,
    val isMonitoring: Boolean = false,
    val startTime: Long = 0, // 添加开始监测时间
    val endTime: Long = 0, // 添加结束监测时间
    val currentStage: String = "准备中",
    val currentHeartRate: Int = 0
)

data class WeeklySleepData(
    val day: String,
    val hours: Float
)

data class MoodData(
    val moodScore: Float,
    val moodText: String,
    val weeklyAverage: Float,
    val moodCounts: Map<String, Int>
)

data class MoodEntry(
    val mood: String,
    val emoji: String,
    val count: Int
)

data class AIChatMessage(
    val id: Int,
    val message: String,
    val isUser: Boolean,
    val timestamp: String
)

data class AIQuickQuestion(
    val id: Int,
    val question: String,
    val icon: String
)

data class ExerciseSessionRecord(
    val planName: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val averageHeartRate: Int,
    val steps: Int
)
