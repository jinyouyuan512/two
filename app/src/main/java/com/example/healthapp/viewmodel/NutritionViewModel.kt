package com.example.healthapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthapp.data.MockData
import com.example.healthapp.model.MealRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date

class NutritionViewModel : ViewModel() {
    private val _meals = MutableStateFlow<List<MealRecord>>(emptyList())
    val meals: StateFlow<List<MealRecord>> = _meals.asStateFlow()

    private val _cupsFilled = MutableStateFlow(4)
    val cupsFilled: StateFlow<Int> = _cupsFilled.asStateFlow()

    private val _waterTargetMl = MutableStateFlow(2000)
    val waterTargetMl: StateFlow<Int> = _waterTargetMl.asStateFlow()

    val cupMl: Int = 200

    private val _calorieTarget = MutableStateFlow(2000)
    val calorieTarget: StateFlow<Int> = _calorieTarget.asStateFlow()

    private val _proteinTarget = MutableStateFlow(60)
    val proteinTarget: StateFlow<Int> = _proteinTarget.asStateFlow()

    private val _carbsTarget = MutableStateFlow(250)
    val carbsTarget: StateFlow<Int> = _carbsTarget.asStateFlow()

    private val _fatTarget = MutableStateFlow(70)
    val fatTarget: StateFlow<Int> = _fatTarget.asStateFlow()

    private val _fiberTarget = MutableStateFlow(25)
    val fiberTarget: StateFlow<Int> = _fiberTarget.asStateFlow()
    
    // 历史识别记录
    private val _foodHistory = MutableStateFlow<List<String>>(emptyList())
    val foodHistory: StateFlow<List<String>> = _foodHistory.asStateFlow()

    data class NutritionTotals(
        val calories: Int,
        val fiberG: Int,
        val carbsG: Int,
        val fatG: Int,
        val proteinG: Int
    )

    fun init() {
        if (_meals.value.isEmpty()) _meals.value = MockData.getMealRecords()
    }

    fun addMealRecord(
        mealType: String,
        foods: String,
        calories: Int,
        fiberG: Int,
        carbsG: Int,
        fatG: Int,
        proteinG: Int
    ) {
        val time = SimpleDateFormat("HH:mm").format(Date())
        val nutrients = buildMap {
            if (fiberG > 0) put("膳食纤维", "${fiberG}g")
            if (carbsG > 0) put("碳水", "${carbsG}g")
            if (fatG > 0) put("脂肪", "${fatG}g")
            if (proteinG > 0) put("蛋白", "${proteinG}g")
        }
        _meals.value = _meals.value + MealRecord(mealType, time, foods, calories, nutrients)
    }

    fun removeMeal(index: Int) {
        if (index in _meals.value.indices) {
            _meals.value = _meals.value.toMutableList().also { it.removeAt(index) }
        }
    }

    fun updateMealRecord(
        index: Int,
        mealType: String,
        foods: String,
        calories: Int,
        fiberG: Int,
        carbsG: Int,
        fatG: Int,
        proteinG: Int
    ) {
        if (index !in _meals.value.indices) return
        val old = _meals.value[index]
        val nutrients = buildMap {
            if (fiberG > 0) put("膳食纤维", "${fiberG}g")
            if (carbsG > 0) put("碳水", "${carbsG}g")
            if (fatG > 0) put("脂肪", "${fatG}g")
            if (proteinG > 0) put("蛋白", "${proteinG}g")
        }
        val updated = old.copy(mealType = mealType, foods = foods, calories = calories, nutrients = nutrients)
        _meals.value = _meals.value.toMutableList().also { it[index] = updated }
    }

    fun loadRecords(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_MEALS, null)
        if (!raw.isNullOrBlank()) {
            runCatching { _meals.value = parseMeals(raw) }
        }
        _cupsFilled.value = prefs.getInt(KEY_WATER_CUPS, _cupsFilled.value)
        _waterTargetMl.value = prefs.getInt(KEY_WATER_TARGET, _waterTargetMl.value)
        _calorieTarget.value = prefs.getInt(KEY_CALORIE_TARGET, _calorieTarget.value)
        _proteinTarget.value = prefs.getInt(KEY_PROTEIN_TARGET, _proteinTarget.value)
        _carbsTarget.value = prefs.getInt(KEY_CARBS_TARGET, _carbsTarget.value)
        _fatTarget.value = prefs.getInt(KEY_FAT_TARGET, _fatTarget.value)
        _fiberTarget.value = prefs.getInt(KEY_FIBER_TARGET, _fiberTarget.value)
        
