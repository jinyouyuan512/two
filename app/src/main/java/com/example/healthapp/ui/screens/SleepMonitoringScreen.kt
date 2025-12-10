package com.example.healthapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.data.MockData
import com.example.healthapp.data.SleepRepository
import com.example.healthapp.data.SleepViewModel
import com.example.healthapp.ui.theme.HealthPurple

@Composable
fun SleepMonitoringScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val sleepRepository = remember { SleepRepository() }
    val sleepViewModel: SleepViewModel = viewModel(
        factory = SleepViewModel.factory(sleepRepository)
    )
    val sleepData by sleepViewModel.sleepData.collectAsState()
    val weeklySleepData by sleepViewModel.weeklySleepData.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HealthPurple, Color(0xFF9C27B0))
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        // Header with sleep data
        SleepHeaderSection(sleepData)
        
        // Tabs
        SleepTabSection(selectedTab) { selectedTab = it }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> SleepOverviewSection(sleepData, weeklySleepData)
            1 -> SleepQualitySection(sleepData)
            2 -> SleepStagesSection(sleepData)
            3 -> SleepRealTimeMonitoringSection(sleepData, sleepViewModel)
        }
    }
}

@Composable
fun SleepHeaderSection(sleepData: com.example.healthapp.model.SleepData) {
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "睡眠监测",
                    color = Color.White,
                    fontSize = 14.sp
                )
                
                Card(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = if (sleepData.isMonitoring) Color.Green.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Clock",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "${sleepData.totalHours} 小时",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "入睡 ${sleepData.bedTime}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "起床 ${sleepData.wakeTime}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                
                Text(
                    text = "${sleepData.sleepScore}分",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Monitoring status
            if (sleepData.isMonitoring) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
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
                                text = "监测中",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "当前阶段：${sleepData.currentStage}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                        
                        Text(
                            text = "心率：${sleepData.currentHeartRate} BPM",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SleepTabSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("概览数据", "质量分析", "分期概览", "实时监测")
    
    // Use Box with Row to fit buttons better
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            tabs.forEachIndexed { index, title ->
                SleepTabButton(
                    title = title,
                    isSelected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SleepTabButton(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
            contentColor = if (isSelected) HealthPurple else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(text = title, fontSize = 12.sp)
    }
}

@Composable
fun SleepOverviewSection(
    sleepData: com.example.healthapp.model.SleepData,
    weeklySleepData: List<com.example.healthapp.model.WeeklySleepData>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Section title
        Text(
            text = "概览数据",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Weekly sleep trend
        WeeklySleepTrendSection(weeklySleepData)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sleep statistics
        SleepStatisticsSection(sleepData)
    }
}

@Composable
fun WeeklySleepTrendSection(weeklySleepData: List<com.example.healthapp.model.WeeklySleepData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "本周睡眠趋势",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Simulated bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklySleepData.forEach { data ->
                    WeeklySleepBar(data)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Statistics
            val avg = (weeklySleepData.map { it.hours }.average()).toFloat()
            val max = weeklySleepData.maxByOrNull { it.hours }?.hours ?: 0f
            val min = weeklySleepData.minByOrNull { it.hours }?.hours ?: 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SleepStatItem("平均时长", "${String.format("%.1f", avg)} 小时")
                SleepStatItem("最长", "${String.format("%.1f", max)} 小时")
                SleepStatItem("最短", "${String.format("%.1f", min)} 小时")
            }
        }
    }
}

@Composable
fun WeeklySleepBar(data: com.example.healthapp.model.WeeklySleepData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .width(24.dp)
                .height((data.hours * 15).dp),
            colors = CardDefaults.cardColors(containerColor = HealthPurple.copy(alpha = 0.6f))
        ) {}
        
        Text(
            text = data.day,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SleepStatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SleepStatisticsSection(sleepData: com.example.healthapp.model.SleepData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "睡眠统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            SleepProgressItem("入睡潜伏期", "${sleepData.sleepLatency} 分钟", 0.6f)
            SleepProgressItem("夜醒次数", "${sleepData.wakeCount} 次", 0.3f)
            SleepProgressItem("昼眠平均", "25 分钟", 0.4f)
        }
    }
}

@Composable
fun SleepProgressItem(label: String, value: String, progress: Float) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp
        )
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(80.dp)
                .height(4.dp),
            color = HealthPurple,
            trackColor = Color.LightGray
        )
        
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SleepQualitySection(sleepData: com.example.healthapp.model.SleepData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Section title
        Text(
            text = "质量分析",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val deep = sleepData.deepSleepPercent
        val rem = sleepData.remSleepPercent
        val light = sleepData.lightSleepPercent
        val awake = sleepData.awakePercent
        val latency = sleepData.sleepLatency
        val wakeCount = sleepData.wakeCount
        val quality = (((deep * 0.4) + (rem * 0.3) + (light * 0.2) - (awake * 0.1)).coerceIn(0.0, 100.0)).toFloat()
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "综合评分", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${sleepData.sleepScore}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = HealthPurple)
                    Column(horizontalAlignment = Alignment.End) {
                        LinearProgressIndicator(progress = { quality / 100f }, modifier = Modifier.width(140.dp), color = HealthPurple, trackColor = Color.LightGray)
                        Text(text = "质量指数 ${String.format("%.0f", quality)}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SleepProgressItem("深睡比例", "$deep%", deep / 100f)
                SleepProgressItem("REM比例", "$rem%", rem / 100f)
                SleepProgressItem("浅睡比例", "$light%", light / 100f)
                SleepProgressItem("清醒比例", "$awake%", (100 - awake).coerceAtLeast(0) / 100f)
                Spacer(modifier = Modifier.height(8.dp))
                SleepProgressItem("入睡潜伏期", "${latency} 分钟", ((60 - latency).coerceAtLeast(0)) / 60f)
                SleepProgressItem("夜醒次数", "${wakeCount} 次", ((5 - wakeCount).coerceAtLeast(0)) / 5f)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "改善建议", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (latency > 20) { Text(text = "睡前放松训练，减少屏幕使用", fontSize = 12.sp, color = Color.Gray) }
                if (wakeCount > 2) { Text(text = "优化环境与作息，减少夜间干扰", fontSize = 12.sp, color = Color.Gray) }
                if (deep < 20) { Text(text = "晚间适度运动，提升深睡比例", fontSize = 12.sp, color = Color.Gray) }
                if (awake > 15) { Text(text = "固定作息时间，降低清醒占比", fontSize = 12.sp, color = Color.Gray) }
                if (latency <= 20 && wakeCount <= 2 && deep >= 20 && awake <= 15) { Text(text = "当前睡眠质量良好，保持规律作息", fontSize = 12.sp, color = Color.Gray) }
            }
        }
    }
}

