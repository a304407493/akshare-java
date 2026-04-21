@echo off
chcp 65001
@echo off

echo ========================================
echo 正在启动 AKShare Java 项目
echo ========================================
echo.

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "%~dp0"

powershell -ExecutionPolicy Bypass -File "run.ps1"

echo.
pause
