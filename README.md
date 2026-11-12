> 2026-11-12 源码归档状态：Android 屏幕文字 OCR 源码归档。原工程 gradle/wrapper/gradle-wrapper.jar 为 0 字节，本次未上传该空文件，尚未完成 Android 构建和真机验证。仓库保留源代码、Gradle 配置及资源文件；需要在本机重新生成有效 Wrapper 并安装相应 Android SDK。

# Screen Text OCR - Android App

一个基于 MediaProjection 和 Google ML Kit 的屏幕文字识别 Android 应用，采用 **Clean Architecture** 设计。

> **⚠️ 注意事项**：本项目已修复多个关键bug（2025-04-08），包括依赖注入配置错误、Service未初始化、AndroidManifest无效引用等。使用前请确保代码为最新版本。

## 功能特性

- ✅ 无感屏幕截图（前台服务持续运行）
- ✅ 中心区域文字识别（只识别屏幕中心 60% 区域）
- ✅ 实时结果展示（Compose UI）
- ✅ 现代化架构（Clean Architecture + Hilt + Compose）
- ✅ 支持 Android 12+ (API 31+)

## 项目结构

```
app/src/main/java/com/screentextocr/
├── data/
│   ├── repository/
│   │   └── ScreenCaptureRepositoryImpl.kt
│   └── models/
├── domain/
│   ├── repository/
│   │   └── ScreenCaptureRepository.kt
│   ├── usecase/
│   │   ├── CaptureScreenUseCase.kt
│   │   ├── RecognizeCenterTextUseCase.kt
│   │   └── CaptureAndRecognizeUseCase.kt
│   └── model/
│       └── RecognitionResult.kt
├── presentation/
│   ├── view/
│   │   └── MainActivity.kt
│   ├── viewmodel/
│   │   └── ScreenCaptureViewModel.kt
│   └── ui/
│       └── ScreenCaptureScreen.kt
├── service/
│   ├── screencapture/
│   │   ├── ScreenCaptureManager.kt
│   │   └── ScreenCaptureManagerImpl.kt
│   ├── recognition/
│   │   ├── TextRecognitionManager.kt
│   │   └── TextRecognitionManagerImpl.kt
│   └── foreground/
│       └── ScreenCaptureForegroundService.kt
├── di/
│   └── AppModule.kt
└── ScreenTextOCRApplication.kt
```

## 技术栈

- **Language**: Kotlin
- **Min SDK**: API 31 (Android 12)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: Clean Architecture
- **UI**: Jetpack Compose + Material 3
- **Dependency Injection**: Hilt (Dagger)
- **Text Recognition**: Google ML Kit
- **Screen Capture**: MediaProjection API
- **Coroutines**: Kotlin Coroutines & Flow

## 权限说明

应用需要以下权限：

- `FOREGROUND_SERVICE` - 前台服务
- `FOREGROUND_SERVICE_MEDIA_PROJECTION` - 媒体投影前台服务（API 31+）
- `POST_NOTIFICATIONS` - 显示通知
- `SYSTEM_ALERT_WINDOW` - 悬浮窗（可选）

**注意**：MediaProjection 权限通过系统对话框由用户授予，不是清单文件权限。

## 构建指南

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 11 或更高版本
- Android SDK 34
- Kotlin 1.9.20

### 构建步骤

1. 克隆或复制项目到本地
2. 用 Android Studio 打开项目
3. 等待 Gradle 同步完成
4. 连接 Android 设备（API 31+）或启动模拟器
5. 点击 Run 按钮或执行：

```bash
./gradlew installDebug
```

### 首次运行

首次运行时，应用会请求以下权限：

1. **MediaProjection 权限** - 点击"Start Capture"后，系统会弹窗询问是否允许录屏
2. **通知权限** - 显示前台服务通知
3. **悬浮窗权限**（可选）- 如果启用悬浮窗功能

授权后，应用将开始周期性截图（默认每 3 秒）并识别中心文字。

## 使用说明

1. 启动应用
2. 点击右下角的 **"Start Capture"** 按钮
3. 在系统弹窗中点击 **"允许"** 授予屏幕录制权限
4. 前台服务会自动启动，通知栏显示持续通知
5. 应用会自动识别屏幕中心区域的文字并显示在界面上
6. 点击 **"Stop"** 停止捕获，或从通知栏点击 **"Stop"** 停止服务

