package com.example.healthapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.JavascriptInterface
import android.util.Log
import android.webkit.SslErrorHandler
import android.net.http.SslError
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import com.example.healthapp.ui.theme.HealthBlue
import com.example.healthapp.ui.theme.HealthPurple
import androidx.navigation.NavHostController

@Composable
fun AIHealthAssistantScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HealthPurple, HealthBlue)
                )
            )
    ) {
        // Header with Back to Home
        AIHeaderSection(navController)
        
        // 聊天界面（使用服务端API）
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val vm: com.example.healthapp.viewmodel.AIChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val messages = vm.messages.collectAsState().value
            val loading = vm.loading.collectAsState().value
            val error = vm.error.collectAsState().value
            val listState = androidx.compose.foundation.lazy.rememberLazyListState()

            LaunchedEffect(Unit) { vm.restore(context) }
            
            // 自动滚动到底部
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages.size) { idx ->
                            val m = messages[idx]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (m.isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (m.isUser) HealthBlue.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
                                    )
                                ) {
                                    Text(
                                        text = m.message,
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (loading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error?.let { err ->
                        Text(text = err, color = Color.Red, fontSize = 12.sp)
                    }

                    var input by remember { mutableStateOf("") }
                    val isListening = remember { mutableStateOf(false) }
                    var recordSeconds by remember { mutableStateOf(0) }
                    var speechLang by remember { mutableStateOf("zh-CN") }
                    var canceled by remember { mutableStateOf(false) }
                    var rms by remember { mutableStateOf(0f) }
                    val thresholdPx = with(LocalDensity.current) { 40.dp.toPx() }
                    var voiceMode by remember { mutableStateOf(true) }
                    LaunchedEffect(isListening.value) {
                        if (isListening.value) {
                            recordSeconds = 0
                            while (isListening.value) {
                                kotlinx.coroutines.delay(1000)
                                recordSeconds += 1
                            }
                        }
                    }
                    val speechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
                    val speechRecognizer = remember(speechAvailable) { if (speechAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null }
                    val recorder = remember { com.example.healthapp.data.audio.AudioRecorder(16000) }
                    DisposableEffect(speechRecognizer) { onDispose { speechRecognizer?.destroy() } }
                    var autoSendOnResult by remember { mutableStateOf(false) }
                    LaunchedEffect(speechRecognizer) {
                        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {}
                            override fun onBeginningOfSpeech() {}
                            override fun onRmsChanged(rmsdB: Float) { rms = rmsdB }
                            override fun onBufferReceived(buffer: ByteArray?) {}
                            override fun onEndOfSpeech() {}
                            override fun onError(error: Int) {
                                isListening.value = false
                                Toast.makeText(context, "语音识别错误($error)", Toast.LENGTH_SHORT).show()
                            }
                            override fun onResults(results: Bundle?) {
                                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = list?.firstOrNull()?.trim().orEmpty()
                                if (text.isNotBlank()) { input = text }
                                if (autoSendOnResult) {
                                    val q = input.trim()
                                    if (q.isNotEmpty()) { vm.send(q, context); input = "" }
                                    autoSendOnResult = false
                                }
                                isListening.value = false
                            }
                            override fun onPartialResults(partialResults: Bundle?) {
                                val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = list?.firstOrNull()?.trim().orEmpty()
                                if (text.isNotBlank()) { input = text }
                            }
                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }
                    fun startListening() {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLang)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, speechLang)
                            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                        }
                        speechRecognizer?.startListening(intent)
                        isListening.value = true
                        recordSeconds = 0
                        autoSendOnResult = true
                    }
                    fun stopListening() {
                        speechRecognizer?.stopListening()
                        isListening.value = false
                    }
                    val voiceLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { r ->
                        if (r.resultCode == Activity.RESULT_OK) {
                            val data = r.data
                            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                            val text = results?.firstOrNull()?.trim().orEmpty()
                            if (text.isNotBlank()) {
                                input = text
                                vm.send(text, context)
                                input = ""
                            }
                        }
                    }
                    val permLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        if (!granted) {
                            Toast.makeText(context, "需要麦克风权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { voiceMode = !voiceMode }) {
                            Icon(imageVector = if (voiceMode) Icons.Filled.Keyboard else Icons.Filled.Mic, contentDescription = null, tint = HealthBlue)
                        }
                        if (voiceMode) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(if (isListening.value) HealthBlue.copy(alpha = 0.2f) else Color(0xFFF5F5F5))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                canceled = false
                                                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                                if (!granted) {
                                                    permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    return@detectTapGestures
                                                }
                                                // Prefer Baidu ASR recording
                                                isListening.value = true
                                                recorder.start(scope)
                                                awaitPointerEventScope {
                                                    val threshold = thresholdPx
                                                    var startY: Float? = null
                                                    while (true) {
                                                        val event = awaitPointerEvent()
                                                        val change = event.changes.firstOrNull()
                                                        if (change == null) break
                                                        if (startY == null) startY = change.position.y
                                                        val start = startY ?: change.position.y
                                                        startY = start
                                                        val deltaUp = start - change.position.y
                                                        if (deltaUp > threshold) { canceled = true; break }
                                                        if (event.changes.all { it.changedToUp() }) break
                                                    }
                                                }
                                                isListening.value = false
                                                val pcm = recorder.stopAndGetPcm()
                                                if (!canceled && pcm.isNotEmpty()) {
                                                    vm.viewModelScope.launch {
                                                        val res = com.example.healthapp.data.remote.BaiduAsrApi.recognizePcm16kDetailed(context, pcm, speechLang)
                                                        if (!res.text.isNullOrBlank()) {
                                                            input = res.text
                                                            val q = input.trim()
                                                            if (q.isNotEmpty()) { vm.send(q, context); input = "" }
                                                        } else {
                                                            val code = res.errNo ?: -1
                                                            val msg = res.errMsg ?: "unknown"
                                                            Toast.makeText(context, "语音识别失败($code:$msg)", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                } else if (canceled) {
                                                    Toast.makeText(context, "已取消录音", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = if (isListening.value) "录音中… 松开结束" else "按住说话", color = Color.Black, fontSize = 14.sp)
                            }
                        } else {
                            OutlinedTextField(
                                value = input,
                                onValueChange = { input = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("输入消息") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    val q = input.trim()
                                    if (q.isNotEmpty()) {
                                        vm.send(q, context)
                                        input = ""
                                    }
                                })
                            )
                        }
                        if (!voiceMode) {
                            IconButton(onClick = { Toast.makeText(context, "更多功能（待开发）", Toast.LENGTH_SHORT).show() }) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = HealthBlue)
                            }
                            Button(
                                onClick = {
                                    val q = input.trim()
                                    if (q.isNotEmpty()) {
                                        vm.send(q, context)
                                        input = ""
                                    }
                                },
                                enabled = input.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = HealthBlue, contentColor = Color.White)
                            ) { Text("发送") }
                        }
                    }
                    if (isListening.value) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "录音中 ${String.format("%02d:%02d", recordSeconds / 60, recordSeconds % 60)}", color = Color.Gray, fontSize = 12.sp)
                            Text(text = "松开结束 / 上滑取消", color = Color.Gray, fontSize = 12.sp)
                        }
                        LinearProgressIndicator(
                            progress = { (rms.coerceAtLeast(0f) / 10f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = HealthBlue,
                            trackColor = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

// 生成Dify聊天机器人HTML
// 已移除内嵌WebView实现

@Composable
fun AIHeaderSection(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AI Avatar
        Card(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
        
        Column {
            Text(
                text = "AI 健康助手",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "24小时智能问答",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = {
            val popped = navController.popBackStack("home", false)
            if (!popped) {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }) {
            Text("返回首页")
        }
    }
}

 
