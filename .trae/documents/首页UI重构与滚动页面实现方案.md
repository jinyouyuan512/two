## 目标
- 依据图片优化首页UI，支持上下滚动，完整实现图片中的所有组件与视觉细节。
- 保持现有主题与导航结构，适配悬浮AI入口与底部导航，不改变其他页面功能。

## 页面结构（Compose）
- 使用 `LazyColumn` 作为页面主容器，分为以下Section：
  1) 渐变Header（紫→蓝）：问候语、提示文字；Header下沿圆角过渡到白色内容区
  2) 今日健康概况卡片：白底圆角卡片，标题与“85分”右上角入口；内部2×2指标网格（步数、心率、睡眠、情绪）
  3) 快速记录：四个圆形按钮（运动、饮水、餐饮、体重）
  4) AI健康建议：紫蓝渐变卡片，顶部说明文字、三条建议条（白色条带）与“查看更多建议”按钮
  5) 本周趋势：白底圆角卡，三行彩色进度条（运动时长/睡眠质量/饮食健康）与右上角“查看详情”
- 每个Section作为单独的 `@Composable`，通过 `item {}`/`items {}` 插入到 `LazyColumn`，使用 `contentPadding = PaddingValues(16.dp)` 和 `verticalArrangement = Arrangement.spacedBy(12.dp)`

## 组件拆分
- `HomeHeader()`：渐变背景、文字层级、顶部安全区适配；使用 `Brush.verticalGradient`
- `HealthOverviewCard()`：白底圆角卡片、标题行、右上角分数；内部 `MetricsGrid()` 显示四Tile
- `MetricTile(type, value, unit, description, accentColor)`：统一尺寸与阴影；颜色按类型（橙/红/靛蓝/黄）
- `QuickActionsRow()`：四个 `QuickActionButton(icon/text)` 圆形卡；点击回调暂留（Toast/导航）
- `AiSuggestionsCard()`：渐变容器；`AiSuggestionItem(icon, title, description)` 三条；底部主按钮
- `WeeklyTrendsCard()`：进度条项 `TrendItem(label, value, color)` 使用新API `progress = { float }`

## 视觉与交互细节
- 卡片圆角与阴影：使用 `AppShapes`（既有）与 `CardDefaults.cardElevation(4.dp)`；
- 文字层级：标题 18sp，正文 14sp，辅助 12sp；数字加粗；
- 间距：页面16dp、卡片内12dp、元素间8–12dp；
- 颜色统一：`HealthPurple/HealthBlue/HealthOrange/HealthRed/HealthGreen/HealthYellow/靛蓝`；
- 动效：按钮与Tile点击涟漪；建议条过渡淡入；
- 无障碍：为按钮和Tile设置 `contentDescription`；触控区域≥48dp；颜色对比合规。

## 数据与状态
- 复用 `MockData.getHealthMetrics()/getHealthSuggestions()` 作为首页显示数据；
- “85分”可从 `MockData`或常量给出；后续可接入真实评分API；
- “查看更多建议”按钮预留回调；

## 与现有结构集成
- 首页实现文件仍为 `HomeScreen.kt`；将现有内容替换为 `LazyColumn` 结构与拆分组件；
- 悬浮AI入口由 `BottomNavBar` 提供，首页无冲突；
- 底部导航与主题保持现状；

## 技术实现步骤
1) 在 `HomeScreen.kt` 中引入 `LazyColumn` 并分节组织；
2) 实现各Section与子组件（Header、概况卡、快速记录、AI建议、本周趋势）；
3) 统一颜色/圆角/阴影与文字层级；弃用API替换（进度条使用lambda）；
4) 接入 `MockData` 并完成交互占位（onClick回调）；
5) 预览与构建验证，滚动体验与视觉一致性调整。

## 验证与交付
- 本地构建无错误；首页元素完整与滚动顺畅；
- 交互反馈与无障碍检查；
- 代码按模块清晰拆分，易维护与扩展。

请确认，我将按该方案实现首页重构并交付可运行版本。