## 核心实现说明

### MediaProjection 截图

- 使用 `MediaProjectionManager` 请求权限
- 创建 `VirtualDisplay` 连接到 `ImageReader`
- 通过 `ImageReader.setOnImageAvailableListener` 获取最新帧
- 兼容 API 31+ 的 `PixelFormat.RGBA_8888`

### 文字识别

- Google ML Kit Text Recognition（拉丁文）
- `InputImage.fromBitmap()` 创建输入图像
- 通过 `TextRecognizer.process()` 异步识别
- 基于文本块的边界框位置筛选中心区域文字

### 前台服务

- 继承 `LifecycleService`，使用 `startForeground()`
- 持久通知显示捕获状态
- 提供停止操作按钮
- 3 秒间隔的周期性捕获循环

### 状态管理

- `ScreenCaptureViewModel` 管理 UI 状态
- `StateFlow` 暴露 `ScreenCaptureState`
- 通过 `collectAsStateWithLifecycle()` 订阅状态变化

## 扩展建议

### 功能扩展

- [ ] 支持多语言识别（使用 `TextRecognizerOptions`）
- [ ] 可调节中心区域比例（设置页面）
- [ ] 历史记录持久化（Room 数据库）
- [ ] 文字导出/分享功能
- [ ] 翻译 API 集成（DeepL、Google Translate）
- [ ] 悬浮窗实时显示识别结果（FloatingWindowService）
- [ ] 智能截屏触发（监听屏幕变化）

### 性能优化

- [ ] 动态调整截图频率（稳定识别后降低频率）
- [ ] Bitmap 内存优化（RGB_565 配置）
- [ ] 图片预处理（对比度增强、降噪）
- [ ] 识别结果缓存（时间窗口去重）

## 已知限制

1. **MediaProjection 仅当前会话有效** - 应用重启后需要重新授权
2. **后台限制** - Android 可能会限制后台服务，建议用户启用"无限制"省电策略
3. **锁屏状态** - 锁屏时 MediaProjection 通常无法工作
4. **电池消耗** - 持续截图会消耗较多电量

## 🐛 故障排除

### 问题：服务启动失败 / 应用崩溃

**可能原因**：
1. 依赖注入配置不完整（已修复 - 确保 `AppModule.kt` 包含所有UseCase提供方法）
2. `ScreenCaptureForegroundService` 中 `captureAndRecognizeUseCase` 未注入（已修复）
3. `ScreenCaptureViewModel` 启动服务时缺少Context（已修复）

**检查**：
- 查看 logcat 输出：`adb logcat | grep ScreenCapture`
- 确认无 `UninitializedPropertyAccessException` 错误
- 确认 `ScreenCaptureViewModel` 已注入 `appContext`

**解决**：
- 确保所有DI模块正确配置
- 清理并重建项目：`./gradlew clean build`

---

### 问题：识别率为空

**解决**：
- 确保截图中包含清晰文字
- 检查是否在屏幕中心区域（默认 60%）
- 文字字体不能太小或太模糊

---

### 问题：服务被系统杀死

**解决**：
- 在系统设置中将应用设为"无限制"电池优化
- 保持前台服务通知可见
- 避免同时运行多个前台服务

---

### 问题：安装失败 - "Package manager has killed"

**原因**：AndroidManifest引用了不存在的 `FloatingWindowService`（已修复）

**解决**：
- 已删除 `AndroidManifest.xml` 中的无效服务声明
- 或创建 `FloatingWindowService.kt` 实现悬浮窗功能

---

### 问题：ImageReader回调不触发

**原因**：Handler配置不当（已修复）

**解决**：
- 已在 `ScreenCaptureManagerImpl.kt` 中添加 `Handler(Looper.getMainLooper())`
- 确保 `setOnImageAvailableListener` 正确传入handler

---

## 🛠️ 已知修复（2025-04-08）

以下问题已在当前版本修复：

