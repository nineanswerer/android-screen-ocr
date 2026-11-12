# Gradle Wrapper 修复指南

## 问题

```
Error: Gradle wrapper jar not found at /gradle/wrapper/gradle-wrapper.jar
```

`gradle/wrapper/gradle-wrapper.jar` 文件缺失，导致无法使用 `./gradlew` 命令。

## 解决方案

### 方法1：使用Android Studio（推荐）

1. 在Android Studio中打开项目
2. Android Studio会自动检测并下载正确的Gradle wrapper
3. 等待Sync完成即可

---

### 方法2：手动下载并安装

如果Android Studio无法自动修复，请手动操作：

#### 步骤1：下载Gradle 8.3

访问以下任一链接下载：
- https://services.gradle.org/distributions/gradle-8.3-bin.zip
- https://gradle.org/releases/

#### 步骤2：提取gradle-wrapper.jar

**正确的文件**：使用 `gradle-wrapper-8.3.jar`（约2KB），**不是** `gradle-wrapper-shared-8.3.jar`

**Windows（PowerShell）**：
```powershell
# 在项目根目录执行
Expand-Archive -Path gradle-8.3-bin.zip -DestinationPath .
Copy-Item .\gradle-8.3\lib\gradle-wrapper-8.3.jar gradle\wrapper\gradle-wrapper.jar
Remove-Item -Recurse -Force gradle-8.3, gradle-8.3-bin.zip
```

**macOS/Linux**：
```bash
unzip gradle-8.3-bin.zip
cp gradle-8.3/lib/gradle-wrapper-8.3.jar gradle/wrapper/gradle-wrapper.jar
rm -rf gradle-8.3 gradle-8.3-bin.zip
```

#### 步骤3：验证安装

```bash
./gradlew --version
```

应该看到类似输出：
```
Gradle 8.3
...
```

---

### 方法3：使用Homebrew（macOS）

```bash
brew install gradle
# 然后使用系统gradle而非wrapper
gradle --version
```

---

### 方法4：重新生成Wrapper（需要已安装Gradle）

如果你已经安装了其他版本的Gradle：

```bash
# 使用系统gradle重新生成wrapper
gradle wrapper --gradle-version 8.3

# 或使用gradlew（如果jar已修复）
./gradlew wrapper --gradle-version 8.3
```

---

## 验证修复成功

运行以下命令检查：

```bash
./gradlew --version
./gradlew clean
./gradlew assembleDebug
```

所有命令应无错误执行。

---

## 常见问题

### Q: 下载速度慢怎么办？
A: 使用国内镜像源，如阿里云镜像：
```
https://mirrors.aliyun.com/gradle/gradle-8.3-bin.zip
```

### Q: 还是报错"no main manifest attribute"？
A: 确保使用的是 `gradle-wrapper-shared-8.3.jar` 而不是 `gradle-wrapper-8.3.jar`

### Q: Android Studio Sync失败？
A: 在Android Studio中：
1. File → Settings → Build, Execution, Deployment → Gradle
2. 勾选 "Use default Gradle wrapper"
3. 点击 "Sync Now"

---

## 技术说明

Gradle Wrapper由以下文件组成：
- `gradle/wrapper/gradle-wrapper.jar` - 可执行jar（必需）
- `gradle/wrapper/gradle-wrapper.properties` - 配置文件（已存在）
- `gradlew` (或 `gradlew.bat`) - 启动脚本（已存在）

本项目只缺失jar文件，补充后即可正常使用。

---

**修复日期**：2025-04-08  
**Gradle版本**：8.3
