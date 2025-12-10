## 目标
- 将“AI助手”入口从底部导航改为全局悬浮按钮（FAB），统一出现在主要页面右下角，点击进入 AIHealthAssistantScreen。
- 底部导航去除“AI助手”项，保留：首页｜数据｜健康｜我的。

## 交互与视觉
- FAB样式：圆形 56dp，紫蓝渐变背景，聊天/机器人图标（AutoMirrored），阴影 8dp。
- 位置与安全区：右下角，距离边缘 16dp；考虑手势导航安全区（使用 Scaffold 的 `floatingActionButton` + `floatingActionButtonPosition = End`）。
- 显隐逻辑：在 AIHealthAssistantScreen 隐藏 FAB（避免重复入口）；在其它页面显示。
- 动效：`AnimatedVisibility` 显隐；点击提供涟漪与轻微缩放反馈；支持 haptic。

## 技术实现
1) 新增组件 `FloatingAiButton.kt`
- 提供 `@Composable FloatingAiButton(onClick: () -> Unit, visible: Boolean)`
- 内部使用 `FloatingActionButton`（Material3）或自定义 `Surface` + Icon + Brush 渐变
- 颜色来源：`HealthPurple` 与 `HealthBlue` 渐变；图标 `Icons.AutoMirrored.Filled.Send` 或机器人图标

2) 修改底部导航容器 `BottomNavBar.kt`
- 使用 `Scaffold(floatingActionButton = { FloatingAiButton(...) }, floatingActionButtonPosition = FabPosition.End)`
- 通过 `navBackStackEntry` 获取当前 route：当 `route != "ai"` 时显示 FAB
- FAB 点击：`navController.navigate("ai") { launchSingleTop = true; restoreState = true; popUpTo(startDestination) { saveState = true } }`

3) 更新导航项 `BottomNavItem.kt`
- 移除 `AIAssistant` 导航项，仅保留 `Home`, `Data`, `Health`, `Profile`
- 确保 `NavHost` 仍保留 `composable("ai") { AIHealthAssistantScreen() }` 作为目标路由

4) 适配页面
- 不需要改动各页面内容；FAB 由 `BottomNavBar` 统一提供
- 在 AI 健康助手页面：隐藏 FAB（visible = false）

## 验证
- IDE 同步与构建
- 在首页/数据/健康/我的页面：右下角显示悬浮按钮，点击进入 AI 助手页
- 在 AI 助手页：FAB 隐藏；返回后 FAB 重新显示
- 验证手势导航安全区、暗色模式下对比度、无障碍（contentDescription）

## 可选增强
- FAB 长按弹出“快捷提问”底部面板
- FAB 未读徽章（当有新建议/消息时显示红点）
- 页面滚动时自动隐藏/显示 FAB（滑动控制）

请确认方案，确认后我将开始实现以上改造并进行验证。