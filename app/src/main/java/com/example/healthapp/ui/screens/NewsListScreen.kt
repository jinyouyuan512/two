package com.example.healthapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.healthapp.ui.theme.HealthPurple
import com.example.healthapp.ui.theme.HealthBlue
import com.example.healthapp.viewmodel.HealthNewsViewModel

@Composable
fun HealthNewsListScreen(navController: NavHostController) {
    val vm: HealthNewsViewModel = viewModel()
    LaunchedEffect(Unit) { vm.load(50) }
    val allNews by vm.news.collectAsState()
    var range by remember { mutableStateOf(Range.Days7) }
    var showCount by remember { mutableStateOf(10) }

    val filtered = remember(allNews, range) {
        val now = java.time.Instant.now()
        val cutoff = when (range) {
            Range.Days7 -> now.minus(java.time.Duration.ofDays(7))
            Range.Days30 -> now.minus(java.time.Duration.ofDays(30))
            Range.All -> null
        }
        val byRange = allNews.filter { n ->
            val ts = (n.published_at ?: n.created_at)
            runCatching { java.time.OffsetDateTime.parse(ts) }
                .getOrNull()?.toInstant()?.let { inst -> cutoff?.let { inst.isAfter(it) } ?: true } ?: true
        }
        byRange.distinctBy { it.title.trim() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(HealthPurple, HealthBlue)))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            OutlinedButton(onClick = {
                val popped = navController.popBackStack("home", false)
                if (!popped) {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                Text("← 返回首页")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "健康新闻列表", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        FilterRow(range) { range = it; showCount = 10 }
        Spacer(modifier = Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                val uri = LocalUriHandler.current
                if (filtered.isEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "暂无新闻", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.load(50) }) { Text("刷新") }
                            Button(onClick = { vm.purgeDuplicates() }) { Text("清理重复") }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(filtered.take(showCount)) { idx, item ->
                            val timeStr = item.published_at ?: item.created_at
                            val line = "${idx + 1}. ${item.title} 来源：${item.source ?: "未知"} | 时间：${timeStr}"
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = line, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (!item.url.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "`" + item.url + "`", fontSize = 12.sp, color = HealthBlue, modifier = Modifier.clickable { uri.openUri(item.url!!) })
                                }
                                if (!item.summary.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = item.summary!!, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { vm.purgeDuplicates() }) { Text("清理重复") }
                    Button(onClick = { showCount += 10 }, enabled = filtered.size > showCount) { Text("加载更多") }
                }
            }
        }
    }
}

private enum class Range { Days7, Days30, All }

@Composable
private fun FilterRow(current: Range, onChange: (Range) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = current == Range.Days7, onClick = { onChange(Range.Days7) }, label = { Text("近7天") })
        FilterChip(selected = current == Range.Days30, onClick = { onChange(Range.Days30) }, label = { Text("近30天") })
        FilterChip(selected = current == Range.All, onClick = { onChange(Range.All) }, label = { Text("全部") })
    }
}
