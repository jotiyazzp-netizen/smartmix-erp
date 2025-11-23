import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';
import '../models/task.dart';
import '../models/task_detail.dart';
import '../models/mix_recipe.dart';

class ApiClient {
  final Dio _dio;
  final FlutterSecureStorage _storage;

  ApiClient()
      : _dio = Dio(BaseOptions(
          baseUrl: ApiConfig.baseUrl,
          connectTimeout: Duration(seconds: ApiConfig.connectTimeout),
          receiveTimeout: Duration(seconds: ApiConfig.receiveTimeout),
          sendTimeout: Duration(seconds: ApiConfig.sendTimeout),
        )),
        _storage = const FlutterSecureStorage() {
    // 打印配置信息（仅调试模式）
    ApiConfig.printConfig();
    
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _storage.read(key: 'token');
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
      onError: (error, handler) {
        return handler.next(error);
      },
    ));
  }

  /// 登录
  Future<String?> login(String username, String password) async {
    try {
      final response = await _dio.post('/auth/login', data: {
        'username': username,
        'password': password,
      });

      if (response.statusCode == 200) {
        final data = response.data;
        String? token;
        if (data is Map<String, dynamic>) {
          final inner = data['data'];
          if (inner is Map<String, dynamic>) {
            token = inner['token'] as String?;
          } else {
            token = data['token'] as String?;
          }
        }
        if (token != null) {
          await _storage.write(key: 'token', value: token);
          return token;
        }
      }
      return null;
    } catch (e) {
      rethrow;
    }
  }

  /// 获取任务列表
  Future<List<Task>> fetchTasks({String? status, int page = 0, int size = 10}) async {
    try {
      final response = await _dio.get('/tasks', queryParameters: {
        if (status != null && status.isNotEmpty) 'status': status,
        'page': page,
        'size': size,
      });
      if (response.statusCode == 200) {
        final body = response.data;
        List<dynamic>? list;
        if (body is Map<String, dynamic>) {
          final data = body['data'];
          if (data is Map<String, dynamic>) {
            // Spring Page payload
            list = data['content'] as List<dynamic>?;
          }
        }
        if (list != null) {
          return list.map((json) => Task.fromJson(json as Map<String, dynamic>)).toList();
        }
      }
      return [];
    } catch (e) {
      rethrow;
    }
  }

  /// 获取任务详情
  Future<TaskDetail> fetchTaskDetail(int id) async {
    try {
      final response = await _dio.get('/tasks/$id');
      if (response.statusCode == 200) {
        final body = response.data;
        if (body is Map<String, dynamic>) {
          final inner = body['data'];
          if (inner is Map<String, dynamic>) {
            return TaskDetail.fromJson(inner);
          }
        }
      }
      throw Exception('获取任务详情失败');
    } catch (e) {
      rethrow;
    }
  }

  /// 创建示例任务
  Future<TaskDetail?> createSampleTask() async {
    try {
      final ts = DateTime.now();
      final taskNo = 'SM-${ts.year}${ts.month.toString().padLeft(2,'0')}${ts.day.toString().padLeft(2,'0')}-${ts.millisecondsSinceEpoch % 100000}';
      final resp = await _dio.post('/tasks', data: {
        'taskNo': taskNo,
        'projectName': '示例工程',
        'strengthGrade': 'C30',
        'slumpRequirement': '160±20mm',
        'volume': 30,
        'specialRequirements': '示例任务自动生成',
      });
      if (resp.statusCode == 200 && resp.data is Map<String,dynamic>) {
        final inner = resp.data['data'];
        if (inner is Map<String,dynamic>) {
          return TaskDetail.fromJson(inner);
        }
      }
      return null;
    } catch (e) {
      rethrow;
    }
  }

  /// 创建任务（表单）
  Future<TaskDetail?> createTask({
    required String taskNo,
    required String projectName,
    required String strengthGrade,
    String? slumpRequirement,
    required double volume,
    String? specialRequirements,
  }) async {
    try {
      final resp = await _dio.post('/tasks', data: {
        'taskNo': taskNo,
        'projectName': projectName,
        'strengthGrade': strengthGrade,
        'slumpRequirement': slumpRequirement,
        'volume': volume,
        'specialRequirements': specialRequirements,
      });
      if (resp.statusCode == 200 && resp.data is Map<String,dynamic>) {
        final inner = resp.data['data'];
        if (inner is Map<String,dynamic>) {
          return TaskDetail.fromJson(inner);
        }
      }
      return null;
    } catch (e) {
      rethrow;
    }
  }

  /// 查询已审核配比（可按强度筛选，分页）
  Future<List<MixRecipe>> fetchApprovedMixRecipes({String? strengthGrade, int page = 0, int size = 10}) async {
    try {
      final resp = await _dio.get('/mix/recipes', queryParameters: {
        if (strengthGrade != null && strengthGrade.isNotEmpty) 'strengthGrade': strengthGrade,
        'status': 'APPROVED',
        'page': page,
        'size': size,
      });
      if (resp.statusCode == 200) {
        final body = resp.data;
        if (body is Map<String,dynamic>) {
          final data = body['data'];
          if (data is Map<String,dynamic>) {
            final list = data['content'] as List<dynamic>?;
            if (list != null) {
              return list.map((e) => MixRecipe.fromJson(e as Map<String,dynamic>)).toList();
            }
          }
        }
      }
      return [];
    } catch (e) {
      rethrow;
    }
  }

  /// 为任务选择配比并设置单方成本（后端将计算总成本）
  Future<TaskDetail?> selectMix({required int taskId, required int mixRecipeId, double? theoreticalUnitCost}) async {
    try {
      final resp = await _dio.post('/tasks/$taskId/select-mix', data: {
        'mixRecipeId': mixRecipeId,
        if (theoreticalUnitCost != null) 'theoreticalUnitCost': theoreticalUnitCost,
      });
      if (resp.statusCode == 200 && resp.data is Map<String,dynamic>) {
        final inner = resp.data['data'];
        if (inner is Map<String,dynamic>) {
          return TaskDetail.fromJson(inner);
        }
      }
      return null;
    } catch (e) {
      rethrow;
    }
  }

  /// 获取 Token
  Future<String?> getToken() async {
    return await _storage.read(key: 'token');
  }

  /// 清除 Token (登出)
  Future<void> clearToken() async {
    await _storage.delete(key: 'token');
  }
}
