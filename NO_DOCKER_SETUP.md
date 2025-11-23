# SmartMix 不使用 Docker 的本地运行方案

如果您的电脑上没有安装 Docker，可以使用这个替代方案在 Windows 上直接运行 MySQL 和 Redis。

---

## 第一步：安装 MySQL

### 1.1 下载 MySQL

访问 MySQL 官网下载页面：

- <https://dev.mysql.com/downloads/mysql/>
- 或使用国内镜像：<https://mirrors.cloud.tencent.com/mysql/downloads/>

选择 **MySQL Installer for Windows**（推荐下载 MSI 安装包）

### 1.2 安装 MySQL

1. 运行安装程序
2. 选择 "Developer Default" 安装类型
3. 设置 root 密码为：`root_password`（与项目配置一致）
4. 其他选项保持默认

### 1.3 创建数据库

打开 MySQL Command Line Client 或 MySQL Workbench，执行：

```sql
CREATE DATABASE smartmix CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'smartmix'@'localhost' IDENTIFIED BY 'smartmix_password';
GRANT ALL PRIVILEGES ON smartmix.* TO 'smartmix'@'localhost';
FLUSH PRIVILEGES;
```

---

## 第二步：安装 Redis（可选，用于缓存）

### 方案 1：使用 Memurai（Redis for Windows）

1. 访问 <https://www.memurai.com/get-memurai>
2. 下载并安装 Memurai（免费版）
3. 安装后会自动作为 Windows 服务运行

### 方案 2：使用 WSL2 中的 Redis

如果您已经启用了 WSL2：

```bash
# 在 WSL2 中执行
sudo apt update
sudo apt install redis-server
sudo service redis-server start
```

### 方案 3：暂时禁用 Redis（最简单）

修改后端配置文件 `backend/src/main/resources/application.properties`，注释掉 Redis 相关配置：

```properties
# spring.redis.host=localhost
# spring.redis.port=6379
```

---

## 第三步：修改后端数据库配置

编辑 `backend/src/main/resources/application.properties`：

```properties
# 数据库配置（连接本地 MySQL，而不是 Docker 容器）
spring.datasource.url=jdbc:mysql://localhost:3306/smartmix?useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root_password

# 如果使用了 Memurai 或 WSL2 Redis
spring.redis.host=localhost
spring.redis.port=6379
```

---

## 第四步：启动服务

### 4.1 启动后端

```powershell
cd C:\Users\Administrator\Desktop\erp\3\backend
mvn spring-boot:run
```

### 4.2 启动前端

```powershell
cd C:\Users\Administrator\Desktop\erp\3\web
npm run dev
```

---

## 验证

访问 <http://localhost:5173，使用> admin/admin123 登录。

---

## 推荐做法

虽然这个方案可以让系统运行起来，但**强烈建议安装 Docker Desktop**，原因：

1. ✅ 更接近生产环境
2. ✅ 避免污染本地系统
3. ✅ 可以一键启动/停止所有服务
4. ✅ 部署到阿里云时使用相同的 Docker 配置

Docker Desktop 安装很简单，只需要：

1. 下载安装包
2. 运行安装
3. 重启电脑
4. 完成！

安装 Docker 后，就可以使用我们准备好的 `快速启动.bat` 等脚本了。
