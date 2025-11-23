class Task {
  final int id;
  final String taskNumber;
  final String projectName;
  final String strengthGrade;
  final double volume;
  final String? slump;
  final String source;
  final String status;
  final String? selectedRecipeNumber;
  final double? unitCost;
  final double? totalCost;
  final String createTime;

  Task({
    required this.id,
    required this.taskNumber,
    required this.projectName,
    required this.strengthGrade,
    required this.volume,
    this.slump,
    required this.source,
    required this.status,
    this.selectedRecipeNumber,
    this.unitCost,
    this.totalCost,
    required this.createTime,
  });

  factory Task.fromJson(Map<String, dynamic> json) {
    final id = json['id'] as int;
    final taskNumber = (json['taskNumber'] ?? json['taskNo']) as String;
    final projectName = json['projectName'] as String;
    final strengthGrade = json['strengthGrade'] as String;
    final volume = (json['volume'] as num).toDouble();
    final slump = json['slump'] ?? json['slumpRequirement'];
    final source = (json['source'] ?? json['sourceSystem']) as String;
    final status = (json['status'] as String);
    final selectedRecipeNumber = json['selectedRecipeNumber'];
    final unitCost = (json['unitCost'] ?? json['theoreticalUnitCost']);
    final totalCost = (json['totalCost'] ?? json['theoreticalTotalCost']);
    final createTime = (json['createTime'] ?? json['createdAt']) as String;

    return Task(
      id: id,
      taskNumber: taskNumber,
      projectName: projectName,
      strengthGrade: strengthGrade,
      volume: volume,
      slump: slump as String?,
      source: source,
      status: status,
      selectedRecipeNumber: selectedRecipeNumber as String?,
      unitCost: unitCost != null ? (unitCost as num).toDouble() : null,
      totalCost: totalCost != null ? (totalCost as num).toDouble() : null,
      createTime: createTime,
    );
  }
}
