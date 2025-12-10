package com.example.healthapp.data

import com.example.healthapp.model.*

object MockData {
    
    fun getHealthMetrics(): List<HealthMetric> = listOf(
        HealthMetric("步数", "8,234", "步", "目标 10,000", "orange"),
        HealthMetric("心率", "72", "bpm", "静息", "red"),
        HealthMetric("睡眠", "7.5", "小时", "昨夜", "indigo"),
        HealthMetric("情绪", "😄", "", "愉快", "yellow")
    )
    
    fun getHealthSuggestions(): List<HealthSuggestion> = listOf(
        HealthSuggestion(1, "建议增加饮水量", "今日饮水 800ml", "💧"),
        HealthSuggestion(2, "更新步数目标", "至 1766 步，加油！", "👟"),
        HealthSuggestion(3, "午餐建议", "摄入更多蛋白质和蔬菜", "🥗")
    )
    
    fun getUserProfile(): UserProfile = UserProfile(
        name = "张明",
        id = "1234567890",
        avatar = "",
        consecutiveDays = 42,
        exerciseCount = 128,
        healthPoints = 86
    )
    
    fun getExercisePlans(): List<ExercisePlan> = listOf(
        ExercisePlan("跑步", 30, 250, "开始"),
        ExercisePlan("游泳", 45, 320, "开始"),
        ExercisePlan("瑜伽", 60, 180, "开始"),
        ExercisePlan("力量训练", 40, 200, "开始")
    )
    
    fun getMealRecords(): List<MealRecord> = listOf(
        MealRecord(
            "早餐", "08:00", "全麦面包、鸡蛋、牛奶", 450,
            mapOf("膳食纤维" to "25g", "碳水" to "45g", "脂肪" to "12g")
        ),
        MealRecord(
            "午餐", "12:30", "糙米饭、鸡胸肉、绿色蔬菜", 680,
            mapOf("膳食纤维" to "25g", "碳水" to "45g", "脂肪" to "12g")
        ),
        MealRecord(
            "晚餐", "18:00", "鲈鱼、清炒西兰花", 520,
            mapOf("膳食纤维" to "25g", "蛋白" to "45g", "脂肪" to "12g")
        )
    )
    
    fun getHeartRateData(): List<HeartRateData> = listOf(
        HeartRateData("00:00", 65),
        HeartRateData("04:00", 58),
        HeartRateData("08:00", 72),
        HeartRateData("12:00", 78),
        HeartRateData("16:00", 85),
        HeartRateData("20:00", 75),
        HeartRateData("24:00", 68)
    )
    
    fun getRecentRecords(): List<String> = listOf(
        "心率测量 — 今天 14:30 — 72 bpm",
        "体重记录 — 今天 08:00 — 71.2 kg",
        "饮水记录 — 昨天 22:00 — 1800 ml",
        "步数统计 — 昨天 18:00 — 10,456 步"
    )
    
    fun getSleepData(): SleepData = SleepData(
        totalHours = 7.5f,
        bedTime = "23:00",
        wakeTime = "06:30",
        sleepScore = 85,
        lightSleepPercent = 25,
        deepSleepPercent = 45,
        remSleepPercent = 20,
        awakePercent = 10,
        sleepLatency = 12,
        wakeCount = 2
    )
    
    fun getWeeklySleepData(): List<WeeklySleepData> = listOf(
        WeeklySleepData("一", 7.2f),
        WeeklySleepData("二", 8.5f),
        WeeklySleepData("三", 6.8f),
        WeeklySleepData("四", 7.5f),
        WeeklySleepData("五", 7.1f),
        WeeklySleepData("六", 8.2f),
        WeeklySleepData("日", 7.8f)
    )
    
    fun getMoodData(): MoodData = MoodData(
        moodScore = 8.2f,
        moodText = "愉悦感",
        weeklyAverage = 7.6f,
        moodCounts = mapOf(
            "开心" to 12,
            "平静" to 10,
            "失落" to 6,
            "焦虑" to 2
        )
    )
    
    fun getMoodEntries(): List<MoodEntry> = listOf(
        MoodEntry("开心", "😊", 12),
        MoodEntry("满足", "🙂", 8),
        MoodEntry("兴奋", "🤗", 5),
        MoodEntry("平静", "😌", 10),
        MoodEntry("不安", "😟", 3),
        MoodEntry("焦虑", "😰", 2),
        MoodEntry("难过", "😢", 4),
        MoodEntry("生气", "😠", 1)
    )
    
    fun getAIChatMessages(): List<AIChatMessage> = listOf(
        AIChatMessage(1, "你好！我是悦康AI健康助手，很高兴为您服务。我可以帮助您分析健康数据、制定运动计划、提供营养建议等。有什么我可以帮助您的吗？", false, "21:35")
    )
    
    fun getAIQuickQuestions(): List<AIQuickQuestion> = listOf(
        AIQuickQuestion(1, "如何提高运动效果？", "💪"),
        AIQuickQuestion(2, "推荐健康食谱", "🥗"),
        AIQuickQuestion(3, "改善睡眠质量", "😴"),
        AIQuickQuestion(4, "减压放松方法", "🧘")
    )
}