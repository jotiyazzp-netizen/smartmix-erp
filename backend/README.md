# SmartMix Backend - README

## 项目简介

SmartMix 智能生产与成本执行系统后端服务，基于 Spring Boot 3.x 开发。

## 技术栈

- **Java**: 17
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **ORM**: Spring Data JPA
- **认证**: JWT + Spring Security
- **API文档**: SpringDoc OpenAPI 3
- **PDF生成**: OpenHTMLToPDF + Thymeleaf

## 快速开始

### 1. 环境准备

确保已安装：

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库准备

创建数据库：

```sql
CREATE DATABASE smartmix DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置环境变量

可以通过环境变量覆盖默认配置：

```bash
# 数据库配置
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/smartmix?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=your_password

# Redis配置
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379
export SPRING_REDIS_PASSWORD=

# JWT密钥（生产环境必须修改）
export APP_JWT_SECRET=your-very-secure-secret-key

# ERP Webhook Token（生产环境必须修改）
export ERP_WEBHOOK_TOKEN=your-erp-webhook-token
```

### 4. 运行项目

```bash
# 进入backend目录
cd backend

# Maven编译并运行
mvn clean spring-boot:run
```

服务将在 `http://localhost:8080` 启动。

### 5. 访问API文档

启动后访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

### 6. 默认账户

系统启动时会自动创建以下默认账户：

- **管理员**: `admin` / `admin123`
- **普通用户**: `user` / `user123`

## 核心模块

### 模块一：ERP数据同步

- `POST /api/erp/materials` - 同步材料主数据
- `POST /api/erp/material-prices` - 同步材料价格
- `POST /api/erp/production-tasks` - 同步生产任务

**注意**: 这些接口需要在请求头中提供 `X-ERP-TOKEN`

### 模块二：智能配比库管理

- `GET /api/mix/recipes` - 分页查询配比
- `GET /api/mix/recipes/{id}` - 查询配比详情
- `POST /api/mix/recipes` - 创建配比
- `PUT /api/mix/recipes/{id}` - 编辑配比
- `POST /api/mix/recipes/{id}/approve` - 审核通过配比
- `POST /api/mix/recipes/{id}/disable` - 停用配比
- `POST /api/mix/recipes/{id}/copy` - 复制配比

### 模块三：成本优化推荐引擎

- `GET /api/cost/recommendations?strengthGrade={grade}&volume={volume}` - 获取成本优化推荐

### 模块四：生产任务与PDF下发

- `GET /api/tasks` - 分页查询生产任务
- `GET /api/tasks/{id}` - 查询任务详情
- `POST /api/tasks` - 创建生产任务
- `POST /api/tasks/{id}/select-mix` - 选择配比
- `GET /api/tasks/{id}/pdf` - 生成PDF任务单

## 认证

### 登录获取Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "username": "admin",
    "realName": "系统管理员",
    "role": "ADMIN"
  }
}
```

### 使用Token访问受保护接口

在请求头中添加：

```
Authorization: Bearer {token}
```

## 项目结构

```
com.company.smartmix
├── auth/              # 用户认证与授权
├── common/            # 通用类（异常、响应等）
├── config/            # 配置类
├── cost/              # 成本优化引擎
├── erp/               # ERP集成
├── material/          # 材料管理
├── mix/               # 配比管理
├── task/              # 生产任务管理
└── SmartMixApplication.java
```

## 生产部署建议

1. **修改默认密钥**: 务必修改 JWT 密钥和 ERP Webhook Token
2. **使用环境变量**: 不要在配置文件中硬编码敏感信息
3. **启用HTTPS**: 生产环境必须启用HTTPS
4. **数据库连接池**: 根据负载调整连接池大小
5. **日志管理**: 配置日志输出到文件并定期轮转
6. **监控**: 添加应用性能监控（APM）

## 许可证

Copyright © 2024 SmartMix Team
