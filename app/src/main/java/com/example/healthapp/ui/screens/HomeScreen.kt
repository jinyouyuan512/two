package com.example.healthapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthapp.data.MockData
import com.example.healthapp.ui.theme.*
import com.example.healthapp.model.HealthMetric
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.viewmodel.MetricsViewModel
import com.example.healthapp.viewmodel.ProfileViewModel
import com.example.healthapp.viewmodel.DailyTipsViewModel

@Composable
fun HomeScreen(onAiClick: () -> Unit = {}, onRecordClick: () -> Unit = {}, onNewsClick: () -> Unit = {}) {
    val metricsVm: MetricsViewModel = viewModel()
    val profileVm: ProfileViewModel = viewModel()
    val dailyTipsVm: DailyTipsViewModel = viewModel()
    val newsVm: com.example.healthapp.viewmodel.HealthNewsViewModel = viewModel()
    LaunchedEffect(Unit) {
        metricsVm.load()
        profileVm.load()
        dailyTipsVm.load()
        newsVm.load(10)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HealthPurple, HealthBlue)
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HeaderSection(profileVm.displayName?.takeIf { it.isNotBlank() } ?: "用户") }
        item { HealthOverviewCardSection(metricsVm) }
        // item { QuickRecordSection(onRecordClick) } // Removed as requested
        item { AIAssistantEntrySection(onAiClick) }
        item { DailyTipSection(dailyTipsVm) }
        item {
            val news by newsVm.news.collectAsState()
            HealthNewsCardSection(news, onNewsClick, onRefresh = { newsVm.load(10) })
        }
        // item { WeeklyTrendsSection(metricsVm) } // Removed as requested
    }
}

@Composable
fun DailyTipSection(vm: DailyTipsViewModel) {
    val tip = vm.dailyTip
    if (!tip.isNullOrBlank()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), // Light yellow
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "💡 每日小贴士",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFFF57F17) // Darker orange/yellow
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tip,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun AIAssistantEntrySection(onAiClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAiClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = HealthPurple.copy(alpha = 0.1f))
                ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                            text = "🩺",
                            fontSize = 24.sp
                            )
                        }
                    }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "AI 健康助手",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "24小时智能问答",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Text(
                    text = "→",
                    fontSize = 20.sp,
                    color = HealthPurple
                )
            }
        }
    }
}

@Composable
fun HeaderSection(displayName: String) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 6..10 -> "早安"
        in 11..12 -> "午安"
        in 13..17 -> "下午好"
        in 18..23 -> "晚上好"
        else -> "夜深了"
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "$greeting，$displayName",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "今天也要保持健康哦 💪",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun HealthOverviewCardSection(vm: MetricsViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今日健康概况",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "85分",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val stepsStr = vm.stepsLatest?.toString() ?: "-"
                val hrStr = vm.currentHeartRate?.toString() ?: "-"
                HealthMetricCard(HealthMetric("步数", stepsStr, "步", "目标 10,000", "orange"), modifier = Modifier.weight(1f))
                HealthMetricCard(HealthMetric("心率", hrStr, "bpm", "静息", "red"), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HealthMetricCard(metric: com.example.healthapp.model.HealthMetric, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = metric.type,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Column {
                Text(
                    text = metric.value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (metric.color) {
                        "orange" -> Color(0xFFFF8C00)
                        "red" -> Color.Red
                        "indigo" -> Color(0xFF4B0082)
                        "yellow" -> Color(0xFFFFD700)
                        else -> Color.Black
                    }
                )
                Text(
                    text = metric.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun QuickRecordSection(onRecordClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "快速记录",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickRecordButton("运动", "🏃", onRecordClick)
            QuickRecordButton("饮水", "💧", onRecordClick)
            QuickRecordButton("餐饮", "🍽️", onRecordClick)
            QuickRecordButton("体重", "⚖️", onRecordClick)
        }
    }
}

@Composable
fun QuickRecordButton(text: String, emoji: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(60.dp).clickable { onClick() },
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }
        }
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun HealthNewsCardSection(
    news: List<com.example.healthapp.data.remote.PostgrestApi.DailyNewsRow>,
    onMoreClick: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "健康新闻",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(colors = listOf(HealthPurple, HealthBlue))
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "今日健康资讯：", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (news.isEmpty()) {
                        Text(text = "暂无新闻", fontSize = 14.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onRefresh, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("刷新") }
                    } else {
                        var index = 1
                        news.take(2).forEach { item ->
                            val timeStr = item.published_at ?: item.created_at
                            val line = "${index}. ${item.title} 来源：${item.source ?: "未知"} | 时间：${timeStr}"
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = line, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    if (!item.url.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                        Text(text = "`" + item.url + "`", fontSize = 12.sp, color = HealthBlue, modifier = Modifier.clickable { uriHandler.openUri(item.url!!) })
                                    }
                                    if (!item.summary.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = item.summary!!, fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            index += 1
                        }
                    }
                    Button(
                        onClick = { onMoreClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "查看更多新闻", color = HealthBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionCard(suggestion: com.example.healthapp.model.HealthSuggestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = suggestion.icon,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                Text(
                    text = suggestion.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = suggestion.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun WeeklyTrendsSection(vm: MetricsViewModel) {
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
                text = "本周趋势",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "查看详情",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val stepsTotal = vm.stepsTotal ?: 0
        val waterTotal = vm.waterTotal ?: 0
        val sleepAvg = vm.sleepAvg ?: 0.0
        val stepsProgress = (stepsTotal / (10000f * 7)).coerceIn(0f, 1f)
        val sleepProgress = ((sleepAvg.toFloat()) / 9f).coerceIn(0f, 1f)
        val waterProgress = (waterTotal / (2000f * 7)).coerceIn(0f, 1f)
        TrendItem("近7日步数", "$stepsTotal 步", Color(0xFFFF8C00), progress = stepsProgress)
        TrendItem("平均睡眠", String.format("%.1f 小时", sleepAvg), Color(0xFF4B0082), progress = sleepProgress)
        TrendItem("近7日饮水", "$waterTotal ml", Color(0xFF2AC56A), progress = waterProgress)
    }
}

@Composable
fun TrendItem(label: String, value: String, color: Color, progress: Float = 0.7f) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(80.dp)
                .height(4.dp),
            color = color,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
        
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
