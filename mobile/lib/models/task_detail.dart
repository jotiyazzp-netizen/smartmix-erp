class TaskDetail {
  final int id;
  final String taskNumber;
  final String projectName;
  final String strengthGrade;
  final String? slump;
  final double volume;
  final String? specialRequirements;
  final String? selectedRecipeNumber;
  final double? unitCost;
  final double? totalCost;
  final String createTime;

  TaskDetail({
    required this.id,
    required this.taskNumber,
    required this.projectName,
    required this.strengthGrade,
    this.slump,
    required this.volume,
    this.specialRequirements,
    this.selectedRecipeNumber,
    this.unitCost,
    this.totalCost,
    required this.createTime,
  });

  factory TaskDetail.fromJson(Map<String, dynamic> json) {
    final id = json['id'] as int;
    final taskNumber = (json['taskNumber'] ?? json['taskNo']) as String;
    final projectName = json['projectName'] as String;
    final strengthGrade = json['strengthGrade'] as String;
    final slump = (json['slump'] ?? json['slumpRequirement']) as String?;
    final volume = (json['volume'] as num).toDouble();
    final specialRequirements = json['specialRequirements'] as String?;
    final selectedRecipeNumber = json['selectedRecipeNumber'] ?? json['sapProductionOrderNo'];
    final unitCost = (json['unitCost'] ?? json['theoreticalUnitCost']);
    final totalCost = (json['totalCost'] ?? json['theoreticalTotalCost']);
    final createTime = (json['createTime'] ?? json['createdAt']) as String;

    return TaskDetail(
      id: id,
      taskNumber: taskNumber,
      projectName: projectName,
      strengthGrade: strengthGrade,
      slump: slump,
      volume: volume,
      specialRequirements: specialRequirements,
      selectedRecipeNumber: selectedRecipeNumber as String?,
      unitCost: unitCost != null ? (unitCost as num).toDouble() : null,
      totalCost: totalCost != null ? (totalCost as num).toDouble() : null,
      createTime: createTime,
    );
  }
}
