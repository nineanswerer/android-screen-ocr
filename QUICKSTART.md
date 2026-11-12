# 快速开始指南

## 项目已完成！

✅ 所有核心代码文件已创建
✅ 项目结构完整（Clean Architecture）
✅ 文档齐全（README + PROJECT_STRUCTURE）

## 立即开始

### 1. 打开项目

```bash
# 在 Android Studio 中
File → Open → 选择项目目录
```

### 2. 同步 Gradle

等待 Android Studio 自动同步，或手动点击：
- 顶部工具栏的 "Sync Project with Gradle Files"

### 3. 配置设备

- 使用真机：Android 12 (API 31) 或更高版本
- 或启动 AVD 模拟器（需选择 API 31+ 系统镜像）

### 4. 运行应用

点击顶部绿色的 Run 按钮 (▶️) 或：
```bash
./gradlew installDebug
```

## 首次使用流程

1. **启动应用**
   - 看到主界面，标题为 "Screen Text OCR"
   - 显示 "Ready to capture" 状态卡

2. **点击 "Start Capture" 按钮**
   - 系统弹窗："允许 [应用] 录制屏幕吗？"
   - 点击 "允许" → 授权屏幕截图

3. **前台服务启动**
   - 通知栏出现持续通知："Screen Capture"
   - FAB 变为红色 "Stop" 按钮

4. **查看识别结果**
   - 应用自动开始每 3 秒截图并识别
   - 识别到的文字会显示在 "Latest Recognition" 卡片
   - 历史记录列表累积所有识别结果

5. **停止服务**
   - 点击界面 Stop 按钮，或通知栏 Stop 动作
   - 服务停止，回到 Idle 状态

## 文件清单

### 核心代码（18 个 Kotlin 文件）

| 层级 | 文件数 | 说明 |
|------|--------|------|
| presentation | 3 | MainActivity, ViewModel, Composable |
| domain | 5 | Repository 接口, Use Cases, 模型 |
| data | 1 | Repository 实现 |
| service | 5 | 截图、识别、前台服务 |
| di | 2 | Application, Hilt Module |
| **总计** | **18** | 全部 Kotlin 文件 |

### 配置文件

- `settings.gradle.kts` - 项目设置
- `build.gradle.kts` (root + app) - 构建配置
- `gradle.properties` - Gradle 属性
- `AndroidManifest.xml` - 清单文件（权限 + 服务 + Activity）
- `proguard-rules.pro` - 混淆规则

### 资源文件（11 个）

- 应用图标（adaptive icon）
- 通知图标
- 颜色、字符串、主题
- 备份规则 XML

### 文档

- `README.md` - 完整项目文档（8000+ 字）
- `PROJECT_STRUCTURE.md` - 架构详解
- `QUICKSTART.md` - 本文件

## 验证清单

- [ ] Gradle 同步无错误
- [ ] 编译通过（Build → Make Project）
- [ ] APK 成功安装到设备
- [ ] 启动应用无 Crash
- [ ] 点击 Start Capture 显示授权弹窗
- [ ] 授权后服务正常启动
- [ ] 识别出的文字显示在界面
- [ ] 点击 Stop 服务正常停止

## 常见问题

### 编译错误："Hilt Android Gradle Plugin not found"

**解决**：确保网络连接，等待 Gradle 下载依赖。如持续失败，检查 `build.gradle.kts` 的 Hilt 版本。

### 运行时："Permission denied"

**原因**：未授予 MediaProjection 权限
**解决**：点击 Start 重新触发授权流程

### 服务无法启动

**检查**：
1. 是否在 API 31+ 设备上运行？
2. Foreground Service 权限是否被系统禁用？
3. 查看 logcat 错误信息

### 识别结果为空白

**原因**：
- 截图中无清晰文字
- 文字不在中心区域（默认 60%）
- 文字太小或背景复杂

**解决**：
- 尝试更大的文字
- 调整中心区域比例（修改代码）
- 使用 RecognizeCenterTextUseCase.kt 调整逻辑

## 下一步

- 查看 `PROJECT_STRUCTURE.md` 了解架构细节
- 阅读 `README.md` 扩展功能建议
- 根据需要定制配置（截图频率、区域比例等）

## 技术支持

如有问题，请检查：
1. Android Studio 版本（建议 Hedgehog 或更高）
2. Kotlin 版本（应为 1.9.20+）
3. Gradle 版本（应为 8.2+）
4. 设备 Android 版本（必须 API 31+）

---

**项目创建完成！祝开发顺利！** 🚀
