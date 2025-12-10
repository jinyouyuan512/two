package com.example.healthapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.healthapp.viewmodel.ExerciseViewModel
import com.example.healthapp.ui.theme.HealthOrange
import com.example.healthapp.ui.theme.HealthRed

@Composable
fun ExerciseScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val vm: ExerciseViewModel = viewModel()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        vm.init()
        vm.loadRecords(context)
    }
    val plans by vm.plans.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HealthOrange, HealthRed)
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        // Header with gradient
        ExerciseHeaderSection()
        
        // Tabs
        ExerciseTabSection(selectedTab) { selectedTab = it }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> ExercisePlanSection(plans, onStart = { plan ->
                vm.start(plan)
                selectedTab = 1
            }, onAddPlan = { name, duration, calories ->
                vm.addPlan(name, duration, calories)
            })
            1 -> RealTimeExerciseSection(vm)
            2 -> AchievementSection(vm)
        }
    }
}

@Composable
fun ExerciseHeaderSection(vm: ExerciseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "运动管理",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val records by vm.records.collectAsState()
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7L * 24L * 60L * 60L * 1000L
        val weekly = records.filter { r ->
            r.endTime.toLongOrNull()?.let { it >= sevenDaysAgo } ?: false
        }
        val weekCount = weekly.size
        val totalCalories = weekly.sumOf { it.caloriesBurned }
        val totalMinutes = weekly.sumOf { it.durationMinutes }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ExerciseStatItem(weekCount.toString(), "本周运动")
            ExerciseStatItem(totalCalories.toString(), "消耗卡路里")
            ExerciseStatItem(totalMinutes.toString(), "运动分钟")
        }
    }
}

@Composable
fun ExerciseStatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun ExerciseTabSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("运动计划", "实时运动", "成果记录")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            ExerciseTabButton(
                title = title,
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
fun ExerciseTabButton(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
            contentColor = if (isSelected) HealthOrange else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Text(text = title, fontSize = 14.sp)
    }
}

@Composable
fun ExercisePlanSection(
    plans: List<com.example.healthapp.model.ExercisePlan>,
    onStart: (com.example.healthapp.model.ExercisePlan) -> Unit,
    onAddPlan: (String, Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "今日计划",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        plans.forEach { plan ->
            ExercisePlanCard(plan, onStart)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        var showDialog by remember { mutableStateOf(false) }
        var name by remember { mutableStateOf("") }
        var durationText by remember { mutableStateOf("") }
        var caloriesText by remember { mutableStateOf("") }

        Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = HealthOrange)) {
            Text(text = "添加计划")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = {
                        val d = durationText.trim().toIntOrNull()
                        val c = caloriesText.trim().toIntOrNull()
                        if (!name.isBlank() && d != null && d > 0 && c != null && c > 0) {
                            onAddPlan(name.trim(), d, c)
                            name = ""
                            durationText = ""
                            caloriesText = ""
                            showDialog = false
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = HealthOrange)) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("取消") }
                },
                title = { Text("添加计划") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") })
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("时长(分钟)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = caloriesText,
                            onValueChange = { caloriesText = it },
                            label = { Text("卡路里") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            )
        }
        
        Text(
            text = "推荐运动",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        RecommendedExercisesGrid()
    }
}

@Composable
fun ExercisePlanCard(
    plan: com.example.healthapp.model.ExercisePlan,
    onStart: (com.example.healthapp.model.ExercisePlan) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = plan.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${plan.duration}分钟 — ${plan.calories}卡",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Button(
                onClick = { onStart(plan) },
                colors = ButtonDefaults.buttonColors(containerColor = HealthOrange),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = "开始")
            }
        }
    }
}

@Composable
fun RecommendedExercisesGrid() {
    val exercises = listOf(
        "有氧运动" to "🏃",
        "力量训练" to "💪",
        "拉伸放松" to "🧘",
        "核心训练" to "🎯"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        exercises.chunked(2).forEach { row ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (name, emoji) ->
                    RecommendedExerciseCard(name, emoji)
                }
            }
        }
    }
}

@Composable
fun RecommendedExerciseCard(name: String, emoji: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun RealTimeExerciseSection(vm: ExerciseViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val session by vm.session.collectAsState()
        val context = LocalContext.current
        Text(text = "实时运动", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (session == null) {
            Text(text = "选择计划后开始运动", fontSize = 14.sp, color = Color.Gray)
        } else {
            Text(text = session!!.plan.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            val elapsed = session!!.elapsedSeconds
            val minutes = elapsed / 60
            val seconds = elapsed % 60
            Text(text = String.format("%02d:%02d", minutes, seconds), fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val avgHr = if (session!!.heartRates.isEmpty()) 0 else session!!.heartRates.sum() / session!!.heartRates.size
            Text(text = "心率: ${avgHr} bpm", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "步数: ${session!!.steps}", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (session!!.isPaused) {
                    Button(onClick = { vm.resume() }, colors = ButtonDefaults.buttonColors(containerColor = HealthOrange)) { Text("继续") }
                } else {
                    Button(onClick = { vm.pause() }, colors = ButtonDefaults.buttonColors(containerColor = HealthOrange)) { Text("暂停") }
                }
                Button(onClick = { vm.stop(context) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("结束并保存") }
            }
        }
    }
}

@Composable
fun AchievementSection(vm: ExerciseViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
    ) {
        val records by vm.records.collectAsState()
        Text(text = "成果记录", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        val totalSessions = records.size
        val totalMinutes = records.sumOf { it.durationMinutes }
        val totalCalories = records.sumOf { it.caloriesBurned }
        AchievementCard("总运动次数", "$totalSessions 次", "累计 ${totalMinutes} 分钟")
        AchievementCard("累计消耗卡路里", "$totalCalories 卡", "坚持就是胜利")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "最近记录", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
        records.takeLast(10).reversed().forEach { r ->
            AchievementCard(r.planName, "${r.durationMinutes} 分钟", "心率均值 ${r.averageHeartRate} bpm, 步数 ${r.steps}")
        }
    }
}

@Composable
fun AchievementCard(title: String, value: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthOrange
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
