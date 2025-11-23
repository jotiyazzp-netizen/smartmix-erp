---
description: gen-backend-mvp
---

---

trigger: gen-backend-mvp
description: 生成 SmartMix 阶段一后端（Java + Spring Boot，与 SAP 风格 ERP 实时推送集成）
---

> 建议在 Antigravity 中使用模型：**Claude Sonnet 4.5 (Thinking)** 执行本 Workflow。

在 `/backend` 目录下，为 SmartMix 阶段一生成完整可运行的后端工程。

要求：

- 严格实现阶段一四个模块：ERP 数据同步、智能配比库管理、成本优化推荐引擎、生产任务与 PDF 模拟下发；  
- 与技术功能框图中 B1/B2/B3 模块对应；  
- 与 SAP 风格 ERP 的集成采用“ERP → 新系统推送式 Webhook”，不允许拉取式同步。

---

## 一、工程初始化

1. 使用 Maven 创建 Spring Boot 3.x 工程：
   - groupId: `com.company.smartmix`
   - artifactId: `smartmix-backend`  
   - 主类：`com.company.smartmix.SmartMixApplication`。

2. 引入依赖：
   - spring-boot-starter-web  
   - spring-boot-starter-data-jpa 或 mybatis-plus-boot-starter  
   - spring-boot-starter-validation  
   - spring-boot-starter-security  
   - spring-boot-starter-data-redis  
   - mysql-connector-j 或 postgresql  
   - springdoc-openapi-starter-webmvc-ui  
   - lombok  
   - thymeleaf  
   - openhtmltopdf-pdfbox  

3. 在 `src/main/resources/application.yml` 中配置：

   ~~~yaml
   spring:
     datasource:
       url: ${SPRING_DATASOURCE_URL}
       username: ${SPRING_DATASOURCE_USERNAME}
       password: ${SPRING_DATASOURCE_PASSWORD}
     jpa:
       hibernate:
         ddl-auto: update
       show-sql: true
     redis:
       host: ${SPRING_REDIS_HOST:redis}
       port: ${SPRING_REDIS_PORT:6379}

   app:
     jwt:
       secret: ${APP_JWT_SECRET:change-me}
     erp:
       webhook-token: ${ERP_WEBHOOK_TOKEN:change-me}
   ~~~

4. 包结构参考：

   ~~~text
   com.company.smartmix
   ├── common/
   ├── config/
   ├── auth/
   ├── erp/
   ├── material/
   ├── mix/
   ├── cost/
   ├── task/
   ├── site/
   └── infra/
   ~~~

---

## 二、模块一：ERP 数据同步（ERP → 新系统）

### 1. Webhook 接口（SAP 风格）

在 `com.company.smartmix.erp` 包中实现：

1）`POST /api/erp/materials` —— 同步材料主数据  

- Header：`X-ERP-TOKEN` 用于鉴权；  
- Request Body：数组，每项包含：

  ~~~json
  {
    "materialCode": "CEM001",
    "description": "P.O 42.5R 水泥",
    "spec": "42.5R",
    "baseUnit": "KG",
    "plantCode": "1000"
  }
  ~~~

- 逻辑：根据 `materialCode + plantCode` upsert `Material` 实体。

2）`POST /api/erp/material-prices` —— 同步材料价格  

- Request Body：数组，每项包含：

  ~~~json
  {
    "materialCode": "CEM001",
    "plantCode": "1000",
    "price": 420,
    "priceUnit": "YuanPerTon",
    "currency": "CNY",
    "effectiveFrom": "2025-01-01T00:00:00",
    "sourceSystem": "SAP-MM"
  }
  ~~~

- 逻辑：  
  - 根据 `materialCode + plantCode` 查找 `Material`；  
  - 写入 `MaterialPrice`，执行 `isCurrent` 切换和 `pricePerKg` 计算；  
  - 记录 `SyncLog`。

3）`POST /api/erp/production-tasks` —— 同步生产任务  

- Request Body 示例：

  ~~~json
  {
    "taskNo": "PRD-20250101-001",
    "projectName": "XX 工程",
    "strengthGrade": "C30",
    "slumpRequirement": "180±20",
    "volume": 300,
    "specialRequirements": "抗渗 S8",
    "sapSalesOrderNo": "50000001",
    "sapProductionOrderNo": "10000001"
  }
  ~~~

- 逻辑：  
  - 若 `taskNo` 已存在则更新任务信息；  
  - 否则创建新任务：`sourceSystem=SAP`, `status=NEW`；  
  - 记录 `SyncLog`。

