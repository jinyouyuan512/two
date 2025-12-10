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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import com.example.healthapp.viewmodel.NutritionViewModel
import com.example.healthapp.ui.theme.HealthDarkGreen
import com.example.healthapp.ui.theme.HealthGreen
import com.example.healthapp.ui.theme.HealthBlue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.launch
import com.example.healthapp.data.remote.FoodRecognitionApi

import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp

@Composable
fun SimpleFlowRow(
    modifier: Modifier = Modifier,
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val rows = mutableListOf<List<Placeable>>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<Placeable>()
        var currentWidth = 0
        var currentHeight = 0

        measurables.map { it.measure(constraints.copy(minWidth = 0)) }.forEach { placeable ->
            if (currentWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                rowHeights.add(currentHeight)
                currentRow = mutableListOf()
                currentWidth = 0
                currentHeight = 0
            }
            currentRow.add(placeable)
            currentWidth += placeable.width + horizontalGap.roundToPx()
            currentHeight = maxOf(currentHeight, placeable.height)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowHeights.add(currentHeight)
        }

        val totalHeight = rowHeights.sum() + (rows.size - 1).coerceAtLeast(0) * verticalGap.roundToPx()
        
        layout(width = constraints.maxWidth, height = totalHeight.coerceAtLeast(0)) {
            var y = 0
            rows.forEachIndexed { index, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.place(x, y)
                    x += placeable.width + horizontalGap.roundToPx()
                }
                y += rowHeights[index] + verticalGap.roundToPx()
            }
        }
    }
}

data class FoodWithConfidence(val food: String, val confidence: Float, val calories: Int? = null, val source: String = "")

@Composable
fun NutritionScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val vm: NutritionViewModel = viewModel()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        vm.init()
        vm.loadRecords(context)
    }
    val mealRecords by vm.meals.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HealthGreen, HealthDarkGreen)
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        // Header with progress
        NutritionHeaderSection(vm)
        
        // Tabs
        NutritionTabSection(selectedTab) { selectedTab = it }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> MealRecordSection(mealRecords, vm, context)
            1 -> NutritionAnalysisSection(vm)
            2 -> AISuggestionsSection(vm)
        }
    }
}

