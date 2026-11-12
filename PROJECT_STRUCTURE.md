# 项目结构说明

## 目录树

```
discord/                          # 项目根目录
├── .gitignore                    # Git 忽略文件
├── README.md                     # 项目文档
├── PROJECT_STRUCTURE.md          # 本文件
├── build.gradle.kts              # 根构建配置
├── settings.gradle.kts           # 项目设置
├── gradle.properties             # Gradle 属性
└── app/                          # 应用模块
    ├── build.gradle.kts          # 应用构建配置
    ├── proguard-rules.pro        # 混淆规则
    └── src/main/
        ├── AndroidManifest.xml   # 清单文件
        ├── java/com/screentextocr/
        │   ├── ScreenTextOCRApplication.kt
        │   │                     # Hilt Application 入口
        │   ├── data/
        │   │   ├── repository/
        │   │   │   └── ScreenCaptureRepositoryImpl.kt
        │   │   └── models/       # 数据模型（预留）
        │   ├── domain/
        │   │   ├── model/
        │   │   │   └── RecognitionResult.kt
        │   │   ├── repository/
        │   │   │   └── ScreenCaptureRepository.kt
        │   │   └── usecase/
        │   │       ├── CaptureScreenUseCase.kt
        │   │       ├── RecognizeCenterTextUseCase.kt
        │   │       └── CaptureAndRecognizeUseCase.kt
        │   ├── presentation/
        │   │   ├── view/
        │   │   │   └── MainActivity.kt
        │   │   ├── viewmodel/
        │   │   │   └── ScreenCaptureViewModel.kt
        │   │   └── ui/
        │   │       └── ScreenCaptureScreen.kt
        │   ├── service/
        │   │   ├── screencapture/
        │   │   │   ├── ScreenCaptureManager.kt
        │   │   │   └── ScreenCaptureManagerImpl.kt
        │   │   ├── recognition/
        │   │   │   ├── TextRecognitionManager.kt
        │   │   │   └── TextRecognitionManagerImpl.kt
        │   │   └── foreground/
        │   │       └── ScreenCaptureForegroundService.kt
        │   └── di/
        │       └── AppModule.kt    # Hilt 依赖注入模块
        ├── res/                   # 资源文件
        │   ├── drawable/
        │   ├── mipmap-anydpi-v26/
        │   ├── values/
        │   └── xml/
        └── proguard-rules.pro     # 混淆规则（已移动到根目录）
```

## 代码层级职责

### 1. app:presentation - 表现层
- **职责**：UI 展示、用户交互、状态管理
- **关键组件**：
  - `MainActivity` - Activity 入口，处理权限回调
  - `ScreenCaptureViewModel` - 状态管理，暴露 StateFlow
  - `ScreenCaptureScreen` - Compose UI，收集并显示状态

**设计原则**：
- 不包含业务逻辑
- 所有操作委托给 Use Cases
- 只处理 UI 状态（Idle, Capturing, Error, Recognized）

### 2. app:domain - 领域层
- **职责**：业务用例、实体模型、接口定义
- **关键组件**：
  - `RecognitionResult` - 识别结果数据类
  - `ScreenCaptureRepository` - Repository 接口
  - Use Cases - 单一职责的业务操作封装

**设计原则**：
- 纯 Kotlin，无 Android 依赖（除必要时）
- 用例是应用的核心业务逻辑
- Repository 接口定义数据操作契约

### 3. app:data - 数据层
- **职责**：数据源实现、Repository 具体实现
- **关键组件**：
  - `ScreenCaptureRepositoryImpl` - 协调各 Manager，实现 Repository

**设计原则**：
- 实现领域层定义的接口
- 对外暴露领域模型，内部可转换数据格式
- 管理生命周期和资源释放

### 4. app:service - 服务层
- **职责**：底层系统服务封装（MediaProjection, ML Kit）
- **关键组件**：
  - `ScreenCaptureManager` / `Impl` - 屏幕截图管理
  - `TextRecognitionManager` / `Impl` - 文字识别管理
  - `ScreenCaptureForegroundService` - 前台服务

**设计原则**：
- 面向接口编程
- 处理 API 兼容性（API 31+）
- 异步操作，避免阻塞主线程

### 5. app:di - 依赖注入
- **职责**：提供依赖实例
- **关键组件**：
  - `AppModule.kt` - Hilt 模块，提供单例服务

**依赖关系**：
- Managers 作为单例提供
- Dispatchers 用于协程上下文
- 模块级别：SingletonComponent

## 数据流

```
用户点击"Start"
    ↓
MainActivity → ViewModel.startCapture()
    ↓
ViewModel → Repository.startCapture()
    ↓
ForegroundService.startForeground()
    ↓
CaptureAndRecognizeUseCase()
    ↓
├─> CaptureScreenUseCase() → ScreenCaptureManager.startCapture()
│       ↓
│   ImageReader 回调 → Bitmap
│       ↓
└─> RecognizeCenterTextUseCase(bitmap)
        ↓
    TextRecognitionManager.recognizeText()
        ↓
    Text + getCenterText() → 识别文字
        ↓
    Flow.emit(RecognitionResult)
        ↓
    ViewModel 收集 → StateFlow
        ↓
    Compose UI 显示
```