### 2. 实体

按 `smartmix_project.md` 中的定义实现以下实体与表结构：

- `Material`、`MaterialPrice`、`ProductionTask`、`SyncLog`；  
- 使用 JPA Entity + Repository 或 MyBatis-Plus Mapper + XML。

---

## 三、模块二：智能配比库管理（B1）

### 1. 实体

- `MixRecipe`（配比主表）；  
- `MixRecipeItem`（配比明细表）。

字段及状态枚举需符合规则文件描述（强度等级、坍落度、PENDING_APPROVAL/APPROVED/DISABLED 等）。

### 2. REST API

- `GET /api/mix/recipes`：分页查询配比（支持按 `strengthGrade` 和 `status` 过滤）；  
- `GET /api/mix/recipes/{id}`：查询配比详情（含材料清单）；  
- `POST /api/mix/recipes`：创建配比（初始状态 `PENDING_APPROVAL`）；  
- `PUT /api/mix/recipes/{id}`：编辑配比（仅待审核状态允许）；  
- `POST /api/mix/recipes/{id}/approve`：审核通过（状态改为 `APPROVED`）；  
- `POST /api/mix/recipes/{id}/disable`：停用配比；  
- `POST /api/mix/recipes/{id}/copy`：复制配比为新记录，编号自动生成或在旧编号基础上附加后缀，状态 `PENDING_APPROVAL`。

---

## 四、模块三：成本优化推荐引擎（B2）

- 接口：`GET /api/cost/recommendations?strengthGrade={grade}&volume={volume}`

### 1. 逻辑步骤

1）根据 `grade` 从 `MixRecipe` 中筛选 `status=APPROVED` 的配比；  
2）对每个配比：

- 查询其 `MixRecipeItem` 列表；  
- 根据每个材料的 `MaterialPrice.isCurrent=true` 获取最新 `pricePerKg`；  
- 若有材料缺少当前价格，可将该配比标记为 `priceIncomplete` 并从结果中过滤；  
- 计算单方成本：`unitCost = Σ(dosagePerM3 × pricePerKg)`；  
- 计算总成本：`totalCost = unitCost × volume`。  

3）将所有有效配比按 `unitCost` 升序排序，首个设置 `isBest=true`。  

4）返回 JSON 列表，字段包括：

- `mixRecipeId`、`mixRecipeCode`、`strengthGrade`、`slump`；  
- `unitCost`、`totalCost`；  
- `isBest`；  
- `materialDetails`：每条包含 `materialCode`、`materialName`、`dosagePerM3`、`unitPrice`、`costPerM3`。

---

## 五、模块四：生产任务与 PDF 模拟下发（B3）

实现任务管理及 PDF 输出接口：

- `GET /api/tasks`：分页查询；  
- `GET /api/tasks/{id}`：获取任务详情（含选定配比和成本信息）；  
- `POST /api/tasks`：创建任务（`sourceSystem=MANUAL`，`status=NEW`）；  
- `POST /api/tasks/{id}/select-mix`：选择配比并重新计算成本字段；  
- `GET /api/tasks/{id}/pdf`：生成并返回 PDF 任务单，`Content-Type: application/pdf`。

### 1. PDF 内容要求

- 任务信息：任务号、工程名称、强度等级、坍落度、需求方量、特殊要求、创建人/时间、来源（ERP/手工）；  
- 选定配比：配比编号、强度等级；  
- 材料清单：每种材料的单方用量与总用量；  
- 成本信息：单方成本与总成本。

实现方式：使用 Thymeleaf 模板生成 HTML，再用 openhtmltopdf 生成 PDF 字节流。

---

## 六、安全与 API 文档

1. 实现 `/api/auth/login` 登录接口（用户名+密码），返回 JWT；  
2. 使用 Spring Security + JWT 保护接口：  
   - `/api/auth/**` 与 `/api/erp/**` 允许匿名或单独验证；  
   - 其他 `/api/**` 接口需 `Authorization: Bearer <token>`；  
3. 实现统一响应结构 `ApiResponse<T>`；  
4. 使用全局异常处理器捕获校验异常、业务异常和系统异常；  
5. 集成 springdoc-openapi，提供 Swagger UI（例如 `/swagger-ui/index.html`）。

> 本 Workflow 只修改 `/backend` 目录下文件，不修改 `/web`、`/mobile`、`/deploy` 等目录。