| # | 问题 | 文件 | 修复方案 |
|---|------|------|---------|
| 1 | Foreground Service未注入useCase | `ScreenCaptureForegroundService.kt` | 添加 `@Inject lateinit var captureAndRecognizeUseCase` |
| 2 | ViewModel缺少context | `ScreenCaptureViewModel.kt` | 构造函数添加 `@ApplicationContext private val appContext: Context` |
| 3 | UseCase未注册到DI | `AppModule.kt` | 添加3个UseCase提供方法 |
| 4 | FloatingWindowService不存在 | `AndroidManifest.xml` | 删除无效service声明 |
| 5 | ImageReader handler为null | `ScreenCaptureManagerImpl.kt` | 添加 `Handler(Looper.getMainLooper())` |

## 开发说明

### 调试与验证

**启动应用后检查清单**：
- [ ] 应用正常编译并安装（无DI错误）
- [ ] MainActivity显示正常，UI无崩溃
- [ ] 点击"Start Capture"按钮有效
- [ ] MediaProjection权限弹窗出现
- [ ] 授权后前台服务启动（通知栏显示）
- [ ] 每3秒识别一次屏幕中心文字
- [ ] 识别结果实时显示在UI上
- [ ] 点击"Stop"服务正常停止

**常见调试命令**：
```bash
# 查看日志
adb logcat | grep -i "ScreenCapture\|OCR\|Hilt"

# 清理构建
./gradlew clean

# 重新构建
./gradlew assembleDebug

# 安装到设备
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

### 添加新 Use Case

1. 在 `domain/usecase/` 创建类
2. **在 `AppModule.kt` 中添加 `@Provides` 方法**注册依赖
   ```kotlin
   @Provides
   @Singleton
   fun provideYourUseCase(
       dependency: SomeDependency
   ): YourUseCase = YourUseCase(dependency)
   ```
3. 在 `ScreenCaptureRepositoryImpl` 或其它地方使用
4. 通过 `@Inject` 注入到 ViewModel 或 Service

> **注意**：所有UseCase必须在 `AppModule.kt` 中注册，否则Hilt无法注入！

### 修改截图频率

在 `ScreenCaptureForegroundService.kt` 的 companion object 中修改：

```kotlin
companion object {
    private const val CAPTURE_INTERVAL_MS = 3000L // 改为所需毫秒数
}
```

**注意**：此间隔控制Foreground Service循环截图的频率。默认每3秒一次。

### 调整中心区域比例

在 `ScreenCaptureManagerImpl.kt` 的 `getCenterRegion()` 方法中修改 `centerRatio` 默认值。

---

## 📋 修复历史与已知问题

### 2025-04-08 关键修复

本次更新修复了应用无法正常运行的多项关键问题：

| 问题 | 影响 | 状态 |
|------|------|------|
| Foreground Service未注入`CaptureAndRecognizeUseCase` | 运行时崩溃 | ✅ 已修复 |
| ViewModel缺少Context无法启动服务 | 功能不可用 | ✅ 已修复 |
| UseCase未注册到DI容器 | 编译/运行时错误 | ✅ 已修复 |
| AndroidManifest引用不存在的Service | 安装失败 | ✅ 已修复 |
| ImageReader使用null handler | 潜在性能问题 | ✅ 已修复 |
| Gradle Wrapper jar文件缺失 | 无法构建 | 📖 [见下方说明](#-gradle-wrapper-修复) |

**详见**：[`PROJECT_STRUCTURE.md`](./PROJECT_STRUCTURE.md) 中的详细架构说明

---

### 🛠️ Gradle Wrapper 修复

**问题**：`gradle/wrapper/gradle-wrapper.jar` 文件缺失，导致无法使用 `./gradlew` 命令。

**解决**：请参照 [FIX_GRADLE_WRAPPER.md](./FIX_GRADLE_WRAPPER.md) 中的步骤手动修复。

**快速修复（Android Studio）**：
1. 在Android Studio中打开项目
2. IDE会自动检测并下载缺失的wrapper文件
3. 等待Gradle Sync完成即可

**验证**：
```bash
./gradlew --version  # 应显示Gradle 8.3
./gradlew build      # 应成功编译
```

---

## License

MIT License - 自由使用和修改

---

**开发者提示**：本项目适合学习 Clean Architecture、MediaProjection API 和 ML Kit 集成。建议先理解各层级职责，再自行扩展功能。
