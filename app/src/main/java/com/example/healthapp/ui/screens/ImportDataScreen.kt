package com.example.healthapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthapp.viewmodel.DataImportViewModel
import com.example.healthapp.viewmodel.ImportedRecord
import com.example.healthapp.data.repository.ImportsRepository
import com.example.healthapp.data.remote.RpaApi
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ImportDataScreen(navController: NavHostController, vm: DataImportViewModel = viewModel()) {
    val context = LocalContext.current
    var preview by remember { mutableStateOf<List<ImportedRecord>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var lastFileName by remember { mutableStateOf<String?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var rpaMessage by remember { mutableStateOf<String?>(null) }
    var rpaLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var manualDate by remember { mutableStateOf("") }
    var manualSteps by remember { mutableStateOf("") }
    var manualSleep by remember { mutableStateOf("") }
    var manualHr by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val input = context.contentResolver.openInputStream(uri) ?: return@runCatching
                val text = BufferedReader(InputStreamReader(input)).use { it.readText() }
                val records = when {
                    uri.toString().endsWith(".csv", true) -> parseCsv(text, vm.type)
                    uri.toString().endsWith(".json", true) -> parseJson(text, vm.type)
                    else -> {
                        // 尝试根据内容判断
                        if (text.trim().startsWith("{")) parseJson(text, vm.type) else parseCsv(text, vm.type)
                    }
                }
                preview = records
                message = "已解析 ${records.size} 条记录"
                lastFileName = uri.lastPathSegment
            }.onFailure {
                message = "解析失败：${it.message}"
                preview = emptyList()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "导入健康数据", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        var typeMenuExpanded by remember { mutableStateOf(false) }
        val typeLabel = when (vm.type) {
            DataImportViewModel.ImportType.Steps -> "步数"
            DataImportViewModel.ImportType.HeartRate -> "心率"
            DataImportViewModel.ImportType.Sleep -> "睡眠"
            DataImportViewModel.ImportType.Weight -> "体重"
            DataImportViewModel.ImportType.Water -> "饮水"
            DataImportViewModel.ImportType.Mood -> "情绪"
        }
        Box {
            OutlinedButton(onClick = { typeMenuExpanded = true }) { Text("类型：$typeLabel") }
            DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                DropdownMenuItem(text = { Text("步数") }, onClick = { vm.setImportType(DataImportViewModel.ImportType.Steps); typeMenuExpanded = false })
                DropdownMenuItem(text = { Text("心率") }, onClick = { vm.setImportType(DataImportViewModel.ImportType.HeartRate); typeMenuExpanded = false })
                DropdownMenuItem(text = { Text("睡眠") }, onClick = { vm.setImportType(DataImportViewModel.ImportType.Sleep); typeMenuExpanded = false })
                DropdownMenuItem(text = { Text("体重") }, onClick = { vm.setImportType(DataImportViewModel.ImportType.Weight); typeMenuExpanded = false })
                DropdownMenuItem(text = { Text("饮水") }, onClick = { vm.setImportType(DataImportViewModel.ImportType.Water); typeMenuExpanded = false })
                DropdownMenuItem(text = { Text("情绪") }, onClick = { vm.setImportType(DataImportViewModel.ImportType.Mood); typeMenuExpanded = false })
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { launcher.launch(arrayOf("text/csv", "application/json")) }) {
                Text(text = "选择文件")
            }
            OutlinedButton(onClick = { preview = emptyList(); message = null }) {
                Text(text = "清空预览")
            }
            OutlinedButton(onClick = {
                if (preview.isEmpty()) { rpaMessage = "请先添加预览数据"; return@OutlinedButton }
                rpaLoading = true
                rpaMessage = null
                scope.launch {
                    val workflow = when (vm.type) {
                        DataImportViewModel.ImportType.Steps -> "import-steps"
                        DataImportViewModel.ImportType.HeartRate -> "import-heart-rate"
                        DataImportViewModel.ImportType.Sleep -> "import-sleep"
                        DataImportViewModel.ImportType.Weight -> "import-weight"
                        DataImportViewModel.ImportType.Water -> "import-water"
                        DataImportViewModel.ImportType.Mood -> "import-mood"
                    }
                    val payload = preview.map { r ->
                        mapOf(
                            "date" to r.date,
                            "steps" to r.steps,
                            "sleepHours" to r.sleepHours,
                            "heartRate" to r.heartRate,
                            "kg" to r.weightKg,
                            "ml" to r.waterMl,
                            "mood" to r.mood,
                            "note" to r.moodNote,
                            "score" to r.moodScore
                        )
                    }
                    val ok = runCatching { RpaApi().trigger(workflow, mapOf("type" to vm.type.name, "records" to payload)) }.getOrDefault(false)
                    rpaMessage = if (ok) "已触发RPA工作流" else "触发失败，请检查 webhook 与 token"
                    rpaLoading = false
                }
            }) {
                Text(text = if (rpaLoading) "RPA处理中..." else "启动RPA自动化")
            }
        }

        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message!!)
        }
        if (rpaMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = rpaMessage!!)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "手动输入")
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = manualDate,
                onValueChange = { manualDate = it },
                label = { Text("日期") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            when (vm.type) {
                DataImportViewModel.ImportType.Steps -> {
                    OutlinedTextField(value = manualSteps, onValueChange = { manualSteps = it }, label = { Text("步数") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                DataImportViewModel.ImportType.HeartRate -> {
                    OutlinedTextField(value = manualHr, onValueChange = { manualHr = it }, label = { Text("心率(bpm)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                DataImportViewModel.ImportType.Sleep -> {
                    OutlinedTextField(value = manualSleep, onValueChange = { manualSleep = it }, label = { Text("睡眠(小时)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                DataImportViewModel.ImportType.Weight -> {
                    OutlinedTextField(value = manualSleep, onValueChange = { manualSleep = it }, label = { Text("体重(kg)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                DataImportViewModel.ImportType.Water -> {
                    OutlinedTextField(value = manualSteps, onValueChange = { manualSteps = it }, label = { Text("饮水(ml)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                DataImportViewModel.ImportType.Mood -> {
                    var mood by remember { mutableStateOf("") }
                    var note by remember { mutableStateOf("") }
                    var score by remember { mutableStateOf("") }
                    OutlinedTextField(value = mood, onValueChange = { mood = it }, label = { Text("情绪") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = score, onValueChange = { score = it }, label = { Text("评分(可选)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        val rec = ImportedRecord(
                            date = manualDate.ifBlank { null },
                            steps = null,
                            sleepHours = null,
                            heartRate = null,
                            weightKg = null,
                            waterMl = null,
                            mood = mood.ifBlank { null },
                            moodNote = note.ifBlank { null },
                            moodScore = score.toIntOrNull()
                        )
                        preview = preview + rec
                        message = "已添加 1 条情绪记录"
                        manualDate = ""
                    }, modifier = Modifier.fillMaxWidth()) { Text("添加到预览") }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Button(onClick = {
                val rec = when (vm.type) {
                    DataImportViewModel.ImportType.Steps -> ImportedRecord(date = manualDate.ifBlank { null }, steps = manualSteps.toIntOrNull(), sleepHours = null, heartRate = null)
                    DataImportViewModel.ImportType.HeartRate -> ImportedRecord(date = manualDate.ifBlank { null }, steps = null, sleepHours = null, heartRate = manualHr.toIntOrNull())
                    DataImportViewModel.ImportType.Sleep -> ImportedRecord(date = manualDate.ifBlank { null }, steps = null, sleepHours = manualSleep.toDoubleOrNull(), heartRate = null)
                    DataImportViewModel.ImportType.Weight -> ImportedRecord(date = manualDate.ifBlank { null }, steps = null, sleepHours = null, heartRate = null, weightKg = manualSleep.toDoubleOrNull())
                    DataImportViewModel.ImportType.Water -> ImportedRecord(date = manualDate.ifBlank { null }, steps = null, sleepHours = null, heartRate = null, waterMl = manualSteps.toIntOrNull())
                    DataImportViewModel.ImportType.Mood -> null
                }
                if (rec != null) {
                    preview = preview + rec
                    message = "已添加 1 条手动记录"
                }
                manualDate = ""
                manualSteps = ""
                manualSleep = ""
                manualHr = ""
            }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "添加到预览")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(preview) { r ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "日期：${r.date ?: "-"}")
                        when (vm.type) {
                            DataImportViewModel.ImportType.Steps -> Text(text = "步数：${r.steps ?: 0}")
                            DataImportViewModel.ImportType.HeartRate -> Text(text = "心率：${r.heartRate ?: 0} bpm")
                            DataImportViewModel.ImportType.Sleep -> Text(text = "睡眠：${r.sleepHours ?: 0.0} 小时")
                            DataImportViewModel.ImportType.Weight -> Text(text = "体重：${r.weightKg ?: 0.0} kg")
                            DataImportViewModel.ImportType.Water -> Text(text = "饮水：${r.waterMl ?: 0} ml")
                            DataImportViewModel.ImportType.Mood -> {
                                Text(text = "情绪：${r.mood ?: "-"}")
                                Text(text = "备注：${r.moodNote ?: "-"}")
                                Text(text = "评分：${r.moodScore ?: 0}")
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                uploading = true
                uploadMessage = null
                val repo = ImportsRepository()
                scope.launch {
                    runCatching {
                        val ok = repo.createJob(
                            filename = lastFileName,
                            source = if (lastFileName != null) "file" else "manual",
                            rowsCount = preview.size
                        )
                        ok
                    }.onSuccess { ok ->
                        if (ok) {
                            val count = repo.saveByType(vm.type, preview)
                            vm.updateImported(preview)
                            uploadMessage = "导入成功：${count} 条"
                            navController.popBackStack()
                        } else {
                            val count = repo.saveByType(vm.type, preview)
                            vm.updateImported(preview)
                            uploadMessage = "导入成功：${count} 条"
                            navController.popBackStack()
                        }
                    }.onFailure {
                        uploadMessage = "导入失败：${it.message ?: "未知错误"}。请确认已登录且日期格式正确（如 2025-12-01 或 2025-12-01T08:00:00Z）"
                    }
                    uploading = false
                }
            },
            enabled = preview.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (uploading) "正在导入..." else "确认导入")
        }

        if (uploadMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = uploadMessage!!)
        }
    }
}

private fun parseCsv(text: String, type: DataImportViewModel.ImportType): List<ImportedRecord> {
    val lines = text.lines().filter { it.isNotBlank() }
    if (lines.isEmpty()) return emptyList()
    val delimiter = detectDelimiter(lines.first())
    val header = splitCsvLine(lines.first(), delimiter).map { it.trim().trim('"') }
    val dateIdx = header.indexOfFirst { it.equals("date", true) || it.contains("日期") }
    val stepsIdx = header.indexOfFirst { it.equals("steps", true) || it.contains("步数") || it.equals("count", true) }
    val sleepIdx = header.indexOfFirst { it.equals("sleepHours", true) || it.contains("睡眠") || it.equals("hours", true) }
    val hrIdx = header.indexOfFirst { it.equals("heartRate", true) || it.contains("心率") || it.equals("bpm", true) }
    val kgIdx = header.indexOfFirst { it.equals("kg", true) || it.contains("体重") }
    val mlIdx = header.indexOfFirst { it.equals("ml", true) || it.contains("饮水") }
    val moodIdx = header.indexOfFirst { it.equals("mood", true) || it.contains("情绪") }
    val noteIdx = header.indexOfFirst { it.equals("note", true) || it.contains("备注") }
    val scoreIdx = header.indexOfFirst { it.equals("score", true) || it.contains("评分") }

    return lines.drop(1).map { line ->
        val cols = splitCsvLine(line, delimiter)
        val date = cols.getOrNull(dateIdx)?.trim()?.trim('"')
        when (type) {
            DataImportViewModel.ImportType.Steps -> ImportedRecord(date = date, steps = cols.getOrNull(stepsIdx)?.trim()?.trim('"')?.toIntOrNull(), sleepHours = null, heartRate = null)
            DataImportViewModel.ImportType.HeartRate -> ImportedRecord(date = date, steps = null, sleepHours = null, heartRate = cols.getOrNull(hrIdx)?.trim()?.trim('"')?.toIntOrNull())
            DataImportViewModel.ImportType.Sleep -> ImportedRecord(date = date, steps = null, sleepHours = cols.getOrNull(sleepIdx)?.trim()?.trim('"')?.toDoubleOrNull(), heartRate = null)
            DataImportViewModel.ImportType.Weight -> ImportedRecord(date = date, steps = null, sleepHours = null, heartRate = null, weightKg = cols.getOrNull(kgIdx)?.trim()?.trim('"')?.toDoubleOrNull())
            DataImportViewModel.ImportType.Water -> ImportedRecord(date = date, steps = null, sleepHours = null, heartRate = null, waterMl = cols.getOrNull(mlIdx)?.trim()?.trim('"')?.toIntOrNull())
            DataImportViewModel.ImportType.Mood -> ImportedRecord(date = date, steps = null, sleepHours = null, heartRate = null, mood = cols.getOrNull(moodIdx)?.trim()?.trim('"'), moodNote = cols.getOrNull(noteIdx)?.trim()?.trim('"'), moodScore = cols.getOrNull(scoreIdx)?.trim()?.trim('"')?.toIntOrNull())
        }
    }
}

private fun detectDelimiter(header: String): Char {
    val comma = header.count { it == ',' }
    val semicolon = header.count { it == ';' }
    val tab = header.count { it == '\t' }
    return when {
        semicolon > comma && semicolon >= tab -> ';'
        tab > comma && tab >= semicolon -> '\t'
        else -> ','
    }
}

private fun splitCsvLine(line: String, delimiter: Char): List<String> {
    val result = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        if (c == '"') {
            if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                sb.append('"')
                i++
            } else {
                inQuotes = !inQuotes
            }
        } else if (c == delimiter && !inQuotes) {
            result.add(sb.toString())
            sb.clear()
        } else {
            sb.append(c)
        }
        i++
    }
    result.add(sb.toString())
    return result
}

private fun parseJson(text: String, type: DataImportViewModel.ImportType): List<ImportedRecord> {
    return runCatching {
        val root = text.trim()
        val arr = if (root.startsWith("[")) JSONArray(root) else JSONObject(root).optJSONArray("records")
        if (arr == null) emptyList() else (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val d = o.optString("date").ifBlank { null }
            when (type) {
                DataImportViewModel.ImportType.Steps -> ImportedRecord(date = d, steps = o.optInt("steps"), sleepHours = null, heartRate = null)
                DataImportViewModel.ImportType.HeartRate -> ImportedRecord(date = d, steps = null, sleepHours = null, heartRate = o.optInt("heartRate"))
                DataImportViewModel.ImportType.Sleep -> ImportedRecord(date = d, steps = null, sleepHours = o.optDouble("sleepHours"), heartRate = null)
                DataImportViewModel.ImportType.Weight -> ImportedRecord(date = d, steps = null, sleepHours = null, heartRate = null, weightKg = o.optDouble("kg"))
                DataImportViewModel.ImportType.Water -> ImportedRecord(date = d, steps = null, sleepHours = null, heartRate = null, waterMl = o.optInt("ml"))
                DataImportViewModel.ImportType.Mood -> ImportedRecord(date = d, steps = null, sleepHours = null, heartRate = null, mood = o.optString("mood"), moodNote = o.optString("note"), moodScore = if (o.has("score")) o.optInt("score") else null)
            }
        }
    }.getOrElse { emptyList() }
}
