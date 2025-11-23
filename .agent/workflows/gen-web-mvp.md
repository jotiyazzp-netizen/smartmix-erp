---
description: gen-web-mvp
---

---

trigger: gen-web-mvp
description: 生成 SmartMix 阶段一 Web 管理后台（Vite + Vue 3 + TS + Ant Design Vue/Element Plus）
---

> 建议在 Antigravity 中使用模型：**Gemini 3 Pro (High)** 执行本 Workflow。

在 `/web` 目录下，为 SmartMix 阶段一生成 Web 管理后台，实现：

1. ERP 数据同步结果查看；  
2. 智能配比库管理（B1）；  
3. 成本优化推荐引擎结果展示（B2）；  
4. 生产任务与 PDF 模拟下发（B3）。

---

## 一、工程初始化

1. 使用 Vite 创建 Vue 3 + TypeScript 项目；  
2. 安装 Ant Design Vue 或 Element Plus（根据项目统一约定使用其一）；  
3. 配置路径别名 `@` → `src`；  
4. 在 `.env` 创建：

   ~~~env
   VITE_API_BASE_URL=http://localhost:8080/api
   ~~~

5. 建立基础目录结构：

   ~~~text
   src/
   ├── api/
   ├── router/
   ├── store/
   ├── views/
   │   ├── auth/
   │   ├── dashboard/
   │   ├── materials/
   │   ├── mix/
   │   └── tasks/
   ├── components/
   └── types/
   ~~~

6. 创建 `src/api/http.ts` 封装 axios：
   - baseURL 使用 `VITE_API_BASE_URL`;  
   - 请求拦截器从 Pinia 或 localStorage 读取 JWT；  
   - 响应拦截器统一处理错误和 401。

7. 创建 Pinia 用户 store（`src/store/user.ts`）和 Vue Router（`src/router/index.ts`）：
   - 路由：`/login`, `/dashboard`, `/materials`, `/mix/recipes`, `/tasks`；  
   - 路由守卫：未登录访问业务页时跳到 `/login`。

---

## 二、页面与业务模块

### 1. 登录页 `/login`

- 表单：用户名、密码；  
- 调用 `/api/auth/login`，成功后保存 token + 角色信息，跳转 `/dashboard`；  
- 失败时弹出 `message.error`。

### 2. 仪表盘 `/dashboard`

- 展示：
  - 今日任务数、本周任务数（调用 `/api/tasks` 或专门统计接口）；  
  - 常用强度等级（如 C30）的当前最低单方成本（调用 `/api/cost/recommendations?strengthGrade=C30&volume=1`）；  
  - 最近一次 ERP 数据同步时间及状态（由后端提供接口）。

### 3. 材料与价格页 `/materials`（模块一）

- 表格列：
  - 物料编码（MATNR，`materialCode`）；  
  - 物料描述（`description`）；  
  - 工厂（WERKS，`plantCode`）；  
  - 单位（MEINS，`baseUnit`）；  
  - 当前价（元/公斤）；  
  - 价格来源（`sourceSystem`）。

- 功能：
  - 顶部搜索框按物料编码/描述进行模糊搜索；  
  - 点击一行弹出 Modal，展示该物料历史价格列表（有效期、原始价格/单位、折算后 `pricePerKg`、币种）。

### 4. 智能配比库管理 `/mix/recipes`（模块二：B1）

- 列表字段：
  - 配比编号；  
  - 强度等级；  
  - 坍落度；  
  - 状态；  
  - 创建人；  
  - 创建时间。

- 筛选项：  
  - 强度等级；  
  - 状态。

- 操作（按角色显示）：
  - 查看详情；  
  - 新建配比；  
  - 编辑（仅待审核）；  
  - 审核通过；  
  - 停用；  
  - 复制。

- 新建/编辑页面：
  - 表单：配比编号、强度等级、坍落度、备注；  
  - 材料清单：  
    - 下拉选择物料（调用 `/api/materials`）；  
    - 显示物料描述和单位；  
    - 输入单方用量（kg/m³）；  
    - 支持增删行；  
    - 底部展示合计信息（如总水泥用量等）。

### 5. 生产任务与成本推荐 `/tasks`（模块三+四：B2+B3）

- 列表字段：
  - 任务号；  
  - 工程名称；  
  - 强度等级；  
  - 需求方量；  
  - 来源（ERP/手工）；  
  - 状态；  
  - 选定配比编号；  
  - 单方成本；  
  - 总成本；  
  - 创建时间。

- 操作：

  1. **创建生产任务**（DISPATCH/ADMIN）  
     - 表单字段：任务号、工程名称、强度等级、坍落度、需求方量、特殊要求、SAP 参考号；  
     - 提交 `POST /api/tasks`。

  2. **查看任务详情**  
     - 展示任务所有字段、选定配比信息、理论材料总用量、成本。

  3. **成本推荐**  
     - 点击“成本推荐”：  
       - 调用 `/api/cost/recommendations`；  
       - 弹出表格，显示每个候选配比的：配比编号、单方成本、总成本、主要材料摘要；  
       - 列表按单方成本升序排序；  
       - 第一条高亮并标识“最低成本推荐”；  
       - 选择后调用 `POST /api/tasks/{id}/select-mix`，更新该任务的成本字段和选定配比。

  4. **下载任务单 PDF**  
     - 在列表和详情页提供“下载任务单”按钮，打开 `/api/tasks/{id}/pdf`；  
     - 由浏览器下载或预览。

---

## 三、前端权限控制

- **LAB**：  
  - 对 `/mix/recipes` 拥有新增/编辑/审核/停用/复制权限；  
  - `/tasks` 只读，隐藏创建任务和成本推荐按钮。

- **DISPATCH**：  
  - 对 `/tasks` 拥有创建/成本推荐/下载任务单权限；  
  - `/mix/recipes` 只读。

- **ADMIN**：  
  - 拥有上述全部权限。

---

## 四、要求

- 项目能够通过 `npm install && npm run dev` 正常启动；  
- 所有页面文案使用简体中文；  
- TypeScript 类型定义清晰，关键逻辑处有中文注释；  
- 本 Workflow 只修改 `/web` 目录下文件。
