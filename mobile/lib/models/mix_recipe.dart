class MixRecipe {
  final int id;
  final String recipeCode;
  final String strengthGrade;
  final String? slump;
  final String status;

  MixRecipe({
    required this.id,
    required this.recipeCode,
    required this.strengthGrade,
    this.slump,
    required this.status,
  });

  factory MixRecipe.fromJson(Map<String, dynamic> json) {
    return MixRecipe(
      id: json['id'] as int,
      recipeCode: json['recipeCode'] as String,
      strengthGrade: json['strengthGrade'] as String,
      slump: json['slump'] as String?,
      status: json['status'] as String,
    );
  }
}