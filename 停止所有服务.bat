@echo off
chcp 65001 >nul
echo ========================================
echo 停止 SmartMix 所有服务
echo ========================================
echo.

echo [1/2] 停止 Docker 容器...
cd /d "%~dp0deploy"
docker-compose down

echo.
echo [2/2] 提示：如果后端和前端还在运行，
echo       请在各自的窗口中按 Ctrl+C 停止
echo.
echo ✅ Docker 服务已停止！
echo.

pause
