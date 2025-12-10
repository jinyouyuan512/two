# 悦康AI健康应用 - Android原生项目

## 项目概述
这是一个基于Android原生技术栈开发的健康管理应用，使用Kotlin语言和Jetpack Compose UI框架。应用包含8个主要功能模块：首页、健康数据中心、运动管理、营养管理、睡眠监测、心理健康、AI健康助手和个人中心。

## 技术栈
- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **导航**: Navigation Compose
- **状态管理**: Compose State
- **构建工具**: Gradle
- **最低Android版本**: API 24 (Android 7.0)
- **目标Android版本**: API 34 (Android 14)

## 功能特性

### 1. 首页 (HomeScreen)
- **健康概况**: 显示步数、心率等核心健康指标
- **快速记录**: 运动、饮水、餐饮、体重快速记录按钮
- **AI助手入口**: 快速访问AI健康助手
- **AI健康建议**: 基于用户数据的个性化建议
- **本周趋势**: 运动时长、睡眠质量、饮食健康趋势

### 2. 睡眠监测 (SleepMonitoringScreen)
- **睡眠总览**: 总睡眠时长、入睡时间、起床时间
- **睡眠评分**: 综合睡眠质量评分
- **本周趋势**: 每日睡眠时长柱状图
- **睡眠分期**: 浅睡眠、深睡眠、REM睡眠、清醒时间占比
- **睡眠统计**: 入睡潜伏期、夜醒次数等详细数据

### 3. 心理健康 (MentalHealthScreen)
- **心情指数**: 当前心情状态和评分
- **情绪记录**: 8种情绪状态快速记录
- **本周情绪趋势**: 情绪变化趋势图
- **情绪统计**: 各种情绪出现频次统计
- **压力评估**: 压力水平监测
- **冥想练习**: 放松和冥想指导

### 4. AI健康助手 (AIHealthAssistantScreen)
- **智能对话**: 24小时在线健康问答
- **快捷提问**: 预设常见健康问题快速提问
- **个性化建议**: 基于用户数据的AI建议
- **聊天历史**: 完整的对话记录

### 5. 健康数据中心 (DataScreen)
- **指标卡片**: 心率、步数、饮水、体重数据展示
- **趋势图表**: 心率、步数、体重的历史趋势
- **最近记录**: 最近的健康数据记录
- **数据导入**: 支持导入外部健康数据

### 6. 运动管理 (ExerciseScreen)
- **今日计划**: 显示今日运动计划和卡路里消耗
- **推荐运动**: 有氧运动、力量训练、拉伸放松、核心训练
- **实时运动**: 运动过程中的实时数据监控
- **成果记录**: 运动完成率和累计数据

### 7. 营养管理 (NutritionScreen)
- **卡路里摄入**: 显示每日卡路里摄入目标和进度
- **饮食记录**: 早餐、午餐、晚餐的营养记录
- **营养标签**: 膳食纤维、碳水、脂肪、蛋白质等营养素
- **饮水记录**: 每日饮水量追踪
- **AI营养建议**: 个性化营养建议

### 8. 个人中心 (ProfileScreen)
- **用户信息**: 头像、姓名、ID、健康积分
- **快捷操作**: 目标管理、成就徽章、数据备份、好友邀请
- **账户设置**: 个人信息、通知设置、密码修改
- **隐私安全**: 隐私设置、数据管理
- **偏好设置**: 推送通知、自动同步、数据分享开关

## 设计特色

### 视觉设计
- **渐变色背景**: 每个页面采用不同的渐变色彩主题
- **卡片式布局**: 现代化的卡片设计，信息层次清晰
- **圆角设计**: 统一的圆角风格，界面友好
- **彩色标签**: 不同功能模块使用不同颜色标识

### 交互设计
- **底部导航**: 8个主要功能模块的快速切换
- **标签页切换**: 数据、运动、营养、睡眠、心理页面的子功能切换
- **开关控制**: 偏好设置的开关交互
- **按钮反馈**: 清晰的按钮状态和点击反馈

## 构建和运行

### 环境要求
- Android Studio Arctic Fox 或更高版本
- Kotlin 1.9.22+
- Gradle 8.2+
- Android SDK 34+

### 构建步骤
1. 克隆项目到本地
2. 在Android Studio中打开项目
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击运行按钮构建并安装应用

### 本地配置（local.properties）
- 在项目根目录的 `local.properties` 中添加以下键值以注入后端配置：
  - `SUPABASE_URL=https://<your-project>.supabase.co`
  - `SUPABASE_ANON_KEY=<anon-key>`
  - `AI_FUNCTION_PATH=ai-health-assistant`
  - `AI_MODEL=gpt-4o-mini`
- 这些值会通过 `BuildConfig` 注入，参见 `app/build.gradle:35-38`。

### 后端资源（Supabase，项目 yk-health）
- Auth：邮箱密码登录/注册与令牌刷新（`/auth/v1`），客户端封装参见 `app/src/main/java/com/example/healthapp/data/remote/AuthApi.kt`
- PostgREST 表结构（RLS 已启用，仅本人可读写）：
  - `profiles(id uuid PK -> auth.users.id, display_name text, avatar_url text, created_at timestamptz, updated_at timestamptz)`
  - `heart_rates(id bigserial, user_id uuid, at timestamptz, bpm int)`
  - `steps(id bigserial, user_id uuid, at date, count int)`
  - `weights(id bigserial, user_id uuid, at date, kg numeric(5,2))`
  - `water_intake(id bigserial, user_id uuid, at date, ml int)`
  - `sleep_sessions(id bigserial, user_id uuid, at date, hours numeric(4,2))`
  - `mood_logs(id bigserial, user_id uuid, at timestamptz, mood text, note text?, score int?)`
- 查询示例：
  - 心率 20 条：`GET /rest/v1/heart_rates?select=at,bpm&order=at.desc&limit=20`
  - 近 7 日步数：`GET /rest/v1/steps?select=at,count&order=at.desc&limit=7`
  - 体重/饮水/睡眠/情绪：同构查询；X 轴显示日期/时间标签
- 插入示例（需携带 `user_id=auth.uid()`）：
  - 心率：`POST /rest/v1/heart_rates`，`[{"user_id":"<uid>","at":"<ISO>","bpm":72}]`，`Prefer: return=representation`
  - 步数/体重/饮水/睡眠/情绪：同构插入
- Edge Functions：
  - `functions/v1/ai-health-assistant`：请求 `ChatRequest{messages, context?, model?}`，响应 `ChatResponse{reply}`；客户端参见 `AIChatApi.kt`
  - `functions/v1/health-aggregates`：请求 `{rangeDays?: number}`，响应 `{stepsTotal, waterTotal, sleepAvg}`（占位，可后续替换为真实聚合）
- Storage（头像）：bucket `avatars`，公开读、私有写；上传后回写 `profiles.avatar_url`

### 常见问题
- 401 未授权：客户端会在 `AuthRefreshHelper` 中自动用 `refresh_token` 刷新并重试（`app/src/main/java/com/example/healthapp/data/remote/AuthRefreshHelper.kt:7-26`）
- URL 规范：`app/build.gradle:20-22` 会修正 `https\://` 为 `https://`
- 秘钥安全：`local.properties` 已在 `.gitignore` 忽略；不要将密钥提交到版本库
- RLS 提示：插入必须包含 `user_id=auth.uid()`；查询需带 `Authorization: Bearer <access_token>`

## 开发者信息
- 应用名称: 悦康AI健康应用
- 版本: 1.0.0
- 开发框架: Android原生 + Jetpack Compose
