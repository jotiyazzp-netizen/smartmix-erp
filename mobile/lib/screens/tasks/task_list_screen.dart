import 'package:flutter/material.dart';
import '../../api/api_client.dart';
import '../../models/task.dart';
import 'package:provider/provider.dart';
import '../../providers/auth_provider.dart';
import 'package:go_router/go_router.dart';

class TaskListScreen extends StatefulWidget {
  const TaskListScreen({super.key});

  @override
  State<TaskListScreen> createState() => _TaskListScreenState();
}

class _TaskListScreenState extends State<TaskListScreen> {
  final ApiClient _apiClient = ApiClient();
  List<Task> _tasks = [];
  bool _isLoading = false;
  String? _error;
  String? _statusFilter;
  int _page = 0;
  int _size = 10;

  @override
  void initState() {
    super.initState();
    _loadTasks();
  }

  Future<void> _loadTasks() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final tasks = await _apiClient.fetchTasks(status: _statusFilter, page: _page, size: _size);
      setState(() {
        _tasks = tasks;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = '网络异常，请稍后重试';
        _isLoading = false;
      });
    }
  }

  Future<void> _openCreateForm() async {
    final formKey = GlobalKey<FormState>();
    final taskNoCtrl = TextEditingController();
    final projectCtrl = TextEditingController();
    final gradeCtrl = TextEditingController(text: 'C30');
    final slumpCtrl = TextEditingController(text: '160±20mm');
    final volumeCtrl = TextEditingController(text: '30');
    final specialCtrl = TextEditingController();

    await showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (ctx) {
        return Padding(
          padding: EdgeInsets.only(bottom: MediaQuery.of(ctx).viewInsets.bottom),
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Form(
              key: formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text('新建任务', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: taskNoCtrl,
                    decoration: const InputDecoration(labelText: '任务单号', border: OutlineInputBorder()),
                    validator: (v) => (v==null || v.trim().isEmpty) ? '请输入任务单号' : null,
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: projectCtrl,
                    decoration: const InputDecoration(labelText: '工程名称', border: OutlineInputBorder()),
                    validator: (v) => (v==null || v.trim().isEmpty) ? '请输入工程名称' : null,
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: gradeCtrl,
                    decoration: const InputDecoration(labelText: '强度等级', border: OutlineInputBorder()),
                    validator: (v) => (v==null || v.trim().isEmpty) ? '请输入强度等级' : null,
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: slumpCtrl,
                    decoration: const InputDecoration(labelText: '坍落度要求', border: OutlineInputBorder()),
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: volumeCtrl,
                    decoration: const InputDecoration(labelText: '方量 (m³)', border: OutlineInputBorder()),
                    keyboardType: TextInputType.number,
                    validator: (v) {
                      final t = double.tryParse(v ?? '');
                      return (t==null || t<=0) ? '请输入正确的方量' : null;
                    },
                  ),
                  const SizedBox(height: 12),
                  TextFormField(
                    controller: specialCtrl,
                    decoration: const InputDecoration(labelText: '特殊要求', border: OutlineInputBorder()),
                    maxLines: 3,
                  ),
                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    height: 44,
                    child: ElevatedButton.icon(
                      icon: const Icon(Icons.check),
                      label: const Text('提交'),
                      onPressed: () async {
                        if (formKey.currentState?.validate() != true) return;
                        Navigator.of(ctx).pop();
                        setState(() => _isLoading = true);
                        try {
                          final created = await _apiClient.createTask(
                            taskNo: taskNoCtrl.text.trim(),
                            projectName: projectCtrl.text.trim(),
                            strengthGrade: gradeCtrl.text.trim(),
                            slumpRequirement: slumpCtrl.text.trim().isEmpty ? null : slumpCtrl.text.trim(),
                            volume: double.parse(volumeCtrl.text.trim()),
                            specialRequirements: specialCtrl.text.trim().isEmpty ? null : specialCtrl.text.trim(),
                          );
                          await _loadTasks();
                          if (created != null && mounted) {
                            context.push('/tasks/${created.id}');
                          }
                        } catch (_) {
                          setState(() => _isLoading = false);
                          if (mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('创建任务失败')),
                            );
                          }
                        }
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('生产任务'),
        centerTitle: true,
        actions: [
          IconButton(
            tooltip: '退出登录',
            icon: const Icon(Icons.logout),
            onPressed: () async {
              // 退出登录并返回登录页
              try {
                final auth = Provider.of<AuthProvider>(context, listen: false);
                await auth.logout();
                if (mounted) context.go('/login');
              } catch (_) {
                if (mounted) context.go('/login');
              }
            },
          )
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(_error!),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _loadTasks,
                        child: const Text('重试'),
                      ),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _loadTasks,
                  child: _tasks.isEmpty
                      ? ListView(
                          children: [
                            const SizedBox(height: 160),
                            const Icon(Icons.inbox, size: 72, color: Colors.grey),
                            const SizedBox(height: 16),
                            const Center(child: Text('暂无任务')),
                            const SizedBox(height: 24),
                            Center(
                              child: ElevatedButton.icon(
                                onPressed: () async {
                                  setState(() => _isLoading = true);
                                  try {
                                    final created = await _apiClient.createSampleTask();
                                    await _loadTasks();
                                    if (created != null && mounted) {
                                      context.push('/tasks/${created.id}');
                                    }
                                  } catch (_) {
                                    setState(() => _isLoading = false);
                                    if (mounted) {
                                      ScaffoldMessenger.of(context).showSnackBar(
                                        const SnackBar(content: Text('创建示例任务失败')),
                                      );
                                    }
                                  }
                                },
                                icon: const Icon(Icons.add),
                                label: const Text('创建示例任务'),
                              ),
                            ),
                          ],
                        )
                      : Column(
                          children: [
                            Padding(
                              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                              child: Row(
                                children: [
                                  const Text('状态:', style: TextStyle(fontSize: 14)),
                                  const SizedBox(width: 8),
                                  DropdownButton<String>(
                                    value: _statusFilter,
                                    hint: const Text('全部'),
                                    items: const [
                                      DropdownMenuItem(value: null, child: Text('全部')),
                                      DropdownMenuItem(value: 'NEW', child: Text('NEW')),
                                      DropdownMenuItem(value: 'PLANNED', child: Text('PLANNED')),
                                      DropdownMenuItem(value: 'COMPLETED', child: Text('COMPLETED')),
                                    ],
                                    onChanged: (val) async {
                                      setState(() { _statusFilter = val; _page = 0; });
                                      await _loadTasks();
                                    },
                                  ),
                                  const Spacer(),
                                  IconButton(onPressed: () async { if (_page>0) { setState(() => _page--); await _loadTasks(); } }, icon: const Icon(Icons.chevron_left)),
                                  Text('${_page+1}'),
                                  IconButton(onPressed: () async { setState(() => _page++); await _loadTasks(); }, icon: const Icon(Icons.chevron_right)),
                                ],
                              ),
                            ),
                            Expanded(
                              child: ListView.builder(
                                itemCount: _tasks.length,
                                itemBuilder: (context, index) {
                                  final task = _tasks[index];
                                  return Card(
                                    margin: const EdgeInsets.symmetric(
                                      horizontal: 16,
                                      vertical: 8,
                                    ),
                                    child: ListTile(
                                      title: Text(
                                        task.taskNumber,
                                        style: const TextStyle(fontWeight: FontWeight.bold),
                                      ),
                                      subtitle: Column(
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          const SizedBox(height: 4),
                                          Text('工程: ${task.projectName}'),
                                          Text('强度: ${task.strengthGrade} | 方量: ${task.volume} m³'),
                                          Text('状态: ${task.status}'),
                                        ],
                                      ),
                                      trailing: const Icon(Icons.arrow_forward_ios),
                                      onTap: () => context.push('/tasks/${task.id}'),
                                    ),
                                  );
                                },
                              ),
                            ),
                          ],
                        ),
                ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openCreateForm,
        icon: const Icon(Icons.add),
        label: const Text('新建任务'),
      ),
    );
  }
}
