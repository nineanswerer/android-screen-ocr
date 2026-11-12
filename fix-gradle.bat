@echo off
REM Gradle Wrapper 自动修复脚本
REM 适用于 Windows 系统

echo ========================================
echo Gradle Wrapper 修复工具
echo ========================================
echo.

REM 检查Java是否安装
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Java！请先安装JDK 8+。
    pause
    exit /b 1
)

echo [1/3] Java环境检查通过

REM 创建临时目录
set TEMP_DIR=%TEMP%\gradle_fix_%RANDOM%
mkdir "%TEMP_DIR%" 2>nul

echo [2/3] 正在下载Gradle 8.3...
powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.3-bin.zip' -OutFile '%TEMP_DIR%\gradle.zip'" 2>nul

if not exist "%TEMP_DIR%\gradle.zip" (
    echo [错误] 下载失败，请检查网络连接
    echo 您可以手动下载 https://services.gradle.org/distributions/gradle-8.3-bin.zip
    echo 然后手动提取 gradle-8.3/lib/gradle-wrapper-8.3.jar 到 gradle/wrapper/
    pause
    exit /b 1
)

echo [3/3] 正在安装gradle-wrapper.jar...
powershell -Command "Expand-Archive -Path '%TEMP_DIR%\gradle.zip' -DestinationPath '%TEMP_DIR%' -Force"
copy "%TEMP_DIR%\gradle-8.3\lib\gradle-wrapper-8.3.jar" "gradle\wrapper\gradle-wrapper.jar" >nul 2>&1

if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo.
    echo ========================================
    echo ✅ 修复成功！gradle-wrapper.jar 已安装
    echo ========================================
    echo.
    echo 验证: gradlew --version
    echo.
) else (
    echo.
    echo ========================================
    echo [错误] 复制文件失败，请手动安装
    echo ========================================
    echo.
    echo 手动步骤:
    echo 1. 解压 %TEMP_DIR%\gradle.zip
    echo 2. 复制 gradle-8.3\lib\gradle-wrapper-8.3.jar
    echo 3. 粘贴到 gradle\wrapper\ 目录
    echo.
)

REM 清理临时文件
rmdir /s /q "%TEMP_DIR%" 2>nul
pause
