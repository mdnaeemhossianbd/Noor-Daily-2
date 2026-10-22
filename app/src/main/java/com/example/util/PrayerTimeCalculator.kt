package com.example.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Astronomical Islamic Prayer Times Calculator.
 * Calculates accurate times for any GPS coordinate (latitude, longitude, timezone).
 */
object PrayerTimeCalculator {

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

    // Fajr (18° below horizon)
    val fajrHA = hourAngle(-18.0)
    val fajrHour = solarNoon - fajrHA

    // Sehri end time: 4 minutes cautionary buffer before Fajr / Subh Sadiq
    val sehriHour = fajrHour - (4.0 / 60.0)

    // Sunrise (0.833° below horizon for atmospheric refraction)
    val sunriseHA = hourAngle(-0.833)
    val sunriseHour = solarNoon - sunriseHA

    // Dhuhr (Solar noon + 2-3 mins buffer)
    val dhuhrHour = solarNoon + (2.0 / 60.0)

    // Asr: shadow length = (factor + tan|lat - decl|)
    val asrFactor = if (isHanafi) 2.0 else 1.0
    val asrAltRad = atan(1.0 / (asrFactor + tan(abs(latRad - declination))))
    val asrAltDeg = Math.toDegrees(asrAltRad)
    val asrHA = hourAngle(asrAltDeg)
    val asrHour = solarNoon + asrHA

    // Maghrib (Sunset: 0.833° below horizon) = Iftar time
    val maghribHA = sunriseHA
    val maghribHour = solarNoon + maghribHA
    val iftarHour = maghribHour

    // Isha (18° below horizon)
    val ishaHA = hourAngle(-18.0)
    val ishaHour = solarNoon + ishaHA

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

    // Determine active and next prayer
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
        activeNameEn = "Tahajjud"; activeNameBn = "তাহাজ্জুদ"; activeNameAr = "التهجد"
      }
      currentMinutes < sunriseM -> {
        nextNameEn = "Sunrise"; nextNameBn = "সূর্যোদয়"; nextNameAr = "الشروق"; targetMinute = sunriseM
        activeNameEn = "Fajr"; activeNameBn = "ফজর"; activeNameAr = "الفجر"
      }
      currentMinutes < dhuhrM -> {
        nextNameEn = "Dhuhr"; nextNameBn = "যোহর"; nextNameAr = "الظهر"; targetMinute = dhuhrM
        activeNameEn = "Duha/Chasht"; activeNameBn = "চাশত/ইশরাক"; activeNameAr = "الضحى"
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
        // Next is tomorrow's Fajr
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
    enabledPrayers: Set<String> = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
  ): PrayerAlarmTarget {
    require(enabledPrayers.isNotEmpty()) { "At least one prayer reminder must be enabled" }
    val now = Calendar.getInstance()
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