@Composable
fun SleepStagesSection(sleepData: com.example.healthapp.model.SleepData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Section title
        Text(
            text = "分期概览",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Sleep stages donut chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                factory = {
                    val chart = com.github.mikephil.charting.charts.PieChart(it)
                    
                    // Configure chart
                    chart.description.isEnabled = false
                    chart.setNoDataText("暂无睡眠分期数据")
                    chart.legend.isEnabled = false
                    chart.setDrawEntryLabels(false)
                    chart.setTouchEnabled(false)
                    chart.isDragDecelerationEnabled = false
                    
                    // Prepare data
                    val entries = listOf(
                        com.github.mikephil.charting.data.PieEntry(
                            sleepData.lightSleepPercent.toFloat(),
                            "浅睡眠"
                        ),
                        com.github.mikephil.charting.data.PieEntry(
                            sleepData.deepSleepPercent.toFloat(),
                            "深睡眠"
                        ),
                        com.github.mikephil.charting.data.PieEntry(
                            sleepData.remSleepPercent.toFloat(),
                            "REM睡眠"
                        ),
                        com.github.mikephil.charting.data.PieEntry(
                            sleepData.awakePercent.toFloat(),
                            "清醒"
                        )
                    )
                    
                    // Create dataset
                    val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "睡眠分期")
                    dataSet.colors = listOf(
                        0xFFE1BEE7.toInt(), // 浅睡眠 - 浅紫色
                        0xFF7B1FA2.toInt(), // 深睡眠 - 深紫色
                        0xFF9C27B0.toInt(), // REM睡眠 - 紫色
                        0xFFCE93D8.toInt()  // 清醒 - 淡紫色
                    )
                    dataSet.setDrawValues(false)
                    dataSet.sliceSpace = 2f
                    
                    // Create donut hole
                    dataSet.valueTextSize = 12f
                    chart.holeRadius = 60f
                    chart.transparentCircleRadius = 65f
                    chart.setHoleColor(0xFFF8F8F8.toInt())
                    
                    // Add center text
                    chart.setDrawCenterText(true)
                    chart.centerText = "睡眠分期\n总计 ${sleepData.totalHours} 小时"
                    chart.setCenterTextSize(14f)
                    chart.setCenterTextColor(0xFF000000.toInt())
                    
                    // Set data
                    val pieData = com.github.mikephil.charting.data.PieData(dataSet)
                    chart.data = pieData
                    chart.invalidate()
                    
                    chart
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sleep stages legend
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "睡眠分期占比", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                SleepStageItem("浅睡眠", "${sleepData.lightSleepPercent}%", Color(0xFFE1BEE7))
                SleepStageItem("深睡眠", "${sleepData.deepSleepPercent}%", Color(0xFF7B1FA2))
                SleepStageItem("REM睡眠", "${sleepData.remSleepPercent}%", Color(0xFF9C27B0))
                SleepStageItem("清醒", "${sleepData.awakePercent}%", Color(0xFFCE93D8))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val total = sleepData.totalHours
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "分期时长", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                val lightH = total * sleepData.lightSleepPercent / 100f
                val deepH = total * sleepData.deepSleepPercent / 100f
                val remH = total * sleepData.remSleepPercent / 100f
                val awakeH = total * sleepData.awakePercent / 100f
                SleepProgressItem("浅睡眠", "${String.format("%.1f", lightH)} 小时", sleepData.lightSleepPercent / 100f)
                SleepProgressItem("深睡眠", "${String.format("%.1f", deepH)} 小时", sleepData.deepSleepPercent / 100f)
                SleepProgressItem("REM睡眠", "${String.format("%.1f", remH)} 小时", sleepData.remSleepPercent / 100f)
                SleepProgressItem("清醒", "${String.format("%.1f", awakeH)} 小时", sleepData.awakePercent / 100f)
            }
        }
    }
}

