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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("请输入问题…") },
                            singleLine = true
                        )
                        TextButton(onClick = { vm.clear(context) }) {
                            Text("清除")
                        }
                        Button(onClick = {
                            val q = input.trim()
                            if (q.isNotEmpty()) {
                                vm.send(q, context)
                                input = ""
                            }
                        }, enabled = input.isNotBlank()) {
                            Text("发送")
                        }
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

 