## 关键类说明

### ScreenCaptureManagerImpl
- **作用**：MediaProjection 核心实现
- **关键方法**：
  - `startCapture()`: 创建 VirtualDisplay + ImageReader
  - `getCenterRegion()`: 裁剪中心区域
- **生命周期**：
  - `requestMediaProjection()` → `setMediaProjection()` → `startCapture()` → `stopCapture()` → `release()`

### TextRecognitionManagerImpl
- **作用**：ML Kit 封装
- **关键方法**：
  - `recognizeText(bitmap)`: 异步识别，返回 Result<Text>
  - `getCenterText(text, width, height)`: 筛选中心块

### ScreenCaptureForegroundService
- **作用**：长期运行，持续截图
- **关键特性**：
  - `LifecycleService` + Hilt
  - 3秒间隔的循环截图
  - 持续通知 + Stop 动作
- **启动方式**：
  - `Context.startForegroundService()`
  - 自停止机制（用户点击 Stop）

### ScreenCaptureViewModel
- **作用**：UI 状态管理
- **State**：
  - `Idle`, `RequestingPermission`, `StartingService`, `Capturing`, `Error`, `RecognizedText`
- **Actions**：
  - `startCapture(activity)`
  - `stopCapture()`
  - `handleActivityResult()`
- **数据保留**：最近 100 条识别结果

### ScreenCaptureScreen
- **作用**：Compose UI
- **组件**：
  - 状态指示卡（Status Card）
  - 最新识别结果卡片
  - 历史记录 LazyColumn
  - FAB（开始/停止）
  - Snackbar 错误提示
- **Theme**：Material 3 动态颜色

## 依赖图

```
                 ┌─────────────────────┐
                 │  MainActivity       │
                 └─────────┬───────────┘
                           │ injects
                 ┌─────────▼───────────┐
                 │ ScreenCaptureViewModel│
                 └─────────┬───────────┘
                           │ uses
                 ┌─────────▼───────────┐
                 │ ScreenCaptureRepository│
                 └─────────┬───────────┘
                           │ uses
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────▼──────┐  ┌────────▼────────┐  ┌─────▼──────┐
│CaptureAnd…UseCase│RecognizeCenter…│   │Foreground  │
└──────────────┘  └─────────────────┘  │Service     │
                                       └────────────┘
        │                  │
┌───────▼──────┐  ┌───────▼──────┐
│ScreenCapture │  │TextRecognition│
│ManagerImpl   │  │ManagerImpl   │
└──────────────┘  └──────────────┘
```

## 配置说明

### 修改截图频率

**文件**：`ScreenCaptureForegroundService.kt`

```kotlin
private const val CAPTURE_INTERVAL_MS = 3000L // 改为 desired ms
```

### 修改中心区域比例

**文件**：`ScreenCaptureManagerImpl.kt`

```kotlin
override suspend fun getCenterRegion(bitmap: Bitmap, centerRatio: Double = 0.6): Bitmap
//                                                          ↑ 修改这里
```

### 修改识别语言

**文件**：`AppModule.kt`

```kotlin
@Provides
@Singleton
fun provideTextRecognizer(): TextRecognizer {
    // return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    // 或使用拉丁文以外脚本：
    // return TextRecognition.getClient(TextRecognizerOptions.Builder().build())
}
```

## 注意事项

1. **权限提示时机**：必须由 Activity 触发权限请求（不可在 Service 中）
2. **MediaProjection 生命周期**：与授予权限的 Activity/Fragment 生命周期无关，但需妥善保存 `MediaProjection` 对象
3. **Bitmap 内存**：及时调用 `bitmap.recycle()` 或使用 `try-with-resources` 模式
4. **线程管理**：所有耗时操作应在 `Dispatchers.IO` 执行
5. **API 差异**：API 31+ 的 `FOREGROUND_SERVICE_MEDIA_PROJECTION` 权限需在 Manifest 声明

## 扩展点

- **Repository**：可扩展缓存层、远程数据源
- **UseCase**：可添加 OCR 后处理（纠正拼写、格式化）
- **UI**：可添加 Settings 页面、历史记录详情
- **Service**：可扩展悬浮窗、快捷设置 Tile
- **Test**：可添加 Unit Test 和 UI Test

## 引用资源

- [MediaProjection 文档](https://developer.android.com/reference/android/media/projection/MediaProjection)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)
- [Foreground Service](https://developer.android.com/guide/components/foreground-services)
- [Hilt 指南](https://developer.android.com/training/dependency-injection/hilt-android)
- [Clean Architecture](https://developer.android.com/topic/architecture)

---

**维护提示**：修改代码后应检查数据流是否完整，特别是 Flow 的收集和释放。
