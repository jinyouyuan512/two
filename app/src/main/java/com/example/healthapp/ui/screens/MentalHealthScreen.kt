package com.example.healthapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.healthapp.data.MockData
import com.example.healthapp.ui.theme.HealthPink
import com.example.healthapp.viewmodel.MetricsViewModel
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentalHealthScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val vm: MetricsViewModel = viewModel()
    val moodEntries = remember { MockData.getMoodEntries() }
    
    LaunchedEffect(selectedTab) {
        vm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HealthPink, Color(0xFFE91E63))
                )
            )
    ) {
        // Shared Header
        MentalHealthHeader(vm, selectedTab)
        
        // Tab Selection
        MoodTabSection(selectedTab) { selectedTab = it }
        
        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            when (selectedTab) {
                0 -> MoodRecordSection(vm, moodEntries)
                1 -> StressAssessmentSection(vm)
                2 -> MeditationSection(vm)
            }
        }
    }
}

@Composable
fun MentalHealthHeader(vm: MetricsViewModel, selectedTab: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "心理健康",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (selectedTab) {
                0 -> {
                    Text(
                        text = "今日心情",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val latestMood = vm.moodLatest
                    val score = try { moodScore(latestMood) } catch (e: Exception) { 0 }
                    Text(
                        text = score.toString(),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = latestMood ?: "暂无",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
                1 -> {
                    Text(
                        text = "平均压力水平",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val avgStress = vm.averageStressLevel
                    Text(
                        text = String.format("%.1f", avgStress),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (avgStress < 3) "状态良好" else if (avgStress < 7) "适当放松" else "需要休息",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
                2 -> {
                    Text(
                        text = "总冥想时长",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${vm.totalMeditationMinutes}",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "分钟",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun MoodTabSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("情绪记录", "压力评估", "冥想练习")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            MoodTabButton(
                title = title,
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MoodTabButton(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
            contentColor = if (isSelected) HealthPink else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(text = title, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun MoodRecordSection(vm: MetricsViewModel, moodEntries: List<com.example.healthapp.model.MoodEntry>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HowDoYouFeelSection(vm, moodEntries)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        WeeklyMoodTrendSection(vm)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MoodStatisticsSection(vm)
    }
}

@Composable
fun HowDoYouFeelSection(vm: MetricsViewModel, moodEntries: List<com.example.healthapp.model.MoodEntry>) {
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "现在感觉如何？",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column {
                moodEntries.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { mood ->
                        MoodOption(mood) {
                            selectedMood = mood.mood
                            // 自动计算情绪得分
                            val score = moodScore(mood.mood)
                            vm.addMood(mood.mood, note, score)
                            // 重置备注
                            note = ""
                        }
                    }
                }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 添加备注输入框
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(text = "添加备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = HealthPink,
                    focusedLabelColor = HealthPink
                )
            )
        }
    }
}

@Composable
fun MoodOption(mood: com.example.healthapp.model.MoodEntry, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Card(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = when (mood.mood) {
                    "开心", "满足", "兴奋", "平静" -> Color(0xFFE8F5E8)
                    else -> Color(0xFFFFEBEE)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mood.emoji,
                    fontSize = 20.sp
                )
            }
        }
        
        Text(
            text = mood.mood,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun WeeklyMoodTrendSection(vm: MetricsViewModel) {
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "本周情绪趋势",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val days = listOf("一", "二", "三", "四", "五", "六", "日")
                val recent = vm.moods.takeLast(7)
                val scores = recent.map { moodScore(it.second).toFloat() }
                val padded = if (scores.size < 7) List(7 - scores.size) { 0f } + scores else scores
                days.forEachIndexed { index, day ->
                    WeeklyMoodBar(day, padded[index])
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        color = Color(0xFF388E3C),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "本周情绪平均：${weeklyAverage(vm).toString()}分，保持积极！",
                        fontSize = 12.sp,
                        color = Color(0xFF388E3C)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyMoodBar(day: String, score: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .width(20.dp)
                .height((score * 10).dp),
            colors = CardDefaults.cardColors(
                containerColor = if (score >= 8) Color(0xFF4CAF50) else if (score >= 6) Color(0xFFFFC107) else Color(0xFFF44336)
            )
        ) {}
        
        Text(
            text = day,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun MoodStatisticsSection(vm: MetricsViewModel) {
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "情绪统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // 添加情绪分布饼图
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp),
                factory = { ctx ->
                    val chart = com.github.mikephil.charting.charts.PieChart(ctx)
                    chart.description.isEnabled = false
                    chart.setNoDataText("暂无情绪数据")
                    chart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    chart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                    chart.legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                    chart.legend.setDrawInside(false)
                    chart.legend.formSize = 12f
                    chart.legend.textSize = 12f
                    chart
                },
                update = { chart: com.github.mikephil.charting.charts.PieChart ->
                    val counts = vm.moods.groupingBy { it.second }.eachCount()
                    if (counts.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }
                    
                    // 设置饼图颜色
                    val colors = listOf(
                        android.graphics.Color.GREEN,
                        android.graphics.Color.BLUE,
                        android.graphics.Color.YELLOW,
                        android.graphics.Color.CYAN,
                        android.graphics.Color.MAGENTA,
                        android.graphics.Color.RED,
                        android.graphics.Color.rgb(255, 165, 0), // 橙色
                        android.graphics.Color.GRAY
                    )
                    
                    // 创建饼图条目
                    val entries = counts.map { (mood, count) ->
                        com.github.mikephil.charting.data.PieEntry(count.toFloat(), mood)
                    }
                    
                    val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "情绪分布").apply {
                        setColors(colors)
                        valueTextSize = 12f
                        valueTextColor = android.graphics.Color.BLACK
                    }
                    
                    chart.data = com.github.mikephil.charting.data.PieData(dataSet)
                    chart.animateXY(300, 300)
                    chart.invalidate()
                }
            )
            
            // 保留原有情绪统计列表
            val counts = vm.moods.groupingBy { it.second }.eachCount()
            val max = counts.values.maxOrNull()?.coerceAtLeast(1) ?: 1
            counts.forEach { (mood, count) ->
                MoodStatItem(mood, count, max)
            }
        }
    }
}

@Composable
fun MoodStatItem(mood: String, count: Int, maxCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mood,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        
        LinearProgressIndicator(
            progress = { count.coerceAtLeast(0) / maxCount.toFloat() },
            modifier = Modifier
                .width(80.dp)
                .height(4.dp),
            color = HealthPink,
            trackColor = Color.LightGray
        )
        
        Text(
            text = "${count}次",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun moodScore(mood: String?): Int {
    return when (mood) {
        "开心" -> 9
        "满足" -> 8
        "兴奋" -> 9
        "平静" -> 8
        "不安" -> 5
        "焦虑" -> 4
        "难过" -> 3
        "生气" -> 2
        else -> 0
    }
}

private fun weeklyAverage(vm: MetricsViewModel): Float {
    val recent = vm.moods.takeLast(7)
    if (recent.isEmpty()) return 0f
    val avg = recent.map { moodScore(it.second) }.average().toFloat()
    return String.format("%.1f", avg).toFloat()
}

@Composable
fun StressAssessmentSection(vm: MetricsViewModel) {
    var level by remember { mutableStateOf(5f) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // 详细的压力建议
    val detailedAdvice = remember(level) {
        when {
            level < 3 -> listOf(
                "压力较低，保持良好作息与运动",
                "建议继续保持当前状态",
                "可以尝试新的挑战以保持活力"
            )
            level < 7 -> listOf(
                "中等压力，适当休息与深呼吸训练",
                "建议每天进行10分钟的冥想练习",
                "保持规律的作息时间",
                "合理安排工作和休息时间"
            )
            else -> listOf(
                "压力偏高，尝试冥想与运动，必要时寻求帮助",
                "建议每天进行20分钟的冥想或有氧运动",
                "考虑调整工作或生活节奏",
                "与朋友或家人交流，分享感受",
                "如长期压力过大，建议寻求专业心理咨询"
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "压力评估", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "请滑动选择当前压力水平", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        Slider(
            value = level, 
            onValueChange = { level = it }, 
            valueRange = 0f..10f
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "当前分值：${level.toInt()}", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        // Save Button
        Button(
            onClick = {
                vm.addStressAssessment(level.toInt())
                showSuccess = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = HealthPink),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("记录压力水平")
        }
        
        if (showSuccess) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("记录成功！", color = Color(0xFF4CAF50), fontSize = 12.sp)
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showSuccess = false
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 详细建议卡片
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "建议：", color = Color(0xFF388E3C), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                detailedAdvice.forEachIndexed { index, advice ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${index + 1}.", color = Color(0xFF388E3C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = advice, color = Color(0xFF388E3C), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // History List
        Text(text = "近期记录", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        
        val assessments = vm.stressAssessments.takeLast(5).reversed()
        if (assessments.isEmpty()) {
             Text(text = "暂无历史记录", fontSize = 14.sp, color = Color.Gray)
        } else {
            assessments.forEach { (time, level) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = time.substring(0, 16), fontSize = 14.sp)
                        Text(text = "压力值: $level", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MeditationSection(vm: MetricsViewModel) {
    var running by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(running) {
        if (running) {
            while (running) {
                kotlinx.coroutines.delay(1000)
                seconds += 1
            }
        }
    }
    val phase = remember(seconds) { if (seconds % 8 < 4) "吸气" else "呼气" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "冥想练习", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "方块呼吸：4秒吸气，4秒呼气", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        
        // Timer Circle
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { (seconds % 8) / 8f },
                modifier = Modifier.size(200.dp),
                color = HealthPink,
                strokeWidth = 12.dp,
                trackColor = HealthPink.copy(alpha = 0.2f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = phase, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = HealthPink)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formatTime(seconds), fontSize = 24.sp, color = Color.Gray)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Button(
                onClick = {
                    running = !running
                    if (!running && seconds > 0) {
                        // 保存冥想练习记录
                        vm.addMeditationSession(seconds, true)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = HealthPink),
                modifier = Modifier.width(120.dp)
            ) { Text(text = if (running) "暂停" else "开始") }
            
            if (seconds > 0) {
                OutlinedButton(
                    onClick = {
                        if (running) {
                            vm.addMeditationSession(seconds, true)
                        }
                        running = false
                        seconds = 0
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HealthPink),
                    modifier = Modifier.width(120.dp)
                ) { Text(text = "结束") }
            }
        }
        
        // 显示冥想练习历史记录
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "练习历史", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(12.dp))
        
        val sessions = vm.meditationSessions.takeLast(5).reversed()
        if (sessions.isEmpty()) {
            Text(text = "暂无练习记录", fontSize = 14.sp, color = Color.Gray)
        } else {
            Column {
                sessions.forEach { (time, duration) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = time.substring(0, 16), fontSize = 14.sp)
                            Text(text = formatTime(duration), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}

// Helper for clip
