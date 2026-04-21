@echo off
chcp 65001
@echo off

:: 设置 Java 环境变量
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202
set PATH=%JAVA_HOME%\bin;%PATH%

:: 验证 Java 版本
echo Java 路径: %JAVA_HOME%
java -version

:: 启动 VS Code
echo 正在启动 VS Code...
code .

pause
