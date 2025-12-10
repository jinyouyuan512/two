## 项目现状速览

* 技术栈：Kotlin、Jetpack Compose、Navigation、Ktor、DataStore、MPAndroidChart；后端以 Supabase 为核心（Auth、PostgREST、Edge Functions）

* 关键配置注入：`app/build.gradle:35-38` 利用 `local.properties` 注入 `SUPABASE_URL`、`SUPABASE_ANON_KEY`、`AI_FUNCTION_PATH`、`AI_MODEL`

* 后端访问：

  * HTTP 客户端与 JSON：`app/src/main/java/com/example/healthapp/data/remote/SupabaseClientProvider.kt:11-30`

  * 认证与刷新：`app/src/main/java/com/example/healthapp/data/remote/AuthApi.kt`、`app/src/main/java/com/example/healthapp/data/remote/AuthRefreshHelper.kt:7-26`

  * 数据与导入：`PostgrestApi.kt`、`ImportsApi.kt`、`ImportsRepository.kt`

  * AI 助手：`AIChatApi.kt:32-45`（调用 Edge Function `functions/v1/<AI_FUNCTION_PATH>`）

* 会话持久化：`TokenStore`（DataStore）`app/src/main/java/com/example/healthapp/data/TokenStore.kt:11-36`；运行时会话：`SessionManager.kt`

* UI 导航入口：`MainActivity.kt` + `BottomNavBar.kt`，登录态控制起始路由（`BottomNavBar.kt:70-80`）

* 主要页面：Home、Data、Health、Profile、AI 助手，以及运动/营养/睡眠/心理四大分屏；图表目前为占位视图，未实际使用 MPAndroidChart。

## 改进目标

* 打通并稳固 Supabase 认证/刷新与会话持久化，完善错误提示

* AI 健康助手体验升级（加载指示、失败回退、快捷入口可点击）

* 将数据图表落地为真实折线/柱状图（心率/步数）

* 完善个人中心：从 Supabase 拉取昵称/头像等；提供更新能力

* 导入流程更健壮：格式识别、落库统计与页面反馈

* 增强可维护性：统一错误映射、单元测试覆盖关键路径

* 完成环境配置与使用文档，降低接入成本

## 技术实施方案

### 1. 环境配置与密钥管理

* 在 `local.properties` 增加并说明以下键值（无需提交到版本库，当前 `.gitignore` 已忽略）：

  * `SUPABASE_URL=https://<your-project>.supabase.co`

  * `SUPABASE_ANON_KEY=<anon-key>`

  * `AI_FUNCTION_PATH=ai-health-assistant`（与 Edge Function 路由一致）

  * `AI_MODEL=gpt-4o-mini`（可按需更换模型标识）

* 确认 `app/build.gradle:20-22` 的 URL 规范修正逻辑有效；保留 `buildConfigField` 注入位置 `app/build.gradle:35-38`。

### 2. 认证与会话完善

* 启动时会话恢复：在 `AuthViewModel.restoreSession` 中根据 `expiresAt` 主动判断是否过期，过期则调用 `AuthRepository.refresh` 刷新并更新 `SessionManager` 与 `TokenStore`（参考现有刷新封装 `AuthRefreshHelper.kt:7-26`）。

* 统一错误映射：保留 `AuthApi.mapError` 的人性化提示（`AuthApi.kt:112-119`），并在 `AuthViewModel` 对错误直接展示；登录/注册按钮增加 `loading` 禁用态与错误 Snackbar。

* 退出登录：沿用 `AuthViewModel.logout` 清理 DataStore 与会话（`AuthViewModel.kt:119-134`）。

### 3. 个人中心完善

* 实现 `ProfileRepository`：

  * `getProfile()` 与 `updateProfile(profile)` 调用 `ProfilesApi` 的 `getProfile`/`upsertProfile`（参照 `ProfilesRepository.kt:5-14` 的模式），以支持昵称与头像更新。

* `ProfileViewModel.load` 已从 Supabase 拉取 `userId` 与 `displayName`（`ProfileViewModel.kt:24-39`），补充头像 URL 处理与空值占位，页面 `HeaderSection` 展示真实数据（`ProfileScreen.kt:65-107`）。

* 后续可选：接入 Supabase Storage 上传头像，返回 `avatar_url` 更新 Profile。

### 4. AI 健康助手体验升级

* 快捷入口：`HomeScreen` 的“AI 健康助手”卡片增加点击跳转到 `ai` 路由（当前仅视觉占位，`HomeScreen.kt:48-106`）。