        // 加载食物历史记录
        val foodHistoryJson = prefs.getString(KEY_FOOD_HISTORY, "[]")
        runCatching {
            val historyArray = JSONArray(foodHistoryJson)
            val historyList = mutableListOf<String>()
            for (i in 0 until historyArray.length()) {
                historyList.add(historyArray.getString(i))
            }
            _foodHistory.value = historyList
        }
    }

    fun saveRecords(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = toJson(_meals.value)
        
        // 保存食物历史记录
        val foodHistoryJson = JSONArray(_foodHistory.value).toString()
        
        prefs.edit()
            .putString(KEY_MEALS, json)
            .putInt(KEY_WATER_CUPS, _cupsFilled.value)
            .putInt(KEY_WATER_TARGET, _waterTargetMl.value)
            .putInt(KEY_CALORIE_TARGET, _calorieTarget.value)
            .putInt(KEY_PROTEIN_TARGET, _proteinTarget.value)
            .putInt(KEY_CARBS_TARGET, _carbsTarget.value)
            .putInt(KEY_FAT_TARGET, _fatTarget.value)
            .putInt(KEY_FIBER_TARGET, _fiberTarget.value)
            .putString(KEY_FOOD_HISTORY, foodHistoryJson)
            .apply()
    }
    
    // 添加食物到历史记录
    fun addFoodToHistory(food: String) {
        viewModelScope.launch {
            val updatedHistory = _foodHistory.value.toMutableList().apply {
                // 如果食物已存在，先移除
                remove(food)
                // 添加到列表开头
                add(0, food)
                // 保持历史记录最多10条
                if (size > 10) {
                    removeLast()
                }
            }
            _foodHistory.value = updatedHistory
        }
    }
    
    // 清空食物历史记录
    fun clearFoodHistory() {
        _foodHistory.value = emptyList()
    }

    fun totals(): NutritionTotals {
        var calories = 0
        var fiber = 0
        var carbs = 0
        var fat = 0
        var protein = 0
        _meals.value.forEach { m ->
            calories += m.calories
            m.nutrients.forEach { (k, v) ->
                val g = v.trim().lowercase().removeSuffix("g").toIntOrNull() ?: 0
                when (k) {
                    "膳食纤维" -> fiber += g
                    "碳水" -> carbs += g
                    "脂肪" -> fat += g
                    "蛋白" -> protein += g
                }
            }
        }
        return NutritionTotals(calories, fiber, carbs, fat, protein)
    }

    fun suggestions(goalCalories: Int = 2000): List<String> {
        val t = totals()
        val list = mutableListOf<String>()
        if (t.calories < goalCalories) {
            list += "今日摄入低于目标，适当补充优质蛋白与复合碳水"
        } else if (t.calories > goalCalories + 200) {
            list += "卡路里偏高，减少高糖高脂食物，增加蔬果与粗粮"
        }
        if (t.proteinG < 60) list += "蛋白质偏低，建议增加鸡胸肉、鱼类、豆制品"
        if (t.fiberG < 25) list += "膳食纤维不足，增加全谷物、蔬菜与水果"
        if (t.fatG > 70) list += "脂肪偏高，减少油炸食品，选择橄榄油与坚果"
        if (t.carbsG > 300) list += "碳水略高，晚餐控制主食分量，选择低GI食物"
        val waterMl = _cupsFilled.value * cupMl
        if (waterMl < 1500) list += "今日饮水不足，建议达到 ${_waterTargetMl.value}ml，分批次饮用"
        if (list.isEmpty()) list += "营养摄入较均衡，保持规律饮食与充足饮水"
        return list
    }

    fun setCupsFilled(n: Int) { _cupsFilled.value = n.coerceIn(0, _waterTargetMl.value / cupMl) }
    fun incCup() { setCupsFilled(_cupsFilled.value + 1) }
    fun decCup() { setCupsFilled(_cupsFilled.value - 1) }
    fun setWaterTarget(targetMl: Int) { _waterTargetMl.value = targetMl }

    fun setCalorieTarget(v: Int) { _calorieTarget.value = v }
    fun setProteinTarget(v: Int) { _proteinTarget.value = v }
    fun setCarbsTarget(v: Int) { _carbsTarget.value = v }
    fun setFatTarget(v: Int) { _fatTarget.value = v }
    fun setFiberTarget(v: Int) { _fiberTarget.value = v }

    fun saveIfNeeded(context: Context) {
        viewModelScope.launch { saveRecords(context) }
    }

    companion object {
        private const val PREFS = "nutrition_records_prefs"
        private const val KEY_MEALS = "meals_json"
        private const val KEY_WATER_CUPS = "water_cups"
        private const val KEY_WATER_TARGET = "water_target_ml"
        private const val KEY_CALORIE_TARGET = "calorie_target"
        private const val KEY_PROTEIN_TARGET = "protein_target"
        private const val KEY_CARBS_TARGET = "carbs_target"
        private const val KEY_FAT_TARGET = "fat_target"
        private const val KEY_FIBER_TARGET = "fiber_target"
        private const val KEY_FOOD_HISTORY = "food_history"
    }

    private fun toJson(list: List<MealRecord>): String {
        val arr = JSONArray()
        list.forEach { m ->
            val obj = JSONObject()
            obj.put("mealType", m.mealType)
            obj.put("time", m.time)
            obj.put("foods", m.foods)
            obj.put("calories", m.calories)
            val n = JSONObject()
            m.nutrients.forEach { (k, v) -> n.put(k, v) }
            obj.put("nutrients", n)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseMeals(json: String): List<MealRecord> {
        val arr = JSONArray(json)
        val list = mutableListOf<MealRecord>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val mealType = obj.optString("mealType")
            val time = obj.optString("time")
            val foods = obj.optString("foods")
            val calories = obj.optInt("calories")
            val nutrientsObj = obj.optJSONObject("nutrients") ?: JSONObject()
            val nutrients = mutableMapOf<String, String>()
            nutrientsObj.keys().forEach { k -> nutrients[k] = nutrientsObj.optString(k) }
            list += MealRecord(mealType, time, foods, calories, nutrients)
        }
        return list
    }
}
