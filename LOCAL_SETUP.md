# SmartMix 本地运行指南

本指南将帮助您在本地计算机上启动和运行 SmartMix 系统，包括后端、前端和数据库。

---

## 前置要求

在开始之前，请确保您的计算机上已安装以下软件：

- ✅ **Java 17** 或更高版本
- ✅ **Node.js 18** 或更高版本
- ✅ **Docker Desktop**（用于运行 MySQL 和 Redis）
- ✅ **Maven 3.9+**（通常 Java 安装时已包含）

### 检查环境

打开 PowerShell 或命令提示符，运行以下命令检查：

```powershell
# 检查 Java 版本
java -version

# 检查 Node.js 版本
node -v

# 检查 npm 版本
npm -v

# 检查 Docker 版本
docker --version
```

---

## 第一步：启动数据库和缓存服务

### 1.1 使用 Docker Compose 启动 MySQL 和 Redis

打开 PowerShell，进入项目的 `deploy` 目录：

```powershell
cd C:\Users\Administrator\Desktop\erp\3\deploy
```

启动 MySQL 和 Redis 容器：

```powershell
docker-compose up -d mysql redis
```

### 1.2 验证容器状态

```powershell
docker ps
```

您应该看到两个正在运行的容器：

- `smartmix-mysql`
- `smartmix-redis`

### 1.3 等待 MySQL 初始化完成

首次启动 MySQL 需要一些时间来初始化数据库，等待约 30 秒，然后检查日志：

```powershell
docker logs smartmix-mysql
```

当您看到类似 `ready for connections` 的消息时，表示 MySQL 已准备就绪。

---

## 第二步：启动后端服务

### 2.1 打开新的 PowerShell 窗口

进入后端目录：

```
Started SmartMixBackendApplication in X.XXX seconds
```

后端将在 `http://localhost:8080` 上运行。

您可以在浏览器中访问：

```
http://localhost:8080/actuator/health
```

如果看到 `{"status":"UP"}`，说明后端健康运行。

---

## 第三步：启动前端服务

### 3.1 打开新的 PowerShell 窗口

进入前端目录：

```powershell
cd C:\Users\Administrator\Desktop\erp\3\web
```

### 3.2 安装依赖（首次运行）

如果还没有安装依赖，运行：

```powershell
npm install
```

### 3.3 启动开发服务器

```powershell
npm run dev
```

### 3.4 访问前端界面

前端开发服务器通常会在 `http://localhost:5173` 上运行（Vite 默认端口）。

启动后，您会看到类似以下的输出：

```
VITE v5.x.x  ready in xxx ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

在浏览器中打开 `http://localhost:5173`，您应该能看到 SmartMix 的登录页面。

---

## 第四步：登录并测试系统

### 4.1 使用默认账户登录

后端启动时会自动创建默认账户，您可以使用以下凭据登录：

- **用户名**: `admin`
- **密码**: `admin123`

### 4.2 验证核心功能

登录后，请验证以下功能：

1. ✅ **材料管理**：
   - 点击"材料管理"菜单
   - 查看材料列表
   - 尝试添加一个新材料（如：水泥）

2. ✅ **配比管理**：
   - 点击"配比管理"菜单
   - 查看配比列表
   - 尝试创建一个新配比

3. ✅ **生产任务**：
   - 点击"生产任务"菜单
   - 查看任务列表
   - 尝试创建一个新任务

4. ✅ **成本推荐**：
   - 在创建生产任务时，系统应该自动显示成本最优的配比推荐

---

## 第五步：停止服务

测试完成后，按照以下步骤优雅地停止服务：

### 5.1 停止前端

在运行前端的 PowerShell 窗口中按 `Ctrl + C`

### 5.2 停止后端

在运行后端的 PowerShell 窗口中按 `Ctrl + C`

### 5.3 停止数据库和 Redis

回到 `deploy` 目录：

```powershell
cd C:\Users\Administrator\Desktop\erp\3\deploy
docker-compose down
```

---

## 常见问题排查

### 问题 1：端口被占用

**错误**: `Port 8080 is already in use`

**解决方案**：

```powershell
# 查找占用 8080 端口的进程
netstat -ano | findstr :8080

# 结束该进程（将 PID 替换为实际的进程 ID）
taskkill /PID <PID> /F
```

### 问题 2：Docker 无法启动

**错误**: `Cannot connect to the Docker daemon`

**解决方案**：

- 确保 Docker Desktop 正在运行
- 在系统托盘中找到 Docker 图标，确认状态为"Running"

### 问题 3：后端无法连接数据库

**错误**: `Communications link failure`

**解决方案**：

```powershell
# 检查 MySQL 容器状态
docker ps -a | findstr mysql

# 查看 MySQL 日志
docker logs smartmix-mysql

# 如果容器未运行，重新启动
docker-compose up -d mysql
```

### 问题 4：前端 API 请求失败

**错误**: 浏览器控制台显示 `Network Error` 或 `CORS Error`

**解决方案**：

1. 确认后端已成功启动并在 `http://localhost:8080` 上运行
2. 检查前端配置文件 `web/vite.config.ts` 中的代理设置
3. 清除浏览器缓存并刷新页面

### 问题 5：Maven 下载依赖很慢

**解决方案**：

- 配置 Maven 使用国内镜像源（如阿里云）
- 编辑 `C:\Users\您的用户名\.m2\settings.xml`
- 或者使用项目提供的 settings.xml 配置

---

## 下一步：部署到阿里云

本地验证成功后，您可以：

1. 📋 **准备生产环境配置**：
   - 编辑 `deploy/.env.production` 文件
   - 填写阿里云 RDS、Redis 等服务的连接信息

2. 🚀 **部署到阿里云 ECS**：
   - 参考 `deploy/DEPLOYMENT.md` 文档
   - 使用 `deploy/deploy.sh` 脚本一键部署

3. 📱 **打包移动端应用**：
   - Android：`flutter build apk --release`
   - iOS：`flutter build ios --release`

---

## 技术支持

如果遇到其他问题，请检查：

- **后端日志**：在后端 PowerShell 窗口中查看
- **前端日志**：在浏览器开发者工具的 Console 中查看
- **Docker 日志**：`docker logs <容器名>`

祝您使用愉快！🎉