@Composable
fun SleepStageItem(stage: String, percentage: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(12.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = color)
        ) {}
        
        Text(
            text = "$stage $percentage",
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SleepRealTimeMonitoringSection(
    sleepData: com.example.healthapp.model.SleepData,
    sleepViewModel: SleepViewModel
) {
    // Get current time that updates every second
    var currentTime by remember { 
        mutableStateOf(java.util.Calendar.getInstance().time)
    }
    // Update time every second
    LaunchedEffect(Unit) {
        kotlinx.coroutines.flow.flow {
            while (true) {
                emit(java.util.Calendar.getInstance().time)
                kotlinx.coroutines.delay(1000)
            }
        }.collect { currentTime = it }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Section title with current time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "实时监测",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(currentTime),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Monitoring status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (sleepData.isMonitoring) Color.Green.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status indicator and title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = if (sleepData.isMonitoring) Color.Green else Color.Gray,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (sleepData.isMonitoring) "监测进行中" else "未监测",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sleepData.isMonitoring) Color.Green else Color.Gray
                    )
                }
                
                // Current time display
                Text(
                    text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(currentTime),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Sleep stage visualization
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = getSleepStageColor(sleepData.currentStage)),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = sleepData.currentStage,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "睡眠阶段",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Heart rate with visual indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart Rate",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${sleepData.currentHeartRate} BPM",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                // Start/Stop button with improved design
                Button(
                    onClick = {
                        if (sleepData.isMonitoring) {
                            sleepViewModel.stopSleepMonitoring()
                        } else {
                            sleepViewModel.startSleepMonitoring()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sleepData.isMonitoring) Color.Red else HealthPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (sleepData.isMonitoring) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Monitoring",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Monitoring",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (sleepData.isMonitoring) "停止监测" else "开始监测",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Detailed sleep metrics grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "睡眠指标",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Sleep metrics grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Sleep latency
                    SleepMetricItem("入睡潜伏期", "${sleepData.sleepLatency} 分钟")
                    // Wake count
                    SleepMetricItem("夜醒次数", "${sleepData.wakeCount} 次")
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Deep sleep
                    SleepMetricItem("深睡比例", "${sleepData.deepSleepPercent}%")
                    // REM sleep
                    SleepMetricItem("REM比例", "${sleepData.remSleepPercent}%")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tips card with improved design
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tips",
                        tint = HealthPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "监测小贴士",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                val tips = listOf(
                    "• 请确保手机放在床边，保持良好的信号连接",
                    "• 建议使用睡眠模式，减少干扰",
                    "• 监测过程中请保持手机电量充足",
                    "• 保持卧室安静、黑暗和凉爽",
                    "• 避免睡前使用电子设备"
                )
                
                Column {
                    tips.forEach { tip ->
                        Text(
                            text = tip,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper function to get color based on sleep stage
private fun getSleepStageColor(stage: String): Color {
    return when (stage) {
        "准备中" -> Color.Gray
        "浅睡眠" -> Color(0xFFE1BEE7)
        "深睡眠" -> Color(0xFF7B1FA2)
        "REM睡眠" -> Color(0xFF9C27B0)
        "清醒" -> Color(0xFFCE93D8)
        else -> Color.Gray
    }
}

// Helper Composable for sleep metric item
@Composable
fun SleepMetricItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = HealthPurple,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
