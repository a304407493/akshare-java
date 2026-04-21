@echo off
chcp 65001
@echo off

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202
set PATH=%JAVA_HOME%\bin;%PATH%

echo ========================================
echo 正在运行 AKShare QuickStart
echo ========================================
echo.

cd /d "%~dp0"

mvn -s settings.xml exec:java -pl examples

echo.
echo ========================================
echo 运行完成
echo ========================================
pause
