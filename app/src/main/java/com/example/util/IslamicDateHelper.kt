package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

import android.os.Build

/**
 * Islamic Date, Gregorian Date, Live Clock, and Hijri Calendar Calculations.
 */
object IslamicDateHelper {

  data class HijriDate(
    val day: Int,
    val monthIndex: Int, // 0-based
    val monthNameEn: String,
    val monthNameBn: String,
    val monthNameAr: String,
    val year: Int,
  )

  private val hijriMonthsEn = listOf(
    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
    "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
    "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
  )

  private val hijriMonthsBn = listOf(
    "মুহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
    "জুমাদাল উলা", "জুমাদাস সানি", "রজব", "শাবান",
    "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
  )

  private val hijriMonthsAr = listOf(
    "مُحَرَّم", "صَفَر", "رَبِيع ٱلْأَوَّل", "رَبِيع ٱلثَّانِي",
    "جُمَادَىٰ ٱلْأُولَىٰ", "جُمَادَىٰ ٱلثَّانِيَة", "رَجَب", "شَعْبَان",
    "رَمَضَان", "شَوَّال", "ذُو ٱلْقَعْدَة", "ذُو ٱلْحِجَّة"
  )

  private val daysBn = listOf(
    "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার"
  )

  private val monthsBn = listOf(
    "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
    "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
  )

