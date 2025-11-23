import 'package:flutter/foundation.dart';

/// API 环境配置
class ApiConfig {
  // 构建环境标识（编译时确定）
  static const bool isProduction = bool.fromEnvironment('PRODUCTION', defaultValue: false);
  
  // API 基础 URL
  // 开发环境：使用本地地址或开发服务器
  // 生产环境：使用生产服务器地址
  static String get baseUrl {
    if (isProduction) {
      // 生产环境 - 请修改为您的服务器地址
      // 示例：
      // - 使用域名: return 'https://api.smartmix.com';
      // - 使用 IP: return 'http://123.45.67.89/api';
      return 'http://YOUR_SERVER_IP_OR_DOMAIN/api';
    } else {
      // 开发环境
      if (defaultTargetPlatform == TargetPlatform.android) {
        // Android 模拟器访问宿主机使用 10.0.2.2
        return 'http://10.0.2.2:8080/api';
      } else if (defaultTargetPlatform == TargetPlatform.iOS) {
        // iOS 模拟器/真机需要使用主机实际 IP
        // 请修改为您的开发机器 IP 地址
        return 'http://192.168.1.100:8080/api';
      } else {
        // 其他平台（Web、Desktop）
        return 'http://localhost:8080/api';
      }
    }
  }
  
  // 连接超时（秒）
  static const int connectTimeout = 15;
  
  // 接收超时（秒）
  static const int receiveTimeout = 15;
  
  // 发送超时（秒）
  static const int sendTimeout = 15;
  
  // 打印当前配置信息（仅用于调试）
  static void printConfig() {
    if (kDebugMode) {
      print('=== API Configuration ===');
      print('Environment: ${isProduction ? "PRODUCTION" : "DEVELOPMENT"}');
      print('Base URL: $baseUrl');
      print('Connect Timeout: ${connectTimeout}s');
      print('Receive Timeout: ${receiveTimeout}s');
      print('========================');
    }
  }
}
