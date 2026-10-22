package com.example.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Astronomical Islamic Prayer Times Calculator.
 * Calculates accurate times for any GPS coordinate (latitude, longitude, timezone).
 */
object PrayerTimeCalculator {

  @Volatile private var configuredMethod: String = "Umm al-Qura"
  @Volatile private var configuredMadhhab: String? = null
  @Volatile private var configuredAdjustments: Map<String, Int> = emptyMap()
  @Volatile private var _settingsRevision: Int = 0
  val settingsRevision: Int get() = _settingsRevision

  /** Updated from the public-read /prayer_settings RTDB node by the existing background sync. */
  fun updateAdminSettings(method: String, madhhab: String, adjustments: Map<String, Int>) {
    val supportedMethods = setOf("Umm al-Qura", "Muslim World League", "Egyptian", "Karachi", "ISNA")
    val nextMethod = method.takeIf { it in supportedMethods } ?: "Umm al-Qura"
    val nextMadhhab = madhhab.takeIf { it == "Hanafi" || it == "Shafi" }
    val nextAdjustments = adjustments.mapValues { (_, value) -> value.coerceIn(-30, 30) }
    if (configuredMethod != nextMethod || configuredMadhhab != nextMadhhab || configuredAdjustments != nextAdjustments) {
      configuredMethod = nextMethod
      configuredMadhhab = nextMadhhab
      configuredAdjustments = nextAdjustments
      _settingsRevision += 1
    }
  }

  private fun adjustmentMinutes(prayer: String): Int = configuredAdjustments[prayer] ?: 0

