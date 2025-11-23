import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../config/api_config.dart';
import '../../api/api_client.dart';
import '../../models/task_detail.dart';
import '../../models/mix_recipe.dart';

class TaskDetailScreen extends StatefulWidget {
  final String taskId;

  const TaskDetailScreen({super.key, required this.taskId});

  @override
  State<TaskDetailScreen> createState() => _TaskDetailScreenState();
}

class _TaskDetailScreenState extends State<TaskDetailScreen> {
  final ApiClient _apiClient = ApiClient();
  TaskDetail? _taskDetail;
  bool _isLoading = false;
  String? _error;
  double? _unitCostInput;

  @override
  void initState() {
    super.initState();
    _loadTaskDetail();
  }

  Future<void> _loadTaskDetail() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final detail = await _apiClient.fetchTaskDetail(int.parse(widget.taskId));
      setState(() {
        _taskDetail = detail;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = '加载失败，请稍后重试';
        _isLoading = false;
      });
    }
  }

  Future<void> _openPdf() async {
    final url = '${ApiConfig.baseUrl}/tasks/${widget.taskId}/pdf';
    final uri = Uri.parse(url);

    try {
      if (await canLaunchUrl(uri)) {
        await launchUrl(uri, mode: LaunchMode.externalApplication);
      } else {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('无法打开 PDF')),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('打开 PDF 失败')),
        );
      }
    }
  }

  Future<void> _chooseMix() async {
    if (_taskDetail == null) return;
    int? selectedId;
    List<MixRecipe> recipes = [];
    int page = 0;
    bool loading = true;

    await showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (ctx) {
        Future<void> loadPage() async {
          loading = true;
          recipes = await _apiClient.fetchApprovedMixRecipes(
            strengthGrade: _taskDetail!.strengthGrade,
            page: page,
            size: 10,
          );
          loading = false;
          if (mounted) setState(() {});
        }

        // 初次加载
        loadPage();

        final unitCtrl = TextEditingController(
          text: _unitCostInput != null ? _unitCostInput!.toStringAsFixed(2) : '',
        );

        return Padding(
          padding: EdgeInsets.only(bottom: MediaQuery.of(ctx).viewInsets.bottom),
          child: StatefulBuilder(builder: (ctx2, setModalState) {
            double? calcTotal;
            if (_unitCostInput != null) {
              calcTotal = _unitCostInput! * _taskDetail!.volume;
            }
            return SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text('选择配比', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      IconButton(
                        onPressed: () async { if (page>0) { page--; await loadPage(); setModalState((){}); } },
                        icon: const Icon(Icons.chevron_left),
                      ),
                      Text('第 ${page+1} 页'),
                      IconButton(
                        onPressed: () async { page++; await loadPage(); setModalState((){}); },
                        icon: const Icon(Icons.chevron_right),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  if (loading) const LinearProgressIndicator(),
                  ...recipes.map((r) => RadioListTile<int>(
                        title: Text('${r.recipeCode}  (${r.strengthGrade})'),
                        subtitle: Text('坍落度: ${r.slump ?? '-'}  状态: ${r.status}'),
                        value: r.id,
                        groupValue: selectedId,
                        onChanged: (v) => setModalState(() { selectedId = v; }),
                      )),
                  const Divider(),
                  TextField(
                    controller: unitCtrl,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: '单方成本 (¥/m³)',
                      border: OutlineInputBorder(),
                    ),
                    onChanged: (v) {
                      final t = double.tryParse(v);
                      setModalState(() { _unitCostInput = t; });
                    },
                  ),
                  const SizedBox(height: 8),
                  if (calcTotal != null)
                    Text('预估总成本: ¥${calcTotal.toStringAsFixed(2)}'),
                  const SizedBox(height: 12),
                  SizedBox(
                    width: double.infinity,
                    height: 44,
                    child: ElevatedButton.icon(
                      icon: const Icon(Icons.done),
                      label: const Text('应用配比'),
                      onPressed: (selectedId == null)
                          ? null
                          : () async {
                              Navigator.of(ctx).pop();
                              setState(() => _isLoading = true);
                              try {
                                final updated = await _apiClient.selectMix(
                                  taskId: _taskDetail!.id,
                                  mixRecipeId: selectedId!,
                                  theoreticalUnitCost: _unitCostInput,
                                );
                                await _loadTaskDetail();
                                if (updated != null && mounted) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(content: Text('配比已应用')),
                                  );
                                }
                              } catch (_) {
                                setState(() => _isLoading = false);
                                if (mounted) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(content: Text('应用配比失败')),
                                  );
                                }
                              }
                            },
                    ),
                  ),
                ],
              ),
            );
          }),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('任务详情'),
        centerTitle: true,
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
                        onPressed: _loadTaskDetail,
                        child: const Text('重试'),
                      ),
                    ],
                  ),
                )
              : _taskDetail == null
                  ? const Center(child: Text('无任务信息'))
                  : SingleChildScrollView(
                      padding: const EdgeInsets.all(16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          _buildInfoCard('基本信息', [
                            _buildInfoRow('任务号', _taskDetail!.taskNumber),
                            _buildInfoRow('工程名称', _taskDetail!.projectName),
                            _buildInfoRow('强度等级', _taskDetail!.strengthGrade),
                            _buildInfoRow('坍落度要求', _taskDetail!.slump ?? '未指定'),
                            _buildInfoRow('需求方量', '${_taskDetail!.volume} m³'),
                          ]),
                          const SizedBox(height: 16),
                          if (_taskDetail!.specialRequirements != null)
                            _buildInfoCard('特殊要求', [
                              Text(_taskDetail!.specialRequirements!),
                            ]),
                          const SizedBox(height: 16),
                          if (_taskDetail!.selectedRecipeNumber != null)
                            _buildInfoCard('配比与成本', [
                              _buildInfoRow('选定配比', _taskDetail!.selectedRecipeNumber ?? '-'),
                              _buildInfoRow(
                                '单方成本',
                                _taskDetail!.unitCost != null
                                    ? '¥${_taskDetail!.unitCost!.toStringAsFixed(2)}'
                                    : '-',
                              ),
                              _buildInfoRow(
                                '总成本',
                                _taskDetail!.totalCost != null
                                    ? '¥${_taskDetail!.totalCost!.toStringAsFixed(2)}'
                                    : '-',
                              ),
                            ]),
                          const SizedBox(height: 12),
                          SizedBox(
                            width: double.infinity,
                            height: 44,
                            child: OutlinedButton.icon(
                              onPressed: _chooseMix,
                              icon: const Icon(Icons.auto_awesome),
                              label: const Text('选择配比并估算成本'),
                            ),
                          ),
                          const SizedBox(height: 24),
                          SizedBox(
                            width: double.infinity,
                            height: 48,
                            child: ElevatedButton.icon(
                              onPressed: _taskDetail!.selectedRecipeNumber == null ? null : _openPdf,
                              icon: const Icon(Icons.picture_as_pdf),
                              label: const Text('查看任务单 (PDF)'),
                            ),
                          ),
                        ],
                      ),
                    ),
    );
  }

  Widget _buildInfoCard(String title, List<Widget> children) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const Divider(),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(
              '$label:',
              style: const TextStyle(color: Colors.grey),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(fontWeight: FontWeight.w500),
            ),
          ),
        ],
      ),
    );
  }
}
