package com.example.util

/**
 * Shared UI labels for the app's supported languages.
 * Content supplied by editors is not machine-translated by this helper.
 */
object AppStrings {
  private val translations: Map<String, List<String>> = mapOf(
    "tasbih.title" to listOf("ডিজিটাল তাসবীহ ও জিকির", "Digital Tasbih & Dhikr", "السبحة الرقمية والذكر"),
    "tasbih.target" to listOf("লক্ষ্য", "Target", "الهدف"),
    "tasbih.meaning" to listOf("অর্থ", "Bangla meaning", "المعنى البنغالي"),
    "tasbih.completed" to listOf("সম্পন্ন চক্কর", "Rounds completed", "الجولات المكتملة"),
    "tasbih.tap" to listOf("ট্যাপ করুন", "Tap to count", "اضغط للعد"),
    "tasbih.total" to listOf("মোট জিকির", "Total dhikr", "إجمالي الذكر"),
    "tasbih.quote" to listOf("পবিত্র জিকিরে অন্তর নির্মল হয়", "Remembering Allah brings peace to the heart", "بذكر الله تطمئن القلوب"),
    "common.reset" to listOf("রিসেট", "Reset", "إعادة ضبط"),
    "common.close" to listOf("বন্ধ করুন", "Close", "إغلاق"),
    "names.title" to listOf("আল্লাহর ৯৯টি গুণবাচক নাম", "The 99 Names of Allah", "أسماء الله الحسنى"),
    "names.search" to listOf("নাম বা অর্থ দিয়ে খুঁজুন", "Search by name or meaning", "ابحث بالاسم أو المعنى"),
    "names.serial" to listOf("ক্রমিক", "Number", "الرقم"),
    "names.meaning_bn" to listOf("বাংলা অর্থ", "Bangla meaning", "المعنى البنغالي"),
    "names.meaning_en" to listOf("English meaning", "English meaning", "المعنى بالإنجليزية"),
    "names.benefit" to listOf("তাৎপর্য ও ফজিলত", "Significance & virtues", "الأهمية والفضائل"),
    "names.benefit_bangla_only" to listOf("বিস্তারিত ব্যাখ্যা শুধু বাংলায় রয়েছে", "Detailed explanation is available in Bangla only", "الشرح التفصيلي متاح باللغة البنغالية فقط"),
    "calendar.title" to listOf("ইসলামিক হিজরি ক্যালেন্ডার", "Islamic Hijri Calendar", "التقويم الهجري الإسلامي"),
    "calendar.important_days" to listOf("বছরের গুরুত্বপূর্ণ ইসলামিক দিবস ও ফজিলত", "Important Islamic dates and virtues", "المناسبات الإسلامية المهمة وفضائلها")
  )

  fun text(language: String, key: String): String {
    val values = translations[key] ?: return key
    val index = when (language) {
      "bn" -> 0
      "ar" -> 2
      else -> 1
    }
    return values[index]
  }

  fun digits(language: String, value: Any): String {
    val raw = value.toString()
    return when (language) {
      "bn" -> raw.map { if (it in '0'..'9') ('০'.code + (it - '0')).toChar() else it }.joinToString("")
      "ar" -> raw.map { if (it in '0'..'9') ('٠'.code + (it - '0')).toChar() else it }.joinToString("")
      else -> raw
    }
  }
}