  data class CalculatedPrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val sehriEndTime: String,
    val iftarTime: String,
    val nextPrayerNameEn: String,
    val nextPrayerNameBn: String,
    val nextPrayerNameAr: String,
    val remainingSecondsToNext: Long,
    val remainingSecondsToSehri: Long,
    val remainingSecondsToIftar: Long,
    val activePrayerNameEn: String,
    val activePrayerNameBn: String,
    val activePrayerNameAr: String,
  )

  fun calculate(
    latitude: Double,
    longitude: Double,
    isHanafi: Boolean = true,
    calendar: Calendar? = null,
    timeZoneId: String? = null
  ): CalculatedPrayerTimes {
    val cal = when {
      calendar != null -> calendar
      timeZoneId != null -> Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
      else -> Calendar.getInstance()
    }

    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    val tzOffsetHours = cal.timeZone.getOffset(cal.timeInMillis).toDouble() / 3600000.0

    // Solar calculation parameters
    val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
    // Equation of Time in minutes
    val eqt = 9.87 * sin(2.0 * b) - 7.53 * cos(b) - 1.5 * sin(b)
    // Solar declination in radians
    val declination = Math.toRadians(23.45 * sin(b))
    val latRad = Math.toRadians(latitude)

    // Solar noon in local hours
    val solarNoon = 12.0 + tzOffsetHours - (longitude / 15.0) - (eqt / 60.0)

    fun hourAngle(altitudeDeg: Double): Double {
      val altRad = Math.toRadians(altitudeDeg)
      val cosH = (sin(altRad) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
      val clampedCosH = cosH.coerceIn(-1.0, 1.0)
      return Math.toDegrees(acos(clampedCosH)) / 15.0
    }

    val method = configuredMethod
    val fajrAngle = when (method) {
      "Umm al-Qura" -> 18.5
      "Egyptian" -> 19.5
      "ISNA" -> 15.0
      else -> 18.0
    }
    val ishaAngle = when (method) {
      "Muslim World League" -> 17.0
      "Egyptian" -> 17.5
      "ISNA" -> 15.0
      else -> 18.0
    }

    // Calculation-method angles, followed by the admin's explicit minute adjustments.
    val fajrHour = solarNoon - hourAngle(-fajrAngle) + adjustmentMinutes("fajr") / 60.0
    // Sehri end time: 4-minute cautionary buffer before adjusted Fajr.
    val sehriHour = fajrHour - (4.0 / 60.0)

    val sunriseHA = hourAngle(-0.833)
    val sunriseHour = solarNoon - sunriseHA + adjustmentMinutes("sunrise") / 60.0
    val dhuhrHour = solarNoon + (2.0 / 60.0) + adjustmentMinutes("dhuhr") / 60.0

    // Admin-configured madhhab overrides the screen's default only after settings are loaded.
    val effectiveHanafi = configuredMadhhab?.let { it == "Hanafi" } ?: isHanafi
    val asrFactor = if (effectiveHanafi) 2.0 else 1.0
    val asrAltRad = atan(1.0 / (asrFactor + tan(abs(latRad - declination))))
    val asrAltDeg = Math.toDegrees(asrAltRad)
    val asrHA = hourAngle(asrAltDeg)
    val asrHour = solarNoon + asrHA + adjustmentMinutes("asr") / 60.0

    val maghribHA = sunriseHA
    val maghribHour = solarNoon + maghribHA + adjustmentMinutes("maghrib") / 60.0
    val iftarHour = maghribHour

    // Umm al-Qura convention uses a fixed interval after sunset (normally 90 minutes).
    val ishaHour = if (method == "Umm al-Qura") {
      maghribHour + 90.0 / 60.0 + adjustmentMinutes("isha") / 60.0
    } else {
      solarNoon + hourAngle(-ishaAngle) + adjustmentMinutes("isha") / 60.0
    }

    fun formatHour(hourDec: Double): String {
      var norm = hourDec
      while (norm < 0.0) norm += 24.0
      while (norm >= 24.0) norm -= 24.0
      val h = norm.toInt()
      val m = ((norm - h) * 60.0).roundToInt().coerceIn(0, 59)
      val period = if (h < 12) "AM" else "PM"
      val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
      return String.format("%02d:%02d %s", displayH, m, period)
    }

    fun toMinutes(hourDec: Double): Int {
      var norm = hourDec
      while (norm < 0.0) norm += 24.0
      while (norm >= 24.0) norm -= 24.0
      val h = norm.toInt()
      val m = ((norm - h) * 60.0).roundToInt().coerceIn(0, 59)
      return h * 60 + m
    }

    val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val currentSeconds = currentMinutes * 60 + cal.get(Calendar.SECOND)

    val fajrM = toMinutes(fajrHour)
    val sehriM = toMinutes(sehriHour)
    val sunriseM = toMinutes(sunriseHour)
    val dhuhrM = toMinutes(dhuhrHour)
    val asrM = toMinutes(asrHour)
    val maghribM = toMinutes(maghribHour)
    val iftarM = toMinutes(iftarHour)
    val ishaM = toMinutes(ishaHour)

    // Countdown for Sehri
    val sehriTargetMin = if (currentMinutes < sehriM) sehriM else sehriM + 24 * 60
    val remainingSecToSehri = (sehriTargetMin * 60L - currentSeconds).coerceAtLeast(0L)

    // Countdown for Iftar
    val iftarTargetMin = if (currentMinutes < iftarM) iftarM else iftarM + 24 * 60
    val remainingSecToIftar = (iftarTargetMin * 60L - currentSeconds).coerceAtLeast(0L)

    // Determine the currently active obligatory prayer and the next obligatory prayer.
    // Sunrise is not an obligatory prayer, so it must not replace Dhuhr as the next-prayer countdown.
    var nextNameEn = "Fajr"
    var nextNameBn = "ফজর"
    var nextNameAr = "الفجر"
    var targetMinute = fajrM

    var activeNameEn = "Isha"
    var activeNameBn = "এশা"
    var activeNameAr = "العشاء"

    when {
      currentMinutes < fajrM -> {
        nextNameEn = "Fajr"; nextNameBn = "ফজর"; nextNameAr = "الفجر"; targetMinute = fajrM
        // Before Fajr, the previous obligatory prayer is Isha.
        activeNameEn = "Isha"; activeNameBn = "এশা"; activeNameAr = "العشاء"
      }
      currentMinutes < dhuhrM -> {
        nextNameEn = "Dhuhr"; nextNameBn = "যোহর"; nextNameAr = "الظهر"; targetMinute = dhuhrM
        activeNameEn = "Fajr"; activeNameBn = "ফজর"; activeNameAr = "الفجر"
      }
      currentMinutes < asrM -> {
        nextNameEn = "Asr"; nextNameBn = "আসর"; nextNameAr = "العصر"; targetMinute = asrM
        activeNameEn = "Dhuhr"; activeNameBn = "যোহর"; activeNameAr = "الظهر"
      }
      currentMinutes < maghribM -> {
        nextNameEn = "Maghrib"; nextNameBn = "মাগরিব"; nextNameAr = "المغرب"; targetMinute = maghribM
        activeNameEn = "Asr"; activeNameBn = "আসর"; activeNameAr = "العصر"
      }
      currentMinutes < ishaM -> {
        nextNameEn = "Isha"; nextNameBn = "এশা"; nextNameAr = "العشاء"; targetMinute = ishaM
        activeNameEn = "Maghrib"; activeNameBn = "মাগরিব"; activeNameAr = "المغرب"
      }
      else -> {
        // After Isha, next prayer is tomorrow's Fajr.
        nextNameEn = "Fajr"; nextNameBn = "ফজর"; nextNameAr = "الفجر"; targetMinute = fajrM + 24 * 60
        activeNameEn = "Isha"; activeNameBn = "এশা"; activeNameAr = "العشاء"
      }
    }

    val remainingSec = (targetMinute * 60L - currentSeconds).coerceAtLeast(0L)

    return CalculatedPrayerTimes(
      fajr = formatHour(fajrHour),
      sunrise = formatHour(sunriseHour),
      dhuhr = formatHour(dhuhrHour),
      asr = formatHour(asrHour),
      maghrib = formatHour(maghribHour),
      isha = formatHour(ishaHour),
      sehriEndTime = formatHour(sehriHour),
      iftarTime = formatHour(iftarHour),
      nextPrayerNameEn = nextNameEn,
      nextPrayerNameBn = nextNameBn,
      nextPrayerNameAr = nextNameAr,
      remainingSecondsToNext = remainingSec,
      remainingSecondsToSehri = remainingSecToSehri,
      remainingSecondsToIftar = remainingSecToIftar,
      activePrayerNameEn = activeNameEn,
      activePrayerNameBn = activeNameBn,
      activePrayerNameAr = activeNameAr,
    )
  }

  fun getCityLiveTime(timeZoneId: String, currentLang: String = "bn"): String {
    val tz = TimeZone.getTimeZone(timeZoneId)
    val cal = Calendar.getInstance(tz)
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    val s = cal.get(Calendar.SECOND)
    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
    val period = if (h < 12) {
      if (currentLang == "bn") "AM" else "AM"
    } else {
      if (currentLang == "bn") "PM" else "PM"
    }
    val raw = String.format("%02d:%02d:%02d %s", displayH, m, s, period)
    return if (currentLang == "bn") {
      val bnDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
      )
      raw.map { bnDigits[it] ?: it }.joinToString("")
    } else {
      raw
    }
  }

  data class PrayerAlarmTarget(
    val nameEn: String,
    val nameBn: String,
    val triggerTimeMillis: Long
  )

  fun getNextPrayerAlarmTarget(
    latitude: Double,
    longitude: Double,
    isHanafi: Boolean = true,
    enabledPrayers: Set<String> = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"),
    timeZoneId: String? = null
  ): PrayerAlarmTarget {
    require(enabledPrayers.isNotEmpty()) { "At least one prayer reminder must be enabled" }
    val now = if (timeZoneId.isNullOrBlank()) Calendar.getInstance()
      else Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
    val todayTimes = calculate(latitude, longitude, isHanafi, now)

    fun prayerTargets(times: CalculatedPrayerTimes) = listOf(
      Triple("Fajr", "ফজর", times.fajr),
      Triple("Dhuhr", "যোহর", times.dhuhr),
      Triple("Asr", "আসর", times.asr),
      Triple("Maghrib", "মাগরিব", times.maghrib),
      Triple("Isha", "এশা", times.isha)
    ).filter { enabledPrayers.contains(it.first) }

    for (prayer in prayerTargets(todayTimes)) {
      val prayerCal = parseTimeToCalendar(prayer.third, now)
      if (prayerCal.timeInMillis > now.timeInMillis + 5000L) {
        return PrayerAlarmTarget(prayer.first, prayer.second, prayerCal.timeInMillis)
      }
    }

    // If all enabled prayers have passed today, choose the first enabled prayer tomorrow.
    val tomorrow = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    val tomorrowTimes = calculate(latitude, longitude, isHanafi, tomorrow)
    val next = prayerTargets(tomorrowTimes).first()
    val target = parseTimeToCalendar(next.third, tomorrow)
    return PrayerAlarmTarget(next.first, next.second, target.timeInMillis)
  }

  fun parseTimeToCalendar(timeStr: String, baseCal: Calendar): Calendar {
    val cal = baseCal.clone() as Calendar
    try {
      val parts = timeStr.trim().split(" ")
      val timeParts = parts[0].split(":")
      var hour = timeParts[0].toInt()
      val minute = timeParts[1].toInt()
      val isPm = parts.getOrNull(1)?.uppercase() == "PM"
      if (isPm && hour < 12) hour += 12
      if (!isPm && hour == 12) hour = 0
      cal.set(Calendar.HOUR_OF_DAY, hour)
      cal.set(Calendar.MINUTE, minute)
      cal.set(Calendar.SECOND, 0)
      cal.set(Calendar.MILLISECOND, 0)
    } catch (_: Exception) {}
    return cal
  }
}

