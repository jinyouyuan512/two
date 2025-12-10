## 总目标与交付
- 接入 Supabase（认证、数据库、存储、Edge Functions），替换本地 MockData，提供真实读写与安全访问
- 完成 Android 侧认证与会话管理、数据仓库与页面数据接入、导入流程、AI 建议调用
- 引入基本离线缓存与错误治理，补齐单元/仪表测试与可验收的端到端演示

## 环境与安全配置
- 在 Supabase 控制台创建项目，启用 GoTrue 认证、Postgres 数据库与 Storage
- 在 Android 侧通过 `BuildConfig` 注入 `SUPABASE_URL` 与 `SUPABASE_ANON_KEY`（开发环境仅本地注入，不提交密钥）
- 依赖方案：采用 `supabase-kt`（GoTrue/Postgrest/Storage/Functions 模块，使用 BOM 统一版本）

## 数据库设计
- `profiles(uid, display_name, avatar_url, settings_json, created_at, updated_at)`
- `steps(uid, at, count)`；`heart_rates(uid, at, bpm)`；`sleep_sessions(uid, start_at, end_at, score, stages_json)`
- `meals(uid, at, items_json, calories)`；`water_intakes(uid, at, ml)`；`moods(uid, at, mood, score)`
- `exercise_sessions(uid, start_at, end_at, type, calories)`
- `imports(uid, filename, source, rows_count, status, created_at)`
- 视图/物化视图（可选）：日/周/月聚合指标供前端快速查询

## 安全策略（RLS）
- 所有事实表与档案表启用 RLS：仅 `auth.uid()` 可读写自身数据
- 策略覆盖 `SELECT/INSERT/UPDATE/DELETE`；管理员聚合表只读可选策略

## Edge Functions（服务端逻辑）
- `import-parser`：解析 CSV/JSON 并批量写库，返回重复/错误报告
- `metrics-aggregator`：周期性生成日/周/月聚合指标
- `ai-suggestions`：基于最近数据生成个性化建议（供首页/AI 聊天使用）

## Android 客户端改造
### 依赖与配置
- 在 `app/build.gradle` 引入 `supabase-kt` BOM 与各模块；添加 `BuildConfig` 字段注入 URL/Key
- 新增 `SupabaseClientProvider` 封装客户端初始化与会话注入

### 认证与会话
- 改造 `AuthViewModel`：登录/注册/忘记密码/刷新会话；持久化 session 并冷启动恢复
- 路由守卫使用真实登录态，退出登录清理会话（替换 `isLoggedIn` 伪逻辑）

### 数据仓库与模型
- 新增仓库：`AuthRepository`, `ProfileRepository`, `MetricsRepository`, `ImportRepository`, `AiRepository`
- 定义 DTO/映射与分页/时间区间查询，统一 `Result` 错误处理与重试/backoff

### 页面数据接入
- 首页：健康概况/AI 建议来自后端（聚合视图与 `ai-suggestions`）
- 数据中心：心率/步数/体重趋势图使用真实数据；导入摘要读取 `imports`
- 运动/营养/睡眠/心理：记录读写打通，支持分页与周/月聚合
- 个人中心：读取/更新 `profiles`、头像上传至 Storage、偏好落库；实现退出登录

### 导入流程
- 文件选择后上传 Storage（含进度），调用 `import-parser`；显示校验报告与失败重试
- 允许本地解析直写库的简易通道（开发模式），统一到服务端后可移除

### AI 助手
- 对话消息写入 `ai_messages`（可选），回复调用 `ai-suggestions` 或扩展函数
- 支持历史分页、loading/错误态、重试与提示

### 缓存与离线
- 引入 Room/SQLDelight 缓存关键数据；离线添加排队上传；冲突以时间戳/版本解决

### 错误处理与体验
- 统一 Loading/Empty/Error 三态组件；网络检测与重试；长任务进度与取消
- 列表分页与懒加载、Compose 性能优化（稳定 key、避免重组）

## 测试与验收
- 单元测试：ViewModel/仓库/解析；Edge Functions 的本地/集成测试
- 仪表测试：认证流、导入流程、页面数据渲染与导航守卫
- 验收清单：登录/退出、数据增删查改、导入成功与报告、AI 建议拉取、离线缓存与恢复

## 里程碑
- 里程碑一：认证与基础表/RLS完成，首页/数据中心接通基础查询
- 里程碑二：导入管线（Storage+函数）与页面上传体验完成
- 里程碑三：四大模块（运动/营养/睡眠/心理）记录与聚合打通
- 里程碑四：AI 建议/聊天、缓存离线、错误治理与测试完善

请确认以上计划，我将按该清单开始实施（从依赖与认证改造入手，随后数据中心与导入流程）。