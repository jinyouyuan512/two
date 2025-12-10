package com.example.healthapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthapp.data.MockData
import com.example.healthapp.ui.theme.HealthBlue
import com.example.healthapp.ui.theme.HealthRed
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.viewmodel.DataImportViewModel
import com.example.healthapp.viewmodel.MetricsViewModel

@Composable
fun DataScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val metricsVm: MetricsViewModel = viewModel()
    LaunchedEffect(Unit) { metricsVm.load() }
    val heartRateData = if (metricsVm.heartRatesView.isNotEmpty()) metricsVm.heartRatesView else MockData.getHeartRateData()
    val importVm: com.example.healthapp.viewmodel.DataImportViewModel = viewModel()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        DataHeaderSection(onImportClick = { navController.navigate("import") })
        
        // 导入数据摘要
        ImportedSummary(importVm)

        TimeRangeSelector(metricsVm)
        HealthHighlight(selectedTab, metricsVm)
        
        DataTabSection(selectedTab) { selectedTab = it }
        
        // Chart or Content based on selected tab
        when (selectedTab) {
            0 -> HeartRateChartSection(heartRateData)
            1 -> StepsChartSection(steps = metricsVm.stepsView)
            2 -> WeightChartSection(weights = metricsVm.weightsView)
            3 -> WaterChartSection(intakes = metricsVm.waterIntakeView)
            4 -> SleepChartSection(hours = metricsVm.sleepHoursView)
        }
        if (selectedTab == 5) {
            MoodChartSection(moods = metricsVm.moods)
        }
        
        // Recent Records
        RecentRecordsSection(metricsVm.recentRecords)
    }
}

@Composable
fun DataHeaderSection(onImportClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "健康数据中心",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onImportClick,
                colors = ButtonDefaults.buttonColors(containerColor = HealthBlue)
            ) {
                Text(text = "+ 导入数据")
            }
        }
    }
}

