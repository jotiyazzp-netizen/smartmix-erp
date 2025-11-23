@echo off
chcp 65001 >nul
echo ========================================
echo 启动 SmartMix Web 前端
echo ========================================
echo.

cd /d "%~dp0web"

echo 正在启动前端开发服务器...
echo 启动后请在浏览器中访问显示的地址
echo.

npm run dev

pause
