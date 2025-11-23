@echo off
chcp 65001 >nul
echo ========================================
echo 启动 SmartMix 后端服务
echo ========================================
echo.

cd /d "%~dp0backend"

echo 正在启动后端服务，请稍候...
echo 首次运行可能需要下载依赖，请耐心等待...
echo.

set SPRING_PROFILES_ACTIVE=default
.\mvnw.cmd spring-boot:run

pause
