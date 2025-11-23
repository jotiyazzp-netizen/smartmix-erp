---
description: gen-mobile-shell
---

---

trigger: gen-mobile-shell
description: 生成 SmartMix 阶段一 Flutter 移动端壳子（Android + iOS）
---

> 建议在 Antigravity 中使用模型：**Claude Sonnet 4.5 (Thinking)** 执行本 Workflow。

在 `/mobile` 目录中，为 SmartMix 阶段一生成 Flutter 移动端 App 基础壳子，实现：

- 登录；  
- 生产任务列表查看；  
- 生产任务详情查看；  
- 打开 PDF 任务单。

---

## 一、Flutter 工程初始化

1. 在 `/mobile` 执行 `flutter create` 初始化工程；  
2. 在 `pubspec.yaml` 中添加依赖：
   - `dio`
   - `flutter_secure_storage`
   - `provider` 或 `riverpod`
   - `go_router` 或 `auto_route`
   - `url_launcher`

3. 创建目录结构：

   ~~~text
   lib/
   ├── api/
   ├── models/
   ├── screens/
   │   ├── login/
   │   └── tasks/
   ├── providers/
   └── widgets/
   ~~~

---

## 二、API 客户端（lib/api/api_client.dart）

1. 定义后端根地址常量：

   ~~~dart
   const String BASE_API_URL = "http://10.0.2.2:8080/api";
   ~~~

   注释中说明：

   - 本地 Android 模拟器访问宿主机需用 10.0.2.2；  
   - iOS 模拟器/真机需要使用主机 IP；  
   - 部署到阿里云时应改为云端域名或 IP。

2. 基于 `dio` 封装 GET/POST 方法：

   - `baseUrl = BASE_API_URL`；  
   - 从 `flutter_secure_storage` 读取 token，在请求头加 `Authorization: Bearer <token>`；  
   - 对错误进行基础处理（抛异常或返回错误信息）。

3. 暴露函数：

   - `Future<String?> login(String username, String password)`：调用 `POST /api/auth/login`；  
   - `Future<List<Task>> fetchTasks()`：调用 `GET /api/tasks`；  
   - `Future<TaskDetail> fetchTaskDetail(int id)`：调用 `GET /api/tasks/{id}`。

---

## 三、登录页（screens/login/）

- UI：用户名输入框、密码输入框、登录按钮；  

- 行为：

  1. 用户输入用户名/密码，点击登录；  
  2. 调用 `login()`；  
  3. 若成功，保存 token 至 `flutter_secure_storage`，更新 Provider 中用户状态，并跳转到任务列表页；  
  4. 若失败，使用 SnackBar 或 Dialog 显示“登录失败，请检查用户名或密码”。

- App 启动逻辑：  
  - 检查 storage 中是否已有 token；  
  - 若有则直接跳转任务列表；无则进入登录页。

---

## 四、任务列表页（screens/tasks/list）

- 调用 `fetchTasks()` 获取任务列表；  

- 列表项显示：

  - 任务号；  
  - 工程名称；  
  - 强度等级；  
  - 需求方量；  
  - 状态（NEW/CONFIRMED/COMPLETED 等）。

- 支持下拉刷新；  

- 点击任务条目跳转任务详情页，并传递任务 id。

---

## 五、任务详情页（screens/tasks/detail）

- 根据 id 调用 `fetchTaskDetail(id)` 获取详情；  

- 显示：

  - 任务号；  
  - 工程名称；  
  - 强度等级；  
  - 坍落度要求；  
  - 需求方量；  
  - 特殊技术要求；  
  - 选定配比编号（如有）；  
  - 单方成本；  
  - 总成本。

- 提供“查看任务单（PDF）”按钮，实现：

  ~~~dart
  final url = "$BASE_API_URL/tasks/$id/pdf";
  if (await canLaunchUrl(Uri.parse(url))) {
    await launchUrl(Uri.parse(url), mode: LaunchMode.externalApplication);
  }
  ~~~

  由系统浏览器或外部 PDF 应用打开任务单。

---

## 六、其它要求

- 所有界面文案为简体中文；  
- 对网络错误给予友好提示（例如“网络异常，请稍后重试”）；  
- 工程可通过 `flutter pub get` 和 `flutter run` 在模拟器正常运行。

> 本 Workflow 只修改 `/mobile` 目录，不操作其他目录。
