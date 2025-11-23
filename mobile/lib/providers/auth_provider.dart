import 'package:flutter/material.dart';
import '../api/api_client.dart';

class AuthProvider extends ChangeNotifier {
  final ApiClient _apiClient;
  String? _token;
  bool _isAuthenticated = false;

  AuthProvider(this._apiClient);

  bool get isAuthenticated => _isAuthenticated;

  Future<void> checkAuth() async {
    _token = await _apiClient.getToken();
    _isAuthenticated = _token != null;
    notifyListeners();
  }

  Future<bool> login(String username, String password) async {
    try {
      _token = await _apiClient.login(username, password);
      _isAuthenticated = _token != null;
      notifyListeners();
      return _isAuthenticated;
    } catch (e) {
      return false;
    }
  }

  Future<void> logout() async {
    await _apiClient.clearToken();
    _token = null;
    _isAuthenticated = false;
    notifyListeners();
  }
}