@Composable
fun NutritionHeaderSection(vm: NutritionViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "营养管理",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        var showCalorieTargetDialog by remember { mutableStateOf(false) }
        val goal by vm.calorieTarget.collectAsState()
        
        // Progress section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                val totals = vm.totals()
                val progress = (totals.calories.toFloat() / goal).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = totals.calories.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = HealthGreen
                    )
                    Text(
                        text = "$goal 卡路里",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                Text(
                    text = "已摄入",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = HealthGreen,
                    trackColor = Color.LightGray
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "建议目标进度",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showCalorieTargetDialog = true }) { Text(text = "设置卡路里目标") }
                }
            }
        }
        if (showCalorieTargetDialog) {
            var targetText by remember { mutableStateOf(goal.toString()) }
            AlertDialog(
                onDismissRequest = { showCalorieTargetDialog = false },
                confirmButton = {
                    Button(onClick = {
                        val t = targetText.trim().toIntOrNull()
                        if (t != null && t in 1000..4000) {
                            vm.setCalorieTarget(t)
                            showCalorieTargetDialog = false
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)) { Text("确定") }
                },
                dismissButton = { TextButton(onClick = { showCalorieTargetDialog = false }) { Text("取消") } },
                title = { Text("设置卡路里目标") },
                text = {
                    OutlinedTextField(value = targetText, onValueChange = { targetText = it }, label = { Text("每日目标(卡)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            )
        }
    }
}

@Composable
fun NutritionTabSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("饮食记录", "营养分析", "AI建议")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            NutritionTabButton(
                title = title,
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
fun NutritionTabButton(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
            contentColor = if (isSelected) HealthGreen else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Text(text = title, fontSize = 14.sp)
    }
}

@Composable
fun MealRecordSection(mealRecords: List<com.example.healthapp.model.MealRecord>, vm: NutritionViewModel, context: android.content.Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
    ) {
        // Quick add buttons
        QuickAddButtons(vm, context)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "今日记录",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        mealRecords.forEachIndexed { index, meal ->
            var showEdit by remember { mutableStateOf(false) }
            MealCard(meal,
                onDelete = {
                    vm.removeMeal(index)
                    vm.saveIfNeeded(context)
                },
                onEdit = { showEdit = true }
            )
            if (showEdit) {
                EditMealDialog(meal, onDismiss = { showEdit = false }, onConfirm = { mt, foods, cal, pro, car, fat, fib ->
                    vm.updateMealRecord(index, mt, foods, cal, fib, car, fat, pro)
                    vm.saveIfNeeded(context)
                    showEdit = false
                })
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Water intake section
        WaterIntakeSection(vm, context)
    }
}

@Composable
fun QuickAddButtons(vm: NutritionViewModel, context: android.content.Context) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        var showPhotoDialog by remember { mutableStateOf(false) }
        var showImageSourceDialog by remember { mutableStateOf(false) }
        var pickedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        var detectedFoods by remember { mutableStateOf<List<FoodWithConfidence>>(emptyList()) }
        var isRecognizing by remember { mutableStateOf(false) }
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        
        // 处理Bitmap
        fun processBitmap(bitmap: android.graphics.Bitmap) {
            pickedBitmap = bitmap
            isRecognizing = true
            detectedFoods = emptyList()
            
            val image = InputImage.fromBitmap(bitmap, 0)
            val rec = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            rec.process(image)
                .addOnSuccessListener { result ->
                    val text = result.text
                    val ocrFoods = suggestFoodsFromText(text)
                    val ocrFoodsWithConf = ocrFoods.map { FoodWithConfidence(it, 0.7f, source = "OCR") }
                    detectedFoods = detectedFoods + ocrFoodsWithConf
                }
                .addOnFailureListener { }
            
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val labelFoodsWithConf = labels.mapNotNull { label ->
                        val food = mapLabelToFood(label.text.lowercase())
                        if (food != null) {
                            FoodWithConfidence(food, label.confidence, source = "离线AI")
                        } else {
                            null
                        }
                    }
                    detectedFoods = detectedFoods + labelFoodsWithConf
                }
                .addOnFailureListener { }
            
            // 转换Bitmap为字节数组进行云端识别
            val stream = java.io.ByteArrayOutputStream()
            // 降低图片质量以加快传输并避免超过API限制
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, stream)
            val bytes = stream.toByteArray()
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            
            scope.launch {
                // 优先调用百度API
                val baiduApi = com.example.healthapp.data.remote.BaiduRecognitionApi()
                var baiduSuccess = false
                var errorMessage: String? = null
                runCatching {
                    val results = baiduApi.recognizeFood(base64)
                    if (results.isNotEmpty()) {
                        val baiduFoods = results.map { 
                            val cal = it.calorie?.toDoubleOrNull()?.toInt() ?: 0
                            FoodWithConfidence(it.name, it.probability.toFloatOrNull() ?: 0.8f, if (cal > 0) cal else null, source = "百度云") 
                        }
                        detectedFoods = detectedFoods + baiduFoods
                        baiduSuccess = true
                        android.widget.Toast.makeText(context, "百度智能云识别成功", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        errorMessage = "Baidu returned no results"
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                    errorMessage = e.message
                    android.widget.Toast.makeText(context, "百度识别失败: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }

                // 如果百度识别失败或未配置，尝试使用原来的API
                if (!baiduSuccess) {
                    if (errorMessage == "Baidu returned no results") {
                        android.widget.Toast.makeText(context, "百度API调用成功但未识别出食物，尝试备用服务", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    if (errorMessage?.contains("not configured") == true) {
                        android.widget.Toast.makeText(context, "请配置百度API Key以获得完整功能", android.widget.Toast.LENGTH_LONG).show()
                    }
                    val api = FoodRecognitionApi()
                    val resp = api.recognize(base64)
                    if (resp != null) {
                        val foodsCloudWithConf = resp.foods.map { FoodWithConfidence(it, 0.9f, source = "备用云") }
                        detectedFoods = detectedFoods + foodsCloudWithConf
                    }
                }
                
                // 所有识别完成后显示对话框
                isRecognizing = false
                showPhotoDialog = true
            }
        }
        
        // 处理图片URI
        fun processImageUri(uri: android.net.Uri) {
            runCatching {
                val input = context.contentResolver.openInputStream(uri)
                val bytes = input?.readBytes()
                input?.close()
                if (bytes != null) {
                    val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    processBitmap(bmp)
                }
            }
        }
        
        // 从相册选择图片
        val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                processImageUri(uri)
            }
        }
        
        // 相机拍照
        val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                processBitmap(bitmap)
            }
        }
        
        // 相机权限请求
        val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 获得权限后启动相机
                takePhotoLauncher.launch(null)
            }
        }
        
        var showDialog by remember { mutableStateOf(false) }
        var mealType by remember { mutableStateOf("") }
        var foods by remember { mutableStateOf("") }
        var caloriesText by remember { mutableStateOf("") }
        var fiberText by remember { mutableStateOf("") }
        var carbsText by remember { mutableStateOf("") }
        var fatText by remember { mutableStateOf("") }
        var proteinText by remember { mutableStateOf("") }
        Button(
            onClick = { showImageSourceDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📷")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "拍照识别")
            }
        }
        
        // 图片来源选择对话框
        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                confirmButton = { TextButton(onClick = { showImageSourceDialog = false }) { Text("取消") } },
                title = { Text("选择图片来源") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                showImageSourceDialog = false
                                // 检查相机权限
                                val hasCameraPermission = context.checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasCameraPermission) {
                                    // 已有权限，直接启动相机
                                    takePhotoLauncher.launch(null)
                                } else {
                                    // 请求相机权限
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📸")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "拍照")
                            }
                        }
                        Button(
                            onClick = {
                                pickImageLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                showImageSourceDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📁")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "从相册选择")
                            }
                        }
                    }
                }
            )
        }
        
        // 识别中加载指示器
        if (isRecognizing) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = { },
                title = { Text("识别中") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator()
                        Text(text = "正在识别食物...")
                    }
                }
            )
        }
        // 合并相同食物并按置信度排序，添加置信度阈值
        fun mergeAndSortFoods(foodsWithConf: List<FoodWithConfidence>, confidenceThreshold: Float = 0.3f): List<FoodWithConfidence> {
            return foodsWithConf.groupBy { it.food }
                .mapValues { (_, list) -> 
                    // 取置信度最高的那个，并且如果其中有卡路里信息，保留卡路里信息
                    val best = list.maxByOrNull { it.confidence }!!
                    val cal = list.firstNotNullOfOrNull { it.calories }
                    best.copy(calories = cal ?: best.calories)
                }
                .values
                .filter { it.confidence >= confidenceThreshold }
                .sortedByDescending { it.confidence }
                .toList()
        }
        
        if (showPhotoDialog && pickedBitmap != null) {
            val sortedFoods = mergeAndSortFoods(detectedFoods)
            // 添加调试信息
            println("Detected foods: $detectedFoods")
            println("Sorted foods: $sortedFoods")
            PhotoRecognizeDialog(bitmap = pickedBitmap!!, initialSelected = sortedFoods, onConfirm = { foodsRes, cal, fib, car, fatRes, pro ->
                val meal = if (mealType.isNotBlank()) mealType.trim() else currentMealType()
                vm.addMealRecord(meal, foodsRes, cal, fib, car, fatRes, pro)
                // 将选择的食物添加到历史记录
                foodsRes.split(" + ").forEach { food ->
                    vm.addFoodToHistory(food)
                }
                vm.saveIfNeeded(context)
                pickedBitmap = null
                showPhotoDialog = false
                detectedFoods = emptyList()
            }, onCancel = {
                pickedBitmap = null
                showPhotoDialog = false
                detectedFoods = emptyList()
            }, vm = vm)
        }
        
        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "+")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "手动添加")
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = {
                        val calories = caloriesText.trim().toIntOrNull()
                        val fiber = fiberText.trim().toIntOrNull() ?: 0
                        val carbs = carbsText.trim().toIntOrNull() ?: 0
                        val fat = fatText.trim().toIntOrNull() ?: 0
                        val protein = proteinText.trim().toIntOrNull() ?: 0
                        if (!mealType.isBlank() && !foods.isBlank() && calories != null && calories > 0) {
                            vm.addMealRecord(mealType.trim(), foods.trim(), calories, fiber, carbs, fat, protein)
                            vm.saveIfNeeded(context)
                            mealType = ""
                            foods = ""
                            caloriesText = ""
                            fiberText = ""
                            carbsText = ""
                            fatText = ""
                            proteinText = ""
                            showDialog = false
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)) { Text("确定") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("取消") } },
                title = { Text("添加饮食记录") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = mealType, onValueChange = { mealType = it }, label = { Text("餐次(早餐/午餐/晚餐)") })
                        OutlinedTextField(value = foods, onValueChange = { foods = it }, label = { Text("食物") })
                        OutlinedTextField(value = caloriesText, onValueChange = { caloriesText = it }, label = { Text("卡路里") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = proteinText, onValueChange = { proteinText = it }, label = { Text("蛋白(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                            OutlinedTextField(value = carbsText, onValueChange = { carbsText = it }, label = { Text("碳水(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = fatText, onValueChange = { fatText = it }, label = { Text("脂肪(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                            OutlinedTextField(value = fiberText, onValueChange = { fiberText = it }, label = { Text("膳食纤维(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MealCard(meal: com.example.healthapp.model.MealRecord, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = meal.mealType,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = meal.time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                text = meal.foods,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    meal.nutrients.forEach { (key, value) ->
                        NutrientTag(key, value)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Text(
                    text = "${meal.calories} 卡",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HealthGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text("编辑") }
                OutlinedButton(onClick = onDelete) { Text("删除") }
            }
        }
    }
}

@Composable
fun NutrientTag(name: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (name) {
            "膳食纤维" -> Color(0xFFE3F2FD)
            "碳水" -> Color(0xFFF3E5F5)
            "脂肪" -> Color(0xFFFFEBEE)
            "蛋白" -> Color(0xFFE8F5E8)
            else -> Color(0xFFF5F5F5)
        }
    ) {
        Text(
            text = "$name $value",
            fontSize = 10.sp,
            color = when (name) {
                "膳食纤维" -> Color(0xFF1976D2)
                "碳水" -> Color(0xFF7B1FA2)
                "脂肪" -> Color(0xFFD32F2F)
                "蛋白" -> Color(0xFF388E3C)
                else -> Color.Gray
            },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun WaterIntakeSection(vm: NutritionViewModel, context: android.content.Context) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "饮水记录",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            var showWaterTargetDialog by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    vm.incCup()
                    vm.saveIfNeeded(context)
                }) { Text(text = "+ 添加", color = HealthGreen) }
                TextButton(onClick = {
                    vm.decCup()
                    vm.saveIfNeeded(context)
                }) { Text(text = "- 减少", color = HealthGreen) }
                TextButton(onClick = { showWaterTargetDialog = true }) { Text(text = "设置目标", color = HealthGreen) }
                if (showWaterTargetDialog) {
                    val target by vm.waterTargetMl.collectAsState()
                    var targetText by remember { mutableStateOf(target.toString()) }
                    AlertDialog(
                        onDismissRequest = { showWaterTargetDialog = false },
                        confirmButton = {
                            Button(onClick = {
                                val t = targetText.trim().toIntOrNull()
                                if (t != null && t in 1000..5000) {
                                    vm.setWaterTarget(t)
                                    vm.saveIfNeeded(context)
                                    showWaterTargetDialog = false
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)) { Text("确定") }
                        },
                        dismissButton = { TextButton(onClick = { showWaterTargetDialog = false }) { Text("取消") } },
                        title = { Text("设置饮水目标(ml)") },
                        text = {
                            OutlinedTextField(value = targetText, onValueChange = { targetText = it }, label = { Text("每日目标") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                    )
                }
            }
        }
        
        // Water cups grid
        val totalCups = 10
        val filledCups by vm.cupsFilled.collectAsState()
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalCups) { index ->
                val isFilled = index < filledCups
                Box(modifier = Modifier.wrapContentSize()) {
                    androidx.compose.material3.Button(
                        onClick = {
                            vm.setCupsFilled(index + 1)
                            vm.saveIfNeeded(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        WaterCup(isFilled = isFilled)
                    }
                }
            }
        }
        
        val target by vm.waterTargetMl.collectAsState()
        val consumed = filledCups * vm.cupMl
        Text(
            text = "已饮用 ${consumed}ml / ${target}ml",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun EditMealDialog(meal: com.example.healthapp.model.MealRecord, onDismiss: () -> Unit, onConfirm: (String, String, Int, Int, Int, Int, Int) -> Unit) {
    var mealType by remember { mutableStateOf(meal.mealType) }
    var foods by remember { mutableStateOf(meal.foods) }
    var caloriesText by remember { mutableStateOf(meal.calories.toString()) }
    var proteinText by remember { mutableStateOf((meal.nutrients["蛋白"]?.removeSuffix("g") ?: "").toString()) }
    var carbsText by remember { mutableStateOf((meal.nutrients["碳水"]?.removeSuffix("g") ?: "").toString()) }
    var fatText by remember { mutableStateOf((meal.nutrients["脂肪"]?.removeSuffix("g") ?: "").toString()) }
    var fiberText by remember { mutableStateOf((meal.nutrients["膳食纤维"]?.removeSuffix("g") ?: "").toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val calories = caloriesText.trim().toIntOrNull()
                val protein = proteinText.trim().toIntOrNull() ?: 0
                val carbs = carbsText.trim().toIntOrNull() ?: 0
                val fat = fatText.trim().toIntOrNull() ?: 0
                val fiber = fiberText.trim().toIntOrNull() ?: 0
                if (!mealType.isBlank() && !foods.isBlank() && calories != null && calories > 0) {
                    onConfirm(mealType.trim(), foods.trim(), calories, protein, carbs, fat, fiber)
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        title = { Text("编辑饮食记录") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = mealType, onValueChange = { mealType = it }, label = { Text("餐次") })
                OutlinedTextField(value = foods, onValueChange = { foods = it }, label = { Text("食物") })
                OutlinedTextField(value = caloriesText, onValueChange = { caloriesText = it }, label = { Text("卡路里") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = proteinText, onValueChange = { proteinText = it }, label = { Text("蛋白(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = carbsText, onValueChange = { carbsText = it }, label = { Text("碳水(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fatText, onValueChange = { fatText = it }, label = { Text("脂肪(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = fiberText, onValueChange = { fiberText = it }, label = { Text("膳食纤维(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
            }
        }
    )
}

@Composable
fun WaterCup(isFilled: Boolean) {
    Card(
        modifier = Modifier.size(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFilled) HealthBlue else Color.LightGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {}
}

@Composable
fun NutritionAnalysisSection(vm: NutritionViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val totals = vm.totals()
        val proteinTarget by vm.proteinTarget.collectAsState()
        val carbsTarget by vm.carbsTarget.collectAsState()
        val fatTarget by vm.fatTarget.collectAsState()
        val fiberTarget by vm.fiberTarget.collectAsState()
        Text(text = "营养分析", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        MacroBar("蛋白质", totals.proteinG, proteinTarget, HealthGreen)
        MacroBar("碳水", totals.carbsG, carbsTarget, Color(0xFF7B1FA2))
        MacroBar("脂肪", totals.fatG, fatTarget, Color(0xFFD32F2F))
        MacroBar("膳食纤维", totals.fiberG, fiberTarget, Color(0xFF1976D2))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "卡路里总计：${totals.calories} 卡", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        var showTargets by remember { mutableStateOf(false) }
        OutlinedButton(onClick = { showTargets = true }) { Text("设置营养目标") }
        if (showTargets) {
            var proteinText by remember { mutableStateOf(proteinTarget.toString()) }
            var carbsText by remember { mutableStateOf(carbsTarget.toString()) }
            var fatText by remember { mutableStateOf(fatTarget.toString()) }
            var fiberText by remember { mutableStateOf(fiberTarget.toString()) }
            AlertDialog(
                onDismissRequest = { showTargets = false },
                confirmButton = {
                    Button(onClick = {
                        val p = proteinText.trim().toIntOrNull()
                        val c = carbsText.trim().toIntOrNull()
                        val f = fatText.trim().toIntOrNull()
                        val fi = fiberText.trim().toIntOrNull()
                        if (p != null && c != null && f != null && fi != null) {
                            vm.setProteinTarget(p)
                            vm.setCarbsTarget(c)
                            vm.setFatTarget(f)
                            vm.setFiberTarget(fi)
                            showTargets = false
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)) { Text("确定") }
                },
                dismissButton = { TextButton(onClick = { showTargets = false }) { Text("取消") } },
                title = { Text("设置营养目标(g)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = proteinText, onValueChange = { proteinText = it }, label = { Text("蛋白(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = carbsText, onValueChange = { carbsText = it }, label = { Text("碳水(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = fatText, onValueChange = { fatText = it }, label = { Text("脂肪(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = fiberText, onValueChange = { fiberText = it }, label = { Text("膳食纤维(g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
            )
        }
    }
}

@Composable
fun AISuggestionsSection(vm: NutritionViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
    ) {
        Text(text = "DeepSeek 生成建议", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        DeepSeekSuggestionSection(vm)
    }
}

@Composable
fun DeepSeekSuggestionSection(vm: NutritionViewModel) {
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val deepVm: com.example.healthapp.viewmodel.DeepSeekViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (loading) {
            CircularProgressIndicator()
        } else {
            if (!result.isNullOrBlank()) {
                SuggestionCard(result!!)
            }
            if (!errorText.isNullOrBlank()) {
                Text(text = errorText!!, color = Color.Red, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            loading = true
            deepVm.generateNutritionAdvice(
                totals = vm.totals(),
                onDone = { text ->
                    result = text
                    loading = false
                    errorText = null
                },
                onError = { e ->
                    loading = false
                    errorText = e.message ?: "生成失败"
                }
            )
        }, colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)) { Text("生成建议") }
    }
}

@Composable
fun PhotoRecognizeDialog(
    bitmap: android.graphics.Bitmap,
    initialSelected: List<FoodWithConfidence> = emptyList(),
    onConfirm: (foods: String, calories: Int, fiberG: Int, carbsG: Int, fatG: Int, proteinG: Int) -> Unit,
    onCancel: () -> Unit,
    vm: NutritionViewModel
) {
    // 使用 mutableStateListOf 以便正确触发重组
    val selectedFoods = remember { mutableStateListOf<String>() }
    
    // 初始化选中项
    LaunchedEffect(Unit) {
        selectedFoods.addAll(initialSelected.take(5).map { it.food })
    }

    var calories by remember { mutableStateOf(0) }
    var fiber by remember { mutableStateOf(0) }
    var carbs by remember { mutableStateOf(0) }
    var fat by remember { mutableStateOf(0) }
    var protein by remember { mutableStateOf(0) }
    
    // 自定义食物相关状态
    var showCustomFoodDialog by remember { mutableStateOf(false) }
    var customFoodName by remember { mutableStateOf("") }
    var customCalories by remember { mutableStateOf("") }
    var customFiber by remember { mutableStateOf("") }
    var customCarbs by remember { mutableStateOf("") }
    var customFat by remember { mutableStateOf("") }
    var customProtein by remember { mutableStateOf("") }
    
    // 合并系统食物表和自定义食物
    val systemFoods = remember {
        mapOf(
            // 主食类
            "米饭(100g)" to Nutrients(130, 0, 28, 0, 2),
            "面条(100g)" to Nutrients(138, 2, 27, 1, 4),
            "面包(1片)" to Nutrients(79, 1, 15, 1, 3),
            "燕麦(50g)" to Nutrients(180, 5, 27, 3, 7),
            "玉米(1根)" to Nutrients(86, 2, 19, 1, 3),
            "土豆(100g)" to Nutrients(77, 2, 17, 0, 2),
            "馒头(1个)" to Nutrients(221, 1, 47, 1, 7),
            "饺子(10个)" to Nutrients(350, 2, 50, 10, 15),
            "包子(1个)" to Nutrients(250, 1, 40, 8, 10),
            
            // 肉类
            "鸡胸肉(100g)" to Nutrients(165, 0, 0, 4, 31),
            "牛肉(100g)" to Nutrients(250, 0, 0, 17, 20),
            "猪肉(100g)" to Nutrients(290, 0, 0, 25, 20),
            "鱼肉(100g)" to Nutrients(124, 0, 0, 2, 25),
            "虾仁(100g)" to Nutrients(99, 0, 1, 0, 20),
            "鸡蛋(1个)" to Nutrients(78, 0, 0, 5, 6),
            
            // 蔬菜类
            "西兰花(100g)" to Nutrients(35, 3, 7, 0, 3),
            "西红柿(100g)" to Nutrients(18, 1, 4, 0, 1),
            "胡萝卜(100g)" to Nutrients(41, 3, 10, 0, 1),
            "生菜(100g)" to Nutrients(15, 2, 2, 0, 1),
            "黄瓜(100g)" to Nutrients(16, 1, 4, 0, 0),
            "芹菜(100g)" to Nutrients(16, 2, 3, 0, 1),
            "菠菜(100g)" to Nutrients(23, 2, 4, 0, 3),
            "花菜(100g)" to Nutrients(25, 3, 5, 0, 2),
            "青椒(100g)" to Nutrients(20, 2, 5, 0, 1),
            "包心菜(100g)" to Nutrients(25, 2, 6, 0, 1),
            "洋葱(100g)" to Nutrients(40, 2, 9, 0, 1),
            "蘑菇(100g)" to Nutrients(22, 1, 3, 0, 3),
            "大蒜(1瓣)" to Nutrients(4, 0, 1, 0, 0),
            "生姜(1片)" to Nutrients(5, 0, 1, 0, 0),
            "韭菜(100g)" to Nutrients(26, 2, 4, 0, 2),
            
            // 水果类
            "苹果(1个)" to Nutrients(95, 4, 25, 0, 0),
            "香蕉(1根)" to Nutrients(105, 3, 27, 0, 1),
            "橙子(1个)" to Nutrients(62, 3, 15, 0, 1),
            "草莓(100g)" to Nutrients(32, 2, 8, 0, 0),
            "葡萄(100g)" to Nutrients(69, 1, 18, 0, 0),
            "梨(1个)" to Nutrients(101, 4, 27, 0, 0),
            "猕猴桃(1个)" to Nutrients(61, 3, 14, 0, 1),
            "西瓜(100g)" to Nutrients(30, 0, 8, 0, 0),
            
            // 乳制品和豆制品
            "牛奶(250ml)" to Nutrients(150, 0, 12, 8, 8),
            "酸奶(200ml)" to Nutrients(110, 0, 17, 3, 5),
            "奶酪(30g)" to Nutrients(100, 0, 1, 8, 7),
            "豆腐(100g)" to Nutrients(70, 2, 4, 4, 8),
            "豆浆(200ml)" to Nutrients(85, 1, 3, 4, 10),
            
            // 坚果和油脂类
            "花生(30g)" to Nutrients(168, 2, 5, 14, 7),
            "核桃(3个)" to Nutrients(173, 2, 4, 16, 4),
            "橄榄油(1勺)" to Nutrients(120, 0, 0, 14, 0),
            "花生油(1勺)" to Nutrients(120, 0, 0, 14, 0),
            
            // 其他
            "酸奶(200ml)" to Nutrients(110, 0, 17, 3, 5),
            "沙拉酱(1勺)" to Nutrients(57, 0, 2, 6, 0),
            "蜂蜜(1勺)" to Nutrients(64, 0, 17, 0, 0),
            "巧克力(30g)" to Nutrients(170, 2, 23, 9, 3)
        )
    }
    
    // 支持自定义食物的动态食物表
    val initialFoodTable = remember(initialSelected) {
        systemFoods.toMutableMap().apply {
            initialSelected.forEach { item ->
                if (item.calories != null && item.calories > 0) {
                    if (!containsKey(item.food)) {
                        this[item.food] = Nutrients(item.calories, 0, 0, 0, 0)
                    }
                }
            }
        }.toMap()
    }
    
    // 专门存储用户在对话框中添加的自定义食物
    var customAddedFoods by remember { mutableStateOf(mapOf<String, Nutrients>()) }
    
    // 动态合并所有食物表：系统默认 + 百度识别结果 + 用户临时添加
    val dynamicFoodTable = remember(initialFoodTable, customAddedFoods) {
        initialFoodTable + customAddedFoods
    }

    fun recomputeTotals() {
        val totals = selectedFoods.mapNotNull { dynamicFoodTable[it] }.fold(Nutrients(0,0,0,0,0)) {
            acc, n ->
            Nutrients(acc.calories + n.calories, acc.fiberG + n.fiberG, acc.carbsG + n.carbsG, acc.fatG + n.fatG, acc.proteinG + n.proteinG)
        }
        calories = totals.calories
        fiber = totals.fiberG
        carbs = totals.carbsG
        fat = totals.fatG
        protein = totals.proteinG
    }
    
    // 当动态食物表更新时（例如百度识别结果返回），自动重新计算
    LaunchedEffect(dynamicFoodTable) {
        recomputeTotals()
    }
    
    // 添加自定义食物
    fun addCustomFood(name: String, customNutrients: Nutrients) {
        customAddedFoods = customAddedFoods + (name to customNutrients)
        selectedFoods.add(name)
        recomputeTotals()
        showCustomFoodDialog = false
    }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = { TextButton(onClick = { onConfirm(selectedFoods.joinToString(" + "), calories, fiber, carbs, fat, protein) }) { Text("保存记录") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("取消") } },
        title = { Text("拍照识别") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = "meal", modifier = Modifier.fillMaxWidth().height(160.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "请选择识别出的食物：", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                val foodHistory by vm.foodHistory.collectAsState()
                
                // 识别结果处理
                val recognizedFoods = initialSelected.map { it.food }
                
                // 突出显示识别出的食物
                if (recognizedFoods.isNotEmpty()) {
                    Text(text = "识别结果：", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HealthDarkGreen, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SimpleFlowRow(modifier = Modifier.fillMaxWidth()) {
                        recognizedFoods.forEach { name ->
                            val selected = name in selectedFoods
                            val hasCalorie = dynamicFoodTable[name]?.calories?.let { it > 0 } == true
                            val source = initialSelected.find { it.food == name }?.source ?: ""
                            val sourceTag = if (source.isNotEmpty()) "[$source] " else ""
                            val displayText = if (hasCalorie) "$sourceTag$name (${dynamicFoodTable[name]?.calories}大卡)" else "$sourceTag$name"
                            
                            FilterChip(
                                selected = selected, 
                                onClick = {
                                    if (selected) selectedFoods.remove(name) else selectedFoods.add(name)
                                    recomputeTotals()
                                }, 
                                label = { Text(displayText) }, 
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HealthGreen,
                                    containerColor = HealthGreen.copy(alpha = 0.1f),
                                    labelColor = if (selected) Color.White else HealthDarkGreen
                                )
                            )
                        }
                    }
                }
                
                // 显示其他可用食物
                Text(text = "其他食物：", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                
                val allAvailableFoods = dynamicFoodTable.keys.toList().sorted()
                // 分页显示其他食物，避免界面过长
                val pageSize = 12
                var currentPage by remember { mutableStateOf(0) }
                val otherFoods = allAvailableFoods.filter { it !in recognizedFoods }
                val paginatedFoods = otherFoods.chunked(pageSize)
                
                if (paginatedFoods.isNotEmpty()) {
                    val currentFoods = paginatedFoods[currentPage]
                    SimpleFlowRow(modifier = Modifier.fillMaxWidth()) {
                        currentFoods.forEach { name ->
                            val selected = name in selectedFoods
                            FilterChip(
                                selected = selected, 
                                onClick = {
                                    if (selected) selectedFoods.remove(name) else selectedFoods.add(name)
                                    recomputeTotals()
                                }, 
                                label = { Text(name) }
                            )
                        }
                    }
                    
                    // 分页控件
                    if (paginatedFoods.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { if (currentPage > 0) currentPage-- },
                                enabled = currentPage > 0
                            ) {
                                Text(text = "上一页")
                            }
                            Text(text = "${currentPage + 1}/${paginatedFoods.size}", fontSize = 12.sp)
                            TextButton(
                                onClick = { if (currentPage < paginatedFoods.size - 1) currentPage++ },
                                enabled = currentPage < paginatedFoods.size - 1
                            ) {
                                Text(text = "下一页")
                            }
                        }
                    }
                }
                
                // 历史记录
                if (foodHistory.isNotEmpty()) {
                    Text(text = "历史记录：", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    SimpleFlowRow(modifier = Modifier.fillMaxWidth()) {
                        foodHistory.take(6).forEach { name ->
                            val selected = name in selectedFoods
                            FilterChip(
                                selected = selected, 
                                onClick = {
                                    if (selected) selectedFoods.remove(name) else selectedFoods.add(name)
                                    recomputeTotals()
                                }, 
                                label = { Text(name) }, 
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HealthBlue.copy(alpha = 0.8f),
                                    containerColor = HealthBlue.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 添加自定义食物按钮
                Button(
                    onClick = { showCustomFoodDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "+")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "添加自定义食物")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                    Column(Modifier.padding(8.dp)) {
                        Text(text = "估算热量：${calories} kcal", fontSize = 12.sp)
                        Text(text = "膳食纤维：${fiber} g", fontSize = 12.sp)
                        Text(text = "碳水：${carbs} g", fontSize = 12.sp)
                        Text(text = "脂肪：${fat} g", fontSize = 12.sp)
                        Text(text = "蛋白：${protein} g", fontSize = 12.sp)
                    }
                }
            }
        }
    )
    
    // 自定义食物对话框
    if (showCustomFoodDialog) {
        AlertDialog(
            onDismissRequest = { showCustomFoodDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        runCatching {
                            val customCal = customCalories.toInt()
                            val customFib = customFiber.toInt()
                            val customCarb = customCarbs.toInt()
                            val customFatRes = customFat.toInt()
                            val customPro = customProtein.toInt()
                            val customNutrients = Nutrients(customCal, customFib, customCarb, customFatRes, customPro)
                            addCustomFood(customFoodName, customNutrients)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreen)
                ) {
                    Text("添加")
                }
            },
            dismissButton = { TextButton(onClick = { showCustomFoodDialog = false }) { Text("取消") } },
            title = { Text("添加自定义食物") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customFoodName,
                        onValueChange = { customFoodName = it },
                        label = { Text("食物名称") },
                        placeholder = { Text("例如：红烧肉(100g)") }
                    )
                    OutlinedTextField(
                        value = customCalories,
                        onValueChange = { customCalories = it },
                        label = { Text("卡路里") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customProtein,
                            onValueChange = { customProtein = it },
                            label = { Text("蛋白(g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customCarbs,
                            onValueChange = { customCarbs = it },
                            label = { Text("碳水(g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customFat,
                            onValueChange = { customFat = it },
                            label = { Text("脂肪(g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customFiber,
                            onValueChange = { customFiber = it },
                            label = { Text("膳食纤维(g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        )
    }
}

private data class Nutrients(val calories: Int, val fiberG: Int, val carbsG: Int, val fatG: Int, val proteinG: Int)

private fun currentMealType(): String {
    val now = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        now < 10 -> "早餐"
        now < 16 -> "午餐"
        else -> "晚餐"
    }
}

private fun suggestFoodsFromText(text: String): List<String> {
    val candidates = mutableListOf<String>()
    val map = mapOf(
        // 主食类
        "米饭" to "米饭(100g)",
        "白饭" to "米饭(100g)",
        "米饭(100g)" to "米饭(100g)",
        "面条" to "面条(100g)",
        "面条(100g)" to "面条(100g)",
        "面包" to "面包(1片)",
        "面包(1片)" to "面包(1片)",
        "燕麦" to "燕麦(50g)",
        "燕麦(50g)" to "燕麦(50g)",
        "玉米" to "玉米(1根)",
        "玉米(1根)" to "玉米(1根)",
        "土豆" to "土豆(100g)",
        "土豆(100g)" to "土豆(100g)",
        "馒头" to "馒头(1个)",
        "馒头(1个)" to "馒头(1个)",
        "饺子" to "饺子(10个)",
        "饺子(10个)" to "饺子(10个)",
        "包子" to "包子(1个)",
        "包子(1个)" to "包子(1个)",
        
        // 肉类
        "鸡胸" to "鸡胸肉(100g)",
        "鸡肉" to "鸡胸肉(100g)",
        "鸡胸肉" to "鸡胸肉(100g)",
        "鸡胸肉(100g)" to "鸡胸肉(100g)",
        "牛肉" to "牛肉(100g)",
        "牛肉(100g)" to "牛肉(100g)",
        "牛排" to "牛肉(100g)",
        "猪肉" to "猪肉(100g)",
        "猪肉(100g)" to "猪肉(100g)",
        "鱼" to "鱼肉(100g)",
        "鱼肉" to "鱼肉(100g)",
        "鱼肉(100g)" to "鱼肉(100g)",
        "三文鱼" to "鱼肉(100g)",
        "虾仁" to "虾仁(100g)",
        "虾仁(100g)" to "虾仁(100g)",
        "虾" to "虾仁(100g)",
        "鸡蛋" to "鸡蛋(1个)",
        "鸡蛋(1个)" to "鸡蛋(1个)",
        "蛋" to "鸡蛋(1个)",
        
        // 蔬菜类
        "西兰花" to "西兰花(100g)",
        "西兰花(100g)" to "西兰花(100g)",
        "番茄" to "西红柿(100g)",
        "西红柿" to "西红柿(100g)",
        "西红柿(100g)" to "西红柿(100g)",
        "胡萝卜" to "胡萝卜(100g)",
        "胡萝卜(100g)" to "胡萝卜(100g)",
        "生菜" to "生菜(100g)",
        "生菜(100g)" to "生菜(100g)",
        "黄瓜" to "黄瓜(100g)",
        "黄瓜(100g)" to "黄瓜(100g)",
        "芹菜" to "芹菜(100g)",
        "芹菜(100g)" to "芹菜(100g)",
        "菠菜" to "菠菜(100g)",
        "菠菜(100g)" to "菠菜(100g)",
        "花菜" to "花菜(100g)",
        "花菜(100g)" to "花菜(100g)",
        "青椒" to "青椒(100g)",
        "青椒(100g)" to "青椒(100g)",
        
        // 水果类
        "香蕉" to "香蕉(1根)",
        "香蕉(1根)" to "香蕉(1根)",
        "苹果" to "苹果(1个)",
        "苹果(1个)" to "苹果(1个)",
        "橙子" to "橙子(1个)",
        "橙子(1个)" to "橙子(1个)",
        "草莓" to "草莓(100g)",
        "草莓(100g)" to "草莓(100g)",
        "葡萄" to "葡萄(100g)",
        "葡萄(100g)" to "葡萄(100g)",
        "梨" to "梨(1个)",
        "梨(1个)" to "梨(1个)",
        "猕猴桃" to "猕猴桃(1个)",
        "猕猴桃(1个)" to "猕猴桃(1个)",
        "西瓜" to "西瓜(100g)",
        "西瓜(100g)" to "西瓜(100g)",
        
        // 乳制品和豆制品
        "牛奶" to "牛奶(250ml)",
        "牛奶(250ml)" to "牛奶(250ml)",
        "酸奶" to "酸奶(200ml)",
        "酸奶(200ml)" to "酸奶(200ml)",
        "奶酪" to "奶酪(30g)",
        "奶酪(30g)" to "奶酪(30g)",
        "豆腐" to "豆腐(100g)",
        "豆腐(100g)" to "豆腐(100g)",
        "豆浆" to "豆浆(200ml)",
        "豆浆(200ml)" to "豆浆(200ml)",
        
        // 坚果和油脂类
        "花生" to "花生(30g)",
        "花生(30g)" to "花生(30g)",
        "核桃" to "核桃(3个)",
        "核桃(3个)" to "核桃(3个)",
        "橄榄油" to "橄榄油(1勺)",
        "橄榄油(1勺)" to "橄榄油(1勺)",
        "花生油" to "花生油(1勺)",
        "花生油(1勺)" to "花生油(1勺)",
        
        // 其他
        "沙拉酱" to "沙拉酱(1勺)",
        "沙拉酱(1勺)" to "沙拉酱(1勺)",
        "蜂蜜" to "蜂蜜(1勺)",
        "蜂蜜(1勺)" to "蜂蜜(1勺)",
        "巧克力" to "巧克力(30g)",
        "巧克力(30g)" to "巧克力(30g)"
    )
    map.forEach { (k, v) -> if (text.contains(k)) candidates.add(v) }
    return candidates.distinct()
}

private fun mapLabelToFood(label: String): String? {
    val lowerLabel = label.lowercase()
    return when {
        // 主食类
        lowerLabel.contains("rice") -> "米饭(100g)"
        lowerLabel.contains("noodle") || lowerLabel.contains("pasta") -> "面条(100g)"
        lowerLabel.contains("bread") -> "面包(1片)"
        lowerLabel.contains("oat") -> "燕麦(50g)"
        lowerLabel.contains("corn") -> "玉米(1根)"
        lowerLabel.contains("potato") -> "土豆(100g)"
        
        // 肉类
        lowerLabel.contains("chicken") -> "鸡胸肉(100g)"
        lowerLabel.contains("beef") || lowerLabel.contains("steak") -> "牛肉(100g)"
        lowerLabel.contains("pork") -> "猪肉(100g)"
        lowerLabel.contains("fish") || lowerLabel.contains("salmon") -> "鱼肉(100g)"
        lowerLabel.contains("shrimp") || lowerLabel.contains("prawn") -> "虾仁(100g)"
        lowerLabel.contains("egg") -> "鸡蛋(1个)"
        
        // 蔬菜类
        lowerLabel.contains("broccoli") -> "西兰花(100g)"
        lowerLabel.contains("tomato") -> "西红柿(100g)"
        lowerLabel.contains("lettuce") -> "生菜(100g)"
        lowerLabel.contains("cucumber") -> "黄瓜(100g)"
        lowerLabel.contains("carrot") -> "胡萝卜(100g)"
        lowerLabel.contains("spinach") -> "菠菜(100g)"
        lowerLabel.contains("cauliflower") -> "花菜(100g)"
        lowerLabel.contains("bell pepper") -> "青椒(100g)"
        lowerLabel.contains("cabbage") -> "包心菜(100g)"
        lowerLabel.contains("onion") -> "洋葱(100g)"
        lowerLabel.contains("garlic") -> "大蒜(1瓣)"
        lowerLabel.contains("ginger") -> "生姜(1片)"
        lowerLabel.contains("mushroom") -> "蘑菇(100g)"
        lowerLabel.contains("chive") || lowerLabel.contains("leek") -> "韭菜(100g)"
        
        // 水果类
        lowerLabel.contains("banana") -> "香蕉(1根)"
        lowerLabel.contains("apple") -> "苹果(1个)"
        lowerLabel.contains("orange") -> "橙子(1个)"
        lowerLabel.contains("strawberry") -> "草莓(100g)"
        lowerLabel.contains("grape") -> "葡萄(100g)"
        lowerLabel.contains("pear") -> "梨(1个)"
        lowerLabel.contains("kiwi") -> "猕猴桃(1个)"
        lowerLabel.contains("watermelon") -> "西瓜(100g)"
        lowerLabel.contains("melon") -> "西瓜(100g)"
        lowerLabel.contains("pineapple") -> "香蕉(1根)"
        lowerLabel.contains("mango") -> "香蕉(1根)"
        lowerLabel.contains("lemon") -> "橙子(1个)"
        
        // 乳制品和豆制品
        lowerLabel.contains("milk") -> "牛奶(250ml)"
        lowerLabel.contains("yogurt") -> "酸奶(200ml)"
        lowerLabel.contains("cheese") -> "奶酪(30g)"
        lowerLabel.contains("tofu") -> "豆腐(100g)"
        
        // 坚果和油脂类
        lowerLabel.contains("peanut") -> "花生(30g)"
        lowerLabel.contains("walnut") -> "核桃(3个)"
        lowerLabel.contains("almond") -> "花生(30g)"
        lowerLabel.contains("cashew") -> "花生(30g)"
        lowerLabel.contains("nut") -> "花生(30g)"
        
        // 其他
        lowerLabel.contains("honey") -> "蜂蜜(1勺)"
        lowerLabel.contains("chocolate") -> "巧克力(30g)"
        lowerLabel.contains("cake") -> "面包(1片)"
        lowerLabel.contains("cookie") -> "面包(1片)"
        
        else -> null
    }
}

private fun suggestFoodsFromLabels(labels: List<String>): List<String> {
    val candidates = mutableListOf<String>()
    labels.forEach { l ->
        val food = mapLabelToFood(l)
        if (food != null) {
            candidates.add(food)
        }
    }
    return candidates.distinct()
}

@Composable
fun MacroBar(name: String, value: Int, target: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = name, fontSize = 14.sp)
            Text(text = "$value / $target g", fontSize = 12.sp, color = Color.Gray)
        }
        LinearProgressIndicator(
            progress = { (value.toFloat() / target).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = Color.LightGray
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SuggestionCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(text = text, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
    }
}