@Composable
fun TimeRangeSelector(vm: MetricsViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "时间范围", fontSize = 12.sp, color = Color.Gray)
        val options = listOf(
            Pair("7天", com.example.healthapp.viewmodel.MetricsViewModel.TimeRange.Days7),
            Pair("14天", com.example.healthapp.viewmodel.MetricsViewModel.TimeRange.Days14),
            Pair("30天", com.example.healthapp.viewmodel.MetricsViewModel.TimeRange.Days30)
        )
        options.forEach { (label, value) ->
            FilterChip(
                selected = vm.timeRange == value,
                onClick = { vm.updateTimeRange(value) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun MetricCardsSection(vm: MetricsViewModel) {
    val hrAvg = if (vm.heartRates.isNotEmpty()) vm.heartRates.map { it.value }.average().toInt().toString() else "-"
    val weightLatest = vm.weightLatest?.let { String.format("%.1f", it) } ?: "-"
    val rangeDays = when (vm.timeRange) { com.example.healthapp.viewmodel.MetricsViewModel.TimeRange.Days7 -> 7; com.example.healthapp.viewmodel.MetricsViewModel.TimeRange.Days14 -> 14; com.example.healthapp.viewmodel.MetricsViewModel.TimeRange.Days30 -> 30 }
    val stepsTotal = vm.stepsTotal ?: (vm.stepsLatest ?: 0)
    val waterTotal = vm.waterTotal ?: (vm.waterLatest ?: 0)
    val stepsProgress = (stepsTotal.toFloat() / (10000f * rangeDays)).coerceIn(0f, 1f)
    val waterProgress = (waterTotal.toFloat() / (2000f * rangeDays)).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetricCard("心率", hrAvg, "bpm")
        MetricCard("步数", stepsTotal.toString(), "步")
        MetricCard("饮水", waterTotal.toString(), "ml")
        MetricCard("体重", weightLatest, "kg")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = "步数进度", fontSize = 12.sp, color = Color.Gray)
            LinearProgressIndicator(progress = { stepsProgress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Color(0xFFFF8C00), trackColor = Color.LightGray)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(text = "饮水进度", fontSize = 12.sp, color = Color.Gray)
            LinearProgressIndicator(progress = { waterProgress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = HealthBlue, trackColor = Color.LightGray)
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(80.dp),
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
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = when (title) {
                    "心率" -> HealthRed
                    "步数" -> Color(0xFFFF8C00)
                    "饮水" -> HealthBlue
                    "体重" -> Color(0xFF4B0082)
                    else -> Color.Black
                }
            )
            Text(
                text = unit,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun DataTabSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("心率", "步数", "体重", "饮水", "睡眠", "情绪")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs.size) { index ->
            val title = tabs[index]
            FilterChip(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                label = { Text(title) }
            )
        }
    }
}

@Composable
fun HealthHighlight(selectedTab: Int, vm: MetricsViewModel) {
    val rangeDays = when (vm.timeRange) {
        com.example.healthapp.viewmodel.MetricsViewModel.TimeRange.Days7 -> 7
        com.example.healthapp.viewmodel.MetricsViewModel.TimeRange.Days14 -> 14
        com.example.healthapp.viewmodel.MetricsViewModel.TimeRange.Days30 -> 30
    }
    val title: String
    val line1: String
    val line2: String
    when (selectedTab) {
        0 -> {
            title = "心率 · 正常数据范围"
            line1 = "静息心率 60–100 bpm（成人）"
            line2 = "运动时心率因人而异，保持舒适可谈话区间"
        }
        1 -> {
            val minTotal = 7000 * rangeDays
            val targetTotal = 10000 * rangeDays
            title = "步数 · 正常数据范围"
            line1 = "日均推荐 ≥ 7,000–10,000 步"
            line2 = "区间推荐总量 ≥ ${minTotal}–${targetTotal} 步"
        }
        2 -> {
            title = "体重 · 正常数据范围"
            line1 = "近 ${rangeDays} 天波动建议 ≤ 1.0 kg"
            line2 = "日均变化建议在 ±0.2 kg 以内"
        }
        3 -> {
            val minMl = 1500 * rangeDays
            val targetMl = 2000 * rangeDays
            title = "饮水 · 正常数据范围"
            line1 = "日均推荐 1,500–2,000 ml"
            line2 = "区间推荐总量 ${minMl}–${targetMl} ml"
        }
        else -> {
            title = "健康概览"
            line1 = "请选择上方分类查看正常范围"
            line2 = "支持心率、步数、体重、饮水"
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = line1, fontSize = 12.sp, color = Color.Gray)
            Text(text = line2, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DataTabButton(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) HealthBlue else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Text(text = title, fontSize = 14.sp)
    }
}

@Composable
fun HeartRateChartSection(data: List<com.example.healthapp.model.HeartRateData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val avg = if (data.isNotEmpty()) data.map { it.value }.average().toInt() else 0
        Text(
            text = "心率趋势（均值 ${avg} bpm）",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                factory = { ctx ->
                    val chart = com.github.mikephil.charting.charts.LineChart(ctx)
                    chart.description.isEnabled = false
                    chart.setNoDataText("暂无心率数据")
                    chart.axisRight.isEnabled = false
                    chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    chart.xAxis.granularity = 1f
                    chart.xAxis.labelRotationAngle = -30f
                    chart.legend.isEnabled = false
                    chart.axisLeft.textColor = android.graphics.Color.DKGRAY
                    chart.xAxis.textColor = android.graphics.Color.DKGRAY
                    chart
                },
                update = { chart ->
                    if (data.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }
                    val labels = data.map { it.time }
                    val entries = data.mapIndexed { index, hr -> com.github.mikephil.charting.data.Entry(index.toFloat(), hr.value.toFloat()) }
                    val set = com.github.mikephil.charting.data.LineDataSet(entries, "心率").apply {
                        color = android.graphics.Color.RED
                        setDrawCircles(false)
                        lineWidth = 2f
                        valueTextSize = 10f
                        valueTextColor = android.graphics.Color.DKGRAY
                        setDrawValues(false)
                        setDrawFilled(true)
                        fillColor = android.graphics.Color.argb(64, 255, 0, 0)
                    }
                    chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    chart.data = com.github.mikephil.charting.data.LineData(set)
                    chart.animateX(300)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun StepsChartSection(steps: List<Pair<String, Int>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val total = steps.sumOf { it.second }
        Text(
            text = "步数趋势（总计 ${total} 步）",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                factory = { ctx ->
                    val chart = com.github.mikephil.charting.charts.BarChart(ctx)
                    chart.description.isEnabled = false
                    chart.setNoDataText("暂无步数数据")
                    chart.axisRight.isEnabled = false
                    chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    chart.xAxis.granularity = 1f
                    chart.xAxis.labelRotationAngle = -30f
                    chart.legend.isEnabled = false
                    chart.axisLeft.textColor = android.graphics.Color.DKGRAY
                    chart.xAxis.textColor = android.graphics.Color.DKGRAY
                    chart
                },
                update = { chart ->
                    if (steps.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }
                    val labels = steps.map { it.first }
                    val entries = steps.mapIndexed { index, pair -> com.github.mikephil.charting.data.BarEntry(index.toFloat(), pair.second.toFloat()) }
                    val set = com.github.mikephil.charting.data.BarDataSet(entries, "步数").apply {
                        color = android.graphics.Color.rgb(255, 140, 0)
                        valueTextSize = 10f
                        valueTextColor = android.graphics.Color.DKGRAY
                        setDrawValues(false)
                    }
                    chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    chart.data = com.github.mikephil.charting.data.BarData(set).apply { barWidth = 0.5f }
                    chart.animateY(300)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun WeightChartSection(weights: List<Pair<String, Double>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "体重变化趋势",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                factory = { ctx ->
                    val chart = com.github.mikephil.charting.charts.LineChart(ctx)
                    chart.description.isEnabled = false
                    chart.setNoDataText("暂无体重数据")
                    chart.axisRight.isEnabled = false
                    chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    chart.xAxis.granularity = 1f
                    chart.xAxis.labelRotationAngle = -30f
                    chart.legend.isEnabled = false
                    chart.axisLeft.textColor = android.graphics.Color.DKGRAY
                    chart.xAxis.textColor = android.graphics.Color.DKGRAY
                    chart
                },
                update = { chart ->
                    if (weights.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }
                    val labels = weights.map { it.first }
                    val entries = weights.mapIndexed { index, pair -> com.github.mikephil.charting.data.Entry(index.toFloat(), pair.second.toFloat()) }
                    val set = com.github.mikephil.charting.data.LineDataSet(entries, "体重").apply {
                        color = android.graphics.Color.rgb(75, 0, 130)
                        setDrawCircles(false)
                        lineWidth = 2f
                        valueTextSize = 10f
                        valueTextColor = android.graphics.Color.DKGRAY
                        setDrawValues(false)
                    }
                    chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    chart.data = com.github.mikephil.charting.data.LineData(set)
                    chart.animateX(300)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun WaterChartSection(intakes: List<Pair<String, Int>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        val total = intakes.sumOf { it.second }
        Text(
            text = "饮水量趋势（总计 ${total} ml）",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                factory = { ctx ->
                    val chart = com.github.mikephil.charting.charts.BarChart(ctx)
                    chart.description.isEnabled = false
                    chart.setNoDataText("暂无饮水数据")
                    chart.axisRight.isEnabled = false
                    chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    chart.xAxis.granularity = 1f
                    chart.xAxis.labelRotationAngle = -30f
                    chart.legend.isEnabled = false
                    chart.axisLeft.textColor = android.graphics.Color.DKGRAY
                    chart.xAxis.textColor = android.graphics.Color.DKGRAY
                    chart
                },
                update = { chart ->
                    if (intakes.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }
                    val labels = intakes.map { it.first }
                    val entries = intakes.mapIndexed { index, pair -> com.github.mikephil.charting.data.BarEntry(index.toFloat(), pair.second.toFloat()) }
                    val set = com.github.mikephil.charting.data.BarDataSet(entries, "饮水").apply {
                        color = android.graphics.Color.rgb(30, 144, 255)
                        valueTextSize = 10f
                        valueTextColor = android.graphics.Color.DKGRAY
                        setDrawValues(false)
                    }
                    chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    chart.data = com.github.mikephil.charting.data.BarData(set).apply { barWidth = 0.5f }
                    chart.animateY(300)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun SleepChartSection(hours: List<Pair<String, Double>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "睡眠时长趋势",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                factory = { ctx ->
                    val chart = com.github.mikephil.charting.charts.LineChart(ctx)
                    chart.description.isEnabled = false
                    chart.setNoDataText("暂无睡眠数据")
                    chart.axisRight.isEnabled = false
                    chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    chart.xAxis.granularity = 1f
                    chart.xAxis.labelRotationAngle = -30f
                    chart.legend.isEnabled = false
                    chart.axisLeft.textColor = android.graphics.Color.DKGRAY
                    chart.xAxis.textColor = android.graphics.Color.DKGRAY
                    chart
                },
                update = { chart ->
                    if (hours.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }
                    val labels = hours.map { it.first }
                    val entries = hours.mapIndexed { index, pair -> com.github.mikephil.charting.data.Entry(index.toFloat(), pair.second.toFloat()) }
                    val set = com.github.mikephil.charting.data.LineDataSet(entries, "睡眠时长").apply {
                        color = android.graphics.Color.rgb(0, 128, 128)
                        setDrawCircles(false)
                        lineWidth = 2f
                        valueTextSize = 10f
                        valueTextColor = android.graphics.Color.DKGRAY
                        setDrawValues(false)
                    }
                    chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    chart.data = com.github.mikephil.charting.data.LineData(set)
                    chart.animateX(300)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun MoodChartSection(moods: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "情绪分布",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                factory = { ctx ->
                    val chart = com.github.mikephil.charting.charts.BarChart(ctx)
                    chart.description.isEnabled = false
                    chart.setNoDataText("暂无情绪数据")
                    chart.axisRight.isEnabled = false
                    chart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    chart.xAxis.granularity = 1f
                    chart.xAxis.labelRotationAngle = -30f
                    chart.legend.isEnabled = false
                    chart.axisLeft.textColor = android.graphics.Color.DKGRAY
                    chart.xAxis.textColor = android.graphics.Color.DKGRAY
                    chart
                },
                update = { chart ->
                    if (moods.isEmpty()) {
                        chart.data = null
                        chart.invalidate()
                        return@AndroidView
                    }
                    val counts = moods.groupingBy { it.second }.eachCount()
                    val labels = counts.keys.toList()
                    val entries = labels.mapIndexed { index, key ->
                        com.github.mikephil.charting.data.BarEntry(index.toFloat(), (counts[key] ?: 0).toFloat())
                    }
                    val set = com.github.mikephil.charting.data.BarDataSet(entries, "情绪").apply {
                        color = android.graphics.Color.rgb(100, 181, 246)
                        valueTextSize = 10f
                        valueTextColor = android.graphics.Color.DKGRAY
                        setDrawValues(false)
                    }
                    chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    chart.data = com.github.mikephil.charting.data.BarData(set).apply { barWidth = 0.5f }
                    chart.animateY(300)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RecentRecordsSection(records: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "最近记录",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        records.forEach { record ->
            RecordItem(record)
        }
    }
}

@Composable
fun RecordItem(record: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = record,
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp
        )
    }
}
@Composable
fun ImportedSummary(vm: DataImportViewModel) {
    if (vm.imported.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "已导入 ${vm.imported.size} 条数据")
                val first = vm.imported.first()
                val parts = buildList {
                    add("日期 ${first.date ?: "-"}")
                    first.steps?.let { add("步数 $it") }
                    first.heartRate?.let { add("心率 $it") }
                    first.sleepHours?.let { add("睡眠 ${String.format("%.1f", it)} 小时") }
                    first.weightKg?.let { add("体重 ${String.format("%.1f", it)} kg") }
                    first.waterMl?.let { add("饮水 ${it} ml") }
                    first.mood?.let { add("情绪 ${it}") }
                }
                Text(text = "示例：${parts.joinToString("，")}")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