  /**
   * Converts a Gregorian Calendar instance into the authentic Umm al-Qura Hijri Date.
   * Leverages java.time.chrono.HijrahDate (official astronomical Umm al-Qura calendar).
   */
  fun getHijriDate(calendar: Calendar = Calendar.getInstance()): HijriDate {
    val gYear = calendar.get(Calendar.YEAR)
    val gMonth = calendar.get(Calendar.MONTH) + 1
    val gDay = calendar.get(Calendar.DAY_OF_MONTH)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        val localDate = java.time.LocalDate.of(gYear, gMonth, gDay)
        val hijrahDate = java.time.chrono.HijrahDate.from(localDate)
        val hDay = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
        val hMonth1Based = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
        val hYear = hijrahDate.get(java.time.temporal.ChronoField.YEAR)
        val hMonthIndex = (hMonth1Based - 1).coerceIn(0, 11)

        return HijriDate(
          day = hDay,
          monthIndex = hMonthIndex,
          monthNameEn = hijriMonthsEn[hMonthIndex],
          monthNameBn = hijriMonthsBn[hMonthIndex],
          monthNameAr = hijriMonthsAr[hMonthIndex],
          year = hYear
        )
      } catch (e: Exception) {
        // Fallback to high-precision astronomical algorithm
      }
    }

    // Compatibility fallback for Android 7.x (API 24–25): this is a tabular/civil Hijri
    // approximation, not an official Umm al-Qura table, so it can differ by a day in Saudi Arabia.
    var m = gMonth
    var y = gYear
    if (m < 3) {
      y -= 1
      m += 12
    }

    val a = floor(y / 100.0)
    val b = 2 - a + floor(a / 4.0)
    val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + gDay + b - 1524.5

    var l = jd - 1948440 + 10632
    val n = floor((l - 1) / 10631)
    l = l - 10631 * n + 354
    val j = floor((10985 - l) / 5316) * floor((50 * l) / 17719) + floor(l / 5670) * floor((43 * l) / 15238)
    l = l - floor((30 - j) / 15) * floor((17719 * j) / 50) - floor(j / 16) * floor((15238 * j) / 43) + 29.0
    var hMonth = floor((24 * l) / 709).toInt()
    val hDay = (l - floor((709 * hMonth) / 24)).toInt().coerceIn(1, 30)
    val hYear = (30 * n + j - 30).toInt()

    if (hMonth > 12) hMonth = 12
    if (hMonth < 1) hMonth = 1
    val hMonthIndex = (hMonth - 1).coerceIn(0, 11)

    return HijriDate(
      day = hDay,
      monthIndex = hMonthIndex,
      monthNameEn = hijriMonthsEn[hMonthIndex],
      monthNameBn = hijriMonthsBn[hMonthIndex],
      monthNameAr = hijriMonthsAr[hMonthIndex],
      year = hYear
    )
  }

  /**
   * Formats the Gregorian and Hijri date based on language.
   */
  fun getFormattedDates(lang: String, calendar: Calendar = Calendar.getInstance()): Pair<String, String> {
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH)
    val year = calendar.get(Calendar.YEAR)

    val hijri = getHijriDate(calendar)

    return when (lang) {
      "bn" -> {
        val bnDayName = daysBn[(dayOfWeek - 1) % 7]
        val bnMonthName = monthsBn[month]
        val gregDate = "$bnDayName, ${toBengaliDigits(dayOfMonth)} $bnMonthName ${toBengaliDigits(year)}"
        val hijriDate = "${toBengaliDigits(hijri.day)} ${hijri.monthNameBn} ${toBengaliDigits(hijri.year)} হিজরি"
        Pair(gregDate, hijriDate)
      }
      "ar" -> {
        val sdfGreg = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
        val gregDate = sdfGreg.format(calendar.time)
        val hijriDate = "${hijri.day} ${hijri.monthNameAr} ${hijri.year} هـ"
        Pair(gregDate, hijriDate)
      }
      else -> {
        val sdfGreg = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
        val gregDate = sdfGreg.format(calendar.time)
        val hijriDate = "${hijri.day} ${hijri.monthNameEn} ${hijri.year} AH"
        Pair(gregDate, hijriDate)
      }
    }
  }

  /**
   * International Gregorian English Date (e.g., "Thursday, 22 October 2026")
   */
  fun getEnglishIntlDate(calendar: Calendar = Calendar.getInstance()): String {
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
    return sdf.format(calendar.time)
  }

  /**
   * Bengali Solar Calendar (বঙ্গাব্দ) calculation according to Bangla Academy standard
   */
  fun getBanglaSolarDate(calendar: Calendar = Calendar.getInstance()): String {
    val gYear = calendar.get(Calendar.YEAR)
    val gMonth = calendar.get(Calendar.MONTH) // 0-based: 0=Jan, 3=Apr
    val gDay = calendar.get(Calendar.DAY_OF_MONTH)

    val banglaMonths = listOf(
      "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
      "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    val isLeapYear = (gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0)
    val falgunDays = if (isLeapYear) 30 else 29
    val monthLengths = listOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, falgunDays, 30)

    val bYear = if (gMonth > 3 || (gMonth == 3 && gDay >= 14)) gYear - 593 else gYear - 594

    val apr14 = Calendar.getInstance().apply {
      set(gYear, Calendar.APRIL, 14, 0, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }

    val currentCal = Calendar.getInstance().apply {
      time = calendar.time
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    val diffDays = if (currentCal.before(apr14)) {
      val prevApr14 = Calendar.getInstance().apply {
        set(gYear - 1, Calendar.APRIL, 14, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
      }
      ((currentCal.timeInMillis - prevApr14.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
    } else {
      ((currentCal.timeInMillis - apr14.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
    }

    var remaining = diffDays
    var bMonthIdx = 0
    for (i in monthLengths.indices) {
      if (remaining < monthLengths[i]) {
        bMonthIdx = i
        break
      }
      remaining -= monthLengths[i]
    }
    val bDay = remaining + 1

    return "${toBengaliDigits(bDay)} ${banglaMonths[bMonthIdx]} ${toBengaliDigits(bYear)} বঙ্গাব্দ"
  }

  /**
   * Current 12-hour formatted time with AM/PM
   */
  fun getLiveTimeString(lang: String, date: Date = Date()): String {
    val sdf = SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH)
    val rawTime = sdf.format(date)
    return if (lang == "bn") {
      toBengaliDigits(rawTime)
        .replace("AM", "মিনিট (ভোর/সকাল)")
        .replace("PM", "মিনিট (দুপুর/রাত)")
    } else {
      rawTime
    }
  }

  fun toBengaliDigits(input: Any): String {
    val str = input.toString()
    val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val builder = StringBuilder()
    for (ch in str) {
      if (ch in '0'..'9') {
        builder.append(bengaliDigits[ch - '0'])
      } else {
        builder.append(ch)
      }
    }
    return builder.toString()
  }

  data class IslamicEvent(
    val nameEn: String,
    val nameBn: String,
    val nameAr: String,
    val hijriDay: Int,
    val hijriMonthIndex: Int,
    val descriptionBn: String
  )

  val annualIslamicEvents = listOf(
    IslamicEvent(
      "Islamic New Year", "পবিত্র আশুরা ও হিজরি নববর্ষ", "رأس السنة الهجرية",
      1, 0, "পবিত্র মহররম মাসের ১ তারিখ ও মুসলিম উম্মাহর ঐতিহাসিক নববর্ষ।"
    ),
    IslamicEvent(
      "Holy Ashura", "পবিত্র আশুরা (১০ মহররম)", "يوم عاشوراء",
      10, 0, "১০ই মহররম হযরত মুসা (আ.) এর মুক্তি ও কারবালার ঐতিহাসিক স্মৃতিবিজড়িত দিন।"
    ),
    IslamicEvent(
      "Eid Miladunnabi", "পবিত্র ঈদে মিলাদুন্নবী (সা.)", "المولد النبوي الشريف",
      12, 2, "১২ই রবিউল আউয়াল বিশ্বনবী হযরত মুহাম্মদ (সা.) এর পবিত্র বেলাদত ও ওফাত দিবস।"
    ),
    IslamicEvent(
      "Shab-e-Meraj", "পবিত্র শবে মেরাজ", "ليلة الإسراء والمعراج",
      27, 6, "২৬ রজব দিবাগত রাত - বিশ্বনবীর উর্ধ্বারোহন ও পাঁচ ওয়াক্ত নামাজের উপহার লাভ।"
    ),
    IslamicEvent(
      "Shab-e-Barat", "পবিত্র শবে বরাত (লাইলাতুল বরাত)", "ليلة النصف من شعبان",
      15, 7, "১৪ শাবান দিবাগত রাতে ক্ষমা ও ভাগ্য রজনী।"
    ),
    IslamicEvent(
      "1st Ramadan", "পবিত্র মাহে রমজান শুরু", "أول أيام رمضان",
      1, 8, "রহমত, মাগফিরাত ও নাজাতের মাস রমজানের রোজা শুরু।"
    ),
    IslamicEvent(
      "Laylatul Qadr", "পবিত্র শবে কদর (লাইলাতুল কদর)", "ليلة القدر",
      27, 8, "হাজার মাসের চেয়েও শ্রেষ্ঠ বরকতময় রাত, কুরআন নাজিলের রাত।"
    ),
    IslamicEvent(
      "Eid-ul-Fitr", "পবিত্র ঈদুল ফিতর", "عيد الفطر المبارك",
      1, 9, "এক মাস সিয়াম সাধনা শেষে মুসলিম উম্মাহর মহা আনন্দের দিন।"
    ),
    IslamicEvent(
      "Day of Arafah", "পবিত্র আরাফাহ দিবস (হজ)", "يوم عرفة",
      9, 11, "হজের মূল দিবস ও আরাফাতের ময়দানে ক্ষমা প্রার্থনার মহান দিন।"
    ),
    IslamicEvent(
      "Eid-ul-Adha", "পবিত্র ঈদুল আজহা (কোরবানি)", "عيد الأضحى المبارك",
      10, 11, "হযরত ইব্রাহিম (আ.) এর ত্যাগের স্মরণে মহা কোরবানি ও আনন্দের দিন।"
    ),
  )
}
