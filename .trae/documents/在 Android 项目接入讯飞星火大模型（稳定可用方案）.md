## 接入方式选择
- 优先采用 `HTTP Open API` 直连方案，依赖清晰、实现简单，便于在现有 Ktor 客户端中稳定落地（不改动网络栈）。参考官方文档：https://www.xfyun.cn/doc/spark/HTTP调用文档.html。
- 可选增强：后续需要流式输出或端能力时，再接入 `WebSocket`（wss://spark-api.xf-yun.com/v3.5/chat，文档：https://www.xfyun.cn/doc/spark/Web.html）或官方 `SparkChain Android SDK`（本地 aar 集成，文档：https://www.xfyun.cn/doc/spark/AndroidSDK.html）。

## 依赖与配置
- 依赖：项目已启用 Ktor 客户端（app/build.gradle 第103–107行），无需新增网络库。
- 权限：在 `AndroidManifest.xml` 添加 `INTERNET` 权限：`<uses-permission android:name="android.permission.INTERNET" />`。
- 密钥管理：在 `local.properties` 增加 `IFLYTEK_API_PASSWORD=你的APIPassword`；在 `app/build.gradle` 以 `buildConfigField` 暴露到运行时（与现有 Supabase 字段同模式），避免将密钥硬编码进源码。
- 模型选择：默认 `model = "generalv3.5"`，按需切换 Lite/Pro/Max/Ultra（文档枚举见 HTTP 文档）。

## 服务封装（Ktor，非流式）
- 定义请求体与最小响应解析，避免因为字段差异导致崩溃：
```
// 请求
@Serializable
data class SparkMessage(val role: String, val content: String)
@Serializable
data class SparkRequest(
  val model: String = "generalv3.5",
  val messages: List<SparkMessage>,
  val stream: Boolean = false,
  val temperature: Double? = null,
  val top_k: Int? = null,
  val max_tokens: Int? = null
)

// 最小响应（容错：用 JsonObject 兜底）
@Serializable
data class SparkChoice(@Serializable val message: SparkMessage? = null)
@Serializable
data class SparkResponse(
  val choices: List<SparkChoice>? = null,
  val error: JsonObject? = null
)
```
- 封装仓库：
```
class SparkRepository(private val client: HttpClient) {
  private val endpoint = "https://spark-api-open.xf-yun.com/v1/chat/completions"
  suspend fun chat(messages: List<SparkMessage>, model: String = BuildConfig.AI_MODEL): Result<String> = runCatching {
    val req = SparkRequest(model = if (model.isBlank()) "generalv3.5" else model, messages = messages, stream = false)
    val resp: SparkResponse = client.post(endpoint) {
      header("Authorization", "Bearer ${BuildConfig.IFLYTEK_API_PASSWORD}")
      contentType(ContentType.Application.Json)
      setBody(req)
      timeout { requestTimeoutMillis = 30000 }
    }.body()
    val content = resp.choices?.firstOrNull()?.message?.content
    require(!content.isNullOrBlank()) { "empty response" }
    content
  }
}
```
- 错误处理：返回 `Result<String>`，上层统一处理网络错误、鉴权错误（401）、参数错误（400）等；记录 `sid` 等字段时可在响应兜底 Json 中提取。

## ViewModel 与 UI 集成（Compose）
- 在 ViewModel 调用仓库，并将结果以 `State` 推给 `DataScreen` 或你的 Chat 界面：
```
class SparkViewModel(private val repo: SparkRepository) : ViewModel() {
  var uiState by mutableStateOf("")
    private set
  var loading by mutableStateOf(false)
    private set
  fun askUser(question: String) {
    viewModelScope.launch {
      loading = true
      val msgs = listOf(
        SparkMessage("system", "你是知识渊博的健康助手"),
        SparkMessage("user", question)
      )
      val result = repo.chat(msgs)
      uiState = result.getOrElse { "请求失败：${it.message}" }
      loading = false
    }
  }
}
```
- 在 Compose 屏中绑定 `uiState/loading`，点击按钮触发 `askUser("...")`，显示结果文本即可。

## 流式输出（可选扩展）
- 若需要打字机效果，将 `stream=true` 并使用 Ktor 读取 chunk/SSE；首次版本为稳定起见采用非流式，待接口验证后再扩展流式解析（HTTP 文档已给出 `stream` 参数）。

## 使用 SDK（可选方案）
- 若偏好官方 SDK：将 `SparkChain.aar` 放入 `app/libs`，在 `build.gradle` 配置 `repositories { flatDir { dirs 'libs' } }` 与 `implementation(name: 'SparkChain', ext: 'aar')`，并在 `proguard-rules.pro` 添加 `-keep class com.iflytek.sparkchain.** {*;}`（文档：https://www.xfyun.cn/doc/spark/AndroidSDK.html）。
- 在 `Application` 初始化：
```
val config = SparkChainConfig.builder()
  .appID("你的appId")
  .apiKey("你的apiKey")
  .apiSecret("你的apiSecret")
val ret = SparkChain.getInst().init(appContext, config)
require(ret == 0)
```
- 调用 `ChatLLM.run()` 获取结果；SDK 方案适合语音、TTS、Embedding 等能力统一管理（相关文档：AndroidSDK/ASR/TTS/Embedding 章节）。

## 安全与规范
- 密钥只放 `local.properties` → `BuildConfig`，不写入仓库；必要时改为 `DataStore` 动态注入。
- 为所有网络调用设置超时与 `try/catch`；对空响应做 `require` 校验，避免 NPE。
- 日志不打印密钥与完整响应，仅打印必要的错误码与提示。

## 验证步骤
- 设置好 `IFLYTEK_API_PASSWORD` 后，运行 `askUser("你好，讯飞星火")` 应返回文本内容。
- 断网、错误密钥时，UI 显示友好错误文案，不崩溃。
- 如需切换模型，在 `BuildConfig.AI_MODEL` 中改为 `generalv3.5/Max/Ultra` 等型号。

## 参考文档
- HTTP 接口与参数（模型/stream/tools 等）：https://www.xfyun.cn/doc/spark/HTTP调用文档.html
- WebSocket 接口（如需流式）：https://www.xfyun.cn/doc/spark/Web.html
- Android SDK（本地 aar 集成与混淆）：https://www.xfyun.cn/doc/spark/AndroidSDK.html