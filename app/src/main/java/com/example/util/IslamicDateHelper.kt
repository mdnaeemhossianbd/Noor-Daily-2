package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

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
   * Converts a Gregorian Calendar instance into a calculated Hijri Date.
   * Uses astronomical Kuwati/Umm al-Qura approximation.
   */
  fun getHijriDate(calendar: Calendar = Calendar.getInstance()): HijriDate {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var m = month
    var y = year
    if (m < 3) {
      y -= 1
      m += 12
    }

    val a = floor(y / 100.0)
    val b = 2 - a + floor(a / 4.0)
    val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

    // Epoch of the Hijri calendar is JD 1948439.5
    val z = jd - 1948439.5
    val cyc = floor(z / 10631.0)
    val rem = z - cyc * 10631.0
    val j = floor((rem + 0.5) / 354.36667)
    val iy = 30 * cyc + j
    val dInYear = rem - floor(j * 354.36667)
    var hMonth = floor((dInYear + 28.5) / 29.5).toInt()
    if (hMonth > 11) hMonth = 11
    if (hMonth < 0) hMonth = 0

    val hDay = (dInYear - floor(hMonth * 29.5) + 1).toInt().coerceIn(1, 30)
    val hYear = iy.toInt() + 1

    return HijriDate(
      day = hDay,
      monthIndex = hMonth,
      monthNameEn = hijriMonthsEn[hMonth],
      monthNameBn = hijriMonthsBn[hMonth],
      monthNameAr = hijriMonthsAr[hMonth],
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
