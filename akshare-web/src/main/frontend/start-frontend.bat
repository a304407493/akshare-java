@echo off
chcp 65001
echo 正在启动AKShare Web前端服务...
echo 端口号: 9876
echo.

cd /d "%~dp0"
echo 请先运行 npm install 安装依赖（首次运行需要）
echo.
npm run dev

pause
