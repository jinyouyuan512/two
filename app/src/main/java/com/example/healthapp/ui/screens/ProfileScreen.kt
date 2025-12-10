package com.example.healthapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Help
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import android.app.Activity
import com.example.healthapp.viewmodel.ProfileViewModel
import com.example.healthapp.viewmodel.AuthViewModel
import com.example.healthapp.ui.theme.HealthBlue
import com.example.healthapp.ui.theme.HealthPurple

@Composable
fun ProfileScreen(navController: NavHostController) {
    val vm: ProfileViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    LaunchedEffect(Unit) { vm.load() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HealthPurple, HealthBlue)
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(vm)
        
        // Quick actions
        QuickActionsSection()
        
        // Settings sections
        SettingsSections()
        
        // Privacy notice
        PrivacyNoticeSection()
        
        // Logout button
        LogoutButton { authViewModel.logout(navController.context) { navController.navigate("auth/login") } }
        
        // Version info
        VersionInfo()
    }
}

@Composable
fun HeaderSection(vm: ProfileViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = vm.displayName?.takeIf { it.isNotBlank() } ?: "用户",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "ID: ${vm.userId ?: "-"}",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton("周成就图", Modifier.weight(1f))
            ActionButton("健康收入", Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStatItem((vm.consecutiveDays ?: 0).toString(), "连续打卡")
            ProfileStatItem((vm.exerciseCount ?: 0).toString(), "运动次数")
            ProfileStatItem((vm.healthPoints ?: 0).toString(), "健康积分")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun ActionButton(text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = { },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(text = text, fontSize = 12.sp)
    }
}

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun QuickActionsSection() {
    val actions = listOf(
        Triple("目标管理", Icons.Default.Flag, Color(0xFF4CAF50)),
        Triple("成就徽章", Icons.Default.EmojiEvents, Color(0xFFFF9800)),
        Triple("数据备份", Icons.Default.Backup, Color(0xFF2196F3)),
        Triple("好友邀请", Icons.Default.PersonAdd, Color(0xFF9C27B0))
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            actions.forEach { (title, icon, color) ->
                QuickActionItem(title, icon, color)
            }
        }
    }
}

