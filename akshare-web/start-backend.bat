@echo off
chcp 65001
echo 正在启动AKShare Web后端服务...
echo 端口号: 8765
echo.

cd /d "%~dp0"
set MAVEN_OPTS=-Xmx512m -Xms256m
call mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m -Xms256m"

pause
