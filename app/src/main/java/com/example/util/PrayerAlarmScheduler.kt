package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.receiver.PrayerAlarmReceiver

object PrayerAlarmScheduler {

  private const val TAG = "PrayerAlarmScheduler"
  const val REQUEST_CODE_PRAYER_ALARM = 9001

  fun scheduleNextPrayerAlarm(context: Context) {
    try {
      val prefs = context.getSharedPreferences("noor_daily_prefs", Context.MODE_PRIVATE)
      val enabled = prefs.getBoolean("azan_notifications_enabled", true)
      if (!enabled) {
        cancelAlarms(context)
        return
      }

      val prayerPreferenceKeys = mapOf(
        "Fajr" to "azan_fajr_enabled",
        "Dhuhr" to "azan_dhuhr_enabled",
        "Asr" to "azan_asr_enabled",
        "Maghrib" to "azan_maghrib_enabled",
        "Isha" to "azan_isha_enabled"
      )
      val enabledPrayers = prayerPreferenceKeys.filter { (_, key) -> prefs.getBoolean(key, true) }.keys
      if (enabledPrayers.isEmpty()) {
        cancelAlarms(context)
        return
      }

      val isHanafi = prefs.getBoolean("is_hanafi_madhhab", true)
      PrayerTimeCalculator.updateAdminSettings(
        method = prefs.getString("prayer_method", "Umm al-Qura") ?: "Umm al-Qura",
        madhhab = prefs.getString("prayer_madhhab", "Hanafi") ?: "Hanafi",
        adjustments = mapOf(
          "fajr" to prefs.getInt("prayer_adjust_fajr", 0),
          "sunrise" to prefs.getInt("prayer_adjust_sunrise", 0),
          "dhuhr" to prefs.getInt("prayer_adjust_dhuhr", 0),
          "asr" to prefs.getInt("prayer_adjust_asr", 0),
          "maghrib" to prefs.getInt("prayer_adjust_maghrib", 0),
          "isha" to prefs.getInt("prayer_adjust_isha", 0)
        )
      )
      val useGpsLocation = prefs.getBoolean("use_gps_location", true)
      val location = LocationAndNotificationHelper.getCurrentDeviceLocation(context)
      val latitude = if (!useGpsLocation) {
        prefs.getString("selected_city_latitude", null)?.toDoubleOrNull() ?: location.latitude
      } else {
        prefs.getString("gps_latitude", null)?.toDoubleOrNull() ?: location.latitude
      }
      val longitude = if (!useGpsLocation) {
        prefs.getString("selected_city_longitude", null)?.toDoubleOrNull() ?: location.longitude
      } else {
        prefs.getString("gps_longitude", null)?.toDoubleOrNull() ?: location.longitude
      }
      val timeZoneId = if (!useGpsLocation) prefs.getString("selected_city_timezone", null) else null
      val nextTarget = PrayerTimeCalculator.getNextPrayerAlarmTarget(
        latitude = latitude,
        longitude = longitude,
        isHanafi = isHanafi,
        enabledPrayers = enabledPrayers,
        timeZoneId = timeZoneId
      )

      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
      if (alarmManager == null) {
        Log.w(TAG, "AlarmManager not available")
        return
      }

      val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
        action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
        putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, nextTarget.nameBn)
        putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_KEY, nextTarget.nameEn)
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE_PRAYER_ALARM,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val triggerAtMillis = nextTarget.triggerTimeMillis
      Log.d(TAG, "Scheduling next Azan alarm for ${nextTarget.nameBn} at $triggerAtMillis")

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        try {
          val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
          alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
          Log.d(TAG, "setAlarmClock succeeded for ${nextTarget.nameBn}")
        } catch (_: Exception) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
              alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (_: SecurityException) {
              alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
          } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
          }
        }
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error scheduling prayer alarm", e)
    }
  }

  fun cancelAlarms(context: Context) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
        action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
      }
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE_PRAYER_ALARM,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      alarmManager.cancel(pendingIntent)
    } catch (e: Exception) {
      Log.e(TAG, "Error canceling alarms", e)
    }
  }
}
