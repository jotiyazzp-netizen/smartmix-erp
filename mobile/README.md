# SmartMix Mobile

SmartMix 移动端应用（阶段一）

## 功能

- 用户登录
- 生产任务列表查看
- 生产任务详情查看
- PDF 任务单查看

## 技术栈

- Flutter
- Dio (HTTP 客户端)
- Provider (状态管理)
- GoRouter (路由)
- FlutterSecureStorage (安全存储)

## 运行

```bash
flutter pub get
flutter run
```

## API 配置

默认后端地址：`http://10.0.2.2:8080/api` (Android 模拟器)

iOS 模拟器/真机需修改为主机 IP 地址。
