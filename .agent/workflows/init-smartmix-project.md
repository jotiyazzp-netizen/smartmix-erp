---
description: init-smartmix-project
---

---

trigger: init-smartmix-project
description: 初始化 SmartMix 项目结构与 SAP ERP 风格技术功能框图文档
---

在当前 Workspace 中，为 **SmartMix 智能生产与成本执行系统** 初始化项目结构和基础文档。

### 一、创建目录结构

在项目根目录中，若不存在则创建：

- `backend/`   ：Java + Spring Boot 后端工程目录；
- `web/`       ：Vue 3 + TypeScript + Ant Design Vue / Element Plus 管理后台目录；
- `mobile/`    ：Flutter 移动端工程目录（一个工程构建 Android APK 和 iOS IPA）；
- `deploy/`    ：部署配置目录（Dockerfile、docker-compose、Nginx、K8s YAML 等）；
- `docs/`      ：业务与技术文档目录。

### 二、创建 docs/smartmix_architecture.md（还原技术功能框图）

在 `docs/` 下创建 `smartmix_architecture.md` 文件，内容需包含：

1. **系统概述**  
   - 用文字描述三个大区域：  
     - A 区：现有 SAP 风格 ERP 系统（采购模块、销售模块、材料主数据与价格、生产任务、ERP 核心数据库）；  
     - B 区：新系统 · 智能生产核心（动态配比库、成本优化引擎、生产调度）；  
     - C 区：生产现场（搅拌楼控制、地磅、车辆）。  

2. **Mermaid 技术功能图**（结构需与图片一致）：

   ~~~mermaid
   flowchart TD
     subgraph A[现有 ERP 系统（SAP 风格）]
       direction LR
       A1[采购模块] --> A2[材料主数据与价格]
       A3[销售模块] --> A4[生产任务]
     end

     subgraph B[新系统：智能生产核心]
       B1[动态配比库]
       B2[成本优化引擎]
       B3[生产调度]
     end

     subgraph C[生产现场]
       direction LR
       C1[搅拌楼控制]
       C2[地磅]
       C3[车辆]
     end

     A2 -- 同步材料价格 --> B2
     A4 -- 同步生产任务 --> B3
     B2 -- 下发优化配比 --> C1
     C1 & C2 -- 采集实际数据 --> B2
     B2 -- 同步理论消耗与成本 --> A5[(ERP 核心数据库)]
   ~~~

   并在图下用列表形式解释每一个节点和箭头。

3. **分阶段实施说明**  
   - 阶段一：数据打通与独立应用（只实现 4 个核心功能模块）；  
   - 阶段二：生产集成；  
   - 阶段三：深度闭环。

4. **技术架构说明**  
   - 描述后端、Web、移动端、部署（Docker/K8s/Nginx + 阿里云 ECS）技术栈；  
   - 指出所有终端通过 HTTP/HTTPS 调用统一后端 API 和数据库。

### 三、创建或更新 README.md

在项目根目录创建/更新 `README.md`，内容包括：

- 项目简介与业务定位（ERP 的生产与成本执行大脑）；  
- 目录结构说明；  
- 技术栈清单；  
- 在 Antigravity 中使用 Workflows 的方式和推荐顺序：
  1. `/init-smartmix-project`
  2. `/gen-backend-mvp`
  3. `/gen-web-mvp`
  4. `/gen-mobile-shell`
  5. `/gen-devops-stack`

### 四、注意事项

- 本 Workflow 不生成任何代码，只创建和更新目录与文档；  
- 文档语言为简体中文。
