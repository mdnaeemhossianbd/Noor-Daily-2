
class CategoryModel {
  final String id;
  final String nameAr;
  final String nameBn;
  final String nameEn;
  final String? parentId;
  final int depth;
  final int order;
  final bool isActive;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  CategoryModel({
    required this.id,
    required this.nameAr,
    required this.nameBn,
    required this.nameEn,
    this.parentId,
    required this.depth,
    required this.order,
    required this.isActive,
    this.createdAt,
    this.updatedAt,
  });

  static DateTime? _date(dynamic value) {
    if (value == null) return null;
    if (value is num) {
      return DateTime.fromMillisecondsSinceEpoch(value.toInt());
    }
    if (value is String) {
      final number = num.tryParse(value);
      if (number != null) {
        return DateTime.fromMillisecondsSinceEpoch(number.toInt());
      }
      return DateTime.tryParse(value);
    }
    return null;
  }

  factory CategoryModel.fromRealtimeDatabase(
    String id,
    Map<String, dynamic> data,
  ) {
    return CategoryModel(
      id: id,
      nameAr: (data['name_ar'] ?? '').toString(),
      nameBn: (data['name_bn'] ?? '').toString(),
      nameEn: (data['name_en'] ?? '').toString(),
      parentId: data['parentId']?.toString(),
      depth: (data['depth'] as num?)?.toInt() ?? 0,
      order: (data['order'] as num?)?.toInt() ?? 0,
      isActive: data['isActive'] as bool? ?? true,
      createdAt: _date(data['createdAt']),
      updatedAt: _date(data['updatedAt']),
    );
  }

  String localizedName(String langCode) {
    switch (langCode.toLowerCase()) {
      case 'ar':
        return nameAr.isNotEmpty ? nameAr : nameEn;
      case 'bn':
        return nameBn.isNotEmpty ? nameBn : nameEn;
      default:
        return nameEn.isNotEmpty
            ? nameEn
            : (nameBn.isNotEmpty ? nameBn : nameAr);
    }
  }
}
