
class PostModel {
  final String id;
  final String titleAr;
  final String titleBn;
  final String titleEn;
  final String subtitleAr;
  final String subtitleBn;
  final String subtitleEn;
  final String bodyAr;
  final String bodyBn;
  final String bodyEn;
  final String categoryId;
  final String? imageUrl;
  final String status;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  PostModel({
    required this.id,
    required this.titleAr,
    required this.titleBn,
    required this.titleEn,
    required this.subtitleAr,
    required this.subtitleBn,
    required this.subtitleEn,
    required this.bodyAr,
    required this.bodyBn,
    required this.bodyEn,
    required this.categoryId,
    this.imageUrl,
    required this.status,
    this.createdAt,
    this.updatedAt,
  });

  static String _str(dynamic value) =>
      value == null ? '' : value.toString();

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

  factory PostModel.fromRealtimeDatabase(
    String id,
    Map<String, dynamic> data,
  ) {
    return PostModel(
      id: id,
      titleAr: _str(data['title_ar'] ?? data['title_arabic'] ?? data['titleAr'] ?? data['titleArabic']),
      titleBn: _str(data['title_bn'] ?? data['titleBn']),
      titleEn: _str(data['title_en'] ?? data['titleEn']),
      subtitleAr: _str(data['subtitle_ar'] ?? data['subtitleAr']),
      subtitleBn: _str(data['subtitle_bn'] ?? data['subtitleBn']),
      subtitleEn: _str(data['subtitle_en'] ?? data['subtitleEn']),
      bodyAr: _str(data['body_ar'] ?? data['bodyAr'] ?? data['mainArabic'] ?? data['title_ar']),
      bodyBn: _str(
        data['body_bn'] ?? data['bodyBn'] ?? data['banglaTranslation'] ?? data['bangla_translation'],
      ),
      bodyEn: _str(data['body_en'] ?? data['bodyEn']),
      categoryId: _str(data['categoryId'] ?? data['category_id'] ?? data['category']),
      imageUrl: data['imageUrl']?.toString(),
      status: _str(data['status']).trim().isEmpty
          ? 'published'
          : _str(data['status']).trim().toLowerCase(),
      createdAt: _date(data['createdAt']),
      updatedAt: _date(data['updatedAt']),
    );
  }

  String localizedTitle(String langCode) {
    switch (langCode.toLowerCase()) {
      case 'ar':
        return titleAr.isNotEmpty ? titleAr : titleEn;
      case 'bn':
        return titleBn.isNotEmpty ? titleBn : titleEn;
      default:
        return titleEn.isNotEmpty
            ? titleEn
            : (titleBn.isNotEmpty ? titleBn : titleAr);
    }
  }

  String localizedSubtitle(String langCode) {
    switch (langCode.toLowerCase()) {
      case 'ar':
        return subtitleAr.isNotEmpty ? subtitleAr : subtitleEn;
      case 'bn':
        return subtitleBn.isNotEmpty ? subtitleBn : subtitleEn;
      default:
        return subtitleEn.isNotEmpty ? subtitleEn : subtitleBn;
    }
  }

  String localizedBody(String langCode) {
    switch (langCode.toLowerCase()) {
      case 'ar':
        return bodyAr.isNotEmpty ? bodyAr : bodyEn;
      case 'bn':
        return bodyBn.isNotEmpty ? bodyBn : bodyEn;
      default:
        return bodyEn.isNotEmpty ? bodyEn : bodyBn;
    }
  }
}
