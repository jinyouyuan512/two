## 后端架构（Supabase 项目：yk-health）
- 项目标识：`tbejjedbmhguhdxxvezv`，基础地址：`https://tbejjedbmhguhdxxvezv.supabase.co`
- 采用 Supabase Auth + PostgREST + Storage + Edge Functions；行级安全（RLS）保障用户数据隔离。
- 本地配置：在 `local.properties` 使用 `SUPABASE_URL` 与 `SUPABASE_ANON_KEY` 指向 yk-health。

## 数据库模型与索引
- 表与字段
  - `profiles(id uuid PK → auth.users.id, display_name text, avatar_url text, created_at timestamptz, updated_at timestamptz)`
  - `heart_rates(id bigserial, user_id uuid, at timestamptz, bpm int)`
  - `steps(id bigserial, user_id uuid, at date, count int)`
  - `weights(id bigserial, user_id uuid, at date, kg numeric(5,2))`
  - `water_intake(id bigserial, user_id uuid, at date, ml int)`
  - `sleep_sessions(id bigserial, user_id uuid, at date, hours numeric(4,2))`
  - `mood_logs(id bigserial, user_id uuid, at timestamptz, mood text, note text?, score int?)`
- 索引
  - 统一创建 `(user_id, at desc)` 组合索引以优化时间序查询与聚合。
- 触发器（可选）
  - `profiles.updated_at` on update 自动更新；未来扩展日汇总表时可加物化视图刷新。

## 安全策略（RLS）
- 启用 RLS 于上述所有表。
- 策略：`USING/CHECK (auth.uid() = user_id)`，仅本人可读写；`profiles` 仅本人可读写自己的资料。
- API 访问统一带 `apikey` 与 `Authorization: Bearer <access_token>`。

## PostgREST 契约（前端直连）
- 查询（示例）
  - 心率最近 20 条：`GET /rest/v1/heart_rates?select=at,bpm&order=at.desc&limit=20`
  - 近 7 日步数：`GET /rest/v1/steps?select=at,count&order=at.desc&limit=7`
  - 体重/饮水/睡眠/情绪：同构查询，按 `at.desc` 与合理 `limit`
- 插入（示例）
  - 心率：`POST /rest/v1/heart_rates`，Body：`[{"user_id":"<uid>","at":"<ISO>","bpm":72}]`，Header：`Prefer: return=representation`
  - 步数/体重/饮水/睡眠/情绪：同构插入，Body 均需要 `user_id`
- 聚合（前端计算或 Edge Function）
  - 当日总步数/饮水：前端汇总或 `rpc`/EF 提供聚合接口。

## Edge Functions（可选增强）
- `health-aggregates`：按用户返回当天/近 7 日的汇总指标（步数总和、饮水总量、睡眠平均等）。
- `ai-health-assistant`：保留 AI 助手路由，契约 `ChatRequest{messages, context, model?} → ChatResponse{reply}`。
- `mood-analyzer`：接收 `mood_logs` 文本，返回情绪分类与评分填充 `score` 字段。

## Storage（头像与附件）
- Bucket：`avatars`，公开读取、私有写入；前端上传后更新 `profiles.avatar_url`。
- 规则：仅本人可写自己路径 `user_id/*`。

## 前端动态化改造
- 配置注入：`BuildConfig.SUPABASE_URL/ANON_KEY` 从 `local.properties` 读取；`SupabaseClientProvider` 统一 Ktor 客户端与 Header 注入。
- 数据层
  - `PostgrestApi`：新增 `weights()`、`waterIntake()`、`sleepSessions()`、`moodLogs()` 查询；插入结构统一包含 `user_id`。
  - `ImportsRepository`：导入记录保存时自动附加 `user_id = auth.uid()`；失败回退与统计摘要保留。
- ViewModel
  - `MetricsViewModel`：持有心率、步数、体重、饮水、睡眠、情绪的列表与最新值派生；`load()` 并发拉取。
  - `ProfileViewModel`：读取并展示 `display_name/avatar_url`，支持更新；空值占位与加载态。
- UI（Compose）
  - 数据页卡片显示真实指标（心率 bpm、步数 count、饮水 ml、体重 kg）。
  - 图表：心率折线、步数柱状、体重折线、饮水柱状、睡眠折线；X 轴显示日期/时间标签；空数据占位。
  - 导入页面：CSV/JSON 解析弹性与分隔符兼容；保存后显示计数摘要。
  - AI 助手入口可点击跳转；对话加载指示与错误提示统一。

## 错误处理与体验统一
- 网络错误映射：集中在 `AuthApi.mapError` 与 API 层扩展；前端统一使用 Snackbar/InfoBar 显示。
- 会话恢复：启动时根据 `expiresAt` 自动刷新 token；失败时退出登录并提示。
- Loading/Empty/Error 三态：列表/图表/卡片统一处理。

## 测试与文档
- 单元测试：`AuthRepository`（登录/刷新/错误）、`MetricsRepository`（解析与聚合）、`AIChatRepository`（上下文与回退）、`TokenStore`（持久化）。
- 文档：更新 `README.md`，包含本地配置示例、表结构说明、RLS 策略、REST/EF 契约、常见问题（401/超时/键泄露规避）。

## 验证流程
- 注册→登录→会话持久化（重启自动恢复）。
- 数据页加载真实图表与卡片指标；导入后计数摘要正确；AI 助手消息流转与失败回退；Profile 显示与更新资料正常；头像上传与显示。

## 交付物
- 后端：DDL 迁移 SQL（表/索引/RLS/策略）、Edge Functions 初始实现、Storage 规则。
- 前端：`PostgrestApi`/`MetricsRepository`/`MetricsViewModel` 更新、图表与卡片动态化、导入与 Profile 改造、错误处理统一。

请确认是否按以上计划推进（使用 yk-health 项目并更新本地配置、实现上述表与功能、完成前端动态化与验证）。