@Composable
fun QuickActionItem(title: String, icon: ImageVector, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Card(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = title,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SettingsSections() {
    val vm: ProfileViewModel = viewModel()
    var showEditProfileDialog by remember { mutableStateOf(false) }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("修改个人信息") },
            text = {
                Column {
                    Text("昵称")
                    OutlinedTextField(
                        value = vm.displayNameDraft,
                        onValueChange = { vm.updateDisplayNameDraft(it) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.updateDisplayName()
                        showEditProfileDialog = false
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SettingsSection("账户设置", listOf(
            "个人信息" to Icons.Default.Person,
            "通知设置" to Icons.Default.Notifications,
            "密码修改" to Icons.Default.Lock
        )) { item ->
            if (item == "个人信息") {
                vm.updateDisplayNameDraft(vm.displayName ?: "")
                showEditProfileDialog = true
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SettingsSection("隐私与安全", listOf(
            "隐私设置" to Icons.Default.Security,
            "数据管理" to Icons.Default.Storage
        ))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SettingsSection("其他", listOf(
            "帮助中心" to Icons.AutoMirrored.Filled.Help,
            "关于应用 v1.0.0" to Icons.Default.Info
        ))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Preference settings with toggles
        PreferenceSection()
    }
}

@Composable
fun SettingsSection(title: String, items: List<Pair<String, ImageVector>>, onItemClick: (String) -> Unit = {}) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                items.forEachIndexed { index, (text, icon) ->
                    SettingsItem(text, icon, onClick = { onItemClick(text) })
                    if (index < items.size - 1) {
                        androidx.compose.material3.HorizontalDivider(color = Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItem(text: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontSize = 14.sp)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "More",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun PreferenceSection() {
    Column {
        Text(
            text = "偏好设置",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                ToggleItem("推送通知", "接收每日小贴士和建议", true, prefKey = "pref_notifications")
                androidx.compose.material3.HorizontalDivider(color = Color(0xFFF5F5F5))
                ToggleItem("自动同步", "自动同步数据", true, prefKey = "pref_auto_sync")
                androidx.compose.material3.HorizontalDivider(color = Color(0xFFF5F5F5))
                ToggleItem("数据分享", "允许匿名数据研究", false, prefKey = "pref_data_share")
                androidx.compose.material3.HorizontalDivider(color = Color(0xFFF5F5F5))
                ReminderTimeSelector()
                androidx.compose.material3.HorizontalDivider(color = Color(0xFFF5F5F5))
                TestNotificationButton()
            }
        }
    }
}

@Composable
fun ToggleItem(title: String, description: String, defaultValue: Boolean, prefKey: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("prefs_profile", android.content.Context.MODE_PRIVATE) }
    var isChecked by remember { mutableStateOf(prefs.getBoolean(prefKey, defaultValue)) }
    val needPermission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
    val hasPermission = if (needPermission) androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED else true
    
    // 检查是否需要精确闹钟权限
    val needExactAlarmPermission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
    
    val notifPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                com.example.healthapp.notifications.ReminderScheduler.scheduleDaily(context)
            } else {
                isChecked = false
                prefs.edit().putBoolean(prefKey, false).apply()
            }
        }
    )
    
    // 显示精确闹钟权限设置对话框
    val showExactAlarmDialog = remember { mutableStateOf(false) }
    if (showExactAlarmDialog.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExactAlarmDialog.value = false },
            title = { androidx.compose.material3.Text("需要精确闹钟权限") },
            text = { androidx.compose.material3.Text("为了确保您能在准确时间收到每日小贴士，请开启精确闹钟权限") },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    showExactAlarmDialog.value = false
                    // 打开应用设置页面，让用户手动开启精确闹钟权限
                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                }) {
                    androidx.compose.material3.Text("去设置")
                }
            },
            dismissButton = {
                androidx.compose.material3.Button(onClick = { showExactAlarmDialog.value = false }) {
                    androidx.compose.material3.Text("取消")
                }
            }
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = title, fontSize = 14.sp)
            Text(
                text = description,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                prefs.edit().putBoolean(prefKey, it).apply()
                if (prefKey == "pref_notifications") {
                    if (it) {
                        if (needPermission && !hasPermission) {
                            notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // 检查精确闹钟权限
                            if (needExactAlarmPermission && !com.example.healthapp.notifications.ReminderScheduler.hasExactAlarmPermission(context)) {
                                showExactAlarmDialog.value = true
                            } else {
                                val time = prefs.getString("pref_notif_time", "09:00") ?: "09:00"
                                val parts = time.split(":")
                                val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                com.example.healthapp.notifications.ReminderScheduler.scheduleDaily(context, h, m)
                                com.example.healthapp.notifications.ReminderScheduler.scheduleWorkNext(context)
                            }
                        }
                    } else {
                        com.example.healthapp.notifications.ReminderScheduler.cancel(context)
                    }
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = HealthPurple,
                checkedTrackColor = HealthPurple.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun ReminderTimeSelector() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("prefs_profile", android.content.Context.MODE_PRIVATE) }
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("07:00", "09:00", "12:00", "20:00")
    var selected by remember { mutableStateOf(prefs.getString("pref_notif_time", options[1]) ?: options[1]) }
    val notificationsEnabled = prefs.getBoolean("pref_notifications", true)
    
    // 检查是否需要精确闹钟权限
    val needExactAlarmPermission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
    
    // 显示精确闹钟权限设置对话框
    val showExactAlarmDialog = remember { mutableStateOf(false) }
    if (showExactAlarmDialog.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExactAlarmDialog.value = false },
            title = { androidx.compose.material3.Text("需要精确闹钟权限") },
            text = { androidx.compose.material3.Text("为了确保您能在准确时间收到每日小贴士，请开启精确闹钟权限") },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    showExactAlarmDialog.value = false
                    // 打开应用设置页面，让用户手动开启精确闹钟权限
                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                }) {
                    androidx.compose.material3.Text("去设置")
                }
            },
            dismissButton = {
                androidx.compose.material3.Button(onClick = { showExactAlarmDialog.value = false }) {
                    androidx.compose.material3.Text("取消")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = "提醒时间", fontSize = 14.sp)
            Text(text = "每日小贴士的发送时间", fontSize = 10.sp, color = Color.Gray)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                OutlinedButton(onClick = { expanded = true }) { Text(selected) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { opt ->
                        DropdownMenuItem(text = { Text(opt) }, onClick = {
                            selected = opt
                            prefs.edit().putString("pref_notif_time", opt).apply()
                            if (notificationsEnabled) {
                                // 检查精确闹钟权限
                                if (needExactAlarmPermission && !com.example.healthapp.notifications.ReminderScheduler.hasExactAlarmPermission(context)) {
                                    showExactAlarmDialog.value = true
                                } else {
                                    val parts = opt.split(":")
                                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    com.example.healthapp.notifications.ReminderScheduler.scheduleDaily(context, h, m)
                                    com.example.healthapp.notifications.ReminderScheduler.scheduleWorkNext(context)
                                }
                            }
                            expanded = false
                        })
                    }
                }
            }
            OutlinedButton(onClick = {
                val activityContext = context as? Activity ?: return@OutlinedButton
                val cal = java.util.Calendar.getInstance()
                val dlg = android.app.TimePickerDialog(
                    activityContext,
                    { _, h, m ->
                        val t = String.format("%02d:%02d", h, m)
                        selected = t
                        prefs.edit().putString("pref_notif_time", t).apply()
                        if (notificationsEnabled) {
                            // 检查精确闹钟权限
                            if (needExactAlarmPermission && !com.example.healthapp.notifications.ReminderScheduler.hasExactAlarmPermission(activityContext)) {
                                showExactAlarmDialog.value = true
                            } else {
                                com.example.healthapp.notifications.ReminderScheduler.scheduleDaily(activityContext, h, m)
                                com.example.healthapp.notifications.ReminderScheduler.scheduleWorkNext(activityContext)
                            }
                        }
                    },
                    cal.get(java.util.Calendar.HOUR_OF_DAY),
                    cal.get(java.util.Calendar.MINUTE),
                    true
                )
                dlg.show()
            }) { Text("自定义") }
        }
    }
}

@Composable
fun TestNotificationButton() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = "发送测试通知", fontSize = 14.sp)
            Text(text = "立即发送一条通知以验证功能", fontSize = 10.sp, color = Color.Gray)
        }
        Button(onClick = {
            com.example.healthapp.notifications.NotificationHelper.ensureChannel(context)
            val builder = androidx.core.app.NotificationCompat.Builder(context, com.example.healthapp.notifications.NotificationHelper.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("测试通知")
                .setContentText("这是一个测试通知")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            
            try  {
                // 检查通知权限
                val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(2002, builder.build())
                }
            } catch (e: SecurityException) {
                // 处理权限不足的情况
                e.printStackTrace()
            }
        }) { Text("发送") }
    }
}



@Composable
fun PrivacyNoticeSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "您的隐私很重要",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Text(
                text = "多项权限和数据保护措施确保安全，您不会未授权同意情况下分享至第三方。",
                fontSize = 10.sp,
                color = Color(0xFF1976D2),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "退出登录",
            color = Color(0xFFD32F2F),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VersionInfo() {
    Text(
        text = "悦康 AI 健康应用 v1.0.0",
        fontSize = 12.sp,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}