* 加载与失败反馈：在 `AIChatViewModel` 发送消息时加入“正在输入…”占位（发送前插入占位消息，成功后替换/失败则提示）；保留失败回退文案（`AIChatRepository.kt:37-57`）。

* 上下文构建：维持近 10 条消息与近 7 日步数/心率（`AIChatRepository.kt:24-35`）；保留 `AI_MODEL` 与 `AI_FUNCTION_PATH` 配置（`AIChatApi.kt:34-45`）。

* 后端契约：Edge Function 接口遵循 `ChatRequest{messages, context, model?} → ChatResponse{reply}`（与 `AIChatApi.kt:19-31` 一致）。

### 5. 数据可视化落地（MPAndroidChart）

* 心率折线图：在 `DataScreen` 的 `HeartRateChartSection` 中用 `AndroidView` 嵌入 `LineChart`，数据来源 `MetricsViewModel.heartRates`（`MetricsViewModel.kt:24-33`），X 轴为时间、Y 轴为 BPM。

* 步数柱状图：在 `StepsChartSection` 中嵌入 `BarChart`，数据来源 `MetricsRepository.fetchSteps()`（`MetricsRepository.kt:11-13`）。

* 统一主题样式与空数据占位；移除原占位 `Text`。

### 6. 导入功能增强

* 解析健壮性：

  * CSV 头容错与自适配（现有 `ImportDataScreen.kt:196-214`），补充分隔符变体与引号处理；JSON 支持对象包裹与数组（`ImportDataScreen.kt:216-231`）。

* 落库统计：维持 `ImportsRepository.saveRecords` 的三表插入与计数反馈（`ImportsRepository.kt:16-45`）；UI 展示导入成功摘要（`ImportDataScreen.kt:152-193` 与 `DataScreen.kt:354-371`）。

* 任务记录：保留 `ImportsApi.createImport` 的 job 创建（`ImportsApi.kt:26-42`），失败时仍做数据保存并提示。

### 7. 错误处理与提示统一

* 网络层：在各 API 调用处附加 `apikey` 与 `Authorization`（已统一），对 `ClientRequestException` 的状态与文案映射进行集中处理（模式参考 `AuthApi.kt:102-119`）。

* UI：在登录/注册/导入/AI 对话页面统一使用 Snackbar 或顶部 InfoBar 显示错误与成功信息。

### 8. 单元测试覆盖

* `AuthRepository`：登录/注册成功/失败与错误映射；刷新逻辑。

* `AIChatRepository`：上下文构建与回退文案分支。

* `TokenStore`：保存/加载/清空；过期恢复分支。

* `ImportsRepository`：CSV/JSON 解析与插入计数；空记录处理。

### 9. 文档与配置

* 更新 `README.md`：

  * 本地开发配置步骤（`local.properties` 示例、Android 版本、Gradle 要求）

  * Supabase 资源：Auth、PostgREST 表结构（`profiles`、`heart_rates(at TIMESTAMP,bpm INT)`、`steps(at DATE,count INT)`、`sleep_sessions(at DATE,hours FLOAT)`）、Edge Function 路由与请求/响应示例

  * 常见问题：401 刷新、网络超时、匿名键泄露风险与规避

## 验证方案

* 运行构建，完成：注册→登录→会话持久化（重启后自动恢复）；Data 页加载心率/步数并展示真实图表；导入 CSV/JSON 后查看统计摘要；Home 页/悬浮按钮进入 AI 助手，消息流转与失败回退；Profile 页展示昵称/ID（含头像占位）。

## 交付物（代码改动范围）

* ViewModel 与 Repository：`AuthViewModel.kt`、`ProfileRepository.kt`、`ProfileViewModel.kt`、`AIChatViewModel.kt`、`AIChatRepository.kt`、`MetricsViewModel.kt`

* 远程 API/数据层：`AuthApi.kt`、`ProfilesApi.kt`、`PostgrestApi.kt`、`ImportsApi.kt`、`SupabaseClientProvider.kt`、`TokenStore.kt`、`SessionManager.kt`

* UI：`HomeScreen.kt`（AI 卡片点击跳转）、`DataScreen.kt`（LineChart/BarChart）、`AIHealthAssistantScreen.kt`（加载与错误反馈）、`ProfileScreen.kt`（头像与昵称显示）

* 文档：`README.md` 补充配置与后端契约说明

