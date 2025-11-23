# SmartMix 智能生产与成本执行系统

这是 SmartMix 项目的主目录，包含后端、前端和移动端的完整代码。

## 🚀 快速开始（本地运行）

### 方式一：使用一键启动脚本（推荐）

1. **双击运行 `快速启动.bat`**
   - 这将自动启动 MySQL 和 Redis 数据库

2. **双击运行 `启动后端.bat`**
   - 在新窗口中启动后端服务
   - 等待看到 "Started SmartMixBackendApplication" 消息

3. **双击运行 `启动前端.bat`**
   - 在新窗口中启动前端服务
   - 浏览器会自动打开 `http://localhost:5173`

4. **使用默认账号登录**
   - 用户名：`admin`
   - 密码：`admin123`

5. **停止服务**
   - 双击运行 `停止所有服务.bat`

### 方式二：手动启动

详细步骤请参考 → [**LOCAL_SETUP.md**](./LOCAL_SETUP.md)

---

## 📦 项目结构

```
3/
├── backend/              # 后端 Spring Boot 项目
├── web/                  # Web 前端 Vue 3 项目
├── mobile/               # Flutter 移动端项目
├── deploy/               # 部署配置和脚本
├── docs/                 # 文档
├── 快速启动.bat          # 一键启动数据库
├── 启动后端.bat          # 启动后端服务
├── 启动前端.bat          # 启动前端服务
├── 停止所有服务.bat      # 停止所有服务
└── LOCAL_SETUP.md        # 本地开发完整指南
```

---

## 🌐 公网部署

### Web 端部署到阿里云

1. 准备阿里云 ECS 服务器
2. 配置 `deploy/.env.production` 文件
3. 运行 `deploy/deploy.sh` 脚本

详细步骤请参考 → `deploy/DEPLOYMENT.md`（即将创建）

### Android 端打包

```bash
cd mobile
flutter build apk --release --dart-define=PRODUCTION=true
```

APK 文件位置：`mobile/build/app/outputs/flutter-apk/app-release.apk`

### iOS 端打包

需要 macOS 环境和 Apple 开发者账号：

```bash
cd mobile
flutter build ios --release --dart-define=PRODUCTION=true
```

---

## 📚 核心功能

1. **材料主数据管理**
   - 从 ERP 同步材料信息和价格
   - 支持手动维护材料数据

2. **智能配比库**
   - 创建和管理混凝土配比方案
   - 配比审核流程
   - 配比复制功能

3. **成本优化引擎**
   - 基于实时材料价格计算配比成本
   - 自动推荐最优配比

4. **生产任务管理**
   - 接收 ERP 生产任务
   - 配比选择和任务下发
   - PDF 任务单生成

---

## 🔧 技术栈

- **后端**: Java 17, Spring Boot 3.x, MySQL, Redis, Maven
- **Web 前端**: Vue 3, TypeScript, Vite, Ant Design Vue
- **移动端**: Flutter 3.x (Android & iOS)
- **部署**: Docker, Docker Compose, Nginx

---

## ❓ 遇到问题？

### 常见问题

1. **端口被占用** → 参考 `LOCAL_SETUP.md` 的"常见问题排查"章节
2. **Docker 无法启动** → 确保 Docker Desktop 正在运行
3. **数据库连接失败** → 检查 MySQL 容器状态

### 获取帮助

- 查看详细文档：`LOCAL_SETUP.md`
- 检查日志文件
- 联系技术支持

---

## 📝 下一步

- ✅ 在本地运行系统并验证功能
- 📋 准备生产环境配置
- 🚀 部署到阿里云
- 📱 打包移动端应用

---

**祝您使用愉快！** 🎉
