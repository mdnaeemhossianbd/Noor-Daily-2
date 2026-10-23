package com.example.model

data class IslamicPost(
  val id: String,
  val categoryId: String,
  val subcategoryId: String? = null,
  val categoryNameEn: String,
  val categoryNameBn: String,
  val categoryNameAr: String,
  val subcategoryNameEn: String? = null,
  val subcategoryNameBn: String? = null,
  val subcategoryNameAr: String? = null,
  val titleEn: String,
  val titleBn: String,
  val titleAr: String,
  val subtitleEn: String,
  val subtitleBn: String,
  val subtitleAr: String,
  val bodyEn: String,
  val bodyBn: String,
  val bodyAr: String,
  val source: String,
  val readTimeMinutes: Int,
  val isAyatOfTheDay: Boolean = false,
  val isDailyHadith: Boolean = false,
) {
  fun localizedTitle(lang: String): String = when (lang) {
    "bn" -> titleBn.ifEmpty { titleEn }
    "ar" -> titleAr.ifEmpty { titleEn }
    else -> titleEn.ifEmpty { titleBn }
  }

  fun localizedSubtitle(lang: String): String = when (lang) {
    "bn" -> subtitleBn.ifEmpty { subtitleEn }
    "ar" -> subtitleAr.ifEmpty { subtitleEn }
    else -> subtitleEn.ifEmpty { subtitleBn }
  }

  fun localizedBody(lang: String): String = when (lang) {
    "bn" -> bodyBn.ifEmpty { bodyEn }
    "ar" -> bodyAr.ifEmpty { bodyEn }
    else -> bodyEn.ifEmpty { bodyBn }
  }

  fun localizedCategory(lang: String): String = when (lang) {
    "bn" -> categoryNameBn.ifEmpty { categoryNameEn }
    "ar" -> categoryNameAr.ifEmpty { categoryNameEn }
    else -> categoryNameEn.ifEmpty { categoryNameBn }
  }

  fun localizedSubcategory(lang: String): String? = when (lang) {
    "bn" -> subcategoryNameBn?.ifEmpty { subcategoryNameEn } ?: subcategoryNameEn
    "ar" -> subcategoryNameAr?.ifEmpty { subcategoryNameEn } ?: subcategoryNameEn
    else -> subcategoryNameEn?.ifEmpty { subcategoryNameBn } ?: subcategoryNameBn
  }
}

data class IslamicCategory(
  val id: String,
  val nameEn: String,
  val nameBn: String,
  val nameAr: String,
  val depth: Int = 0,
  val iconName: String = "book",
  val postCount: Int = 0,
  val subcategoryCount: Int = 0,
) {
  fun localizedName(lang: String): String = when (lang) {
    "bn" -> nameBn.ifEmpty { nameEn }
    "ar" -> nameAr.ifEmpty { nameEn }
    else -> nameEn.ifEmpty { nameBn }
  }
}

data class IslamicSubcategory(
  val id: String,
  val categoryId: String,
  val parentSubcategoryId: String? = null,
  val nameEn: String,
  val nameBn: String,
  val nameAr: String = "",
  val iconName: String = "folder",
  val order: Int = 0,
  val postCount: Int = 0,
  val childCount: Int = 0,
) {
  fun localizedName(lang: String): String = when (lang) {
    "bn" -> nameBn.ifEmpty { nameEn }
    "ar" -> nameAr.ifEmpty { nameEn }
    else -> nameEn.ifEmpty { nameBn }
  }
}

data class PrayerSchedule(
  val fajr: String,
  val sunrise: String,
  val dhuhr: String,
  val asr: String,
  val maghrib: String,
  val isha: String,
  val nextPrayerName: String,
  val nextPrayerRemainingMinutes: Long,
  val activePrayerName: String,
)

data class CityLocation(
  val nameEn: String,
  val nameBn: String,
  val nameAr: String,
  val country: String,
  val countryBn: String = "বাংলাদেশ",
  val latitude: Double = 23.8103,
  val longitude: Double = 90.4125,
  val timeZoneId: String = "Asia/Dhaka",
  val fajrTime: String = "04:55 AM",
  val sunriseTime: String = "06:12 AM",
  val dhuhrTime: String = "12:05 PM",
  val asrTimeHanafi: String = "04:15 PM",
  val asrTimeShafi: String = "03:30 PM",
  val maghribTime: String = "05:58 PM",
  val ishaTime: String = "07:15 PM",
)

