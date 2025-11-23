import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'api/api_client.dart';
import 'providers/auth_provider.dart';
import 'screens/login/login_screen.dart';
import 'screens/tasks/task_list_screen.dart';
import 'screens/tasks/task_detail_screen.dart';

void main() {
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider(ApiClient())),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'SmartMix Mobile',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      routerConfig: _router,
      debugShowCheckedModeBanner: false,
    );
  }
}

final _router = GoRouter(
  initialLocation: '/',
  redirect: (context, state) async {
    final authProvider = Provider.of<AuthProvider>(context, listen: false);
    await authProvider.checkAuth();

    final isAuthenticated = authProvider.isAuthenticated;
    final isGoingToLogin = state.uri.path == '/login';

    if (!isAuthenticated && !isGoingToLogin) {
      return '/login';
    }

    if (isAuthenticated && isGoingToLogin) {
      return '/tasks';
    }

    return null;
  },
  routes: [
    GoRoute(
      path: '/',
      redirect: (context, state) => '/login',
    ),
    GoRoute(
      path: '/login',
      builder: (context, state) => const LoginScreen(),
    ),
    GoRoute(
      path: '/tasks',
      builder: (context, state) => const TaskListScreen(),
    ),
    GoRoute(
      path: '/tasks/:id',
      builder: (context, state) {
        final id = state.pathParameters['id']!;
        return TaskDetailScreen(taskId: id);
      },
    ),
  ],
);
