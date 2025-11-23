@echo off
chcp 65001 >nul
echo ========================================
echo SmartMix 本地快速启动脚本
echo ========================================
echo.

echo [1/3] 正在启动 MySQL 和 Redis...
cd /d "%~dp0deploy"
docker-compose up -d mysql redis

echo.
echo [2/3] 等待数据库初始化（30秒）...
timeout /t 30 /nobreak >nul

echo.
echo [3/3] 准备启动服务...
echo.
echo ✅ 数据库和缓存已启动！
echo.
echo 📝 接下来请手动执行以下步骤：
echo.
echo 1. 打开新的 PowerShell 窗口，运行后端：
echo    cd C:\Users\Administrator\Desktop\erp\3\backend
echo    mvn spring-boot:run
echo.
echo 2. 打开另一个 PowerShell 窗口，运行前端：
echo    cd C:\Users\Administrator\Desktop\erp\3\web
echo    npm run dev
echo.
echo 3. 在浏览器中访问: http://localhost:5173
echo    默认账号: admin / admin123
echo.
echo ========================================
pause
